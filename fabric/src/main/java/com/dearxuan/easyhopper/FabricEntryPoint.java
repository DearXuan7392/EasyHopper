package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import net.fabricmc.api.ModInitializer;
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
    }
}