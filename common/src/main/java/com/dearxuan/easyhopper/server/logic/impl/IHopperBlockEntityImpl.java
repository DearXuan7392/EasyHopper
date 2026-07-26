package com.dearxuan.easyhopper.server.logic.impl;

import net.minecraft.world.item.ItemStack;

public interface IHopperBlockEntityImpl {

    ItemStack getClassifiedItemStack();

    boolean canTransferItem(ItemStack itemStack);

    int getContainerSizeAfterClassification();
}
