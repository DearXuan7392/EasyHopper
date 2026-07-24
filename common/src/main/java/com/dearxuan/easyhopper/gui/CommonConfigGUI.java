package com.dearxuan.easyhopper.gui;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.net.NetManager;
import com.dearxuan.easyhopper.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CommonConfigGUI {

    /**
     * 获取当前环境下的配置
     * - 多人模式: 从服务器获取配置副本
     * - 单人模式: 返回本地 ModConfig.INSTANCE
     */
    public static ModConfig getConfig() {
        if (isInMultiplayer()) {
            return NetManager.loadConfigFromServer();
        }
        return ModConfig.INSTANCE;
    }

    /**
     * 保存配置到当前环境
     * - 多人模式: 将指定配置推送到服务器
     * - 单人模式: 将 ModConfig.INSTANCE 写入本地磁盘
     */
    public static void saveConfig() {
        if (isInMultiplayer()) {
            NetManager.pushConfigToServer(ModConfig.INSTANCE);
        } else {
            ConfigManager.save();
        }
    }

    /**
     * 保存指定的配置副本到当前环境
     * - 多人模式: 将指定配置推送到服务器
     * - 单人模式: 将 ModConfig.INSTANCE 写入本地磁盘
     *
     * @param config 多人模式下要推送的配置对象
     */
    public static void saveConfig(ModConfig config) {
        if (isInMultiplayer()) {
            NetManager.pushConfigToServer(config);
        } else {
            ConfigManager.save();
        }
    }

    /**
     * 检查当前是否处于多人模式
     */
    public static boolean isInMultiplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.getCurrentServer() != null;
    }

    /**
     * 检查当前玩家是否有权限修改配置
     * - 单人模式: 始终有权限
     * - 多人模式: 需要管理员权限
     */
    public static boolean hasPermission() {
        return !isInMultiplayer() || NetManager.hasPermissionToPush();
    }

    /**
     * 自动检测平台加载的 Mod，按优先级 [Cloth Config -> YACL] 返回对应的界面 Screen
     */
    public static Screen createScreen(Screen parentScreen) {
        // 优先检查 Cloth Config
        if (Services.PLATFORM.isModLoaded("cloth-config") || Services.PLATFORM.isModLoaded("cloth_config")) {
            try {
                return ClothConfigGUI.createScreen(parentScreen);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // 备选检查 YetAnotherConfigLib (YACL)
        if (Services.PLATFORM.isModLoaded("yet_another_config_lib_v3") || Services.PLATFORM.isModLoaded("yacl")) {
            try {
                return YaclConfigGUI.createScreen(parentScreen);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // 两个配置库均未安装时，在右上角弹出原生 Toast 提示
        showMissingConfigToast();

        // 返回 null，Mod Menu / ModList 会处理退回到上一级或禁用配置按钮
        return null;
    }

    /**
     * 在游戏右上角弹出原生 SystemToast 通知
     */
    private static void showMissingConfigToast() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                // 创建原生系统通知 toast
                SystemToast.addOrUpdate(
                        client.gui.toastManager(),
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