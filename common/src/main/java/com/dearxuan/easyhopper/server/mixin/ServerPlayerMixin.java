package com.dearxuan.easyhopper.server.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ServerPlayer.class, priority = 500)
public interface ServerPlayerMixin {

    @Accessor("server")
    MinecraftServer getServer();
}
