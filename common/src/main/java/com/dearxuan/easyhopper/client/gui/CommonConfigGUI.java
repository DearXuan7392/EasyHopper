package com.dearxuan.easyhopper.client.gui;

import com.dearxuan.easyhopper.config.ConfigManager;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.client.net.NetManager;
import com.dearxuan.easyhopper.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CommonConfigGUI {

    /**
     * 检查当前是否处于任何世界中 (单人 / 多人)
     */
    public static boolean isInWorld() {
        return Minecraft.getInstance().level != null;
    }

    /**
     * 检查当前是否处于多人模式 (用于 UI 标题切换)
     */
    public static boolean isInMultiplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.getCurrentServer() != null;
    }

    /**
     * 获取当前环境下的配置
     * - 在游戏中 (单人/多人): 从服务器获取配置副本
     * - 未进入世界: 返回本地 ModConfig.INSTANCE
     */
    public static ModConfig getConfig() {
        if (isInWorld()) {
            return NetManager.loadConfigFromServer();
        }
        return ModConfig.INSTANCE;
    }

    /**
     * 保存指定的配置副本到当前环境
     * - 在游戏中 (单人/多人): 将指定配置推送到服务器
     * - 未进入世界: 将 ModConfig.INSTANCE 写入本地磁盘
     *
     * @param config 多人模式下要推送的配置对象
     */
    public static void saveConfig(ModConfig config) {
        if (isInWorld()) {
            NetManager.pushConfigToServer(config);
        } else {
            ConfigManager.save();
        }
    }

    /**
     * 检查当前玩家是否有权限修改配置
     * - 未进入世界: 始终有权限 (本地编辑)
     * - 游戏中: 使用服务端下发的权限值
     */
    public static boolean hasPermission() {
        if (!isInWorld()) {
            return true;
        }
        return NetManager.hasPermissionToPush();
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