package net.vainnglory.masksnglory.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.*;

public class GoldenScrapManager {

    private static final int KILLS_PER_SCRAP = 3;
    private static final float MIN_DAMAGE_THRESHOLD = 2.0f;
    private static final long PENALTY_DURATION_MS = 15 * 60 * 1000L;
    private static final UUID HEALTH_PENALTY_ID = UUID.fromString("a3b4c5d6-e7f8-1234-abcd-ef0123456789");

    private static final Map<UUID, Map<UUID, Float>> damageTracker = new HashMap<>();
    private static final Map<UUID, Integer> killProgress = new HashMap<>();
    private static final Map<UUID, Set<UUID>> usedVictims = new HashMap<>();
    private static final Map<UUID, UUID> preQualifiedKills = new HashMap<>();
    private static final Map<UUID, Long> healthPenaltyRemaining = new HashMap<>();
    private static final Map<UUID, Long> healthPenaltyStarted = new HashMap<>();

    public static void recordDamage(UUID dealer, UUID target, float amount) {
        damageTracker.computeIfAbsent(dealer, k -> new HashMap<>()).merge(target, amount, Float::sum);
    }

    public static boolean wouldQualify(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        UUID killerId = killer.getUuid();
        UUID victimId = victim.getUuid();

        if (!killer.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.RET_HELMET)) return false;
        if (!hasFullNetherite(victim)) return false;

        float damageDealt = damageTracker
                .getOrDefault(victimId, Collections.emptyMap())
                .getOrDefault(killerId, 0.0f);
        if (damageDealt < MIN_DAMAGE_THRESHOLD) return false;

        Set<UUID> used = usedVictims.getOrDefault(killerId, Collections.emptySet());
        return !used.contains(victimId);
    }

    public static void preQualifyKill(UUID victimId, UUID killerId) {
        preQualifiedKills.put(victimId, killerId);
    }

    public static void handleKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        UUID killerId = killer.getUuid();
        UUID victimId = victim.getUuid();

        UUID expectedKiller = preQualifiedKills.remove(victimId);
        if (expectedKiller == null || !expectedKiller.equals(killerId)) return;

        Set<UUID> used = usedVictims.computeIfAbsent(killerId, k -> new HashSet<>());
        if (used.contains(victimId)) return;

        used.add(victimId);
        int progress = killProgress.merge(killerId, 1, Integer::sum);

        if (progress >= KILLS_PER_SCRAP) {
            ItemStack scrap = new ItemStack(ModItems.GOLDENSCRAP);
            if (!killer.getInventory().insertStack(scrap)) {
                killer.dropItem(scrap, false);
                killer.sendMessage(Text.literal("Something shiny fell out of your pocket."), false);
            } else {
                killer.sendMessage(Text.literal("Something shines inside your pocket."), false);
            }
            killProgress.remove(killerId);
            usedVictims.remove(killerId);
        } else {
            killer.sendMessage(Text.literal("Hunt progress: " + progress + "/" + KILLS_PER_SCRAP), true);
        }
    }

    public static void resetProgress(UUID id) {
        killProgress.remove(id);
        usedVictims.remove(id);
    }

    public static boolean hasFoodItems(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isFood()) return true;
        }
        return false;
    }

    public static void markForHealthPenalty(UUID id) {
        healthPenaltyRemaining.put(id, PENALTY_DURATION_MS);
    }

    public static boolean isMarkedForHealthPenalty(UUID id) {
        return healthPenaltyRemaining.containsKey(id) && !healthPenaltyStarted.containsKey(id);
    }

    public static void applyHealthPenalty(ServerPlayerEntity player) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr == null || attr.getModifier(HEALTH_PENALTY_ID) != null) return;
        attr.addTemporaryModifier(new EntityAttributeModifier(
                HEALTH_PENALTY_ID, "golden_scrap_penalty", -0.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        ));
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static void removeHealthPenalty(ServerPlayerEntity player) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) attr.removeModifier(HEALTH_PENALTY_ID);
    }

    public static void startHealthPenaltyTimer(UUID id) {
        healthPenaltyStarted.put(id, System.currentTimeMillis());
    }

    public static void pauseHealthPenalty(UUID id) {
        Long start = healthPenaltyStarted.remove(id);
        if (start == null || !healthPenaltyRemaining.containsKey(id)) return;
        long remaining = healthPenaltyRemaining.get(id) - (System.currentTimeMillis() - start);
        if (remaining <= 0) {
            healthPenaltyRemaining.remove(id);
        } else {
            healthPenaltyRemaining.put(id, remaining);
        }
    }

    public static void resumeHealthPenalty(UUID id, ServerPlayerEntity player) {
        if (!healthPenaltyRemaining.containsKey(id)) {
            removeHealthPenalty(player);
            return;
        }
        applyHealthPenalty(player);
        healthPenaltyStarted.put(id, System.currentTimeMillis());
    }

    public static void tickHealthPenalties(MinecraftServer server) {
        long now = System.currentTimeMillis();
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : new HashMap<>(healthPenaltyRemaining).entrySet()) {
            UUID id = entry.getKey();
            Long start = healthPenaltyStarted.get(id);
            if (start == null) continue;

            long remaining = entry.getValue() - (now - start);
            if (remaining <= 0) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);
                if (player != null) removeHealthPenalty(player);
                toRemove.add(id);
            } else {
                healthPenaltyRemaining.put(id, remaining);
                healthPenaltyStarted.put(id, now);
            }
        }

        toRemove.forEach(id -> {
            healthPenaltyRemaining.remove(id);
            healthPenaltyStarted.remove(id);
        });
    }

    private static boolean hasFullNetherite(PlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.NETHERITE_HELMET) &&
                player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.NETHERITE_CHESTPLATE) &&
                player.getEquippedStack(EquipmentSlot.LEGS).isOf(Items.NETHERITE_LEGGINGS) &&
                player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.NETHERITE_BOOTS);
    }

    public static void cleanupAfterDeath(UUID playerId) {
        damageTracker.remove(playerId);
        preQualifiedKills.remove(playerId);
    }

    public static void cleanupOnDisconnect(UUID playerId) {
        damageTracker.remove(playerId);
    }
}
