package com.dearxuan.easyhopper.server;

import com.dearxuan.easyhopper.server.config.ServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Fabric 服务端专用入口点, 仅在专用服务端加载.
 * C2S 数据包处理器已在 FabricEntryPoint 中通过 SERVER_STARTING 事件注册,
 * 此处保留入口点以备将来扩展.
 */
public class FabricServerEntryPoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        // 配置初始化已在 FabricEntryPoint 的 SERVER_STARTING 事件中处理
    }
}