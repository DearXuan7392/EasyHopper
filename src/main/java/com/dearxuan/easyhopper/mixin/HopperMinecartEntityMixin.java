package com.dearxuan.easyhopper.mixin;

import com.dearxuan.easyhopper.Config.ModConfig;
import net.minecraft.block.entity.Hopper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartEntityMixin extends StorageMinecartEntity implements Hopper {

    @Shadow
    public abstract boolean isEnabled();

    @Shadow
    public abstract boolean canOperate();

    private int cooldown = 0;

    protected HopperMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        super.tick();
        this.cooldown--;
        if (this.cooldown <= 0) {
            if (!this.getWorld().isClient && this.isAlive() && this.isEnabled() && this.canOperate()) {
                this.cooldown = ModConfig.INSTANCE.HOPPER_MINECART_TRANSFER_COOLDOWN;
                this.markDirty();
                return;
            }
            if (ModConfig.INSTANCE.HOPPER_MINECART_BETTER_EXTRACT) {
                this.cooldown = ModConfig.INSTANCE.HOPPER_MINECART_TRANSFER_COOLDOWN;
            }
        }

    }
}
