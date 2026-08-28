package com.dearxuan.easyhopper.utils;

import com.dearxuan.easyhopper.server.config.ServerConfig;
import net.minecraft.world.entity.player.Player;

public class PlayerUtil {
    public static boolean hasOpPermission(Player player) {
        return player.hasPermissions(2);
    }

    public static boolean hasPermissionToPushConfig(Player player) {
        return hasOpPermission(player) && ServerConfig.INSTANCE.ALLOW_OP_MODIFY;
    }
}
