package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.Config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

@Mixin(HopperBlockEntity.class)
public abstract class EasyHopperMixin extends LootableContainerBlockEntity {
    @Shadow
    private int transferCooldown;

    @Shadow
    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side){
        return null;
    }

    @Shadow
    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side){
        return null;
    }

    @Shadow
    private static boolean insertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier){
        return false;
    }

    @Shadow
    private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory){
        return false;
    }

    @Shadow
    private static Inventory getInputInventory(World world, Hopper hopper) {
        return null;
    }

    @Shadow
    private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state){
        return null;
    }

    @Shadow
    private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
        return false;
    }

    @Shadow
    private static boolean isInventoryFull(Inventory inventory, Direction direction){
        return false;
    }

    @Shadow
    private static IntStream getAvailableSlots(Inventory inventory, Direction side){ return null; }

    @Shadow
    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side){
        return false;
    }

    protected EasyHopperMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = {"setTransferCooldown"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void EasyCooldown(int cooldown, CallbackInfo info){
        if(cooldown > 0){
            this.transferCooldown = cooldown - 8 + ModConfig.INSTANCE.TRANSFER_COOLDOWN;
            info.cancel();
        }
    }

    @Inject(
            method = {"insertAndExtract"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void EasyInsertAndExtract(
            World world,
            BlockPos pos,
            BlockState state,
            HopperBlockEntity blockEntity,
            BooleanSupplier booleanSupplier,
            CallbackInfoReturnable<Boolean> info){
        if (world.isClient) {
            info.setReturnValue(false);
            info.cancel();
            return;
        }

        IEasyHopperBlockEntity iBlockEntity =  (IEasyHopperBlockEntity) blockEntity;

        if (!iBlockEntity.Invoke_needsCooldown() && state.get(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            // 输出一个物品
            for(int i = 0; i < ModConfig.INSTANCE.TRANSFER_OUTPUT_COUNT; ++i){
                if (!blockEntity.isEmpty()) {
                    if(ModConfig.INSTANCE.CLASSIFICATION_HOPPER && !iBlockEntity.getInventory().get(4).isEmpty()){
                        bl = EasyClassificationInsert(world, pos, state, blockEntity);
                    }else{
                        bl |= insert(world, pos, state, blockEntity);
                    }
                }
            }
            // 输入一个物品
            for(int i = 0; i < ModConfig.INSTANCE.TRANSFER_INPUT_COUNT; ++i){
                if (!iBlockEntity.Invoke_isFull()) {
                    bl |= booleanSupplier.getAsBoolean();
                }
            }
            if (bl) {
                iBlockEntity.Invoke_setTransferCooldown(8);
                HopperBlockEntity.markDirty(world, pos, state);
                info.setReturnValue(true);
                return;
            }
        }
        info.setReturnValue(false);
    }

    /**
     * 将漏斗里的物品输出到另一个容器
     * @param world 当前世界
     * @param pos 输出容器坐标
     * @param state 输出容器方块状态
     * @param inventory 漏斗
     * @return 是否输出成功
     */
    private static boolean EasyClassificationInsert(
            World world,
            BlockPos pos,
            BlockState state,
            Inventory inventory){
        // 这是被输出的容器,可能为另一个漏斗
        Inventory inventory2 = getOutputInventory(world, pos, state);
        if (inventory2 == null) {
            return false;
        }
        Direction direction = state.get(HopperBlock.FACING).getOpposite();
        if (isInventoryFull(inventory2, direction)) {
            return false;
        }
        // 遍历漏斗的前 4 格
        for (int i = 0; i < 4; ++i) {
            if (inventory.getStack(i).isEmpty()) continue;
            // 克隆漏斗的第 i 个格子
            ItemStack itemStack = inventory.getStack(i).copy();
            // 被分类物品
            ItemStack classificationItemStack = inventory.getStack(4);
            // 未分类,或第 i 个物品就是被分类物品,则继续执行
            if(classificationItemStack.isEmpty() || itemStack.getItem() == classificationItemStack.getItem()){
                ItemStack itemStack2 = HopperBlockEntity.transfer(inventory, inventory2, inventory.removeStack(i, 1), direction);
                if (itemStack2.isEmpty()) {
                    inventory2.markDirty();
                    return true;
                }
                inventory.setStack(i, itemStack);
            }
        }
        return false;
    }

    @Inject(
            method = {"transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void EasyTransfer_1(
            Inventory from,
            Inventory to,
            ItemStack stack,
            Direction side,
            CallbackInfoReturnable<ItemStack> info){
        if(ModConfig.INSTANCE.CLASSIFICATION_HOPPER){
            if(from instanceof HopperBlockEntity && !CanHopperTransfer((HopperBlockEntity) from, stack)){
                info.setReturnValue(stack);
                return;
            }
            if(to instanceof HopperBlockEntity && !CanHopperTransfer((HopperBlockEntity) to, stack)){
                info.setReturnValue(stack);
                return;
            }
        }
        if (to instanceof SidedInventory) {
            SidedInventory sidedInventory = (SidedInventory)to;
            if (side != null) {
                int[] is = sidedInventory.getAvailableSlots(side);
                int i = 0;
                while (i < is.length) {
                    if (stack.isEmpty()) {
                        info.setReturnValue(stack);
                        return;
                    }
                    stack = transfer(from, to, stack, is[i], side);
                    ++i;
                }
                info.setReturnValue(stack);
                return;
            }
        }
        int j = to.size();
        // 如果启用分类漏斗,则排除最后一格
        if(ModConfig.INSTANCE.CLASSIFICATION_HOPPER && to instanceof HopperBlockEntity){
            --j;
        }
        int i = 0;
        while (i < j) {
            if (stack.isEmpty()) {
                info.setReturnValue(stack);
                return;
            }
            stack = transfer(from, to, stack, i, side);
            ++i;
        }
        info.setReturnValue(stack);
    }

    @Inject(
            method = {"transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void EasyTransfer_2(
            Inventory from,
            Inventory to,
            ItemStack stack,
            int slot,
            Direction side,
            CallbackInfoReturnable<ItemStack> info){
        if(ModConfig.INSTANCE.CLASSIFICATION_HOPPER){
            if(from instanceof HopperBlockEntity && !CanHopperTransfer((HopperBlockEntity) from, stack)){
                info.setReturnValue(stack);
                return;
            }
            if(to instanceof HopperBlockEntity && !CanHopperTransfer((HopperBlockEntity) to, stack)){
                info.setReturnValue(stack);
                return;
            }
        }
    }

    private static boolean CanHopperTransfer(HopperBlockEntity hopper, ItemStack itemStack){
        ItemStack classificationItemStack = ((IEasyHopperBlockEntity)hopper).getInventory().get(4);
        if(classificationItemStack.isEmpty()){
            return true;
        }else{
            return itemStack.getItem() == classificationItemStack.getItem();
        }
    }

    @Inject(
            method = {"extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void EasyExtract(
            World world,
            Hopper hopper,
            CallbackInfoReturnable<Boolean> info){
        Inventory inventory = getInputInventory(world, hopper);
        if (inventory != null) {
            Direction direction = Direction.DOWN;
            if (isInventoryEmpty(inventory, direction)) {
                info.setReturnValue(false);
                return;
            }else{
                IntStream is;
                if(ModConfig.INSTANCE.CLASSIFICATION_HOPPER && inventory instanceof HopperBlockEntity){
                    is = IntStream.range(0, 4);
                }else{
                    is = getAvailableSlots(inventory, direction);
                }
                info.setReturnValue(is.anyMatch(slot -> extract(hopper, inventory, slot, direction)));
                return;
            }
        }
        for (ItemEntity itemEntity : HopperBlockEntity.getInputItemEntities(world, hopper)) {
            if (!HopperBlockEntity.extract(hopper, itemEntity)) continue;
            info.setReturnValue(true);
            return;
        }
        info.setReturnValue(false);
    }
}

