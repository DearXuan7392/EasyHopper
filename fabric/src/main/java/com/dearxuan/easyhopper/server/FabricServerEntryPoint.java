package com.dearxuan.easyhopper.server;

import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.net.ServerConfigHandler;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric 服务端专用入口点, 仅在专用服务端加载.
 * 注册 C2S 数据包编解码器、处理器及玩家进服事件.
 */
public class FabricServerEntryPoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CommonServerEntryPoint.init();

        // 1. 注册 C2S 处理器: 接收客户端推送的配置修改
        ServerPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (ServerConfigHandler.applyConfigFromPlayer(player, payload)) {
                    // 广播给所有在线玩家（含发起者，更新其权限状态）
                    for (ServerPlayer otherPlayer : context.server().getPlayerList().getPlayers()) {
                        boolean hasPerm = PlayerUtil.hasPermissionToPushConfig(otherPlayer);
                        ServerPlayNetworking.send(otherPlayer, new ConfigSyncPayload(ServerConfig.INSTANCE, hasPerm));
                    }
                }
            });
        });

        // 3. 注册玩家进服事件: 进服时推送当前服务端配置
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            boolean hasPermission = PlayerUtil.hasPermissionToPushConfig(player);
            ServerPlayNetworking.send(player, new ConfigSyncPayload(ServerConfig.INSTANCE, hasPermission));
        });
    }
}