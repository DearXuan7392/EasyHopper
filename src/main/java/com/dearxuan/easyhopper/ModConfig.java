package com.dearxuan.easyhopper;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
        name = "easy-hopper"
)
public class ModConfig implements ConfigData {
    @Excluded
    public static ModConfig INSTANCE;

    @Comment("加快漏斗速度,为0时不变,数字越大,速度越快,最大为8")
    public int TransferSpeedUp = 4;
    public ModConfig(){

    }

    public static void init(){
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        INSTANCE = (ModConfig) AutoConfig
                .getConfigHolder(ModConfig.class)
                .getConfig();
    }
}
