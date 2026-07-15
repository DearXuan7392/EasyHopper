package com.dearxuan.easyhopper;

import net.fabricmc.api.ModInitializer;

public class EntryPointFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // 当 Fabric 模组加载器准备好加载您的模组时, 将调用此方法.
        // 您可以在此项目中访问 Fabric 和 Common 的代码.

        // 使用 Fabric 来引导 Common 模组.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
    }
}
