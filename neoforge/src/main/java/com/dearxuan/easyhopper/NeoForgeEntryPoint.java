package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.CommonServerEntryPoint;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.config.ServerConfigHandler;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEntryPoint {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // 双向注册: ConfigSyncPayload 同时用于 C2S (客户端推送) 和 S2C (服务端同步)
        registrar.playBidirectional(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC,
                NeoForgeEntryPoint::handleServerConfigPush,
                NeoForgeEntryPoint::handleClientConfigSync
        );

        // C2S: 客户端请求配置同步 (空信号, 仅 C2S)
        registrar.playToServer(
                ConfigRequestPayload.TYPE,
                ConfigRequestPayload.CODEC,
                NeoForgeEntryPoint::handleConfigRequest
        );
    }

    /**
     * S2C: 处理服务端下发的配置同步
     * 更新客户端本地缓存 (ServerConfig.INSTANCE + 权限缓存)
     */
    private static void handleClientConfigSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            NetManager.updateServerConfig(payload.toConfig(), payload.hasPermission());
        });
    }

    private static void handleServerConfigPush(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerConfigHandler.applyConfigFromPlayer(player, payload);
        });
    }

    private static void handleConfigRequest(ConfigRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            boolean hasPermission = PlayerUtil.hasPermissionToPushConfig(player);
            PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(ServerConfig.INSTANCE, hasPermission));
        });
    }

    /**
     * 游戏事件总线: 监听服务器启动事件 (包括专用服务端和集成服务器/单人世界)
     * 确保在每次进入世界时, 服务端都从配置文件加载配置
     */
    @EventBusSubscriber(modid = Constants.MOD_ID)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            CommonServerEntryPoint.init();
        }
    }
}