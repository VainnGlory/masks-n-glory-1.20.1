package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CastIronManager {

    private static final int MAX_CHARGE = 5;
    private static final float FAST_STACK_SECONDS = 3.0f;
    private static final int FAST_STACK_TICKS = (int)(FAST_STACK_SECONDS * 20);
    private static final String CHARGE_KEY = "MNG_CastIronCharge";

    private static final Map<UUID, Integer> chargeMap = new HashMap<>();
    private static final Map<UUID, Long> firstDentTimeMap = new HashMap<>();
    private static final Map<UUID, Boolean> blockingMap = new HashMap<>();

    public static void setBlocking(PlayerEntity player, boolean blocking) {
        if (blocking) {
            blockingMap.put(player.getUuid(), true);
        } else {
            blockingMap.remove(player.getUuid());
        }
    }

    public static boolean isBlocking(PlayerEntity player) {
        return blockingMap.getOrDefault(player.getUuid(), false);
    }

    public static int getCharge(PlayerEntity player) {
        return chargeMap.getOrDefault(player.getUuid(), 0);
    }

    private static void writeChargeToWeapon(PlayerEntity blocker, int charge) {
        ItemStack weapon = blocker.getMainHandStack();
        if (weapon.getItem() instanceof GoldenPanItem) {
            weapon.getOrCreateNbt().putInt(CHARGE_KEY, charge);
        }
    }

    public static int readChargeFromWeapon(ItemStack stack) {
        if (stack.getNbt() == null) return 0;
        return stack.getNbt().getInt(CHARGE_KEY);
    }

    public static boolean absorbHit(PlayerEntity blocker, LivingEntity attacker, ServerWorld world) {
        UUID id = blocker.getUuid();
        int charge = chargeMap.getOrDefault(id, 0) + 1;
        long now = world.getTime();

        world.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(),
                MasksNGlorySounds.ITEM_PAN_HIT, SoundCategory.PLAYERS,
                1.0f, 0.9f + world.random.nextFloat() * 0.2f);

        if (!firstDentTimeMap.containsKey(id)) {
            firstDentTimeMap.put(id, now);
        }

        if (charge >= MAX_CHARGE) {
            chargeMap.remove(id);
            firstDentTimeMap.remove(id);
            writeChargeToWeapon(blocker, 0);
            triggerShockwave(blocker, attacker, world, now);
        } else {
            chargeMap.put(id, charge);
            writeChargeToWeapon(blocker, charge);
            if (charge == MAX_CHARGE - 1) {
                world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        blocker.getX(), blocker.getY() + 1.0, blocker.getZ(),
                        6, 0.3, 0.3, 0.3, 0.03);
            }
        }

        return true;
    }

    private static void triggerShockwave(PlayerEntity blocker, LivingEntity lastAttacker,
                                         ServerWorld world, long triggerTime) {
        UUID id = blocker.getUuid();
        Long firstDentTime = firstDentTimeMap.remove(id);
        boolean fastStack = firstDentTime != null && (triggerTime - firstDentTime) <= FAST_STACK_TICKS;

        float baseStrength = 3.5f;
        float strength = fastStack ? baseStrength * 1.6f : baseStrength;
        float radius = fastStack ? 6.0f : 4.5f;

        world.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(),
                SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 5.0f, 0.7f);

        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                blocker.getX(), blocker.getY() + 0.5, blocker.getZ(),
                1, 0, 0, 0, 0);

        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                blocker.getX(), blocker.getY() + 1.0, blocker.getZ(),
                40, 0.8, 0.5, 0.8, 0.06);

        Box area = new Box(blocker.getPos().add(-radius, -1, -radius),
                blocker.getPos().add(radius, 3, radius));
        List<LivingEntity> nearby = world.getNonSpectatingEntities(LivingEntity.class, area);

        for (LivingEntity near : nearby) {
            if (near == blocker) continue;
            Vec3d away = near.getPos().subtract(blocker.getPos());
            double dist = away.length();
            if (dist > radius || dist == 0) continue;

            float falloff = 1.0f - (float)(dist / radius);
            float knock = strength * falloff;
            Vec3d dir = away.normalize();
            near.setVelocity(dir.x * knock, 0.6 + knock * 0.3, dir.z * knock);
            near.velocityModified = true;
        }

        if (lastAttacker != null) {
            Vec3d recoilDir = blocker.getPos().subtract(lastAttacker.getPos());
            if (recoilDir.lengthSquared() > 0.001) {
                recoilDir = recoilDir.normalize();
                blocker.setVelocity(recoilDir.x * 2.2, 0.5, recoilDir.z * 2.2);
                blocker.velocityModified = true;
            }
        }
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!isBlocking(player)) continue;
                int charge = chargeMap.getOrDefault(player.getUuid(), 0);
                if (charge >= MAX_CHARGE - 1) {
                    ServerWorld sw = player.getServerWorld();
                    sw.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            player.getX(), player.getY() + 1.2, player.getZ(),
                            2, 0.25, 0.25, 0.25, 0.02);
                }
            }
        });
    }
}