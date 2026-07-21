package com.dearxuan.easyhopper;

import net.fabricmc.api.ModInitializer;

public class EntryPointFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        CommonClass.init();
    }
}
