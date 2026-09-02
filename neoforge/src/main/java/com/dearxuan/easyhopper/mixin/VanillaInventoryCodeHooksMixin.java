package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.logic.impl.IHopperBlockEntityImpl;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.items.VanillaInventoryCodeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * NeoForge 专属：修改 VanillaInventoryCodeHooks.insertHook，
 * 使漏斗在过滤模式下通过 IItemHandler 输出物品时同样遵守过滤规则。
 *
 * 对应 Fabric 端在 ejectItems 上的两个 @Redirect：
 *   1. getContainerSize → getContainerSizeAfterClassification
 *   2. isEmpty → isEmpty || !canTransferItem
 */
@Mixin(value = VanillaInventoryCodeHooks.class, priority = 500)
public abstract class VanillaInventoryCodeHooksMixin {

    @Redirect(
            method = "lambda$insertHook$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getContainerSize()I")
    )
    private static int redirectGetContainerSize(
            HopperBlockEntity instance
    ) {
        if (ServerConfig.INSTANCE.HOPPER_FILTERING) {
            return instance.getContainerSize() - 1;
        } else {
            return instance.getContainerSize();
        }
    }

    /**
     * Redirect the isEmpty method,
     * if this item differs from the classified item,
     * it is treated as empty to avoid being transferred
     *
     * @param instance the item stack
     * @return true if the item stack is empty or differs from the classified item
     */
    @Redirect(
            method = "lambda$insertHook$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    )
    private static boolean isEmpty(
            ItemStack instance,
            @Local(argsOnly = true) HopperBlockEntity hopperBlockEntity
    ) {
        return instance.isEmpty() || !((IHopperBlockEntityImpl) hopperBlockEntity).canTransferItem(instance);
    }
}