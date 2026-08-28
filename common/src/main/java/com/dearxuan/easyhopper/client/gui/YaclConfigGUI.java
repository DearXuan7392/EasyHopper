package com.dearxuan.easyhopper.client.gui;

import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class YaclConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        boolean inWorld = CommonConfigGUI.isInWorld();
        boolean inMultiplayer = CommonConfigGUI.isInMultiplayer();

        boolean isWaitingSync = inWorld && !NetManager.isSyncCompleted();

        ModConfig targetConfig = CommonConfigGUI.getConfig();
        boolean hasPermission = CommonConfigGUI.hasPermission();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("easyhopper.title"));

        Component categoryTitle = inMultiplayer
                ? Component.translatable("easyhopper.gui.server_config")
                : Component.translatable("easyhopper.gui.local_config");

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(categoryTitle);

        Component noticeComponent;
        if (isWaitingSync) {
            noticeComponent = Component.translatable("easyhopper.gui.notice.syncing");
        } else if (inMultiplayer) {
            noticeComponent = hasPermission
                    ? Component.translatable("easyhopper.gui.notice.server_editable")
                    : Component.translatable("easyhopper.gui.notice.server_readonly");
        } else {
            noticeComponent = Component.translatable("easyhopper.gui.notice.local");
        }

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
            boolean editable = !isWaitingSync && easyConfig.canModifyInGame() && hasPermission;

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

        Screen screen = builder
                .category(categoryBuilder.build())
                .save(() -> {
                    if (!isWaitingSync) {
                        CommonConfigGUI.saveConfig(targetConfig);
                    }
                })
                .build()
                .generateScreen(parentScreen);

        if (isWaitingSync) {
            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                while (!NetManager.isSyncCompleted() && System.currentTimeMillis() - startTime < 3000) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (NetManager.isSyncCompleted()) {
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().screen == screen) {
                            Screen updatedScreen = ClothConfigGUI.createScreen(parentScreen);
                            Minecraft.getInstance().setScreen(updatedScreen);
                        }
                    });
                }
            }).start();
        }

        return screen;
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