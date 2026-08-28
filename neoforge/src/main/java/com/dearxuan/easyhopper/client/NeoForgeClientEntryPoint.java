package com.dearxuan.easyhopper.client;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.client.gui.CommonConfigGUI;
import com.dearxuan.easyhopper.server.CommonServerEntryPoint;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge 客户端专属入口, 仅在物理客户端加载.
 * 使用 @EventBusSubscriber(value = Dist.CLIENT) 确保服务端不会加载此类.
 * 注册客户端 GUI.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEntryPoint {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CommonClientEntryPoint.init();

        // 监听客户端登录事件, 确保集成服务器配置已加载
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, e -> {
            // 单人世界 (集成服务器): 确保服务端配置已从文件加载
            if (Minecraft.getInstance().getCurrentServer() == null) {
                CommonServerEntryPoint.init();
            }
        });

        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent(container -> {
            container.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (modContainer, parentScreen) -> CommonConfigGUI.createScreen(parentScreen)
            );
        });
    }
}