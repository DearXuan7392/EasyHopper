package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.CommonServerEntryPoint;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.config.ServerConfigHandler;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeEntryPoint {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // 双向注册: ConfigSyncPayload 同时用于 C2S (客户端推送) 和 S2C (服务端同步)
        // playBidirectional 只接受单个 handler, 在 handler 内通过 flow() 区分方向
        registrar.playBidirectional(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC,
                NeoForgeEntryPoint::handleConfigSync
        );

        // C2S: 客户端请求配置同步 (空信号, 仅 C2S)
        registrar.playToServer(
                ConfigRequestPayload.TYPE,
                ConfigRequestPayload.CODEC,
                NeoForgeEntryPoint::handleConfigRequest
        );
    }

    /**
     * 双向处理 ConfigSyncPayload:
     * - C2S (SERVERBOUND): 客户端推送配置到服务端, 由服务端处理
     * - S2C (CLIENTBOUND): 服务端同步配置到客户端, 更新客户端本地缓存
     */
    private static void handleConfigSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow() == PacketFlow.SERVERBOUND) {
                // C2S: 客户端推送配置到服务端
                ServerPlayer player = (ServerPlayer) context.player();
                ServerConfigHandler.applyConfigFromPlayer(player, payload);
            } else {
                // S2C: 服务端同步配置到客户端
                NetManager.updateServerConfig(payload.toConfig(), payload.hasPermission());
            }
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