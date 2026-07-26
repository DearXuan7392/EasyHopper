package com.dearxuan.easyhopper.server.logic.mixin;

import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.server.config.ServerConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecartHopper.class, priority = 500)
public abstract class MinecartHopperMixin extends AbstractMinecartContainer implements Hopper {

    @Unique
    private int easyHopperNeoForge$cooldown = 0;

    protected MinecartHopperMixin(EntityType<?> p_38213_, Level p_38214_) {
        super(p_38213_, p_38214_);
    }

    /**
     * Inject the tick method,
     * to make the hopper only transfer items once per cooldown
     *
     * @param ci the callback info
     */
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectTick(
            CallbackInfo ci
    ) {
        --this.easyHopperNeoForge$cooldown;
        if (this.easyHopperNeoForge$cooldown <= 0) {
            this.easyHopperNeoForge$cooldown = ServerConfig.INSTANCE.HOPPER_MINECART_TRANSFER_COOLDOWN;
        } else {
            ci.cancel();
        }
    }
}
