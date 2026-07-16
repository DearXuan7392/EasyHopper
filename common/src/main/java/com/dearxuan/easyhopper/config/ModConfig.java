package com.dearxuan.easyhopper.config;

import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;

public class ModConfig {

    public static ModConfig INSTANCE = new ModConfig();

    /**
     * 漏斗传输冷却时间
     */
    @EasyConfig(
            value = @Value(min = 1, max = 1200)
    )
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    /**
     * 漏斗单次输入数量
     */
    @EasyConfig(
            value = @Value(min = 1, max = 64)
    )
    public int HOPPER_INPUT_COUNT = 1;

    /**
     * 漏斗单次输出数量
     */
    @EasyConfig(
            value = @Value(min = 1, max = 64)
    )
    public int HOPPER_OUTPUT_COUNT = 1;

    /**
     * 漏斗分类
     */
    @EasyConfig
    public boolean HOPPER_CLASSIFICATION = false;

    /**
     * 漏斗检测传输冷却
     */
    @EasyConfig
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    /**
     * 漏斗矿车传输冷却
     */
    @EasyConfig
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    public ModConfig() {
        this.HOPPER_TRANSFER_COOLDOWN = 20;
        this.HOPPER_INPUT_COUNT = 4;
        this.HOPPER_OUTPUT_COUNT = 4;
        this.HOPPER_CLASSIFICATION = true;
        this.HOPPER_EXTRACT_COOLDOWN = true;
        this.HOPPER_MINECART_TRANSFER_COOLDOWN = 8;
    }

    public ModConfig(int HOPPER_TRANSFER_COOLDOWN,
                     int HOPPER_INPUT_COUNT,
                     int HOPPER_OUTPUT_COUNT,
                     boolean HOPPER_CLASSIFICATION,
                     boolean HOPPER_EXTRACT_COOLDOWN,
                     int HOPPER_MINECART_TRANSFER_COOLDOWN) {
        this.HOPPER_TRANSFER_COOLDOWN = HOPPER_TRANSFER_COOLDOWN;
        this.HOPPER_INPUT_COUNT = HOPPER_INPUT_COUNT;
        this.HOPPER_OUTPUT_COUNT = HOPPER_OUTPUT_COUNT;
        this.HOPPER_CLASSIFICATION = HOPPER_CLASSIFICATION;
        this.HOPPER_EXTRACT_COOLDOWN = HOPPER_EXTRACT_COOLDOWN;
        this.HOPPER_MINECART_TRANSFER_COOLDOWN = HOPPER_MINECART_TRANSFER_COOLDOWN;
    }
}
