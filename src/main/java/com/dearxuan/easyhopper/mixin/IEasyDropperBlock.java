package com.dearxuan.easyhopper.mixin;

import net.minecraft.block.DropperBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DropperBlock.class)
public interface IEasyDropperBlock {

    @Invoker("dispense")
    void Invoke_dispense(ServerWorld world, BlockPos pos);
}
