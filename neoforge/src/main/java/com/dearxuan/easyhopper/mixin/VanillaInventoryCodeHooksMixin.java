package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.server.logic.impl.IHopperBlockEntityImpl;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
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

    /**
     * 限制输出槽位数量：过滤模式下只遍历前4个槽位（跳过第5格分类槽）
     */
    @Redirect(
            method = "lambda$extractHook$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/Hopper;getContainerSize()I"
            )
    )
    private static int redirectGetContainerSize(Hopper instance) {
        return ((IHopperBlockEntityImpl) instance).getContainerSizeAfterClassification();
    }

    /**
     * 过滤物品类型：跳过与分类物品不匹配的槽位
     * 原始逻辑：if (stack.isEmpty()) continue;
     * 修改后：  if (stack.isEmpty() || !canTransferItem(stack)) continue;
     */
    @Redirect(
            method = "lambda$extractHook$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 0  // 只匹配循环中的第一次 isEmpty 调用
            )
    )
    private static boolean redirectIsEmpty(
            ItemStack instance,
            @Local(argsOnly = true) Hopper hopper
    ) {
        return instance.isEmpty() || !((IHopperBlockEntityImpl) hopper).canTransferItem(instance);
    }
}