package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.net.INetHelper;
import com.dearxuan.easyhopper.server.net.ServerConfigHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric 网络辅助类, 实现 NetHelper 接口
 */
public class FabricNetHelper implements INetHelper {

    @Override
    public void registerPackets() {
        // C2S: 注册 serverbound 编解码器
        PayloadTypeRegistry.serverboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // S2C: 注册 clientbound 编解码器
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // 注册服务端 C2S 接收器
        ServerPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (ServerConfigHandler.applyConfigFromPlayer(player, payload)) {
                    for (ServerPlayer otherPlayer : context.server().getPlayerList().getPlayers()) {
                        if (otherPlayer != player) {
                            sendToPlayer(otherPlayer, payload);
                        }
                    }
                }
            });
        });
    }

    @Override
    public void registerPlayerJoinEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sendToPlayer(handler.getPlayer(), new ConfigSyncPayload(ModConfig.INSTANCE));
        });
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ConfigSyncPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}