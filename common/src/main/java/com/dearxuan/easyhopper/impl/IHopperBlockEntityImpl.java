package com.dearxuan.easyhopper.impl;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.BOTH)
public interface IHopperBlockEntityImpl {

    ItemStack getClassifiedItemStack();

    boolean canTransferItem(ItemStack itemStack);

    int getContainerSizeAfterClassification();
}
