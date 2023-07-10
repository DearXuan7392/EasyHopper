package com.dearxuan.easyhopper.EntryPoint;

import com.dearxuan.easyhopper.Config.ModConfig;
import com.dearxuan.easyhopper.Config.ModMenu.ModInfo;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        ModInfo.Init("Easy Hopper", "easyhopper", ModConfig.class);
    }
}
