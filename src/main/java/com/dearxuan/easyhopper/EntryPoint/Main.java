package com.dearxuan.easyhopper.EntryPoint;

import com.dearxuan.easyhopper.Config.ModConfig;
import com.dearxuan.easyhopper.Config.ModSaver;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("easyhopper");

    @Override
    public void onInitialize() {
        LOGGER.info("-------------Easy Hopper Start-------------");
        ModSaver.InitModConfig(ModConfig.class);
        LOGGER.info("-------------Easy Hopper  End -------------");
    }
}
