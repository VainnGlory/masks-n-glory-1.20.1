package net.vainnglory.masksnglory.world;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.custom.AmalgamItem;

import java.util.*;

public class FarlandsNullManager {
    public static final int AMP_STAGE1 = 10;
    public static final int AMP_STAGE2 = 11;
    public static final int AMP_STAGE3 = 12;

    private static final int STAGE2_TICKS = 6000;
    private static final int STAGE3_TICKS = 18000;

    private static final Map<UUID, Integer> farlandsTicks = new HashMap<>();

    private static int amplifierFor(int ticks) {
        if (ticks >= STAGE3_TICKS) return AMP_STAGE3;
        if (ticks >= STAGE2_TICKS) return AMP_STAGE2;
        if (ticks > 0) return AMP_STAGE1;
        return -1;
    }

    private static boolean hasActiveAmalgam(ServerPlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() instanceof AmalgamItem && stack.getDamage() < stack.getMaxDamage()) {
                return true;
            }
        }
        return false;
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!player.getWorld().getRegistryKey().equals(World.OVERWORLD)) continue;
            UUID id = player.getUuid();
            if (!FarlandsHelper.isInFarlands(player.getX(), player.getZ())) continue;
            if (hasActiveAmalgam(player)) continue;

            int prev = farlandsTicks.getOrDefault(id, 0);
            int curr = prev + 1;
            farlandsTicks.put(id, curr);

            int amp = amplifierFor(curr);
            if (amp >= 0) {
                StatusEffectInstance existing = player.getStatusEffect(ModEffects.NULL_EFFECT);
                if (existing == null || existing.getAmplifier() < amp) {
                    player.addStatusEffect(new StatusEffectInstance(
                            ModEffects.NULL_EFFECT, Integer.MAX_VALUE, amp, false, false, true
                    ));
                }
            }

            if (amp >= AMP_STAGE2) {
                int interval = (amp >= AMP_STAGE3) ? 40 : 100;
                float range = (amp >= AMP_STAGE3) ? 4.0f : 2.0f;
                if (curr % interval == 0) {
                    long seed = id.getLeastSignificantBits() ^ ((long) curr * 6364136223846793005L);
                    Random rng = Random.create(seed);
                    double nx = player.getX() + (rng.nextDouble() * 2.0 - 1.0) * range;
                    double nz = player.getZ() + (rng.nextDouble() * 2.0 - 1.0) * range;
                    player.teleport(
                            (ServerWorld) player.getWorld(),
                            nx, player.getY(), nz,
                            new HashSet<>(), player.getYaw(), player.getPitch()
                    );
                }
            }
        }
    }

    public static void onWakeUp(ServerPlayerEntity player) {
        farlandsTicks.remove(player.getUuid());
        player.removeStatusEffect(ModEffects.NULL_EFFECT);
    }

    public static void cleanup(UUID id) {
        farlandsTicks.remove(id);
    }
}