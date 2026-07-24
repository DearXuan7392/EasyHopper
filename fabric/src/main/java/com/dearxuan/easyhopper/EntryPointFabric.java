package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import net.fabricmc.api.ModInitializer;

@Environment(EnvType.BOTH)
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