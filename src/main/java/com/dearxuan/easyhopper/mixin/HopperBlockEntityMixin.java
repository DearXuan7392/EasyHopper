package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.Config.ModConfig;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(value = HopperBlockEntity.class, priority = 500)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity implements IHopperBlockEntityMixin {

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    /**
     * 在漏斗中插入物品
     *
     * @param world       世界
     * @param pos         漏斗位置
     * @param blockEntity 漏斗实体
     * @return 是否插入成功
     * @apiNote 仅供本代码调用
     */
    @Shadow
    private static boolean insert(
            World world,
            BlockPos pos,
            HopperBlockEntity blockEntity) {
        return false;
    }

    /**
     * 修改漏斗冷却时间
     * @param cooldown 冷却时间
     * @return 修改后的冷却时间
     * @apiNote 修改原函数中 transferCooldown 的接受值, 修改冷却时间
     */
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

    @Inject(
            method = "insertAndExtract",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void injectInsertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier, CallbackInfoReturnable<Boolean> info) {
        if (world.isClient()) {
            info.setReturnValue(false);
        } else {
            IHopperBlockEntityMixin iHopperBlockEntity = (IHopperBlockEntityMixin) blockEntity;
            if (!iHopperBlockEntity.invokeNeedsCooldown() && state.get(HopperBlock.ENABLED)) {
                boolean bl = false;
                // 漏斗尝试输出物品
                for(int i=0;i<ModConfig.INSTANCE.HOPPER_OUTPUT_COUNT;++i){
                    if(blockEntity.isEmpty()){
                        break;
                    }else{
                        bl = insert(world, pos, blockEntity);
                    }
                }

                // 漏斗尝试吸取物品
                for(int i=0;i<ModConfig.INSTANCE.HOPPER_INPUT_COUNT;++i){
                    if(iHopperBlockEntity.invokeIsFull()){
                        break;
                    }else {
                        bl |= booleanSupplier.getAsBoolean();
                    }
                }

                if (bl) {
                    iHopperBlockEntity.invokeSetTransferCooldown(8);
                    markDirty(world, pos, state);
                    info.setReturnValue(true);
                    return;
                } else {
                    if(ModConfig.INSTANCE.HOPPER_EXTRACT_COOLDOWN){
                        iHopperBlockEntity.invokeSetTransferCooldown(8);
                    }
                }
            }
            info.setReturnValue(true);
        }
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
            // 其中 stack 是 from 容器中的物品
            System.out.println("1, " + from.getClass() + "; " + to.getClass());
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
    private static void injectTransfer2(
            Inventory from,
            Inventory to,
            ItemStack stack,
            int slot,
            Direction side,
            CallbackInfoReturnable<ItemStack> info) {
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            // 其中 slot 是 to 容器中的物品格序号
            if (from instanceof HopperBlockEntity hopperBlockEntity && (!canHopperTransfer(hopperBlockEntity, stack))) {
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
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION && fromInventory instanceof HopperBlockEntity) {
            if (slot == hopperInventory.size() - 1) {
                info.setReturnValue(false);
            }
        }
    }

    public boolean isEmpty(){
        DefaultedList<ItemStack> defaultedList = this.invokeGetHeldStacks();
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

    /**
     * @author DearXuan
     * @reason 修改漏斗已满的判定, 忽略分类格
     */
    @Overwrite
    private boolean isFull(){
        DefaultedList<ItemStack> defaultedList = this.invokeGetHeldStacks();
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

    @Unique
    private static boolean canHopperTransfer(HopperBlockEntity hopper, ItemStack itemStack) {
        ItemStack classificationItemStack = ((IHopperBlockEntityMixin) hopper).accessGetInventory().get(hopper.size() - 1);
        if (classificationItemStack.isEmpty()) {
            return true;
        } else {
            return itemStack.getItem() == classificationItemStack.getItem();
        }
    }

}

