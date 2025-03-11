package com.dearxuan.easyhopper.Config.ModMenu;

import com.dearxuan.easyhopper.Config.Retention.EasyConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.dearxuan.easyhopper.Config.ModMenu.ModInfo.ModId;

public class ModText {

    private final static ModEnv Default_DongWorkInServer = ModEnv.ServerOnly;

    public static MutableText GetTitle() {
        return Text.translatable(ModId + ".title");
    }

    public static MutableText GetTranslatable(String key) {
        return GetTranslatable(key, Default_DongWorkInServer);
    }

    public static MutableText GetTooltip(String key) {
        return GetTooltip(key, Default_DongWorkInServer);
    }

    public static MutableText GetTooltip(String key, String promptKey) {
        return GetTooltip(key, Default_DongWorkInServer);
    }

    public static MutableText GetTooltip(String key, EasyConfig easyConfig) {
        return GetTooltip(key, Default_DongWorkInServer);
    }

    public static MutableText GetTooltip(String key, ModEnv modEnv) {
        MutableText mutableText = Text.translatable(ModId + "." + key + ".tooltip");
        // 非单人模式或房主, 无法修改游戏功能
        if (modEnv == ModEnv.ServerOnly && MinecraftClient.getInstance().world != null && !MinecraftClient.getInstance().isInSingleplayer()) {
            MutableText warn = Text
                    .translatable("dearxuan.DONT_WORK_IN_SERVER")
                    .setStyle(Style.EMPTY.withColor(0xFF0000));
            mutableText.append("\n").append(warn);
        }
        return mutableText;
    }
    public static MutableText GetTranslatable(String key, ModEnv modEnv) {
        MutableText mutableText = Text.translatable(ModId + "." + key);
        // 非单人模式或房主, 无法修改游戏功能
        if (modEnv == ModEnv.ServerOnly && MinecraftClient.getInstance().world != null && !MinecraftClient.getInstance().isInSingleplayer()) {
            mutableText.setStyle(Style.EMPTY.withFormatting(Formatting.STRIKETHROUGH));
        }
        return mutableText;
    }


}
