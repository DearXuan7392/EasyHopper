package com.dearxuan.easyhopper.net;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class NetManager {

    private static final Gson GSON = new Gson();

    /**
     * 检查当前玩家是否有权限推送配置信息
     * 需要管理员权限 (OP 等级 >= GAMEMASTERS)
     */
    public static boolean hasPermissionToPush() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return false;
            return PlayerUtil.hasOpPermission(client.player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取服务器配置 (深拷贝副本, 防止 GUI 修改影响缓存)
     * 若 SERVER_CONFIG 尚未被服务端推送覆盖, 则返回本地配置的副本
     */
    public static ModConfig loadConfigFromServer() {
        return deepCopy(ModConfig.SERVER_CONFIG);
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
     * 更新服务端配置缓存 (由网络接收回调调用)
     * 同时更新 ModConfig.INSTANCE 使配置在游戏中立即生效
     */
    public static void updateServerConfig(ModConfig config) {
        ModConfig.SERVER_CONFIG = config;
        ModConfig.INSTANCE = config;
    }

    /**
     * 清除服务端配置缓存 (由客户端断开连接回调调用)
     * 重置为本地配置, 并重新加载本地磁盘上的配置
     */
    public static void clearCache() {
        ConfigManager.load();
        ModConfig.SERVER_CONFIG = deepCopy(ModConfig.INSTANCE);
    }

    /**
     * 使用 Gson 深拷贝 ModConfig 实例
     */
    public static ModConfig deepCopy(ModConfig original) {
        return GSON.fromJson(GSON.toJson(original), ModConfig.class);
    }
}