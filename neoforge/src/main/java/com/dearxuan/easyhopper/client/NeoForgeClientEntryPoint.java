package com.dearxuan.easyhopper.client;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.client.gui.CommonConfigGUI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * NeoForge 客户端专属入口, 仅在物理客户端加载.
 * 使用 @EventBusSubscriber(value = Dist.CLIENT) 确保服务端不会加载此类.
 */
@Environment(EnvType.CLIENT)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEntryPoint {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent(container -> {
            container.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (modContainer, parentScreen) -> CommonConfigGUI.createScreen(parentScreen)
            );
        });
    }
}