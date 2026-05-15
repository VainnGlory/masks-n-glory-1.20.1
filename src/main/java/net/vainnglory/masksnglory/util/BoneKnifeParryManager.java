package net.vainnglory.masksnglory.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class BoneKnifeParryManager {

    private static final int PARRY_WINDOW_TICKS = 10;
    private static final double SPEED_MULTIPLIER = 1.3;
    private static final Map<UUID, ParryData> pendingParries = new HashMap<>();

    private static final double PROJECTILE_PARRY_RANGE = 3.0;

    private static final int PAN_PARRY_WINDOW_TICKS = 20;
    private static final Map<UUID, Long> panParryWindows = new HashMap<>();
    private static final Map<UUID, Long> panParryCooldowns = new HashMap<>();

    public static void registerArrow(UUID playerId, PersistentProjectileEntity arrow, long fireTime) {
        pendingParries.put(playerId, new ParryData(arrow, fireTime));
    }

    public static boolean tryParry(PlayerEntity player, World world) {
        UUID id = player.getUuid();
        ParryData data = pendingParries.remove(id);
        if (data == null) return false;
        if (world.getTime() - data.fireTime > PARRY_WINDOW_TICKS) return false;
        if (data.arrow.isRemoved()) return false;

        Vec3d vel = data.arrow.getVelocity();
        data.arrow.setVelocity(vel.multiply(SPEED_MULTIPLIER));
        data.arrow.setDamage(data.arrow.getDamage() * SPEED_MULTIPLIER);
        data.arrow.setCritical(true);
        data.arrow.velocityModified = true;

        playParryEffects(player, world);
        return true;
    }

    public static void activatePanParryWindow(UUID playerId, long currentTime) {
        panParryWindows.put(playerId, currentTime);
    }

    public static boolean tryProjectileParry(PlayerEntity player, World world) {
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d eyePos = player.getEyePos();

        List<ProjectileEntity> nearby = world.getEntitiesByClass(
                ProjectileEntity.class,
                player.getBoundingBox().expand(PROJECTILE_PARRY_RANGE),
                e -> !e.isRemoved()
                        && e.getOwner() != player
                        && e.getVelocity().lengthSquared() > 0.01
        );

        if (nearby.isEmpty()) return false;

        ProjectileEntity closest = nearby.stream()
                .filter(e -> {
                    Vec3d toE = e.getPos().subtract(eyePos);
                    return toE.lengthSquared() > 0.001 && lookVec.dotProduct(toE.normalize()) >= 0.5;
                })
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)))
                .orElse(null);
        if (closest == null) return false;

        Entity originalOwner = closest.getOwner();
        double boostedSpeed = closest.getVelocity().length() * SPEED_MULTIPLIER;

        if (originalOwner != null && !originalOwner.isRemoved()) {
            Vec3d toOwner = originalOwner.getPos()
                    .add(0, originalOwner.getHeight() / 2.0, 0)
                    .subtract(closest.getPos());
            if (toOwner.lengthSquared() > 0.001) {
                closest.setVelocity(toOwner.normalize().multiply(boostedSpeed));
            } else {
                closest.setVelocity(closest.getVelocity().negate().multiply(SPEED_MULTIPLIER));
            }
        } else {
            closest.setVelocity(closest.getVelocity().negate().multiply(SPEED_MULTIPLIER));
        }

        closest.velocityModified = true;
        closest.setOwner(player);

        if (closest instanceof PersistentProjectileEntity arrow) {
            arrow.setDamage(arrow.getDamage() * SPEED_MULTIPLIER);
            arrow.setCritical(true);
        }

        playParryEffects(player, world);
        return true;
    }

    public static boolean tryPanParry(LivingEntity target, PlayerEntity attacker, World world, float slamDamage) {
        if (!(target instanceof PlayerEntity targetPlayer)) return false;

        Long lastParry = panParryCooldowns.get(targetPlayer.getUuid());
        if (lastParry != null && world.getTime() - lastParry < 40L) return false;

        Long windowStart = panParryWindows.remove(targetPlayer.getUuid());
        if (windowStart == null) return false;
        if (world.getTime() - windowStart > PAN_PARRY_WINDOW_TICKS) return false;

        panParryCooldowns.put(targetPlayer.getUuid(), world.getTime());

        attacker.hurtTime = 0;
        attacker.timeUntilRegen = 0;
        attacker.damage(world.getDamageSources().playerAttack(targetPlayer), slamDamage * 0.75f);

        Vec3d dir = attacker.getPos().subtract(targetPlayer.getPos());
        Vec3d launch = dir.lengthSquared() > 0.001 ? dir.normalize() : new Vec3d(1, 0, 0);
        attacker.setVelocity(launch.x * 1.5, 1.2, launch.z * 1.5);
        attacker.velocityModified = true;

        targetPlayer.swingHand(Hand.MAIN_HAND);
        playParryEffects(targetPlayer, world);
        return true;
    }

    private static void playParryEffects(PlayerEntity player, World world) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            FlashEffectPacket.send(serverPlayer);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.6F, 1.4F);
    }

    private record ParryData(PersistentProjectileEntity arrow, long fireTime) {}
}

