package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.SERVER)
@Mixin(value = ServerPlayer.class, priority = 500)
public interface ServerPlayerMixin {

    @Accessor("server")
    MinecraftServer getServer();
}
