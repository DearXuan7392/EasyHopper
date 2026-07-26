package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.CommonServerEntryPoint;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class FabricEntryPoint implements ModInitializer {
    @Override
    public void onInitialize() {
        // 注册 S2C 编解码器 (服务端推送配置到客户端)
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // 注册 C2S 编解码器 (客户端推送配置到服务端)
        PayloadTypeRegistry.serverboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // 注册 C2S 编解码器 (客户端请求配置同步)
        PayloadTypeRegistry.serverboundPlay().register(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);

        // 监听服务器启动事件 (包括专用服务端和集成服务器/单人世界)
        // 确保在每次进入世界时, 服务端都从配置文件加载配置
        // 使用 SERVER_STARTING 在 initServer() 之前触发, 比 SERVER_STARTED 更早
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CommonServerEntryPoint.init();
        });
    }
}