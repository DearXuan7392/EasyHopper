package com.dearxuan.easyhopper.platform;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.BOTH)
public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {return "Fabric";}

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
