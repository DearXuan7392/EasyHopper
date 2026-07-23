package com.dearxuan.easyhopper.net;

import com.dearxuan.easyhopper.Constants;
import com.dearxuan.easyhopper.config.ModConfig;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(String jsonConfig) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config_sync"));
    private static final Gson GSON = new Gson();

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC = CustomPacketPayload.codec(
            ConfigSyncPayload::write,
            ConfigSyncPayload::new
    );

    public ConfigSyncPayload(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public ConfigSyncPayload(ModConfig config) {
        this(GSON.toJson(config));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.jsonConfig);
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