package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.net.INetHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge 通用主入口 (Both), 客户端与服务端都会执行.
 */
@Mod(Constants.MOD_ID)
public class EntryPointNeoForge {

    public EntryPointNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // 1. 初始化通用逻辑与配置加载
        CommonClass.init();

        // 2. 初始化网络 (注册 Payload + 接收器 + 玩家进服事件)
        INetHelper INetHelper = new NeoForgeNetHelper(modEventBus, NeoForge.EVENT_BUS);
        INetHelper.registerPackets();
        INetHelper.registerPlayerJoinEvent();
    }
}