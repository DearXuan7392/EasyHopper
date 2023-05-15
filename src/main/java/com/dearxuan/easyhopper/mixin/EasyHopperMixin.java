package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public abstract class EasyHopperMixin extends LootableContainerBlockEntity {
    @Shadow private int transferCooldown;

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
            this.transferCooldown = cooldown - ModConfig.INSTANCE.TransferSpeedUp;
            info.cancel();
        }
    }
}
