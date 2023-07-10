package com.dearxuan.easyhopper.Config;

import com.dearxuan.easyhopper.Config.ModMenu.EasyConfig;

public class ModConfig {

    public static ModConfig INSTANCE;

    /**
     * 漏斗传输冷却世界
     */
    @EasyConfig(
            min = "0",
            max = "64"
    )
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    /**
     * 漏斗单次输入数量
     */
    @EasyConfig(
            min = "1",
            max = "64"
    )
    public int HOPPER_INPUT_COUNT = 1;

    /**
     * 漏斗单次输出数量
     */
    @EasyConfig(
            min = "1",
            max = "64"
    )
    public int HOPPER_OUTPUT_COUNT = 1;

    /**
     * 漏斗分类
     */
    public boolean HOPPER_CLASSIFICATION = false;

    /**
     * 漏斗传输优化
     */
    public boolean HOPPER_BETTER_EXTRACT = false;

    /**
     * 漏斗检测传输冷却
     */
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    /**
     * 漏斗矿车传输冷却
     */
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    /**
     * 漏斗矿车性能增强
     */
    public boolean HOPPER_MINECART_BETTER_EXTRACT = false;

    public ModConfig() {

    }
}
