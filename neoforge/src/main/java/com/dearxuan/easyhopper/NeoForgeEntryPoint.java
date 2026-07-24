package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.mixin.ServerPlayerMixin;
import com.dearxuan.easyhopper.server.net.ServerConfigHandler;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEntryPoint {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        NetManager.updateServerConfig(payload.toConfig(), payload.hasPermission());
                    });
                }
        );
        registrar.playToServer(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC,
                NeoForgeEntryPoint::handleServerConfigPush
        );
    }

    private static void handleServerConfigPush(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (ServerConfigHandler.applyConfigFromPlayer(player, payload)) {
                MinecraftServer server = ((ServerPlayerMixin) player).getServer();
                for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                    if (otherPlayer != player) {
                        boolean hasPerm = PlayerUtil.hasPermissionToPushConfig(otherPlayer);
                        PacketDistributor.sendToPlayer(otherPlayer, new ConfigSyncPayload(ServerConfig.INSTANCE, hasPerm));
                    }
                }
            }
        });
    }
}