package com.dearxuan.easyhopper.server.mixin;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.server.impl.IHopperBlockEntityImpl;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Environment(EnvType.BOTH)
@Mixin(value = HopperBlockEntity.class, priority = 500)
@Implements(@Interface(iface = IHopperBlockEntityImpl.class, prefix = "impl$"))
public abstract class HopperBlockEntityMixin extends RandomizableContainerBlockEntity implements Hopper, IHopperBlockEntityImpl {

    @Shadow
    private int cooldownTime;
    @Shadow
    private Direction facing;
    @Shadow
    private NonNullList<ItemStack> items;

    protected HopperBlockEntityMixin(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.HOPPER, worldPosition, blockState);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.cooldownTime = -1;
        this.facing = (Direction)blockState.getValue(HopperBlock.FACING);
    }

    @Shadow
    private static boolean ejectItems(Level level, BlockPos pos, HopperBlockEntity blockEntity) {
        return false;
    }

    /**
     * inject to the setCooldown method,
     * modify the cooldown time of the transfer
     *
     * @param value the cooldown time to set
     * @return the cooldown time after modification
     */
    @ModifyVariable(
            method = "setCooldown",
            at = @At(value = "HEAD", ordinal = 0),
            argsOnly = true
    )
    private int injectTryMoveItems(int value) {
        if (value > 0) {
            return value - HopperBlockEntity.MOVE_ITEM_SPEED + ModConfig.INSTANCE.HOPPER_TRANSFER_COOLDOWN;
        } else {
            return 0;
        }
    }

    /**
     * inject to the tryMoveItems method,
     * to transfer more items at once
     *
     * @param level       the world
     * @param pos         the position of the hopper
     * @param state       the state of the hopper
     * @param entity the hopper block entity
     * @param action   the validator to check if the transfer is valid
     * @param cir         the callback info returnable
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
        } else {
            IHopperBlockEntityMixin iHopperBlockEntity = (IHopperBlockEntityMixin) entity;
            if (!iHopperBlockEntity.invokeIsOnCooldown() && (Boolean)state.getValue(HopperBlock.ENABLED)) {
                boolean changed = false;
                int inputTimes = 0, outputTimes = 0;
                while (outputTimes++ < ModConfig.INSTANCE.HOPPER_OUTPUT_COUNT && !entity.isEmpty()) {
                    changed |= ejectItems(level, pos, entity);
                }

                while (inputTimes++ < ModConfig.INSTANCE.HOPPER_INPUT_COUNT && !iHopperBlockEntity.invokeInventoryFull()) {
                    changed |= action.getAsBoolean();
                }

                if (changed || ModConfig.INSTANCE.HOPPER_EXTRACT_COOLDOWN) {
                    iHopperBlockEntity.invokeSetCooldown(8);
                    setChanged(level, pos, state);
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
            return;
        }
    }

    @Redirect(
            method = "tryTakeInItemFromSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack redirectGetItemFromSlot(
            Container instance,
            int slot
    ) {
        if (ModConfig.INSTANCE.HOPPER_FILTERING && instance instanceof HopperBlockEntity && slot == 4) {
            return ItemStack.EMPTY;
        } else {
            return instance.getItem(slot);
        }
    }

    /**
     * Redirect the getContainerSize method when try to output items,
     * to make the hopper only eject first 4 items
     *
     * @param instance the hopper block entity
     * @return the container size minus 1 if the hopper is classified else the container size
     */
    @Redirect(
            method = "ejectItems",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getContainerSize()I")
    )
    private static int redirectGetContainerSize1(
            HopperBlockEntity instance
    ) {
        return ((IHopperBlockEntityImpl) instance).getContainerSizeAfterClassification();
    }

    /**
     * Redirect the getContainerSize method when try to input items,
     * to make the hopper only accept first 4 items
     *
     * @param instance the hopper block entity
     * @return the container size minus 1 if the hopper is classified else the container size
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
     * Redirect the isEmpty method,
     * if this item differs from the classified item,
     * it is treated as empty to avoid being transferred
     *
     * @param instance the item stack
     * @return true if the item stack is empty or differs from the classified item
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
     * Check if the item can be placed in the slot
     *
     * @param slot  the slot to check
     * @param stack the item stack to check
     * @return `true` if the item can be placed in the slot else `false`
     */
    public boolean canPlaceItem(
            int slot,
            @NotNull ItemStack stack
    ) {
        if (ModConfig.INSTANCE.HOPPER_FILTERING) {
            int lastSlot = this.getContainerSize() - 1;
            if (slot == lastSlot) {
                return false;
            }

            return this.canTransferItem(stack);
        }
        return true;
    }

    /**
     * Rewrite isEmpty() in RandomizableContainerBlockEntity
     *
     * @return `true` if the hopper is empty else `false`
     */
    public boolean isEmpty() {
        this.unpackLootTable(null);
        int maxSlot = this.getContainerSize();
        if (ModConfig.INSTANCE.HOPPER_FILTERING) {
            --maxSlot;
        }

        for (int i = 0; i < maxSlot; ++i) {
            if (!this.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Inject the inventoryFull method,
     * to make the hopper only accept first 4 items
     *
     * @param cir the callback info returnable
     */
    @Inject(
            method = "inventoryFull",
            at = @At("HEAD"),
            cancellable = true
    )
    public void injectInventoryFull(CallbackInfoReturnable<Boolean> cir) {
        int maxSlot = this.getContainerSize();
        if (ModConfig.INSTANCE.HOPPER_FILTERING) {
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

    @NotNull
    public ItemStack impl$getClassifiedItemStack() {
        if (ModConfig.INSTANCE.HOPPER_FILTERING) {
            return this.getItem(this.getContainerSize() - 1);
        }
        return ItemStack.EMPTY;
    }

    public boolean impl$canTransferItem(ItemStack itemStack) {
        ItemStack classifiedItemStack = this.getClassifiedItemStack();
        return classifiedItemStack.isEmpty() || ItemStack.isSameItem(classifiedItemStack, itemStack);
    }

    public int impl$getContainerSizeAfterClassification() {
        if (ModConfig.INSTANCE.HOPPER_FILTERING) {
            return this.getContainerSize() - 1;
        }
        return this.getContainerSize();
    }
}
