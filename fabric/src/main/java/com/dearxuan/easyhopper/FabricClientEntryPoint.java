package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.net.ConfigSyncPayload;
import com.dearxuan.easyhopper.net.NetManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Fabric 客户端专用入口点, 用于注册客户端网络接收器和事件监听
 */
@Environment(EnvType.CLIENT)
public class FabricClientEntryPoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 当客户端连接建立时 (进入 Play 阶段前), 注册配置同步接收器
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            // 客户端接收服务端推送的配置
            ClientPlayNetworking.registerReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
                context.client().execute(() -> {
                    NetManager.updateServerConfig(payload.toConfig());
                });
            });
        });

        // 断开连接时清除服务端配置缓存, 恢复本地配置
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            NetManager.clearCache();
        });
    }
}