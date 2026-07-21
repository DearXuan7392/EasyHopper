package com.dearxuan.easyhopper.gui;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class YaclConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("easyhopper.title"));

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("easyhopper.title"));

        ModConfig defaultConfig = new ModConfig();

        for (Field field : ModConfig.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(EasyConfig.class)) {
                continue;
            }

            field.setAccessible(true);
            EasyConfig easyConfig = field.getAnnotation(EasyConfig.class);
            String fieldName = field.getName();

            String nameKey = "easyhopper." + fieldName;
            String tooltipKey = easyConfig.tooltip().equals("<modid>.<name>.tooltip")
                    ? nameKey + ".tooltip"
                    : easyConfig.tooltip();

            Class<?> type = field.getType();

            if (type == int.class || type == Integer.class) {
                int defVal = getFieldValueInt(field, defaultConfig, 0);

                Option<Integer> option = Option.<Integer>createBuilder()
                        .name(Component.translatable(nameKey))
                        .description(OptionDescription.of(Component.translatable(tooltipKey)))
                        .binding(
                                defVal,
                                () -> getFieldValueInt(field, ModConfig.INSTANCE, defVal),
                                val -> setFieldValue(field, ModConfig.INSTANCE, val)
                        )
                        .controller(opt -> {
                            IntegerFieldControllerBuilder controller = IntegerFieldControllerBuilder.create(opt);
                            Value valueAnno = easyConfig.value();
                            if (valueAnno != null) {
                                controller.min((int) valueAnno.min());
                                controller.max((int) valueAnno.max());
                            }
                            return controller;
                        })
                        .build();

                categoryBuilder.option(option);
            } else if (type == boolean.class || type == Boolean.class) {
                boolean defVal = getFieldValueBoolean(field, defaultConfig, false);

                Option<Boolean> option = Option.<Boolean>createBuilder()
                        .name(Component.translatable(nameKey))
                        .description(OptionDescription.of(Component.translatable(tooltipKey)))
                        .binding(
                                defVal,
                                () -> getFieldValueBoolean(field, ModConfig.INSTANCE, defVal),
                                val -> setFieldValue(field, ModConfig.INSTANCE, val)
                        )
                        .controller(BooleanControllerBuilder::create)
                        .build();

                categoryBuilder.option(option);
            }
        }

        return builder
                .category(categoryBuilder.build())
                .save(ConfigManager::save)
                .build()
                .generateScreen(parentScreen);
    }

    private static int getFieldValueInt(Field field, Object instance, int defaultValue) {
        try {
            return field.getInt(instance);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static boolean getFieldValueBoolean(Field field, Object instance, boolean defaultValue) {
        try {
            return field.getBoolean(instance);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static void setFieldValue(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}