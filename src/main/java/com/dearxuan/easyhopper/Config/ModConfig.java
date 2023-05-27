package com.dearxuan.easyhopper.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig{

    @Excluded
    public static ModConfig INSTANCE;

    @Excluded
    private final static String configPath = FabricLoader.getInstance().getGameDir() + "/config/easyhopper.json";

    public int TRANSFER_COOLDOWN = 8;

    public int TRANSFER_INPUT_COUNT = 1;

    public int TRANSFER_OUTPUT_COUNT = 1;

    public boolean CLASSIFICATION_HOPPER = false;

    public ModConfig(){

    }

    public static boolean Save(){
        return WriteConfig();
    }

    public static void init() {
        if(!ReadConfig()){
            ModConfig.INSTANCE = new ModConfig();
            WriteConfig();
        }
    }

    private static boolean WriteConfig(){
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(FileWriter writer = new FileWriter(configPath)){
                writer.write(gson.toJson(ModConfig.INSTANCE));
                return true;
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    private static boolean ReadConfig(){
        File file = new File(configPath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if(file.exists()){
                try (BufferedReader reader = Files.newBufferedReader(Path.of(configPath))) {
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
