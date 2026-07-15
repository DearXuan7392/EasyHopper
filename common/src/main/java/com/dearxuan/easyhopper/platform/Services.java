package com.dearxuan.easyhopper.platform;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

// 服务加载器是 Java 的内置功能, 允许我们定位在不同环境之间有所不同的接口实现.
// 在 MultiLoader 的上下文中, 我们使用此功能来访问 common 代码中的模拟 API ,
// 该 API 在运行时会被替换为平台特定的实现.
public class Services {

    // 在此示例中, 我们提供了一个平台助手, 用于提供有关模组运行在哪个平台上的信息.
    // 例如, 这可用于检查代码是运行在 NeoForge 还是 Fabric 上, 或询问模组加载器另一个模组是否已加载.
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    // 此代码用于为当前环境加载服务. 您的服务实现必须通过在 META-INF/services 目录下创建一个以服务的完全限定类名命名的文本文件来手动定义.
    // 在该文件中, 您应写入要加载的平台实现的完全限定类名.
    // 例如, 我们在 Forge 上的文件指向 ForgePlatformHelper, 而 Fabric 上的文件指向 FabricPlatformHelper.
    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
               .findFirst()
               .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
