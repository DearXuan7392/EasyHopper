package com.dearxuan.easyhopper.gui;

import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class CommonConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        // 创建主 Builder
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("easyhopper.title"));

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("easyhopper.title"));

        // 默认实例，用于通过反射获取字段的默认值
        ModConfig defaultConfig = new ModConfig();

        // 反射遍历 ModConfig 的所有字段
        for (Field field : ModConfig.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(EasyConfig.class)) {
                continue; // 过滤掉没有标记 @EasyConfig 的字段
            }

            field.setAccessible(true);
            EasyConfig easyConfig = field.getAnnotation(EasyConfig.class);
            String fieldName = field.getName();

            // 1. 获取国际化 Key (名称与提示)
            String nameKey = "easyhopper." + fieldName;
            String tooltipKey = easyConfig.tooltip().equals("<modid>.<name>.tooltip")
                    ? nameKey + ".tooltip"
                    : easyConfig.tooltip();

            Class<?> type = field.getType();

            // 2. 处理 int 类型配置项
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
            }
            // 3. 处理 boolean 类型配置项
            else if (type == boolean.class || type == Boolean.class) {
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
                .save(() -> {
                    ModConfig.INSTANCE.save();
                })
                .build()
                .generateScreen(parentScreen);
    }

    // --- 反射辅助工具方法 ---

    private static int getFieldValueInt(Field field, Object instance, int defaultValue) {
        try {
            return field.getInt(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    private static boolean getFieldValueBoolean(Field field, Object instance, boolean defaultValue) {
        try {
            return field.getBoolean(instance);
        } catch (Exception e) {
            e.printStackTrace();
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