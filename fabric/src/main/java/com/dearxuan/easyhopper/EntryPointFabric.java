package com.dearxuan.easyhopper;

import net.fabricmc.api.ModInitializer;

public class EntryPointFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // 1. 初始化通用逻辑与配置加载
        CommonClass.init();

        // 2. 初始化网络 (注册 Payload + 接收器 + 玩家进服事件)
        FabricNetHelper netHelper = new FabricNetHelper();
        netHelper.registerPackets();
        netHelper.registerPlayerJoinEvent();
    }
}