package com.dearxuan.easyhopper.utils;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import com.dearxuan.easyhopper.config.ModConfig;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.BOTH)
public class PlayerUtil {
    public static boolean hasOpPermission(Player player) {
        return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS));
    }

    public static boolean hasPermissionToPushConfig(Player player) {
        return hasOpPermission(player) && ModConfig.INSTANCE.ALLOW_OP_MODIFY;
    }
}
