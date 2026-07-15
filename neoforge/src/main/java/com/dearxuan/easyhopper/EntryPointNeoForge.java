package com.dearxuan.easyhopper;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class EntryPointNeoForge {

    public EntryPointNeoForge(IEventBus eventBus) {

        // 当 NeoForge 模组加载器准备好加载您的模组时, 将调用此方法.
        // 您可以在此项目中访问 NeoForge 和 Common 的代码.

        // 使用 NeoForge 来引导 Common 模组.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

    }
}
