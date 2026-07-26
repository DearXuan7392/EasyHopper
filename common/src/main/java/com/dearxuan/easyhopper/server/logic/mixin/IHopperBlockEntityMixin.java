package com.dearxuan.easyhopper.server.logic.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = HopperBlockEntity.class, priority = 500)
interface IHopperBlockEntityMixin {
    @Accessor("items")
    NonNullList<ItemStack> accessGetItems();

    @Invoker("getItems")
    NonNullList<ItemStack> invokeGetItems();

    @Invoker("inventoryFull")
    boolean invokeInventoryFull();

    @Invoker("isOnCooldown")
    boolean invokeIsOnCooldown();

    @Invoker("setCooldown")
    void invokeSetCooldown(int transferCooldown);
}
