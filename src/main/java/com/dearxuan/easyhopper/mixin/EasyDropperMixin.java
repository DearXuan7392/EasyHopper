package com.dearxuan.easyhopper.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DropperBlock.class)
public abstract class EasyDropperMixin extends DispenserBlock {
    public EasyDropperMixin(Settings settings) {
        super(settings);
    }


}
