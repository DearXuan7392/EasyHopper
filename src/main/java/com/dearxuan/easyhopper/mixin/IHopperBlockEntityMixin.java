package com.dearxuan.easyhopper.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
interface IHopperBlockEntityMixin {

    @Accessor("inventory")
    DefaultedList<ItemStack> accessGetInventory();

    @Invoker("getHeldStacks")
    DefaultedList<ItemStack> invokeGetHeldStacks();

    @Invoker("isFull")
    boolean invokeIsFull();

    @Invoker("needsCooldown")
    boolean invokeNeedsCooldown();

    @Invoker("setTransferCooldown")
    void invokeSetTransferCooldown(int transferCooldown);
}
