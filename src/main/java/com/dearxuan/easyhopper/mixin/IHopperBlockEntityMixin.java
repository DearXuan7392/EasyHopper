package com.dearxuan.easyhopper.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
interface IHopperBlockEntityMixin {

    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();

    @Invoker("needsCooldown")
    boolean Invoke_needsCooldown();

    @Invoker("setTransferCooldown")
    void Invoke_setTransferCooldown(int transferCooldown);

    @Invoker("isFull")
    boolean Invoke_isFull();

    // -----------------------
    @Invoker("getHeldStacks")
    DefaultedList<ItemStack> invokeGetHeldStacks();
}
