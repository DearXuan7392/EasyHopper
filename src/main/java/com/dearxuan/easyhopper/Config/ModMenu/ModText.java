package com.dearxuan.easyhopper.Config.ModMenu;

import com.dearxuan.easyhopper.Config.Retention.EasyConfig;
import net.minecraft.client.MinecraftClient;
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
        if (MinecraftClient.getInstance().world != null && !MinecraftClient.getInstance().isInSingleplayer() && easyConfig.env() == ModEnv.ServerOnly) {
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


}
