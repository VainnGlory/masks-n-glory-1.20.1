package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class FarlandsNetherTravelMixin {

    private static final int FARLANDS_NETHER_LIMIT = 49950;

    @Redirect(method = "getTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;clamp(DDD)Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos masksnglory$clampFarlandsNetherTravel(WorldBorder border, double x, double y, double z, ServerWorld destination) {
        BlockPos clamped = border.clamp(x, y, z);
        Entity self = (Entity) (Object) this;

        if (destination.getRegistryKey() != World.OVERWORLD || self.getWorld().getRegistryKey() != World.NETHER) {
            return clamped;
        }

        int clampedX = MathHelper.clamp(clamped.getX(), -FARLANDS_NETHER_LIMIT, FARLANDS_NETHER_LIMIT);
        int clampedZ = MathHelper.clamp(clamped.getZ(), -FARLANDS_NETHER_LIMIT, FARLANDS_NETHER_LIMIT);
        if (clampedX == clamped.getX() && clampedZ == clamped.getZ()) {
            return clamped;
        }
        return new BlockPos(clampedX, clamped.getY(), clampedZ);
    }
}
