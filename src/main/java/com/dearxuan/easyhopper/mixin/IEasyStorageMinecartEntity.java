package com.dearxuan.easyhopper.mixin;

import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StorageMinecartEntity.class)
interface IEasyStorageMinecartEntity {

    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();
}
