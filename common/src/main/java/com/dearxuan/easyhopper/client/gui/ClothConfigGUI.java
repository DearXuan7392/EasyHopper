package com.dearxuan.easyhopper.client.gui;

import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class ClothConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        boolean inWorld = CommonConfigGUI.isInWorld();
        boolean inMultiplayer = CommonConfigGUI.isInMultiplayer();

        // 检查是否在等待服务器返回数据
        boolean isWaitingSync = inWorld && !NetManager.isSyncCompleted();

        ModConfig targetConfig = CommonConfigGUI.getConfig();
        boolean hasPermission = CommonConfigGUI.hasPermission();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("easyhopper.title"));

        Component categoryTitle = inMultiplayer
                ? Component.translatable("easyhopper.gui.server_config")
                : Component.translatable("easyhopper.gui.local_config");

        ConfigCategory category = builder.getOrCreateCategory(categoryTitle);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 顶部模式与权限提示栏
        Component noticeComponent;
        if (isWaitingSync) {
            noticeComponent = Component.translatable("easyhopper.gui.notice.syncing"); // "正在同步服务器数据..."
        } else if (inMultiplayer) {
            noticeComponent = hasPermission
                    ? Component.translatable("easyhopper.gui.notice.server_editable")
                    : Component.translatable("easyhopper.gui.notice.server_readonly");
        } else {
            noticeComponent = Component.translatable("easyhopper.gui.notice.local");
        }

        category.addEntry(entryBuilder.startTextDescription(noticeComponent).build());

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
            // 在未同步完成前，强制只读禁用；同步完成后恢复编辑权限
            boolean editable = !isWaitingSync && easyConfig.canModifyInGame() && hasPermission;

            Component tooltipComponent = Component.translatable(tooltipKey);
            if (!easyConfig.canModifyInGame()) {
                tooltipComponent = tooltipComponent.copy().append("\n").append(Component.translatable("easyhopper.config.edit_in_config_only"));
            }

            if (type == int.class || type == Integer.class) {
                int defVal = getFieldValueInt(field, defaultConfig, 0);
                int currentVal = getFieldValueInt(field, targetConfig, defVal);

                Value valueAnno = easyConfig.value();
                int min = valueAnno.defined() ? (int) valueAnno.min() : Integer.MIN_VALUE;
                int max = valueAnno.defined() ? (int) valueAnno.max() : Integer.MAX_VALUE;

                var entry = entryBuilder.startIntField(Component.translatable(nameKey), currentVal)
                        .setDefaultValue(defVal)
                        .setMin(min)
                        .setMax(max)
                        .setTooltip(tooltipComponent)
                        .setSaveConsumer(newValue -> {
                            if (editable) {
                                setFieldValue(field, targetConfig, newValue);
                            }
                        })
                        .build();

                entry.setEditable(editable);
                category.addEntry(entry);
            } else if (type == boolean.class || type == Boolean.class) {
                boolean defVal = getFieldValueBoolean(field, defaultConfig, false);
                boolean currentVal = getFieldValueBoolean(field, targetConfig, defVal);

                var entry = entryBuilder.startBooleanToggle(Component.translatable(nameKey), currentVal)
                        .setDefaultValue(defVal)
                        .setTooltip(tooltipComponent)
                        .setSaveConsumer(newValue -> {
                            if (editable) {
                                setFieldValue(field, targetConfig, newValue);
                            }
                        })
                        .build();

                entry.setEditable(editable);
                category.addEntry(entry);
            }
        }

        builder.setSavingRunnable(() -> {
            if (!isWaitingSync) {
                CommonConfigGUI.saveConfig(targetConfig);
            }
        });

        // 监听服务器数据包更新：当在 GUI 里时收到 S2C 同步包，自动重新渲染更新界面
        builder.setAfterInitConsumer(screen -> {
            // 如果还在等待同步，开一个线程或在 render/tick 中检查更新
            if (isWaitingSync) {
                // 利用一个轻量定时逻辑在收到回调后重新建立界面
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
        });

        return builder.build();
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