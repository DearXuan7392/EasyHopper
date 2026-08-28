package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.CommonServerEntryPoint;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.config.ServerConfigHandler;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricEntryPoint implements ModInitializer {
    @Override
    public void onInitialize() {
        // 注册 S2C 编解码器 (服务端推送配置到客户端)
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // 注册 C2S 编解码器 (客户端推送配置到服务端)
        PayloadTypeRegistry.playC2S().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // 注册 C2S 编解码器 (客户端请求配置同步)
        PayloadTypeRegistry.playC2S().register(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);

        // 监听服务器启动事件 (包括专用服务端和集成服务器/单人世界)
        // 确保在每次进入世界时, 服务端都从配置文件加载配置, 并注册 C2S 网络包处理器
        // 使用 SERVER_STARTING 在 initServer() 之前触发, 比 SERVER_STARTED 更早
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CommonServerEntryPoint.init();

            // 注册 C2S 处理器: 接收客户端推送的配置修改
            ServerPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    ServerConfigHandler.applyConfigFromPlayer(player, payload);
                });
            });

            // 注册 C2S 处理器: 接收客户端配置同步请求, 响应当前配置和权限
            ServerPlayNetworking.registerGlobalReceiver(ConfigRequestPayload.TYPE, (payload, context) -> {
                context.server().execute(() -> {
                    ServerPlayer player = context.player();
                    boolean hasPermission = PlayerUtil.hasPermissionToPushConfig(player);
                    ServerPlayNetworking.send(player, new ConfigSyncPayload(ServerConfig.INSTANCE, hasPermission));
                });
            });
        });
    }
}