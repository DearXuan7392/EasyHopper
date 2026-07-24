package com.dearxuan.easyhopper.client;

import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.client.net.NetManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Fabric 客户端专用入口点, 用于注册 S2C 编解码器、网络接收器和事件监听.
 */
public class FabricClientEntryPoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CommonClientEntryPoint.init();

        // 1. 客户端连接建立时 (进入 Play 阶段前), 注册配置同步接收器
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> {
                    NetManager.updateServerConfig(payload.toConfig(), payload.hasPermission());
                });
            });
        });

        // 2. 断开连接时清除服务端配置缓存, 恢复本地配置
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            NetManager.clearCache();
        });
    }
}