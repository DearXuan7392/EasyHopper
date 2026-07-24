package com.dearxuan.easyhopper.client.net;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class NetManager {

    private static final Gson GSON = new Gson();

    /**
     * 服务端下发的权限缓存, 由 S2C 数据包更新
     * - 单人模式: 服务端始终下发 true
     * - 多人模式: 服务端根据 OP 权限 + ALLOW_OP_MODIFY 判断
     */
    private static boolean hasPermission = false;

    /**
     * 检查当前玩家是否有权限修改服务器配置
     * 返回服务端下发的缓存权限值
     */
    public static boolean hasPermissionToPush() {
        return hasPermission;
    }

    /**
     * 获取服务器配置 (深拷贝副本, 防止 GUI 修改影响缓存)
     * 若 SERVER_CONFIG 尚未被服务端推送覆盖, 则返回本地配置的副本
     */
    public static ModConfig loadConfigFromServer() {
        return deepCopy(ServerConfig.INSTANCE);
    }

    /**
     * 向服务器推送配置信息
     * 通过 ConfigSyncPayload 发送到服务端
     */
    public static boolean pushConfigToServer(ModConfig config) {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() == null) return false;

            ConfigSyncPayload payload = new ConfigSyncPayload(config);
            client.getConnection().send(new ServerboundCustomPayloadPacket(payload));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向服务器请求当前配置和权限同步
     * 服务端收到后以 ConfigSyncPayload 响应, 更新本地缓存
     */
    public static void requestConfigSync() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() == null) return;

            client.getConnection().send(new ServerboundCustomPayloadPacket(new ConfigRequestPayload()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新服务端配置缓存 (由网络接收回调调用)
     * 同时更新 ServerConfig.SERVER_CONFIG 使配置在游戏中立即生效
     *
     * @param config         服务端下发的配置
     * @param hasPermission  服务端判断的权限
     */
    public static void updateServerConfig(ModConfig config, boolean hasPermission) {
        ServerConfig.INSTANCE = config;
        NetManager.hasPermission = hasPermission;
    }

    /**
     * 清除服务端配置缓存 (由客户端断开连接回调调用)
     * 重置为本地配置, 并重新加载本地磁盘上的配置
     */
    public static void clearCache() {
        ModConfig config = ConfigManager.load();
        ServerConfig.INSTANCE = deepCopy(config);
        NetManager.hasPermission = false;
    }

    /**
     * 使用 Gson 深拷贝 ModConfig 实例
     */
    public static ModConfig deepCopy(ModConfig original) {
        return GSON.fromJson(GSON.toJson(original), ModConfig.class);
    }
}