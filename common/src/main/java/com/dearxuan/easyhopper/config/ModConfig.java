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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {

    public static ModConfig INSTANCE = new ModConfig();

    private static final File CONFIG_FILE = Path.of("config", "easyhopper.yaml").toFile();

    // 预加载的 en_us.json 回退缓存
    private static final Map<String, String> EN_US_FALLBACK_MAP = new HashMap<>();

    static {
        // 读取 Jar 内置的 assets/easyhopper/lang/en_us.json 作为底层兜底
        try (InputStream stream = ModConfig.class.getResourceAsStream("/assets/easyhopper/lang/en_us.json")) {
            if (stream != null) {
                JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                json.entrySet().forEach(entry -> EN_US_FALLBACK_MAP.put(entry.getKey(), entry.getValue().getAsString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EasyConfig(value = @Value(min = 1, max = 1200))
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    @EasyConfig(value = @Value(min = 1, max = 64))
    public int HOPPER_INPUT_COUNT = 1;

    @EasyConfig(value = @Value(min = 1, max = 64))
    public int HOPPER_OUTPUT_COUNT = 1;

    @EasyConfig
    public boolean HOPPER_CLASSIFICATION = false;

    @EasyConfig
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    @EasyConfig(value = @Value(min = 1, max = 1200))
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    public ModConfig() {}

    /**
     * 辅助方法：多级回退获取翻译文本
     * 顺序：当前语言（I18n） -> en_us.json -> Key 本身
     */
    private static String getTranslationWithFallback(String key) {
        // 1. 尝试直接从 Minecraft 运行时的语言系统读取 (I18n)
        try {
            if (I18n.exists(key)) {
                return I18n.get(key);
            }
        } catch (Throwable ignored) {
            // 在服务端或 I18n 未准备好的环境触发时忽略并进入下一层 fallback
        }

        // 2. 如果找不到，退回到 en_us.json 查找
        if (EN_US_FALLBACK_MAP.containsKey(key)) {
            return EN_US_FALLBACK_MAP.get(key);
        }

        // 3. 都找不到，返回 key 本身
        return key;
    }

    /**
     * 根据当前内存配置和包含 Fallback 的键值生成标准的 YAML 字符串
     */
    public String generateYamlString() {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Representer representer = new Representer(options);
            representer.addClassTag(ModConfig.class, Tag.MAP); // 隐藏 !!com.dearxuan... 类型前缀[cite: 9]

            Yaml yaml = new Yaml(new Constructor(ModConfig.class, new LoaderOptions()), representer, options);
            String rawYaml = yaml.dump(this);

            StringBuilder finalYamlWithComments = new StringBuilder();
            finalYamlWithComments.append("# EasyHopper Configuration File\n\n");

            for (String line : rawYaml.split("\n")) {
                if (line.contains(":")) {
                    String key = line.split(":")[0].trim();
                    try {
                        Field field = ModConfig.class.getDeclaredField(key);
                        if (field.isAnnotationPresent(EasyConfig.class)) {
                            EasyConfig anno = field.getAnnotation(EasyConfig.class);

                            // 1. 拼接 Key
                            String nameKey = "easyhopper." + key;
                            String tooltipKey = anno.tooltip().equals("<modid>.<name>.tooltip")
                                    ? nameKey + ".tooltip"
                                    : anno.tooltip();

                            // 2. 尝试读取配置键对应的名称与 Tooltip 说明（自动带 en_us 回退）
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

                            // 3. 补充数值范围说明
                            Value val = anno.value();
                            if (val != null) {
                                finalYamlWithComments.append("# Range: [").append((long) val.min()).append(" ~ ").append((long) val.max()).append("]\n");
                            }
                        }
                    } catch (NoSuchFieldException ignored) {}
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
     * 保存当前配置到磁盘
     */
    public void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }

            String content = generateYamlString();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取配置，并校对写入内容，如果不一致则覆写
     */
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Representer representer = new Representer(new DumperOptions());
                representer.getPropertyUtils().setSkipMissingProperties(true);

                Yaml yaml = new Yaml(new Constructor(ModConfig.class, new LoaderOptions()), representer);
                ModConfig loaded = yaml.load(reader);

                if (loaded != null) {
                    INSTANCE = loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
                INSTANCE = new ModConfig();
            }
        }

        // 规范数值合法性
        validateAndSanitize(INSTANCE);

        // 比较磁盘文件内容与预写出内容，不一致时覆盖重写
        checkAndSyncWithDisk();
    }

    private static void checkAndSyncWithDisk() {
        try {
            String expectedContent = INSTANCE.generateYamlString().trim();

            if (!CONFIG_FILE.exists()) {
                INSTANCE.save();
                return;
            }

            String actualContent = Files.readString(CONFIG_FILE.toPath()).replace("\r\n", "\n").trim();

            // 如果内容有差异（如语言缺失导致使用了 en_us 的新注释、数值超限被裁剪等），覆盖写入
            if (!expectedContent.equals(actualContent)) {
                INSTANCE.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE.save();
        }
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

    public static void syncWithCurrentLanguage() {
        // 重新检查数值和语言注释，不一致则覆写
        validateAndSanitize(INSTANCE);
        checkAndSyncWithDisk();
    }
}