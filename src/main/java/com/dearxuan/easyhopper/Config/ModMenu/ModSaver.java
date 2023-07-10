package com.dearxuan.easyhopper.Config.ModMenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;

import static com.dearxuan.easyhopper.Config.ModMenu.ModInfo.ConfigClass;
import static com.dearxuan.easyhopper.Config.ModMenu.ModInfo.LOGGER;

public class ModSaver {

    public static HashMap<String, Object> DefaultValue = new HashMap<>();


    /**
     * Init mod configuration<br/>
     * needs fabric api
     */
    public static void InitModConfig(Class config) {
        try {
            ConfigClass = config;
            // 获取配置文件类,
            Field[] fields = ConfigClass.getFields();

            // 创建实例
            Object defaultConfig = ConfigClass.getDeclaredConstructor().newInstance();

            // 获取默认值
            for (Field field : fields) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    try {
                        DefaultValue.put(field.getName(), field.get(defaultConfig));
                    } catch (IllegalAccessException e) {
                        LOGGER.error(e);
                    }
                }
            }

            // 尝试读取配置, 若失败, 则使用默认配置
            if (!ReadConfig()) {
                ConfigClass.getField("INSTANCE").set(null, defaultConfig);
            }
            // 覆盖原配置文件, 防止mod升级后新增的配置无法写入
            WriteConfig();

            // 控制台打印配置信息
            for (Field field : fields) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    try {
                        LOGGER.info(field.getName() + ": " + field.get(ModInfo.getInstance()));
                    } catch (IllegalAccessException e) {
                        LOGGER.error(e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Save config file
     *
     * @return true if success
     */
    public static boolean Save() {
        return WriteConfig();
    }

    private static boolean WriteConfig() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(ModInfo.ConfigurationFilePath.toFile())) {
                writer.write(gson.toJson(ModInfo.getInstance()));
                return true;
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    private static boolean ReadConfig() {
        File file = new File(ModInfo.ConfigurationFilePath.toUri());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (file.exists()) {
                try (BufferedReader reader = Files.newBufferedReader(ModInfo.ConfigurationFilePath)) {
                    Object modConfig = gson.fromJson(reader, ConfigClass);
                    ModInfo.setInstance(modConfig);
                    return true;
                }
            }
        } catch (Exception ignored) {

        }
        return false;
    }


}
