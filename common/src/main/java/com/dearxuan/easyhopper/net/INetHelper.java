package com.dearxuan.easyhopper.net;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import net.minecraft.server.level.ServerPlayer;

/**
 * 网络辅助接口, 定义与模组加载端无关的网络操作
 * 由 Fabric 和 NeoForge 分别实现
 */
@Environment(EnvType.BOTH)
public interface INetHelper {

    /**
     * 注册 Payload 类型及处理器 (C2S + S2C)
     */
    void registerPackets();

    /**
     * 注册玩家进服事件, 进服时推送当前服务端配置
     */
    void registerPlayerJoinEvent();

    /**
     * 向指定玩家发送配置同步数据包
     */
    void sendToPlayer(ServerPlayer player, ConfigSyncPayload payload);
}