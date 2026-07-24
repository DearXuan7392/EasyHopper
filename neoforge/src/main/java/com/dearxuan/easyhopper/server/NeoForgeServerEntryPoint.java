package com.dearxuan.easyhopper.server;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.config.ConfigSyncPayload;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * NeoForge 服务端专属入口, 仅在专用服务端 (Dedicated Server) 加载.
 * 使用 @EventBusSubscriber(value = Dist.DEDICATED_SERVER) 确保客户端不会加载此类.
 * 注册玩家进服事件.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NeoForgeServerEntryPoint {

    @SubscribeEvent
    public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
        CommonServerEntryPoint.init();

        // 注册玩家进服事件: 进服时推送当前服务端配置
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                boolean hasPermission = PlayerUtil.hasPermissionToPushConfig(player);
                PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(ServerConfig.INSTANCE, hasPermission));
            }
        });
    }
}