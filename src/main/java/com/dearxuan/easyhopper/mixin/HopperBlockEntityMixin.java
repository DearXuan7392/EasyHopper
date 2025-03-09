package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.Config.ModConfig;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity implements IHopperBlockEntityMixin {

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow
    private static boolean insert(World world, BlockPos pos, HopperBlockEntity blockEntity) {
        return false;
    }

    @ModifyVariable(
            method = "setTransferCooldown",
            at = @At(value = "HEAD", ordinal = 0),
            argsOnly = true)
    private int modifySetTransferCooldown(int cooldown) {
        if (cooldown > 0) {
            return cooldown - HopperBlockEntity.TRANSFER_COOLDOWN + ModConfig.INSTANCE.HOPPER_TRANSFER_COOLDOWN;
        } else {
            return 0;
        }
    }

    @Redirect(
            method = "insertAndExtract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;isEmpty()Z")
    )
    private static boolean redirectBlockEntityIsEmpty(HopperBlockEntity instance) {
        return false;
    }

    @Redirect(
            method = "insertAndExtract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;isFull()Z")
    )
    private static boolean redierctIsFull(HopperBlockEntity instance) {
        return false;
    }

    @Redirect(
            method = "insertAndExtract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;insert(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/HopperBlockEntity;)Z")
    )
    private static boolean redirectInsert(World world, BlockPos pos, HopperBlockEntity blockEntity) {
        boolean bl = false;
        for (int i = 0; i < ModConfig.INSTANCE.HOPPER_OUTPUT_COUNT; ++i) {
            if (!isHopperEmpty(blockEntity)) {
                bl |= insert(world, pos, blockEntity);
            }
        }
        return bl;
    }

    @Redirect(
            method = "insertAndExtract",
            at = @At(value = "INVOKE", target = "Ljava/util/function/BooleanSupplier;getAsBoolean()Z")
    )
    private static boolean redirectGetAsBoolean(BooleanSupplier instance, @Local(argsOnly = true) HopperBlockEntity hopperBlockEntity) {
        boolean bl = false;
        for (int i = 0; i < ModConfig.INSTANCE.HOPPER_INPUT_COUNT; ++i) {
            if (!isHopperFull(hopperBlockEntity)) {
                bl |= instance.getAsBoolean();
            }
        }
        return bl;
    }

    @Redirect(
            method = "insert",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;size()I")
    )
    private static int redirectSize(HopperBlockEntity blockEntity) {
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            return blockEntity.size() - 1;
        } else {
            return blockEntity.size();
        }
    }

    @Inject(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void injectTransfer1(
            Inventory from,
            Inventory to,
            ItemStack stack,
            Direction side,
            CallbackInfoReturnable<ItemStack> info) {
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            if (from instanceof HopperBlockEntity hopperBlockEntity && !canHopperTransfer(hopperBlockEntity, stack)) {
                info.setReturnValue(stack);
            } else if (to instanceof HopperBlockEntity hopperBlockEntity && !canHopperTransfer(hopperBlockEntity, stack)) {
                info.setReturnValue(stack);
            }
        }
    }

    @Inject(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void injectTransfer2(Inventory from, Inventory to, ItemStack stack, int slot, Direction side, CallbackInfoReturnable<ItemStack> info) {
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            if (from instanceof HopperBlockEntity hopperBlockEntity && (!canHopperTransfer(hopperBlockEntity, stack) || slot == from.size() - 1)) {
                info.setReturnValue(stack);
            } else if (to instanceof HopperBlockEntity hopperBlockEntity && (!canHopperTransfer(hopperBlockEntity, stack) || slot == to.size() - 1)) {
                info.setReturnValue(stack);
            }
        }
    }

    @Redirect(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;size()I")
    )
    private static int redirectSize(Inventory instance) {
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION && instance instanceof HopperBlockEntity) {
            return instance.size() - 1;
        } else {
            return instance.size();
        }
    }

    @Inject(
            method = "canExtract",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void injectCanExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> info) {
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION && fromInventory instanceof HopperBlockEntity hopperBlockEntity) {
            if (slot == hopperInventory.size() - 1) {
                info.setReturnValue(false);
            }
        }
    }

    private static boolean isHopperFull(HopperBlockEntity hopperBlockEntity) {
        IHopperBlockEntityMixin iHopperBlockEntity = (IHopperBlockEntityMixin) hopperBlockEntity;
        DefaultedList<ItemStack> defaultedList = iHopperBlockEntity.invokeGetHeldStacks();
        int maxSlot = defaultedList.size();
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            --maxSlot;
        }
        for (int i = 0; i < maxSlot; ++i) {
            ItemStack itemStack = defaultedList.get(i);
            if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxCount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHopperEmpty(HopperBlockEntity hopperBlockEntity) {
        IHopperBlockEntityMixin iHopperBlockEntity = (IHopperBlockEntityMixin) hopperBlockEntity;
        DefaultedList<ItemStack> defaultedList = iHopperBlockEntity.invokeGetHeldStacks();
        int maxSlot = defaultedList.size();
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            --maxSlot;
        }
        for (int i = 0; i < maxSlot; ++i) {
            if (!defaultedList.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean canHopperTransfer(HopperBlockEntity hopper, ItemStack itemStack) {
        ItemStack classificationItemStack = ((IHopperBlockEntityMixin) hopper).getInventory().get(hopper.size() - 1);
        if (classificationItemStack.isEmpty()) {
            return true;
        } else {
            return itemStack.getItem() == classificationItemStack.getItem();
        }
    }

}

