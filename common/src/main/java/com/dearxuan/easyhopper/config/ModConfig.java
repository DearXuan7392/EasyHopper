package com.dearxuan.easyhopper.config;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import com.google.gson.Gson;

@Environment(EnvType.BOTH)
public class ModConfig {

    public static ModConfig INSTANCE = new ModConfig();

    /**
     * 服务器配置缓存, 默认初始化为本地配置的副本
     * - 进入服务器时, 服务端推送配置覆盖此实例
     * - 服务端修改配置时, 广播消息覆盖此实例
     * - 断开连接时, 重新初始化为本地配置
     */
    public static ModConfig SERVER_CONFIG = new ModConfig();

    private static final Gson GSON = new Gson();

    @EasyConfig(value = @Value(min = 1, max = 1200))
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    @EasyConfig(value = @Value(min = 1, max = 64))
    public int HOPPER_INPUT_COUNT = 1;

    @EasyConfig(value = @Value(min = 1, max = 64))
    public int HOPPER_OUTPUT_COUNT = 1;

    @EasyConfig
    public boolean HOPPER_FILTERING = false;

    @EasyConfig
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    @EasyConfig(value = @Value(min = 1, max = 1200))
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    @EasyConfig(allowInGame = false)
    public boolean ALLOW_OP_MODIFY = true;

    public ModConfig() {}

    public static void load() {
        ConfigManager.load();
        SERVER_CONFIG = GSON.fromJson(GSON.toJson(INSTANCE), ModConfig.class);
    }

}