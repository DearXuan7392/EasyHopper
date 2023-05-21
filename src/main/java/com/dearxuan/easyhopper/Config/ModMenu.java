package com.dearxuan.easyhopper.Config;

import com.dearxuan.easyhopper.EasyHopper;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class ModMenu implements ModMenuApi {

    private static final String HEAD = "easyhopper";

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory(){
        return parent -> {
            try{
                ConfigBuilder builder = ConfigBuilder
                        .create()
                        .setParentScreen(parent)
                        .setTitle(Text.translatable(HEAD + ".title"));
                builder.setSavingRunnable(()->{
                    AutoConfig.getConfigHolder(ModConfig.class).save();
                });
                ConfigCategory general = builder
                        .getOrCreateCategory(Text.translatable(HEAD + ".title"));
                ConfigEntryBuilder entryBuilder = builder.entryBuilder();
                general
                        .addEntry(BuildConfig(entryBuilder, "TRANSFER_COOLDOWN", 8))
                        .addEntry(BuildConfig(entryBuilder, "TRANSFER_INPUT_COUNT", 1))
                        .addEntry(BuildConfig(entryBuilder, "TRANSFER_OUTPUT_COUNT", 1))
                        .addEntry(BuildConfig(entryBuilder, "CLASSIFICATION_HOPPER", false));
                Screen screen = builder.build();
                return screen;
            }catch (Exception e){
                EasyHopper.LOGGER.error(e.getMessage());
                return null;
            }
        };
    }

    private static AbstractConfigListEntry BuildConfig(ConfigEntryBuilder entryBuilder, String name, int defaultValue){
        try{
            String Text_Name = HEAD + "." + name;
            String Text_Tooltip = Text_Name + ".tooltip";
            Field field = ModConfig.class.getDeclaredField(name);
            return entryBuilder
                    .startIntField(Text.translatable(Text_Name), field.getInt(ModConfig.INSTANCE))
                    .setDefaultValue(defaultValue)
                    .setTooltip(Text.translatable(Text_Tooltip))
                    .setSaveConsumer(value -> {
                        try {
                            field.setInt(ModConfig.INSTANCE, value);
                        } catch (IllegalAccessException e) {
                            EasyHopper.LOGGER.error(e.getMessage());
                        }
                    })
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private static AbstractConfigListEntry BuildConfig(ConfigEntryBuilder entryBuilder, String name, double defaultValue){
        try{
            String Text_Name = HEAD + "." + name;
            String Text_Tooltip = Text_Name + ".tooltip";
            Field field = ModConfig.class.getDeclaredField(name);
            return entryBuilder
                    .startDoubleField(Text.translatable(Text_Name), field.getDouble(ModConfig.INSTANCE))
                    .setDefaultValue(defaultValue)
                    .setTooltip(Text.translatable(Text_Tooltip))
                    .setSaveConsumer(value -> {
                        try {
                            field.setDouble(ModConfig.INSTANCE, value);
                        } catch (IllegalAccessException e) {
                            EasyHopper.LOGGER.error(e.getMessage());
                        }
                    })
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private static AbstractConfigListEntry BuildConfig(ConfigEntryBuilder entryBuilder, String name, boolean defaultValue){
        try{
            String Text_Name = HEAD + "." + name;
            String Text_Tooltip = Text_Name + ".tooltip";
            Field field = ModConfig.class.getDeclaredField(name);
            return entryBuilder
                    .startBooleanToggle(Text.translatable(Text_Name), field.getBoolean(ModConfig.INSTANCE))
                    .setDefaultValue(defaultValue)
                    .setTooltip(Text.translatable(Text_Tooltip))
                    .setSaveConsumer(value -> {
                        try {
                            field.setBoolean(ModConfig.INSTANCE, value);
                        } catch (IllegalAccessException e) {
                            EasyHopper.LOGGER.error(e.getMessage());
                        }
                    })
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
