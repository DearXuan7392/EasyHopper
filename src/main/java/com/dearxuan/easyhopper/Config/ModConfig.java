package com.dearxuan.easyhopper.Config;

import com.dearxuan.easyhopper.Config.ModMenu.ModEnv;
import com.dearxuan.easyhopper.Config.Retention.EasyConfig;
import com.dearxuan.easyhopper.Config.Retention.Editable;
import com.dearxuan.easyhopper.Config.Retention.Value;

public class ModConfig {

    public static ModConfig INSTANCE;

    /**
     * 漏斗传输冷却时间
     */
    @EasyConfig(
            value = @Value(min = 1, max = 1200),
            env = ModEnv.ServerOnly
    )
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    /**
     * 漏斗单次输入数量
     */
    @EasyConfig(
            value = @Value(min = 1, max = 64),
            env = ModEnv.ServerOnly
    )
    public int HOPPER_INPUT_COUNT = 1;

    /**
     * 漏斗单次输出数量
     */
    @EasyConfig(
            value = @Value(min = 1, max = 64),
            env = ModEnv.ServerOnly
    )
    public int HOPPER_OUTPUT_COUNT = 1;

    /**
     * 漏斗分类
     */
    @EasyConfig(
            env = ModEnv.ServerOnly
    )
    public boolean HOPPER_CLASSIFICATION = false;

    /**
     * 漏斗传输优化, 已在 1.20.5 被官方优化
     */
    @EasyConfig(
            env = ModEnv.ServerOnly,
            promptKey = "easyhopper.HOPPER_BETTER_EXTRACT.disabled",
            editable = @Editable(editable = false, setToDefault = true)
    )
    public boolean HOPPER_BETTER_EXTRACT = false;

    /**
     * 漏斗检测传输冷却
     */
    @EasyConfig(
            env = ModEnv.ServerOnly
    )
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    /**
     * 漏斗矿车传输冷却
     */
    @EasyConfig(
            env = ModEnv.ServerOnly
    )
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    public ModConfig() {

    }
}
