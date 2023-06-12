package com.dearxuan.easyhopper.Config;

import com.dearxuan.easyhopper.EntryPoint.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ModSaver {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ModConfigAnnotation {
        String ModId();
    }

    private static Class ModConfigClass = null;

    public static HashMap<String, Object> DefaultValue = new HashMap<>();

    private static String ModId = null;

    private static String ConfigPath = null;


    public static void InitModConfig(Class modConfigClass){
        ModConfigClass = modConfigClass;
        ModId = ((ModConfigAnnotation)ModConfigClass.getAnnotation(ModConfigAnnotation.class)).ModId();
        ConfigPath = FabricLoader.getInstance().getGameDir() + "/config/" + ModId + ".json";

        Field[] fields = ModConfig.class.getDeclaredFields();
        ModConfig defaultConfig = new ModConfig();
        for (Field field : fields){
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())){
                try {
                    DefaultValue.put(field.getName(), field.get(defaultConfig));
                } catch (IllegalAccessException e) {
                    Main.LOGGER.error(e.getMessage());
                }
            }
        }

        if(!ReadConfig()){
            ModConfig.INSTANCE = new ModConfig();
            WriteConfig();
        }
    }

    public static boolean Save(){
        return WriteConfig();
    }

    private static boolean WriteConfig(){
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(FileWriter writer = new FileWriter(ConfigPath)){
                writer.write(gson.toJson(ModConfig.INSTANCE));
                return true;
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    private static boolean ReadConfig(){
        File file = new File(ConfigPath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if(file.exists()){
                try (BufferedReader reader = Files.newBufferedReader(Path.of(ConfigPath))) {
                    ModConfig modConfig = gson.fromJson(reader, ModConfig.class);
                    ModConfig.INSTANCE = modConfig;
                    return true;
                }
            }
        } catch (Exception ignored){

        }
        return false;
    }
}
