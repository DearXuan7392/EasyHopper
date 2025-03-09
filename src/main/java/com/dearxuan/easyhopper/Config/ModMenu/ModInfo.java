package com.dearxuan.easyhopper.Config.ModMenu;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ModInfo {

    public static String ModName;

    public static String ModId;

    public static Class ConfigClass;

    public static Logger LOGGER;

    public static Path ConfigurationFilePath;

    public static void Init(String ModName, String ModId, Class ConfigClass) {
        ModInfo.ModName = ModName;
        ModInfo.ModId = ModId;
        ConfigurationFilePath = FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve(ModId + ".json");
        ModInfo.LOGGER = new Logger(ModName);
        ModInfo.LOGGER.info("-------------Easy Hopper Start-------------");
        ModSaver.InitModConfig(ConfigClass);
        ModInfo.LOGGER.info("-------------Easy Hopper  End -------------");
    }

    public static Object getInstance() throws NoSuchFieldException, IllegalAccessException {
        return ConfigClass.getField("INSTANCE").get(null);
    }

    public static void setInstance(Object obj) throws NoSuchFieldException, IllegalAccessException {
        ConfigClass.getField("INSTANCE").set(null, obj);
    }


}
