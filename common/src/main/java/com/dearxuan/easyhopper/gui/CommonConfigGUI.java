package com.dearxuan.easyhopper.gui;

import com.dearxuan.easyhopper.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CommonConfigGUI {

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
                        client.gui.toastManager(), // 优先使用源码中的 client.gui.toastManager()，若报错可改为 client.getToastManager()
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