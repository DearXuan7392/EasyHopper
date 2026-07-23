package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.net.ConfigSyncPayload;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric 网络辅助类, 负责 ConfigSyncPayload 的注册与收发
 */
public class FabricNetHelper {

    /**
     * 注册 ConfigSyncPayload 的编解码器到 Play 阶段的负载类型注册表
     */
    public static void registerPayloads() {
        // C2S (客户端发送给服务端)：注册到 serverbound
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        // S2C (服务端发送给客户端)：注册到 clientbound
        // 如果此包也会由服务端广播给客户端，请务必同时注册 clientboundPlay
        PayloadTypeRegistry.serverboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
    }

    /**
     * 注册服务端接收器, 处理客户端推送的配置修改
     */
    public static void registerServerReceivers() {
        // 关键修改：将 registerReceiver 修改为 registerGlobalReceiver
        ServerPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                // 检查权限: 仅管理员 (OP) 可修改配置
                if (!PlayerUtil.hasOpPermission(player)) {
                    Constants.LOG.warn(
                            "Player {} attempted to modify config without permission",
                            player.getName().getString()
                    );
                    return;
                }
                // 应用配置
                ModConfig config = payload.toConfig();
                ModConfig.INSTANCE = config;

                // 打印日志: 记录配置修改者
                Constants.LOG.info(
                        "Config modified by player {} ({}). New values: transferCooldown={}, inputCount={}, outputCount={}, filtering={}, extractCooldown={}, minecartCooldown={}, allowOpModify={}",
                        player.getName().getString(),
                        player.getUUID(),
                        config.HOPPER_TRANSFER_COOLDOWN,
                        config.HOPPER_INPUT_COUNT,
                        config.HOPPER_OUTPUT_COUNT,
                        config.HOPPER_FILTERING,
                        config.HOPPER_EXTRACT_COOLDOWN,
                        config.HOPPER_MINECART_TRANSFER_COOLDOWN,
                        config.ALLOW_OP_MODIFY
                );

                // 广播给所有其他在线玩家 (发起者已在自己的客户端上生效)
                for (ServerPlayer otherPlayer : context.server().getPlayerList().getPlayers()) {
                    if (otherPlayer != player) {
                        ServerPlayNetworking.send(otherPlayer, payload);
                    }
                }
            });
        });
    }

    /**
     * 向指定玩家同步当前服务端配置 (通常用于玩家进服时)
     */
    public static void syncConfigToPlayer(ServerPlayer player) {
        ConfigSyncPayload payload = new ConfigSyncPayload(ModConfig.INSTANCE);
        ServerPlayNetworking.send(player, payload);
    }
}