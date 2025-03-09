package com.dearxuan.easyhopper.mixin;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
interface IEasyHopperBlockEntity {

    @Accessor("inventory")
    void setInventory(DefaultedList<ItemStack> inventory);

    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();

    @Invoker("needsCooldown")
    boolean Invoke_needsCooldown();

    @Invoker("setTransferCooldown")
    void Invoke_setTransferCooldown(int transferCooldown);

    @Invoker("isFull")
    boolean Invoke_isFull();
}
