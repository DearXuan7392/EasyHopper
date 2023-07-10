package com.dearxuan.easyhopper.Config.ModMenu;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.dearxuan.easyhopper.Config.ModMenu.ModInfo.ConfigClass;
import static com.dearxuan.easyhopper.Config.ModMenu.ModInfo.LOGGER;


@Environment(EnvType.CLIENT)
public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            try {
                ConfigBuilder builder = ConfigBuilder
                        .create()
                        .setParentScreen(parent)
                        .setTitle(ModText.GetTitle());
                builder.setSavingRunnable(ModSaver::Save);
                ConfigCategory general = builder
                        .getOrCreateCategory(ModText.GetTitle());
                ConfigEntryBuilder entryBuilder = builder.entryBuilder();
                for (Field field : ConfigClass.getFields()) {
                    int modifier = field.getModifiers();
                    if (Modifier.isStatic(modifier) || !Modifier.isPublic(modifier)) {
                        continue;
                    }

                    Function errorSupplier = null;
                    Consumer consumer = null;
                    Number min = 0;
                    Number max = 256;

                    EasyConfig easyConfig = field.getAnnotation(EasyConfig.class);
                    if (easyConfig != null) {
                        if (!easyConfig.min().isBlank()) {
                            min = Double.parseDouble(easyConfig.min());
                        }
                        if (!easyConfig.max().isBlank()) {
                            max = Double.parseDouble(easyConfig.max());
                        }
                    }
                    general.addEntry(BuildConfig(
                            entryBuilder,
                            field.getName(),
                            errorSupplier,
                            consumer,
                            min,
                            max

                    ));
                }
                Screen screen = builder.build();
                return screen;
            } catch (Exception e) {
                LOGGER.error(e);
                return null;
            }
        };
    }

    private static <T> AbstractConfigListEntry BuildConfig(ConfigEntryBuilder configEntryBuilder, String name, Function<T, Optional<Text>> errorSupplier, Consumer<T> consumer, Number min, Number max) {
        try {
            Field field = ConfigClass.getField(name);
            AbstractFieldBuilder builder;
            String type = field.getType().getSimpleName();
            Object defaultValue = ModSaver.DefaultValue.get(name);
            switch (type) {
                case "int":
                    builder = configEntryBuilder
                            .startIntField(ModText.GetTranslatable(name), field.getInt(ModInfo.getInstance()))
                            .setDefaultValue((Integer) defaultValue)
                            .setMin(min.intValue())
                            .setMax(max.intValue())
                            .setTooltip(ModText.GetTooltip(name));
                    break;
                case "double":
                    builder = configEntryBuilder
                            .startDoubleField(ModText.GetTranslatable(name), field.getDouble(ModInfo.getInstance()))
                            .setDefaultValue((Double) defaultValue)
                            .setMin(min.doubleValue())
                            .setMax(max.doubleValue())
                            .setTooltip(ModText.GetTooltip(name));
                    break;
                case "boolean":
                    builder = configEntryBuilder
                            .startBooleanToggle(ModText.GetTranslatable(name), field.getBoolean(ModInfo.getInstance()))
                            .setDefaultValue((Boolean) defaultValue)
                            .setTooltip(ModText.GetTooltip(name));
                    break;
                case "String":
                    builder = configEntryBuilder
                            .startStrField(ModText.GetTranslatable(name), (String) field.get(ModInfo.getInstance()))
                            .setDefaultValue((String) defaultValue)
                            .setTooltip(ModText.GetTooltip(name));
                    break;
                default:
                    throw new Exception("Unknown Type: " + type);
            }
            if (consumer == null) {
                builder.setSaveConsumer(value -> {
                    try {
                        field.set(ModInfo.getInstance(), value);
                    } catch (Exception e) {
                        LOGGER.error(e);
                    }
                });
            } else {
                builder.setSaveConsumer(consumer);
            }
            if (errorSupplier != null) {
                builder.setErrorSupplier(errorSupplier);
            }
            return builder.build();
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }
}