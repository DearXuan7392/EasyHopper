package com.dearxuan.easyhopper.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class I18nUtils {

    private static final Map<String, String> CURRENT_LANG_MAP = new HashMap<>();
    private static final Map<String, String> EN_US_MAP = new HashMap<>();

    static {
        refreshLanguage();
    }

    /**
     * 重新刷新加载当前语言包
     */
    public static void refreshLanguage() {
        CURRENT_LANG_MAP.clear();
        EN_US_MAP.clear();

        // 1. 正确获取当前客户端设置中选中的语言代码 (例如 "en_us", "zh_cn")
        String currentLanguage = "en_us"; // 默认 fallback 改为 en_us 标准
        try {
            if (Minecraft.getInstance() != null && Minecraft.getInstance().options != null) {
                // 👈 关键点：直接从 GameOptions 获取当前设置的语言代码
                currentLanguage = Minecraft.getInstance().options.languageCode.toLowerCase();
            }
        } catch (Throwable ignored) {
            // 环境未准备好或在服务端运行时
        }

        // 2. 加载当前语言 JSON
        loadLangFile(currentLanguage, CURRENT_LANG_MAP);

        // 3. 加载 en_us.json 作为兜底
        loadLangFile("en_us", EN_US_MAP);
    }

    private static void loadLangFile(String langCode, Map<String, String> targetMap) {
        String path = "/assets/easyhopper/lang/" + langCode + ".json";
        try (InputStream stream = I18nUtils.class.getResourceAsStream(path)) {
            if (stream != null) {
                JsonObject json = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                json.entrySet().forEach(entry -> targetMap.put(entry.getKey(), entry.getValue().getAsString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定 key 的翻译
     * 顺序：当前语言 JSON -> en_us.json -> key 原样返回
     */
    public static String getTranslation(String key) {
        if (CURRENT_LANG_MAP.isEmpty()) {
            refreshLanguage();
        }

        if (CURRENT_LANG_MAP.containsKey(key)) {
            return CURRENT_LANG_MAP.get(key);
        }
        if (EN_US_MAP.containsKey(key)) {
            return EN_US_MAP.get(key);
        }
        return key;
    }
}