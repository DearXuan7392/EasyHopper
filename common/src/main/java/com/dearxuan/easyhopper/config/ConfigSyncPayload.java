package com.dearxuan.easyhopper.config;

import com.dearxuan.easyhopper.Constants;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigSyncPayload(String jsonConfig, boolean hasPermission) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config_sync"));
    private static final Gson GSON = new Gson();

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC = CustomPacketPayload.codec(
            ConfigSyncPayload::write,
            ConfigSyncPayload::new
    );

    public ConfigSyncPayload(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean());
    }

    /**
     * C2S: 客户端推送配置到服务端, 权限由服务端判断
     */
    public ConfigSyncPayload(ModConfig config) {
        this(GSON.toJson(config), false);
    }

    /**
     * S2C: 服务端同步配置到客户端, 附带该玩家的权限信息
     */
    public ConfigSyncPayload(ModConfig config, boolean hasPermission) {
        this(GSON.toJson(config), hasPermission);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.jsonConfig);
        buf.writeBoolean(this.hasPermission);
    }

    public ModConfig toConfig() {
        try {
            return GSON.fromJson(this.jsonConfig, ModConfig.class);
        } catch (Exception e) {
            return new ModConfig();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}