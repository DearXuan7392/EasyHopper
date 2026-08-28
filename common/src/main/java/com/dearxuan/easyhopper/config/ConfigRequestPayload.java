package com.dearxuan.easyhopper.config;

import com.dearxuan.easyhopper.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S: 客户端请求服务端同步配置和权限信息
 * 当玩家打开配置界面时发送, 服务端以 ConfigSyncPayload 响应
 */
public record ConfigRequestPayload() implements CustomPacketPayload {

    public static final Type<ConfigRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config_request"));
    public static final StreamCodec<FriendlyByteBuf, ConfigRequestPayload> CODEC = StreamCodec.unit(new ConfigRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}