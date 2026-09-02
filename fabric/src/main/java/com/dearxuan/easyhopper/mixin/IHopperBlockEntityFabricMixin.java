package com.dearxuan.easyhopper.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Fabric 专属：
 * Fabric 中 setCooldown 是 private，需要通过 @Invoker 访问
 */
@Mixin(value = HopperBlockEntity.class, priority = 500)
public interface IHopperBlockEntityFabricMixin {

    @Invoker("setCooldown")
    void invokeSetCooldown(int transferCooldown);
}