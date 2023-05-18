package com.dearxuan.easyhopper.client;

import com.dearxuan.easyhopper.Config.ModConfig;
import net.fabricmc.api.ClientModInitializer;

public class EasyHopperClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ModConfig.init();
    }
}
