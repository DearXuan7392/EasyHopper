package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.Config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public abstract class EasyHopperMixin extends LootableContainerBlockEntity {
    @Shadow
    private int transferCooldown;

    protected EasyHopperMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = {"setTransferCooldown"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void EasyCooldown_head(int cooldown, CallbackInfo info){
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
    private static void Insert(
            World world,
            BlockPos pos,
            BlockState state,
            HopperBlockEntity blockEntity,
            BooleanSupplier booleanSupplier,
            CallbackInfoReturnable<Boolean> info){
        if (world.isClient) {
            info.setReturnValue(false);
        }
        if (!((IEasyHopperEntity) blockEntity).Invoke_needsCooldown() && state.get(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            for(int i=0;i<ModConfig.INSTANCE.TRANSFER_OUTPUT_COUNT;i++){
                if (!blockEntity.isEmpty()) {
                    bl = IEasyHopperEntity.Invoke_insert(world, pos, state, blockEntity);
                }
            }
            for(int i=0;i<ModConfig.INSTANCE.TRANSFER_INPUT_COUNT;i++){
                if (!((IEasyHopperEntity) blockEntity).Invoke_isFull()) {
                    bl |= booleanSupplier.getAsBoolean();
                }
            }
            if (bl) {
                ((IEasyHopperEntity) blockEntity).Invoke_setTransferCooldown(8);
                HopperBlockEntity.markDirty(world, pos, state);
                info.setReturnValue(true);
            }
        }
        info.setReturnValue(false);
    }
}

@Mixin(HopperBlockEntity.class)
interface IEasyHopperEntity {

    @Invoker("needsCooldown")
    public boolean Invoke_needsCooldown();

    @Invoker("isFull")
    public boolean Invoke_isFull();

    @Invoker("setTransferCooldown")
    public void Invoke_setTransferCooldown(int transferCooldown);

    @Invoker("insert")
    public static boolean Invoke_insert(World world, BlockPos pos, BlockState state, Inventory inventory){
        return false;
    };
}