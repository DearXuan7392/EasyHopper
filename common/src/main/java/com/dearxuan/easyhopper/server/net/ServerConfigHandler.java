package com.dearxuan.easyhopper.server.net;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;

/**
 * 服务端配置处理器, 处理客户端推送的配置修改
 * 仅依赖服务端安全的类, 避免引入客户端类导致加载异常
 */
public class ServerConfigHandler {

    /**
     * 处理客户端推送的配置修改 (C2S)
     * 检查权限, 应用配置, 打印日志
     *
     * @param player  发起修改的玩家
     * @param payload 携带新配置的数据包
     * @return 权限不足时返回 false, 成功应用配置返回 true
     */
    public static boolean applyConfigFromPlayer(ServerPlayer player, ConfigSyncPayload payload) {
        if (!PlayerUtil.hasPermissionToPushConfig(player)) {
            Constants.LOG.warn(
                    "Player {} attempted to modify config without permission",
                    player.getName().getString()
            );
            return false;
        }
        ModConfig config = payload.toConfig();
        ServerConfig.INSTANCE = config;
        ConfigManager.save(config);

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
        return true;
    }
}