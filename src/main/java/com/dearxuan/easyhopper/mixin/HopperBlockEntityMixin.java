package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.Config.ModConfig;
import com.dearxuan.easyhopper.Config.ModMenu.ModInfo;
import com.dearxuan.easyhopper.EntryPoint.Main;
import com.dearxuan.easyhopper.Support.HopperHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    @Shadow
    private int transferCooldown;

    @Shadow
    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        return null;
    }

    @Shadow
    private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
        return false;
    }

    @Shadow
    private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
        return false;
    }

    @Shadow
    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        return null;
    }

    @Shadow
    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
        return false;
    }

    @Shadow
    private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
        return false;
    }

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = {"setTransferCooldown"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void Easy_setTransferCooldown(int cooldown, CallbackInfo info) {
        if (cooldown > 0) {
            this.transferCooldown = cooldown - 8 + ModConfig.INSTANCE.HOPPER_TRANSFER_COOLDOWN;
            info.cancel();
        }
    }

    @Inject(
            method = {"insertAndExtract"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void Easy_insertAndExtract(
            World world,
            BlockPos pos,
            BlockState state,
            HopperBlockEntity blockEntity,
            BooleanSupplier booleanSupplier,
            CallbackInfoReturnable<Boolean> info) {
        if (world.isClient) {
            info.setReturnValue(false);
            info.cancel();
            return;
        }

        IEasyHopperBlockEntity iBlockEntity = (IEasyHopperBlockEntity) blockEntity;

        if (!iBlockEntity.Invoke_needsCooldown() && state.get(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            // 输出若干个物品
            for (int i = 0; i < ModConfig.INSTANCE.HOPPER_OUTPUT_COUNT; ++i) {
                if (blockEntity.isEmpty()) {
                    break;
                } else {
                    bl |= insert(world, pos, state, blockEntity);
                }
            }
            // 输入若干个物品
            for (int i = 0; i < ModConfig.INSTANCE.HOPPER_INPUT_COUNT; ++i) {
                if (iBlockEntity.Invoke_isFull()) {
                    break;
                } else {
                    bl |= booleanSupplier.getAsBoolean();
                }
            }
            if (ModConfig.INSTANCE.HOPPER_EXTRACT_COOLDOWN && !bl) {
                iBlockEntity.Invoke_setTransferCooldown(8);
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
     * 重定向漏斗运输函数里的 size() 函数, 如果当前容器为漏斗, 且开启了分类, 则仅遍历 4 个槽位
     */
    @Redirect(
            method = "insert",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;size()I")
    )
    private static int Easy_getInventorySize(Inventory inventory) {
        // inventory 是当前漏斗
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            return 4;
        } else {
            return inventory.size();
        }
    }


    @Redirect(
            method = "insert",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;")
    )
    private static ItemStack Easy_getStack(Inventory inventory, int slot) {
        // 如果开启了漏斗分类, 且当前槽位与分类槽位不同, 则返回空槽位
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            // 分类槽
            ItemStack classificationStack = inventory.getStack(4);
            // 分类槽不为空, 且物品不相同, 则返回空, 相当于跳过该槽位
            if (!classificationStack.isEmpty() && !HopperHelper.isSameItem(inventory.getStack(4), classificationStack)) {
                return ItemStack.EMPTY;
            }
        }
        return inventory.getStack(slot);
    }

    /**
     * @param from  从哪个容器输入, 可能是漏斗或漏斗矿车
     * @param to    输出到哪个容器, 不可能是漏斗矿车
     * @param stack 待传输的物品, 在 from 里面
     * @param slot  输出到 to 的第几个物品格
     */
    @Inject(
            method = {"transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void Easy_Transfer(
            Inventory from,
            Inventory to,
            ItemStack stack,
            int slot,
            Direction side,
            CallbackInfoReturnable<ItemStack> info) {
        // from 是漏斗且开启了漏斗分类, 或者from是漏斗矿车且开启了分类
        if (from instanceof HopperBlockEntity hopperBlockEntity && ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            if (!CanHopperOrMinecartTransfer(hopperBlockEntity, stack)) {
                info.setReturnValue(stack);
                return;
            }
        }
        if (from instanceof HopperMinecartEntity hopperMinecartEntity && ModConfig.INSTANCE.HOPPER_MINECART_CLASSIFICATION) {
            if (!CanHopperOrMinecartTransfer(hopperMinecartEntity, stack)) {
                info.setReturnValue(stack);
                return;
            }
        }
        // to 是漏斗且开启了漏斗分类, 或者from是漏斗矿车且开启了分类
        if (to instanceof HopperBlockEntity hopperBlockEntity) {
            if (slot == 4 || !CanHopperOrMinecartTransfer(hopperBlockEntity, stack)) {
                info.setReturnValue(stack);
                return;
            }
        }
        if (to instanceof HopperMinecartEntity hopperMinecartEntity) {
            if (slot == 4 || !CanHopperOrMinecartTransfer(hopperMinecartEntity, stack)) {
                info.setReturnValue(stack);
            }
        }
    }

    private static boolean CanHopperOrMinecartTransfer(HopperBlockEntity hopperBlockEntity, ItemStack itemStack) {
        // 执行到这里的代码一定打开了某个分类功能
        if (ModConfig.INSTANCE.HOPPER_CLASSIFICATION) {
            ItemStack classificationItemStack = ((IEasyHopperBlockEntity) hopperBlockEntity).getInventory().get(4);
            if (!classificationItemStack.isEmpty()) {
                return HopperHelper.isSameItem(itemStack, classificationItemStack);
            }
        }
        return true;
    }

    private static boolean CanHopperOrMinecartTransfer(HopperMinecartEntity hopperMinecartEntity, ItemStack itemStack) {
        // 执行到这里的代码一定打开了某个分类功能
        if (ModConfig.INSTANCE.HOPPER_MINECART_CLASSIFICATION) {
            ItemStack classificationItemStack = ((IEasyStorageMinecartEntity) hopperMinecartEntity).getInventory().get(4);
            if (!classificationItemStack.isEmpty()) {
                return HopperHelper.isSameItem(itemStack, classificationItemStack);
            }
        }
        return true;
    }

    @Inject(
            method = {"extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void Easy_extract(
            World world,
            Hopper hopper,
            CallbackInfoReturnable<Boolean> info) {
        if (!(ModConfig.INSTANCE.HOPPER_CLASSIFICATION || ModConfig.INSTANCE.HOPPER_BETTER_EXTRACT || ModConfig.INSTANCE.HOPPER_MINECART_CLASSIFICATION)) {
            // 传输优化和漏斗或漏斗矿车分类都没开, 直接结束
            return;
        }
        Inventory inventory = null;
        double x = hopper.getHopperX(), y = hopper.getHopperY() + 1.0, z = hopper.getHopperZ();
        //  检测上方方块
        BlockPos blockPos = BlockPos.ofFloored(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider) block).getInventory(blockState, world, blockPos);
        } else if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory) blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, blockPos, true);
                }
            }
        }
        // 检测其他实体,例如矿车
        if (inventory == null) {
            List<Entity> list = world.getOtherEntities((Entity) null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES);
            if (!list.isEmpty()) {
                inventory = (Inventory) list.get(world.random.nextInt(list.size()));
            }
        }
        // 如果是容器,则不再吸取掉落物(对矿车也生效)
        if (inventory != null) {
            Direction direction = Direction.DOWN;
            if (isInventoryEmpty(inventory, direction)) {
                info.setReturnValue(false);
            } else {
                IntStream is;
                if ((ModConfig.INSTANCE.HOPPER_CLASSIFICATION && inventory instanceof HopperBlockEntity)
                        || (ModConfig.INSTANCE.HOPPER_MINECART_CLASSIFICATION && inventory instanceof HopperMinecartEntity)) {
                    is = IntStream.range(0, 4);
                } else {
                    is = getAvailableSlots(inventory, direction);
                }
                Inventory finalInventory = inventory;
                info.setReturnValue(is.anyMatch(slot -> extract(hopper, finalInventory, slot, direction)));
            }
        } else if (ModConfig.INSTANCE.HOPPER_BETTER_EXTRACT && hopper instanceof HopperBlockEntity && blockState.isFullCube(world, blockPos)) {
            // 如果开启了传输优化, 且上方是完整方块,也不再吸取掉落物(矿车不生效)
            info.setReturnValue(false);
        } else {
            for (ItemEntity itemEntity : HopperBlockEntity.getInputItemEntities(world, hopper)) {
                if (!HopperBlockEntity.extract(hopper, itemEntity)) continue;
                info.setReturnValue(true);
                return;
            }
            info.setReturnValue(false);
        }
    }

    @Inject(
            method = {"extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void extract(
            Hopper hopper,
            Inventory inventory,
            int slot,
            Direction side,
            CallbackInfoReturnable<Boolean> info) {
        ItemStack itemStack = inventory.getStack(slot);
        if (!itemStack.isEmpty() && canExtract(hopper, inventory, itemStack, slot, side)) {
            if (ModConfig.INSTANCE.HOPPER_BETTER_EXTRACT) {
                // 取出对应物品
                ItemStack tryToTransfer = itemStack.copy();
                // 将数量设置为 1
                tryToTransfer.setCount(1);
                // 尝试转移该物品,并得到剩余物品格
                ItemStack legacy = transfer(inventory, hopper, tryToTransfer, null);
                // 剩余物品为空,则原容器删除该物品
                if (legacy.isEmpty()) {
                    inventory.removeStack(slot, 1);
                    inventory.markDirty();
                    info.setReturnValue(true);
                    return;
                }
            } else {
                ItemStack itemStack2 = itemStack.copy();
                ItemStack itemStack3 = transfer(inventory, hopper, inventory.removeStack(slot, 1), null);
                if (itemStack3.isEmpty()) {
                    inventory.markDirty();
                    info.setReturnValue(true);
                    return;
                }

                inventory.setStack(slot, itemStack2);

            }
        }
        info.setReturnValue(false);
    }
}

