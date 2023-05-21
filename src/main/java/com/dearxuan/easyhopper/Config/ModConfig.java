package com.dearxuan.easyhopper.Config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(
        name = "easyhopper"
)
public class ModConfig implements ConfigData {

    @Excluded
    public static ModConfig INSTANCE;

    public int TRANSFER_COOLDOWN = 8;

    public int TRANSFER_INPUT_COUNT = 1;

    public int TRANSFER_OUTPUT_COUNT = 1;

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
