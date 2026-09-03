package com.dearxuan.easyhopper.server.logic.mixin;

import com.dearxuan.easyhopper.server.config.ServerConfig;
import com.dearxuan.easyhopper.server.logic.impl.IHopperBlockEntityImpl;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(value = HopperBlockEntity.class, priority = 500)
@Implements(@Interface(iface = IHopperBlockEntityImpl.class, prefix = "impl$"))
public abstract class HopperBlockEntityMixin extends RandomizableContainerBlockEntity implements Hopper, IHopperBlockEntityImpl {

    @Shadow
    private int cooldownTime;

    @Shadow
    private Direction facing;

    @Shadow
    private NonNullList<ItemStack> items;

    @Shadow
    private static boolean ejectItems(Level level, BlockPos pos, HopperBlockEntity blockEntity) {
        return false;
    }

    protected HopperBlockEntityMixin(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityType.HOPPER, worldPosition, blockState);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.cooldownTime = -1;
        this.facing = (Direction) blockState.getValue(HopperBlock.FACING);
    }

    // ==================== 两平台通用的注入 ====================

    /**
     * 修改 setCooldown 的冷却时间
     */
    @ModifyVariable(
            method = "setCooldown",
            at = @At(value = "HEAD", ordinal = 0),
            argsOnly = true
    )
    private int injectSetCooldown(int value) {
        if (value > 0) {
            return value - HopperBlockEntity.MOVE_ITEM_SPEED + ServerConfig.INSTANCE.HOPPER_TRANSFER_COOLDOWN;
        } else {
            return 0;
        }
    }

    /**
     * 替换 tryMoveItems 逻辑，支持一次传输多个物品
     */
    @Inject(
            method = "tryMoveItems",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void injectTryMoveItems(
            Level level,
            BlockPos pos,
            BlockState state,
            HopperBlockEntity entity,
            BooleanSupplier action,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (level.isClientSide()) {
            cir.setReturnValue(false);
            return;
        }
        IHopperBlockEntityMixin iHopperBlockEntity = (IHopperBlockEntityMixin) entity;
        if (!iHopperBlockEntity.invokeIsOnCooldown() && (Boolean) state.getValue(HopperBlock.ENABLED)) {
            boolean changed = false;
            int inputTimes = 0, outputTimes = 0;
            while (outputTimes++ < ServerConfig.INSTANCE.HOPPER_OUTPUT_COUNT && !entity.isEmpty()) {
                changed |= ejectItems(level, pos, entity);
            }
            while (inputTimes++ < ServerConfig.INSTANCE.HOPPER_INPUT_COUNT && !iHopperBlockEntity.invokeInventoryFull()) {
                changed |= action.getAsBoolean();
            }
            if (changed || ServerConfig.INSTANCE.HOPPER_EXTRACT_COOLDOWN) {
                iHopperBlockEntity.invokeSetCooldown(8);
                setChanged(level, pos, state);
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }

    /**
     * 重定向 tryTakeInItemFromSlot 中的 getItem，
     * 过滤模式下跳过最后一个槽位
     */
    @Redirect(
            method = "tryTakeInItemFromSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack redirectGetItemFromSlot(
            Container instance,
            int slot
    ) {
        if (ServerConfig.INSTANCE.HOPPER_FILTERING && instance instanceof HopperBlockEntity && slot == 4) {
            return ItemStack.EMPTY;
        } else {
            return instance.getItem(slot);
        }
    }

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

    /**
     * 重定向 addItem 中的 getContainerSize，
     * 过滤模式下限制输入槽位数量
     */
    @Redirect(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I")
    )
    private static int redirectGetContainerSize2(
            Container instance
    ) {
        if (instance instanceof HopperBlockEntity) {
            return ((IHopperBlockEntityImpl) instance).getContainerSizeAfterClassification();
        } else {
            return instance.getContainerSize();
        }
    }

    /**
     * 重写 inventoryFull，过滤模式下只检查前4个槽位
     */
    @Inject(
            method = "inventoryFull",
            at = @At("HEAD"),
            cancellable = true
    )
    public void injectInventoryFull(CallbackInfoReturnable<Boolean> cir) {
        int maxSlot = this.getContainerSize();
        if (ServerConfig.INSTANCE.HOPPER_FILTERING) {
            --maxSlot;
        }
        for (int i = 0; i < maxSlot; ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxStackSize()) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }

    // ==================== 方法覆写（非注入，两平台通用） ====================

    /**
     * 过滤模式下限制物品放置
     */
    public boolean canPlaceItem(
            int slot,
            @NotNull ItemStack stack
    ) {
        if (ServerConfig.INSTANCE.HOPPER_FILTERING) {
            int lastSlot = this.getContainerSize() - 1;
            if (slot == lastSlot) {
                return false;
            }
            return this.canTransferItem(stack);
        }
        return true;
    }

    /**
     * 重写 isEmpty，过滤模式下忽略最后一个槽位
     */
    public boolean isEmpty() {
        this.unpackLootTable(null);
        int maxSlot = this.getContainerSize();
        if (ServerConfig.INSTANCE.HOPPER_FILTERING) {
            --maxSlot;
        }
        for (int i = 0; i < maxSlot; ++i) {
            if (!this.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // ==================== IHopperBlockEntityImpl 实现 ====================

    @NotNull
    public ItemStack impl$getClassifiedItemStack() {
        if (ServerConfig.INSTANCE.HOPPER_FILTERING) {
            return this.getItem(this.getContainerSize() - 1);
        }
        return ItemStack.EMPTY;
    }

    public boolean impl$canTransferItem(ItemStack itemStack) {
        ItemStack classifiedItemStack = this.getClassifiedItemStack();
        return classifiedItemStack.isEmpty() || ItemStack.isSameItem(classifiedItemStack, itemStack);
    }

    public int impl$getContainerSizeAfterClassification() {
        if (ServerConfig.INSTANCE.HOPPER_FILTERING) {
            return this.getContainerSize() - 1;
        }
        return this.getContainerSize();
    }
}