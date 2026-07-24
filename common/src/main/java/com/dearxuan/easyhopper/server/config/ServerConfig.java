package com.dearxuan.easyhopper.server.config;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.google.gson.Gson;

public class ServerConfig {

    private static final Gson GSON = new Gson();

    /**
     * 服务器配置缓存, 默认初始化为本地配置的副本
     * - 进入服务器时, 服务端推送配置覆盖此实例
     * - 服务端修改配置时, 广播消息覆盖此实例
     * - 断开连接时, 重新从配置文件加载
     */
    public static ModConfig INSTANCE = new ModConfig();

    /**
     * 初始化服务器配置, 从磁盘加载并填充 SERVER_CONFIG
     */
    public static void init() {
        INSTANCE = deepCopy(ConfigManager.load());
    }

    /**
     * 使用 Gson 深拷贝 ModConfig 实例
     */
    public static ModConfig deepCopy(ModConfig original) {
        return GSON.fromJson(GSON.toJson(original), ModConfig.class);
    }
}