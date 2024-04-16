package com.dearxuan.easyhopper.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
interface IEasyHopperBlockEntity {

    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();

    @Invoker("needsCooldown")
    boolean Invoke_needsCooldown();

    @Invoker("setTransferCooldown")
    void Invoke_setTransferCooldown(int transferCooldown);

    @Invoker("getInvStackList")
    DefaultedList<ItemStack> Invoke_getInvStackList();

    @Invoker("isFull")
    boolean Invoke_isFull();
}
