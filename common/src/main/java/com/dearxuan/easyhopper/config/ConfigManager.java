package com.dearxuan.easyhopper.config;

import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.language.I18n;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final File CONFIG_FILE = Path.of("config", "easyhopper.yaml").toFile();
    private static final Map<String, String> EN_US_FALLBACK_MAP = new HashMap<>();

    static {
        // 读取 Jar 内置的 assets/easyhopper/lang/en_us.json 作为底层兜底
        try (InputStream stream = ConfigManager.class.getResourceAsStream("/assets/easyhopper/lang/en_us.json")) {
            if (stream != null) {
                JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                json.entrySet().forEach(entry -> EN_US_FALLBACK_MAP.put(entry.getKey(), entry.getValue().getAsString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTranslationWithFallback(String key) {
        try {
            if (I18n.exists(key)) {
                return I18n.get(key);
            }
        } catch (Throwable ignored) {
        }

        if (EN_US_FALLBACK_MAP.containsKey(key)) {
            return EN_US_FALLBACK_MAP.get(key);
        }

        return key;
    }

    public static String generateYamlString(ModConfig config) {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Representer representer = new Representer(options);
            representer.addClassTag(ModConfig.class, Tag.MAP);

            Yaml yaml = new Yaml(new Constructor(ModConfig.class, new LoaderOptions()), representer, options);
            String rawYaml = yaml.dump(config);

            StringBuilder finalYamlWithComments = new StringBuilder();
            finalYamlWithComments.append("# EasyHopper Configuration File\n\n");

            for (String line : rawYaml.split("\n")) {
                if (line.contains(":")) {
                    String key = line.split(":")[0].trim();
                    try {
                        Field field = ModConfig.class.getDeclaredField(key);
                        if (field.isAnnotationPresent(EasyConfig.class)) {
                            EasyConfig anno = field.getAnnotation(EasyConfig.class);

                            String nameKey = "easyhopper." + key;
                            String tooltipKey = anno.tooltip().equals("<modid>.<name>.tooltip")
                                    ? nameKey + ".tooltip"
                                    : anno.tooltip();

                            String translatedName = getTranslationWithFallback(nameKey);
                            if (!translatedName.equals(nameKey)) {
                                finalYamlWithComments.append("# ").append(translatedName).append("\n");
                            }

                            String translatedTooltip = getTranslationWithFallback(tooltipKey);
                            if (!translatedTooltip.equals(tooltipKey)) {
                                for (String tLine : translatedTooltip.split("\n")) {
                                    finalYamlWithComments.append("# ").append(tLine).append("\n");
                                }
                            }

                            // 检查 Field 是否为数值类型（如 int, long, float, double 等）
                            Class<?> fieldType = field.getType();
                            boolean isNumberType = Number.class.isAssignableFrom(fieldType)
                                    || fieldType == int.class || fieldType == long.class
                                    || fieldType == float.class || fieldType == double.class;

                            if (isNumberType) {
                                Value val = anno.value();
                                if (val != null) {
                                    finalYamlWithComments.append("# Range: [").append((long) val.min()).append(" ~ ").append((long) val.max()).append("]\n");
                                }
                            }
                        }
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                finalYamlWithComments.append(line).append("\n");
            }

            return finalYamlWithComments.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 保存当前配置到磁盘（只有显式调用 save 时才会写入覆盖配置文件）
     */
    public static void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }

            String content = generateYamlString(ModConfig.INSTANCE);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE.toPath(), StandardCharsets.UTF_8)) {
                writer.write(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取配置（若文件不存在则初始化并创建文件，存在则读取并进行数值矫正，绝不主动覆写磁盘文件）
     */
    public static void load() {
        if (!CONFIG_FILE.exists()) {
            // 首次启动时不存在配置文件，才自动创建一次
            save();
            return;
        }

        // 读取现有的配置
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE.toPath(), StandardCharsets.UTF_8)) {
            Representer representer = new Representer(new DumperOptions());
            representer.getPropertyUtils().setSkipMissingProperties(true);

            Yaml yaml = new Yaml(new Constructor(ModConfig.class, new LoaderOptions()), representer);
            ModConfig loaded = yaml.load(reader);

            if (loaded != null) {
                ModConfig.INSTANCE = loaded;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ModConfig.INSTANCE = new ModConfig();
        }

        // 仅在内存中规范超限数值，不写回磁盘
        validateAndSanitize(ModConfig.INSTANCE);
    }

    private static void validateAndSanitize(ModConfig config) {
        for (Field field : ModConfig.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(EasyConfig.class)) {
                field.setAccessible(true);
                EasyConfig anno = field.getAnnotation(EasyConfig.class);
                Value val = anno.value();

                if (val != null && (field.getType() == int.class || field.getType() == Integer.class)) {
                    try {
                        int currentVal = field.getInt(config);
                        int min = (int) val.min();
                        int max = (int) val.max();

                        if (currentVal < min || currentVal > max) {
                            int sanitized = Math.max(min, Math.min(max, currentVal));
                            field.setInt(config, sanitized);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}