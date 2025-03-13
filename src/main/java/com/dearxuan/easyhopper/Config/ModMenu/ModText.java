package com.dearxuan.easyhopper.Config.ModMenu;

import com.dearxuan.easyhopper.Config.Retention.EasyConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.dearxuan.easyhopper.Config.ModMenu.ModInfo.ModId;

public class ModText {

    public static MutableText GetTitle() {
        return Text.translatable(ModId + ".title");
    }

    public static MutableText GetTooltip(String name, EasyConfig easyConfig) {
        String tooltipKey = easyConfig
                .tooltip()
                .replaceAll("<modid>", ModId)
                .replaceAll("<name>", name);
        MutableText text = Text.translatable(tooltipKey);
        if (!easyConfig.promptKey().isBlank()) {
            String promptKey = easyConfig
                    .promptKey()
                    .replaceAll("<modid>", ModId)
                    .replaceAll("<name>", name);
            MutableText prompt = Text
                    .translatable(promptKey)
                    .setStyle(Style.EMPTY.withColor(0x00FF00));
            text.append("\n").append(prompt);
        }
        if (!canEditConfig(easyConfig.env())) {
            MutableText warn = Text
                    .translatable("error.DONT_WORK_IN_SERVER")
                    .setStyle(Style.EMPTY.withColor(0xFF0000));
            text.append("\n").append(warn);
        }
        return text;
    }

    public static MutableText GetTranslatable(String key) {
        MutableText mutableText = Text.translatable(ModId + "." + key);
        return mutableText;
    }

    private static boolean canEditConfig(ModEnv modEnv) {
        // 未进入游戏
        if (MinecraftClient.getInstance().world == null) {
            return true;
        }
        // 单机游戏
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            return true;
        }
        // 局域网联机房主
        if (modEnv == ModEnv.ServerOnly) {
            MinecraftServer server = MinecraftClient.getInstance().getServer();
            return server != null && server.isRemote();
        }
        return true;
    }
}
