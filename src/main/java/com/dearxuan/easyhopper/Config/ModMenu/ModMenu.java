package com.dearxuan.easyhopper.Config.ModMenu;


import com.dearxuan.easyhopper.Config.Retention.EasyConfig;
import com.dearxuan.easyhopper.Config.Retention.Value;
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

                    Function<?, Optional<Text>> errorSupplier = null;
                    Consumer<?> consumer = null;

                    EasyConfig easyConfig = field.getAnnotation(EasyConfig.class);
                    assert easyConfig.env() != ModEnv.Null;

                    general.addEntry(BuildConfig(
                            entryBuilder,
                            field.getName(),
                            errorSupplier,
                            consumer,
                            easyConfig

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

    private static <T> AbstractConfigListEntry<?> BuildConfig(
            ConfigEntryBuilder configEntryBuilder,
            String name,
            Function errorSupplier,
            Consumer consumer,
            EasyConfig easyConfig) {
        try {
            Field field = ConfigClass.getField(name);
            AbstractFieldBuilder<T, AbstractConfigListEntry<?>, ?> builder;
            String type = field.getType().getSimpleName();
            Object defaultValue = ModSaver.DefaultValue.get(name);
            switch (type) {
                case "int":
                    builder = configEntryBuilder
                            .startIntField(ModText.GetTranslatable(name), field.getInt(ModInfo.getInstance()))
                            .setDefaultValue((Integer) defaultValue)
                            .setMin((int)easyConfig.value().min())
                            .setMax((int)easyConfig.value().max())
                            .setTooltip(ModText.GetTooltip(name, promptKey));

                    break;
                case "boolean":
                    builder = configEntryBuilder
                            .startBooleanToggle(ModText.GetTranslatable(name), field.getBoolean(ModInfo.getInstance()))
                            .setDefaultValue((Boolean) defaultValue)
                            .setTooltip(ModText.GetTooltip(name, promptKey));
                    break;
                case "String":
                    builder = configEntryBuilder
                            .startStrField(ModText.GetTranslatable(name), (String) field.get(ModInfo.getInstance()))
                            .setDefaultValue((String) defaultValue)
                            .setTooltip(ModText.GetTooltip(name, promptKey));
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