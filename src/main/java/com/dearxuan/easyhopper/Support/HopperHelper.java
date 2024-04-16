package com.dearxuan.easyhopper.Support;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;

public class HopperHelper {

    public static boolean isSameItem(ItemStack itemStack1, ItemStack itemStack2) {
        Item item1 = itemStack1.getItem();
        Item item2 = itemStack2.getItem();
        // 不是同个类型, 直接返回false
        if (item1 != item2) {
            return false;
        }
        // 是同个类型, 继续判断

        // 都是药水, 判断药水类型是否相同
        if (item1 instanceof PotionItem) {
            return PotionUtil.getPotion(itemStack1) == PotionUtil.getPotion(itemStack2);
        }

        return true;
    }
}
