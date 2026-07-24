package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.BOTH)
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
