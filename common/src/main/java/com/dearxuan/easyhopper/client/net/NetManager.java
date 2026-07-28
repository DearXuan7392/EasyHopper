package com.dearxuan.easyhopper.client.net;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ConfigRequestPayload;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public class NetManager {

    private static final Gson GSON = new Gson();
    private static boolean hasPermission = false;

    // 新增：标记当前向服务端的同步请求是否已完成
    private static volatile boolean syncCompleted = false;

    public static boolean isSyncCompleted() {
        return syncCompleted;
    }

    public static void setSyncCompleted(boolean completed) {
        syncCompleted = completed;
    }

    public static boolean hasPermissionToPush() {
        return hasPermission;
    }

    public static ModConfig loadConfigFromServer() {
        return deepCopy(ServerConfig.INSTANCE);
    }

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

    public static void requestConfigSync() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() == null) return;

            syncCompleted = false; // 重置标记
            client.getConnection().send(new ServerboundCustomPayloadPacket(new ConfigRequestPayload()));
        } catch (Exception e) {
            e.printStackTrace();
            syncCompleted = true; // 出错时直接解除阻塞
        }
    }

    /**
     * 更新服务端配置缓存（在客户端收到 S2C 同步网络包时被调用）
     */
    public static void updateServerConfig(ModConfig config, boolean hasPermission) {
        ServerConfig.INSTANCE = config;
        NetManager.hasPermission = hasPermission;
        NetManager.syncCompleted = true; // 收到回应，标记为同步已完成
    }

    public static void clearCache() {
        ModConfig config = ConfigManager.load();
        ServerConfig.INSTANCE = deepCopy(config);
        NetManager.hasPermission = false;
        NetManager.syncCompleted = false;
    }

    public static ModConfig deepCopy(ModConfig original) {
        return GSON.fromJson(GSON.toJson(original), ModConfig.class);
    }
}