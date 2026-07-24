package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.mixin.ServerPlayerMixin;
import com.dearxuan.easyhopper.net.ConfigSyncPayload;
import com.dearxuan.easyhopper.net.INetHelper;
import com.dearxuan.easyhopper.net.NetManager;
import com.dearxuan.easyhopper.net.ServerConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 网络辅助类, 实现 NetHelper 接口
 */
@Environment(EnvType.BOTH)
public class NeoForgeNetHelper implements INetHelper {

    private final IEventBus modEventBus;
    private final IEventBus neoForgeEventBus;

    public NeoForgeNetHelper(IEventBus modEventBus, IEventBus neoForgeEventBus) {
        this.modEventBus = modEventBus;
        this.neoForgeEventBus = neoForgeEventBus;
    }

    @Override
    public void registerPackets() {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            final PayloadRegistrar registrar = event.registrar("1");

            // C2S: 客户端推送配置到服务端
            registrar.playToServer(
                    ConfigSyncPayload.TYPE,
                    ConfigSyncPayload.CODEC,
                    this::handleServerConfigPush
            );

            // S2C: 服务端同步配置到客户端
            registrar.playToClient(
                    ConfigSyncPayload.TYPE,
                    ConfigSyncPayload.CODEC,
                    this::handleClientConfigSync
            );
        });
    }

    @Override
    public void registerPlayerJoinEvent() {
        neoForgeEventBus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                sendToPlayer(player, new ConfigSyncPayload(ModConfig.INSTANCE));
            }
        });
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ConfigSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    private void handleServerConfigPush(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (ServerConfigHandler.applyConfigFromPlayer(player, payload)) {
                MinecraftServer server = ((ServerPlayerMixin) player).getServer();
                for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                    if (otherPlayer != player) {
                        sendToPlayer(otherPlayer, payload);
                    }
                }
            }
        });
    }

    private void handleClientConfigSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            NetManager.updateServerConfig(payload.toConfig());
        });
    }
}