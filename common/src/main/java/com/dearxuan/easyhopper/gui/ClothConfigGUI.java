package com.dearxuan.easyhopper.gui;

import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class ClothConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        boolean inMultiplayer = CommonConfigGUI.isInMultiplayer();

        // 多人模式下读取服务器配置，单人/离线模式下读取本地配置
        ModConfig targetConfig = CommonConfigGUI.getConfig();
        boolean hasPermission = CommonConfigGUI.hasPermission();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("easyhopper.title"));

        // 分类名称加上模式提示
        Component categoryTitle = inMultiplayer
                ? Component.translatable("easyhopper.gui.server_config")
                : Component.translatable("easyhopper.gui.local_config");

        ConfigCategory category = builder.getOrCreateCategory(categoryTitle);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 顶部模式与权限提示栏
        Component noticeComponent = inMultiplayer
                ? (hasPermission
                ? Component.translatable("easyhopper.gui.notice.server_editable")
                : Component.translatable("easyhopper.gui.notice.server_readonly"))
                : Component.translatable("easyhopper.gui.notice.local");

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
            // 在游戏内可修改性判断：注解允许 且 (单人模式 或 拥有服务器修改权限)
            boolean editable = easyConfig.allowInGame() && hasPermission;

            // 处理 Integer 类型
            if (type == int.class || type == Integer.class) {
                int defVal = getFieldValueInt(field, defaultConfig, 0);
                int currentVal = getFieldValueInt(field, targetConfig, defVal);

                Value valueAnno = easyConfig.value();
                int min = valueAnno != null ? (int) valueAnno.min() : Integer.MIN_VALUE;
                int max = valueAnno != null ? (int) valueAnno.max() : Integer.MAX_VALUE;

                var entry = entryBuilder.startIntField(Component.translatable(nameKey), currentVal)
                        .setDefaultValue(defVal)
                        .setMin(min)
                        .setMax(max)
                        .setTooltip(Component.translatable(tooltipKey))
                        .setSaveConsumer(newValue -> {
                            if (editable) {
                                setFieldValue(field, targetConfig, newValue);
                            }
                        })
                        .build();

                entry.setEditable(editable);
                category.addEntry(entry);
            }
            // 处理 Boolean 类型
            else if (type == boolean.class || type == Boolean.class) {
                boolean defVal = getFieldValueBoolean(field, defaultConfig, false);
                boolean currentVal = getFieldValueBoolean(field, targetConfig, defVal);

                var entry = entryBuilder.startBooleanToggle(Component.translatable(nameKey), currentVal)
                        .setDefaultValue(defVal)
                        .setTooltip(Component.translatable(tooltipKey))
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

        // 保存逻辑：多人模式推送至服务器，单人模式写回本地磁盘
        builder.setSavingRunnable(() -> CommonConfigGUI.saveConfig(targetConfig));

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