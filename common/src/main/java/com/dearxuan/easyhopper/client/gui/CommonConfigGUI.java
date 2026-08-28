package com.dearxuan.easyhopper.client.gui;

import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CommonConfigGUI {

    public static boolean isInWorld() {
        return Minecraft.getInstance().level != null;
    }

    public static boolean isInMultiplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.getCurrentServer() != null;
    }

    public static ModConfig getConfig() {
        if (isInWorld()) {
            return NetManager.loadConfigFromServer();
        }
        return ConfigManager.load();
    }

    public static void saveConfig(ModConfig config) {
        if (isInWorld()) {
            if (isInMultiplayer()) {
                NetManager.pushConfigToServer(config);
            } else {
                ConfigManager.save(config);
                NetManager.updateServerConfig(config, true);
            }
        } else {
            ConfigManager.save(config);
        }
    }

    public static boolean hasPermission() {
        if (!isInWorld()) {
            return true;
        }
        if (!isInMultiplayer()) {
            return true;
        }
        return NetManager.hasPermissionToPush();
    }

    /**
     * 打开配置界面入口
     */
    public static Screen createScreen(Screen parentScreen) {
        // 如果在世界里，立刻发起网络同步请求，但同时直接创建 UI 界面展现给玩家
        if (isInWorld()) {
            NetManager.setSyncCompleted(false);
            NetManager.requestConfigSync();
        } else {
            NetManager.setSyncCompleted(true);
        }

        return createRealScreen(parentScreen);
    }

    public static Screen createRealScreen(Screen parentScreen) {
        if (Services.PLATFORM.isModLoaded("cloth-config") || Services.PLATFORM.isModLoaded("cloth_config")) {
            try {
                return ClothConfigGUI.createScreen(parentScreen);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (Services.PLATFORM.isModLoaded("yet_another_config_lib_v3") || Services.PLATFORM.isModLoaded("yacl")) {
            try {
                return YaclConfigGUI.createScreen(parentScreen);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        showMissingConfigToast();
        return null;
    }

    private static void showMissingConfigToast() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                SystemToast.addOrUpdate(
                        client.getToasts(),
                        SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                        Component.translatable("easyhopper.toast.title"),
                        Component.translatable("easyhopper.toast.description")
                );
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}