package com.dearxuan.easyhopper;

import net.fabricmc.api.ModInitializer;

public class EasyHopper implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConfig.init();
    }
}
