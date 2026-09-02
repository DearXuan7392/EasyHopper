package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.server.logic.impl.IHopperBlockEntityImpl;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fabric 专属 Mixin：
 * 针对 ejectItems 方法的 Redirect 注入。
 * Fabric 版本的 ejectItems 没有 NeoForge 的 insertHook，
 * 字节码结构与原版一致，可以直接 Redirect。
 */
@Mixin(value = HopperBlockEntity.class, priority = 500)
public abstract class HopperBlockEntityFabricMixin {

    /**
     * 重定向 ejectItems 中的 getContainerSize，
     * 过滤模式下只输出前4个槽位的物品
     */
    @Redirect(
            method = "ejectItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getContainerSize()I")
    )
    private static int redirectGetContainerSize(
            HopperBlockEntity instance
    ) {
        return ((IHopperBlockEntityImpl) instance).getContainerSizeAfterClassification();
    }

    /**
     * 重定向 ejectItems 中的 ItemStack.isEmpty()，
     * 过滤模式下跳过与分类物品不匹配的物品
     */
    @Redirect(
            method = "ejectItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    )
    private static boolean redirectIsEmpty(
            ItemStack instance,
            @Local(argsOnly = true) HopperBlockEntity hopperBlockEntity
    ) {
        return instance.isEmpty() || !((IHopperBlockEntityImpl) hopperBlockEntity).canTransferItem(instance);
    }
}