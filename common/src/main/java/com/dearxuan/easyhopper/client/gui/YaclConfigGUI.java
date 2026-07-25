package com.dearxuan.easyhopper.client.gui;

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
        boolean inMultiplayer = CommonConfigGUI.isInMultiplayer();

        // 多人模式下读取服务器配置，单人/离线模式下读取本地配置
        ModConfig targetConfig = CommonConfigGUI.getConfig();
        boolean hasPermission = CommonConfigGUI.hasPermission();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("easyhopper.title"));

        Component categoryTitle = inMultiplayer
                ? Component.translatable("easyhopper.gui.server_config")
                : Component.translatable("easyhopper.gui.local_config");

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(categoryTitle);

        // 顶部提示组件
        Component noticeComponent = inMultiplayer
                ? (hasPermission
                ? Component.translatable("easyhopper.gui.notice.server_editable")
                : Component.translatable("easyhopper.gui.notice.server_readonly"))
                : Component.translatable("easyhopper.gui.notice.local");

        categoryBuilder.option(LabelOption.create(noticeComponent));

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
            boolean editable = easyConfig.canModifyInGame() && hasPermission;

            Component tooltipComponent = Component.translatable(tooltipKey);
            if (!easyConfig.canModifyInGame()) {
                tooltipComponent = tooltipComponent.copy().append("\n").append(Component.translatable("easyhopper.config.edit_in_config_only"));
            }

            if (type == int.class || type == Integer.class) {
                int defVal = getFieldValueInt(field, defaultConfig, 0);

                Option<Integer> option = Option.<Integer>createBuilder()
                        .name(Component.translatable(nameKey))
                        .description(OptionDescription.of(tooltipComponent))
                        .available(editable)
                        .binding(
                                defVal,
                                () -> getFieldValueInt(field, targetConfig, defVal),
                                val -> {
                                    if (editable) {
                                        setFieldValue(field, targetConfig, val);
                                    }
                                }
                        )
                        .controller(opt -> {
                            IntegerFieldControllerBuilder controller = IntegerFieldControllerBuilder.create(opt);
                            Value valueAnno = easyConfig.value();
                            if (valueAnno.defined()) {
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
                        .description(OptionDescription.of(tooltipComponent))
                        .available(editable)
                        .binding(
                                defVal,
                                () -> getFieldValueBoolean(field, targetConfig, defVal),
                                val -> {
                                    if (editable) {
                                        setFieldValue(field, targetConfig, val);
                                    }
                                }
                        )
                        .controller(BooleanControllerBuilder::create)
                        .build();

                categoryBuilder.option(option);
            }
        }

        return builder
                .category(categoryBuilder.build())
                .save(() -> CommonConfigGUI.saveConfig(targetConfig))
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