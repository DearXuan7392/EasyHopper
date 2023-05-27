package com.dearxuan.easyhopper.EntryPoint;

import com.dearxuan.easyhopper.Config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyHopper implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("easyhopper");

    @Override
    public void onInitialize() {

        ModConfig.init();
    }
}
