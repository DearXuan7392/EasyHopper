package com.dearxuan.easyhopper;


import com.dearxuan.easyhopper.gui.CommonConfigGUI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class EntryPointNeoForge {

    public EntryPointNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parentScreen) -> CommonConfigGUI.createScreen(parentScreen)
        );

        // 1. 初始化通用逻辑与配置加载
        CommonClass.init();

        // 2. 注册网络 Payload 类型和处理器
        NeoForgeNetHelper.register(modEventBus);

        // 3. 注册玩家进服事件 (在 NeoForge 事件总线上)
        NeoForgeNetHelper.registerPlayerJoinEvent(NeoForge.EVENT_BUS);
    }
}