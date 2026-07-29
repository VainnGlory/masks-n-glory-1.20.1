package net.vainnglory.masksnglory.util;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.mixin.ProjectileOwnerAccessor;

import java.util.UUID;

public class StasisBobber {

    private static final double LEASH_RANGE_SQUARED = 1024.0;

    public static UUID ownerId(FishingBobberEntity bobber) {
        UUID stored = ((ProjectileOwnerAccessor) bobber).masksnglory$getOwnerUuid();
        if (stored != null) return stored;
        return bobber.getPlayerOwner() == null ? null : bobber.getPlayerOwner().getUuid();
    }

    public static ServerPlayerEntity onlineOwner(FishingBobberEntity bobber) {
        MinecraftServer server = bobber.getServer();
        if (server == null) return null;
        UUID id = ownerId(bobber);
        if (id == null) return null;
        return server.getPlayerManager().getPlayer(id);
    }

    public static void relink(FishingBobberEntity bobber) {
        ServerPlayerEntity owner = onlineOwner(bobber);
        if (owner == null) return;
        if (owner.fishHook == null) {
            owner.fishHook = bobber;
        }
    }

    public static boolean shouldFreeze(FishingBobberEntity bobber) {
        if (ownerId(bobber) == null) return false;

        ServerPlayerEntity owner = onlineOwner(bobber);
        if (owner == null) return true;
        if (owner.getWorld() != bobber.getWorld()) return true;
        return bobber.squaredDistanceTo(owner) > LEASH_RANGE_SQUARED;
    }
}
