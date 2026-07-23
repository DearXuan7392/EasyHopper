package com.dearxuan.easyhopper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EntryPointFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // 1. 初始化通用逻辑与配置加载
        CommonClass.init();

        // 2. 注册网络 Payload 类型
        FabricNetHelper.registerPayloads();

        // 3. 注册服务端接收器
        FabricNetHelper.registerServerReceivers();

        // 4. 服务端事件: 玩家进服时推送当前服务端配置
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FabricNetHelper.syncConfigToPlayer(handler.getPlayer());
        });
    }
}