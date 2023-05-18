package com.dearxuan.easyhopper.Config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
        name = "easyhopper"
)
public class ModConfig implements ConfigData {
    @Excluded
    public static ModConfig INSTANCE;

    @Comment("控制漏斗输送物品冷却时间")
    public int TRANSFER_COOLDOWN = 8;

    @Comment("控制漏斗每次输入多少个物品")
    public int TRANSFER_INPUT_COUNT = 1;

    @Comment("控制漏斗每次输出多少个物品")
    public int TRANSFER_OUTPUT_COUNT = 1;

    @Comment("启用时,会占用漏斗的最后一格作为分类格,这个漏斗只会允许与最后一格相同的物品通过")
    public boolean CLASSIFICATION_HOPPER = false;

    public ModConfig(){

    }

    public static void init(){
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        INSTANCE = (ModConfig) AutoConfig
                .getConfigHolder(ModConfig.class)
                .getConfig();
    }
}
