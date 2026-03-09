package net.vainnglory.masksnglory.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoneKnifeParryManager {

    private static final int PARRY_WINDOW_TICKS = 10;
    private static final double SPEED_MULTIPLIER = 1.8;

    private static final Map<UUID, ParryData> pendingParries = new HashMap<>();

    public static void registerArrow(UUID playerId, PersistentProjectileEntity arrow, long fireTime) {
        pendingParries.put(playerId, new ParryData(arrow, fireTime));
    }

    public static boolean tryParry(PlayerEntity player, World world) {
        UUID id = player.getUuid();
        ParryData data = pendingParries.remove(id);
        if (data == null) return false;

        long elapsed = world.getTime() - data.fireTime;
        if (elapsed > PARRY_WINDOW_TICKS) return false;
        if (data.arrow.isRemoved()) return false;

        Vec3d vel = data.arrow.getVelocity();
        data.arrow.setVelocity(vel.multiply(SPEED_MULTIPLIER));
        data.arrow.setDamage(data.arrow.getDamage() * SPEED_MULTIPLIER);
        data.arrow.setCritical(true);
        data.arrow.velocityModified = true;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            FlashEffectPacket.send(serverPlayer);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.6F, 1.4F);

        return true;
    }

    private record ParryData(PersistentProjectileEntity arrow, long fireTime) {}
}

