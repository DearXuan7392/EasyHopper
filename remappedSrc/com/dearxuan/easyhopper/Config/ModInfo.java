package com.dearxuan.easyhopper.Config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;

public class ModInfo {

    public final static String ModName = "Easy Hopper";

    public final static String ModId = "easyhopper";

    public final static Path ConfigurationFilePath = FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve(ModId + ".json");

    private final static boolean Default_DongWorkInServer = true;

    public static MutableText GetTitle(){
        return Text.translatable(ModId + ".title");
    }

    public static MutableText GetTranslatable(String key){
        return GetTranslatable(key, Default_DongWorkInServer);
    }

    public static MutableText GetTooltip(String key){
        return GetTooltip(key, Default_DongWorkInServer);
    }

    public static MutableText GetTranslatable(String key, boolean DontWorkInServer){
        MutableText mutableText = Text.translatable(ModId + "." + key);
        // 非单人模式或房主, 无法修改游戏功能
        if(DontWorkInServer && MinecraftClient.getInstance().world != null && !MinecraftClient.getInstance().isInSingleplayer()){
            mutableText.setStyle(Style.EMPTY.withFormatting(Formatting.STRIKETHROUGH));
        }
        return mutableText;
    }

    public static MutableText GetTooltip(String key, boolean DontWorkInServer){
        MutableText mutableText = Text.translatable(ModId + "." + key + ".tooltip");
        // 非单人模式或房主, 无法修改游戏功能
        if(DontWorkInServer && MinecraftClient.getInstance().world != null && !MinecraftClient.getInstance().isInSingleplayer()){
            MutableText warn = Text
                    .translatable("dearxuan.DONT_WORK_IN_SERVER")
                    .setStyle(Style.EMPTY.withColor(0xFF0000));
            mutableText.append("\n").append(warn);
        }
        return mutableText;
    }
}
