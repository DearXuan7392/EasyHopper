package com.dearxuan.easyhopper.server;

import com.dearxuan.easyhopper.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

/**
 * NeoForge 服务端专属入口, 仅在专用服务端 (Dedicated Server) 加载.
 * 使用 @EventBusSubscriber(value = Dist.DEDICATED_SERVER) 确保客户端不会加载此类.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NeoForgeServerEntryPoint {

    @SubscribeEvent
    public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
        CommonServerEntryPoint.init();
    }
}