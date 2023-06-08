package com.dearxuan.easyhopper.Config;

@ModSaver.ModConfigAnnotation(
        ModId = "easyhopper"
)
public class ModConfig{

    public static ModConfig INSTANCE;

    public int TRANSFER_COOLDOWN = 8;

    public int TRANSFER_INPUT_COUNT = 1;

    public int TRANSFER_OUTPUT_COUNT = 1;

    public boolean CLASSIFICATION_HOPPER = false;

    public ModConfig(){

    }
}
