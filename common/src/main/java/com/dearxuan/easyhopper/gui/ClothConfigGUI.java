package com.dearxuan.easyhopper.gui;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class ClothConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("easyhopper.title"));

        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("easyhopper.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

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
            boolean editable = easyConfig.allowInGame();

            // 处理 Integer 类型
            if (type == int.class || type == Integer.class) {
                int defVal = getFieldValueInt(field, defaultConfig, 0);
                int currentVal = getFieldValueInt(field, ModConfig.INSTANCE, defVal);

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
                                setFieldValue(field, ModConfig.INSTANCE, newValue);
                            }
                        })
                        .build();

                // 在 build 出来的 Entry 实例上设置是否可编辑
                entry.setEditable(editable);

                category.addEntry(entry);
            }
            // 处理 Boolean 类型
            else if (type == boolean.class || type == Boolean.class) {
                boolean defVal = getFieldValueBoolean(field, defaultConfig, false);
                boolean currentVal = getFieldValueBoolean(field, ModConfig.INSTANCE, defVal);

                var entry = entryBuilder.startBooleanToggle(Component.translatable(nameKey), currentVal)
                        .setDefaultValue(defVal)
                        .setTooltip(Component.translatable(tooltipKey))
                        .setSaveConsumer(newValue -> {
                            if (editable) {
                                setFieldValue(field, ModConfig.INSTANCE, newValue);
                            }
                        })
                        .build();

                // 在 build 出来的 Entry 实例上设置是否可编辑
                entry.setEditable(editable);

                category.addEntry(entry);
            }
        }

        // 点击保存时将新值保存回文件
        builder.setSavingRunnable(ConfigManager::save);

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