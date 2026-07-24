package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

// 此类属于 common 项目, 意味着它在所有支持的加载器之间共享. 此处编写的代码只能导入和访问原版代码库、原版使用的库,
// 以及可选地提供通用兼容二进制文件的第三方库. 这意味着 common 代码不能直接使用加载器特定的概念 (如 NeoForge 事件),
// 但它将与所有支持的模组加载器兼容.
@Environment(EnvType.BOTH)
public class CommonClass {

    // 加载器特定的项目能够导入和使用 common 项目中的任何代码. 这使得您可以将大部分代码写在这里,
    // 并从加载器特定的项目中加载它. 此示例包含一些由加载器特定项目的入口点调用的代码.
    public static void init() {
        ModConfig.load();
    }
}
