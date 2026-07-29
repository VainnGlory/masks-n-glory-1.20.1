package net.vainnglory.masksnglory.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.ModArmorMaterials;

import java.util.*;

public class MaskAbilities {

    private record DecayData(int stacks, long lastHitTick) {
    }

    private static final UUID DMAN_BOOST_UUID = UUID.fromString("f5aedf7a-bfbb-4048-8c1c-d15b1f69fbc3");
    private static final UUID STONEI_ARMOR_UUID = UUID.fromString("4a657c14-5cde-4f98-8cbf-4dd52e758010");
    private static final UUID EGO_GRUDGE_UUID = UUID.fromString("48f71b13-932a-404f-9b9c-906e655585fc");
    private static final UUID PIKO_DAMAGE_UUID = UUID.fromString("f9346fa2-97bd-4061-a6f5-2738f4fe5eea");
    private static final UUID ROSEN_SPEED_UUID = UUID.fromString("af269a38-49f2-476e-869c-59113c5bc289");
    private static final UUID RAT_HEALTH_UUID = UUID.fromString("9f9c28f2-4822-4c9e-b00c-3f13c666dea4");

    private static final Map<UUID, Float> egoGrudge = new HashMap<>();
    private static final Set<UUID> pendingEgoGrudgeRemoval = new HashSet<>();
    private static final Map<UUID, Set<UUID>> eyeMaskGlowed = new HashMap<>();
    private static final Set<UUID> togWasEating = new HashSet<>();
    private static final Map<UUID, UUID> houndLastAttacker = new HashMap<>();
    private static final Map<UUID, UUID> houndGlowedEntity = new HashMap<>();
    private static final Map<UUID, Integer> houndCritCount = new HashMap<>();
    private static final Map<UUID, DecayData> decayTargets = new HashMap<>();
    private static final Map<UUID, Long> stoneiShellExpiry = new HashMap<>();
    private static final Map<UUID, Long> stoneiCooldownEnd = new HashMap<>();
    private static final Set<UUID> pendingDmanRemoval = new HashSet<>();
    private static final Map<UUID, Long> ojiLastHit = new HashMap<>();
    private static final Set<UUID> ojiGuard = new HashSet<>();
    private static final Set<UUID> corvGuard = new HashSet<>();
    private static final Map<UUID, Long> nullSneakStart = new HashMap<>();
    private static final Map<UUID, Long> nullCooldown = new HashMap<>();
    private static final Map<UUID, Long> grinWearStart = new HashMap<>();
    private static final Set<UUID> pikoModifierActive = new HashSet<>();
    private static final Set<UUID> rosenSpeedModifierActive = new HashSet<>();
    private static final Set<UUID> happyWearing = new HashSet<>();
    private static final Map<UUID, Map<UUID, Integer>> rosenStareTicks = new HashMap<>();
    private static final Set<UUID> rosenWearing = new HashSet<>();
    private static final Map<UUID, Long> ratCooldown = new HashMap<>();
    private static final Set<UUID> ratWearing = new HashSet<>();
    private static final int RAT_TP_COOLDOWN_TICKS = 12000;

    public static ArmorMaterial getMaskMaterial(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem armor)) return null;
        return armor.getMaterial();
    }

    public static void clearPlayerData(UUID id) {
        egoGrudge.remove(id);
        houndCritCount.remove(id);
        nullSneakStart.remove(id);
        nullCooldown.remove(id);
        ojiLastHit.remove(id);
        grinWearStart.remove(id);
        pikoModifierActive.remove(id);
        happyWearing.remove(id);
        rosenStareTicks.remove(id);
        rosenWearing.remove(id);
        rosenSpeedModifierActive.remove(id);
        for (Map<UUID, Integer> map : rosenStareTicks.values()) {
            map.remove(id);
        }
    }

    public static void recordHoundAttacker(UUID playerUUID, UUID attackerUUID) {
        houndLastAttacker.put(playerUUID, attackerUUID);
    }

    public static void addEgoGrudge(UUID id, float damage) {
        float current = egoGrudge.getOrDefault(id, 0f);
        egoGrudge.put(id, Math.min(4.0f, current + damage * 0.25f));
    }

    public static void activateStoneiShell(PlayerEntity player) {
        UUID id = player.getUuid();
        long now = player.getWorld().getTime();
        Long cooldownEnd = stoneiCooldownEnd.get(id);
        if (cooldownEnd != null && now < cooldownEnd) return;
        EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armor == null) return;
        armor.removeModifier(STONEI_ARMOR_UUID);
        armor.addTemporaryModifier(new EntityAttributeModifier(
                STONEI_ARMOR_UUID, "MNG Fortress",
                10.0, EntityAttributeModifier.Operation.ADDITION));
        stoneiShellExpiry.put(id, now + 60);
        stoneiCooldownEnd.put(id, now + 120);
    }

    public static boolean isOjiFirstHit(UUID id, long currentTime) {
        return currentTime - ojiLastHit.getOrDefault(id, 0L) > 80;
    }

    public static void ojiRecordHit(UUID id, long time) {
        ojiLastHit.put(id, time);
    }

    public static boolean ojiEnterGuard(UUID id) {
        return ojiGuard.add(id);
    }

    public static void ojiExitGuard(UUID id) {
        ojiGuard.remove(id);
    }

    public static boolean corvEnterGuard(UUID id) {
        return corvGuard.add(id);
    }

    public static void corvExitGuard(UUID id) {
        corvGuard.remove(id);
    }

    public static void tick(PlayerEntity player, ArmorMaterial material) {
        if (material == ModArmorMaterials.EMASKS) {
            tickEye(player);
        } else if (eyeMaskGlowed.containsKey(player.getUuid())) {
            cleanupEye(player);
        }

        if (material == ModArmorMaterials.STSHARD) {
            tickStonei(player);
        } else {
            EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (armor != null) armor.removeModifier(STONEI_ARMOR_UUID);
            stoneiShellExpiry.remove(player.getUuid());
            stoneiCooldownEnd.remove(player.getUuid());
        }

        if (material == ModArmorMaterials.HHSHARD) {
            tickHound(player);
        } else if (houndGlowedEntity.containsKey(player.getUuid())) {
            UUID glowedId = houndGlowedEntity.remove(player.getUuid());
            if (glowedId != null && player.getWorld() instanceof ServerWorld world) {
                Entity e = world.getEntity(glowedId);
                if (e != null) e.setGlowing(false);
            }
            houndLastAttacker.remove(player.getUuid());
            houndCritCount.remove(player.getUuid());
        }

        if (material == ModArmorMaterials.NMASKS) {
            tickNull(player);
        } else {
            nullSneakStart.remove(player.getUuid());
        }

        if (material == ModArmorMaterials.GMASKS) {
            tickGrin(player);
        } else {
            grinWearStart.remove(player.getUuid());
        }

        if (material == ModArmorMaterials.DOSHARD) tickDog(player);
        if (material == ModArmorMaterials.TSHARD) tickTog(player);
        if (material == ModArmorMaterials.KMASKS) tickKnight(player);
        if (material == ModArmorMaterials.DVSHARD) tickDave(player);
        if (material == ModArmorMaterials.CSHARD) tickCorv(player);

        if (material == ModArmorMaterials.ESHARD) {
            if (!player.hasStatusEffect(ModEffects.PINNING)) {
                player.addStatusEffect(new StatusEffectInstance(
                        ModEffects.PINNING, 200, 0, false, false, true));
            }
        }

        if (material == ModArmorMaterials.PSHARD) {
            tickPiko(player);
        } else {
            UUID id = player.getUuid();
            if (pikoModifierActive.remove(id)) {
                EntityAttributeInstance atk = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (atk != null) atk.removeModifier(PIKO_DAMAGE_UUID);
            }
        }

        if (material == ModArmorMaterials.HMASKS) {
            tickHappy(player);
        } else if (happyWearing.remove(player.getUuid())) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.UNLUCK, 400, 0, false, true, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 1, false, true, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1, false, true, true));
        }

        if (material == ModArmorMaterials.ROSENM) {
            tickRosen(player);
            UUID id = player.getUuid();
            if (!rosenSpeedModifierActive.contains(id)) {
                EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (speed != null) {
                    speed.removeModifier(ROSEN_SPEED_UUID);
                    speed.addTemporaryModifier(new EntityAttributeModifier(
                            ROSEN_SPEED_UUID, "MNG Rosen Forced Sprint", 0.3, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    rosenSpeedModifierActive.add(id);
                }
            }
        } else {
            cleanupRosen(player);
            UUID id = player.getUuid();
            if (rosenSpeedModifierActive.remove(id)) {
                EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (speed != null) speed.removeModifier(ROSEN_SPEED_UUID);
            }

            if (material == ModArmorMaterials.RATSHARD) {
                tickRat(player);
            } else {
                EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealth != null) maxHealth.removeModifier(RAT_HEALTH_UUID);
                ratWearing.remove(player.getUuid());
            }
        }
    }

    private static void tickRat(PlayerEntity player) {
        ratWearing.add(player.getUuid());
        EntityAttributeInstance maxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth != null && maxHealth.getModifier(RAT_HEALTH_UUID) == null) {
            maxHealth.addTemporaryModifier(new EntityAttributeModifier(
                    RAT_HEALTH_UUID, "MNG Rat Health Penalty", -4.0, EntityAttributeModifier.Operation.ADDITION));
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        if (!player.hasStatusEffect(StatusEffects.HASTE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 400, 1, false, false, true));
        }

        if (player.isSneaking() && player.getOffHandStack().isOf(Items.CLOCK) && player instanceof ServerPlayerEntity sp) {
            ItemStack helmet = player.getInventory().getArmorStack(3);
            triggerRatTeleport(sp, helmet);
        }
    }

    private static void triggerRatTeleport(ServerPlayerEntity player, ItemStack helmet) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        UUID id = player.getUuid();
        long now = player.getWorld().getTime();

        Long lastUse = ratCooldown.get(id);
        if (lastUse != null && now - lastUse < RAT_TP_COOLDOWN_TICKS) {
            long remaining = (RAT_TP_COOLDOWN_TICKS - (now - lastUse)) / 20;
            player.sendMessage(Text.literal("▸ " + remaining + "s").formatted(Formatting.DARK_GRAY), true);
            return;
        }
        ratCooldown.put(id, now);

        ServerWorld overworld = server.getOverworld();
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
        player.teleport(overworld, 70003.5, 200.0, 70003.5, Collections.emptySet(), player.getYaw(), player.getPitch());
        overworld.playSound(null, 70003.5, 200.0, 70003.5,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);

        int remainingDurability = helmet.getMaxDamage() - helmet.getDamage();
        int damageAmount = (remainingDurability + 1) / 2;
        if (damageAmount > 0) {
            helmet.damage(damageAmount, player, e -> e.sendEquipmentBreakStatus(EquipmentSlot.HEAD));
        }
    }

    private static void tickNull(PlayerEntity player) {
        UUID id = player.getUuid();
        if (!player.isSneaking()) {
            nullSneakStart.remove(id);
            return;
        }
        long now = player.getWorld().getTime();
        nullSneakStart.computeIfAbsent(id, k -> now);
        long heldTicks = now - nullSneakStart.get(id);
        if (heldTicks >= 20) {
            nullSneakStart.remove(id);
            if (player instanceof ServerPlayerEntity sp) triggerNullTP(sp);
        }
    }

    private static void tickHappy(PlayerEntity player) {
        happyWearing.add(player.getUuid());
        if (!player.hasStatusEffect(ModEffects.SUGAR_RUSH)) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.SUGAR_RUSH, 400, 0, false, false, true));
        }
        if (!player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 400, 0, false, false, true));
        }
        if (!player.hasStatusEffect(StatusEffects.LUCK)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 400, 0, false, false, true));
        }
    }

    private static void triggerNullTP(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        UUID id = player.getUuid();
        long now = player.getWorld().getTime();

        Long lastTp = nullCooldown.get(id);
        if (lastTp != null && now - lastTp < 2400) {
            long remaining = (2400 - (now - lastTp)) / 20;
            player.sendMessage(Text.literal("▸ " + remaining + "s").formatted(Formatting.DARK_GRAY), true);
            return;
        }
        nullCooldown.put(id, now);

        if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
            ServerWorld nether = server.getWorld(World.NETHER);
            if (nether == null) return;
            BlockPos spawn = server.getOverworld().getSpawnPos();
            double netherX = spawn.getX() / 8.0;
            double netherZ = spawn.getZ() / 8.0;
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
            player.teleport(nether, netherX, 130.0, netherZ, Collections.emptySet(), player.getYaw(), player.getPitch());
            nether.playSound(null, netherX, 130.0, netherZ,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);

        } else if (player.getWorld().getRegistryKey() == World.NETHER) {
            ServerWorld overworld = server.getOverworld();
            BlockPos spawn = overworld.getSpawnPos();
            int surfaceY = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
            player.teleport(overworld, spawn.getX() + 0.5, surfaceY, spawn.getZ() + 0.5,
                    Collections.emptySet(), player.getYaw(), player.getPitch());
            overworld.playSound(null, spawn.getX() + 0.5, surfaceY, spawn.getZ() + 0.5,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
        }
    }

    private static void tickGrin(PlayerEntity player) {
        if (!player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BAD_OMEN, 400, 0, false, false, true));
        }
        UUID id = player.getUuid();
        long now = player.getWorld().getTime();
        grinWearStart.computeIfAbsent(id, k -> now);
        long worn = now - grinWearStart.get(id);
        if (worn > 0 && worn % 100 == 0) {
            player.getHungerManager().setFoodLevel(0);
        }
    }

    private static void tickDog(PlayerEntity player) {
        if (!player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 400, 3, false, false, true));
        }
    }

    private static void tickEye(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        UUID playerId = player.getUuid();
        Set<UUID> prev = eyeMaskGlowed.getOrDefault(playerId, new HashSet<>());
        Set<UUID> now = new HashSet<>();
        Box area = new Box(player.getBlockPos()).expand(12);
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player)) {
            entity.setGlowing(true);
            now.add(entity.getUuid());
        }
        for (UUID uid : prev) {
            if (!now.contains(uid)) {
                Entity gone = world.getEntity(uid);
                if (gone != null) gone.setGlowing(false);
            }
        }
        eyeMaskGlowed.put(playerId, now);
    }

    private static void cleanupEye(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        Set<UUID> glowed = eyeMaskGlowed.remove(player.getUuid());
        if (glowed == null) return;
        for (UUID uid : glowed) {
            Entity entity = world.getEntity(uid);
            if (entity != null) entity.setGlowing(false);
        }
    }

    private static void tickTog(PlayerEntity player) {
        boolean eating = player.isUsingItem() && player.getActiveItem().isFood();
        if (!eating && togWasEating.remove(player.getUuid())) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH, 80, 0, false, true, true));
        } else if (eating) {
            togWasEating.add(player.getUuid());
        }
    }

    private static void tickKnight(PlayerEntity player) {
        if (player.getHealth() >= 8.0f) return;
        ItemStack helmet = player.getInventory().getArmorStack(3);
        NbtCompound nbt = helmet.getOrCreateNbt();
        long lastActivated = nbt.getLong("MNG_LastStand");
        long currentTime = player.getWorld().getTime();
        if (currentTime - lastActivated > 1200) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 120, 2, false, true, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 1, false, true, true));
            nbt.putLong("MNG_LastStand", currentTime);
        }
    }

    private static void tickDave(PlayerEntity player) {
        if (player.getHealth() < 8.0f && !player.hasStatusEffect(StatusEffects.SPEED)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 40, 1, false, false, true));
        }
    }

    private static void tickHound(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        UUID playerId = player.getUuid();
        UUID prevGlowedId = houndGlowedEntity.get(playerId);
        UUID attackerId = houndLastAttacker.get(playerId);
        if (attackerId != null) {
            Entity attacker = world.getEntity(attackerId);
            if (attacker != null) {
                attacker.setGlowing(true);
                houndGlowedEntity.put(playerId, attackerId);
            }
        }
        if (prevGlowedId != null && !prevGlowedId.equals(attackerId)) {
            Entity prev = world.getEntity(prevGlowedId);
            if (prev != null) prev.setGlowing(false);
        }
    }

    private static void tickStonei(PlayerEntity player) {
        UUID id = player.getUuid();
        Long expiry = stoneiShellExpiry.get(id);
        if (expiry == null) return;
        long now = player.getWorld().getTime();
        if (now >= expiry) {
            stoneiShellExpiry.remove(id);
            EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (armor != null) armor.removeModifier(STONEI_ARMOR_UUID);
        }
    }

    private static void tickCorv(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        Vec3d eyePos = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Box searchBox = new Box(eyePos, eyePos.add(look.multiply(16.0))).expand(1.0);
        LivingEntity found = null;
        double closest = Double.MAX_VALUE;
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, searchBox, e -> e != player)) {
            Vec3d toEntity = entity.getEyePos().subtract(eyePos);
            double dot = toEntity.normalize().dotProduct(look);
            if (dot > 0.97) {
                double dist = eyePos.squaredDistanceTo(entity.getEyePos());
                if (dist < closest) {
                    closest = dist;
                    found = entity;
                }
            }
        }
        if (found != null) {
            String bar = String.format("%.1f / %.1f ❤", found.getHealth(), found.getMaxHealth());
            sp.sendMessage(Text.literal(bar).formatted(Formatting.RED), true);
        }
    }

    private static void tickPiko(PlayerEntity player) {
        UUID id = player.getUuid();
        if (!pikoModifierActive.contains(id)) {
            EntityAttributeInstance atk = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (atk != null) {
                atk.removeModifier(PIKO_DAMAGE_UUID);
                atk.addTemporaryModifier(new EntityAttributeModifier(
                        PIKO_DAMAGE_UUID, "MNG Piko Weakness",
                        -0.40, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                pikoModifierActive.add(id);
            }
        }
    }

    private static void tickRosen(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        UUID wearerId = player.getUuid();

        if (rosenWearing.add(wearerId)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false));
        }

        Map<UUID, Integer> stareMap = rosenStareTicks.computeIfAbsent(wearerId, k -> new HashMap<>());
        Set<UUID> currentObservers = new HashSet<>();
        Vec3d wearerEye = player.getEyePos();

        for (ServerPlayerEntity observer : world.getEntitiesByClass(
                ServerPlayerEntity.class,
                new Box(player.getBlockPos()).expand(100),
                e -> e != player)) {
            Vec3d toWearer = wearerEye.subtract(observer.getEyePos()).normalize();
            double dot = toWearer.dotProduct(observer.getRotationVec(1.0f));
            if (dot > 0.97) {
                UUID observerId = observer.getUuid();
                int ticks = stareMap.merge(observerId, 1, Integer::sum);
                currentObservers.add(observerId);
                applyRosenDebuff(observer, ticks);
            }
        }

        stareMap.keySet().retainAll(currentObservers);
    }

    private static void applyRosenDebuff(ServerPlayerEntity observer, int stareTicks) {
        if (stareTicks >= 160) {
            observer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2, false, false, false));
            observer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 1, false, false, false));
            observer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 40, 0, false, false, false));
        } else if (stareTicks >= 80) {
            observer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false, false));
            observer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, false, false, false));
        } else if (stareTicks >= 40) {
            observer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, false, false));
        }
    }

    private static void cleanupRosen(PlayerEntity player) {
        if (rosenWearing.remove(player.getUuid())) {
            player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
        rosenStareTicks.remove(player.getUuid());
    }

    public static void registerCallbacks() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || !(entity instanceof LivingEntity target))
                return ActionResult.PASS;
            ArmorMaterial mat = getMaskMaterial(player);
            if (mat == null) return ActionResult.PASS;

            if (mat == ModArmorMaterials.GMASKS && player instanceof ServerPlayerEntity sp) {
                float bonus = 3.0f * (target.getHealth() / target.getMaxHealth());
                if (bonus > 0.1f) target.damage(world.getDamageSources().playerAttack(sp), bonus);
            }

            if (mat == ModArmorMaterials.DOSHARD && player instanceof ServerPlayerEntity sp) {
                boolean hasAlly = !world.getEntitiesByClass(ServerPlayerEntity.class,
                        new Box(player.getBlockPos()).expand(10),
                        p -> p != player && getMaskMaterial(p) != null).isEmpty();
                if (hasAlly) target.damage(world.getDamageSources().playerAttack(sp), 2.0f);
            }

            if (mat == ModArmorMaterials.PSHARD) {
                if (player.getAttackCooldownProgress(0.5f) >= 0.9f) {
                    float dmg = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    player.heal(dmg * 0.15f);
                }
            }

            if (mat == ModArmorMaterials.HHSHARD) {
                boolean isCrit = player.fallDistance > 0f && !player.isOnGround()
                        && !player.isSprinting() && !player.isInsideWaterOrBubbleColumn();
                if (isCrit) {
                    int crits = houndCritCount.merge(player.getUuid(), 1, Integer::sum);
                    if (crits >= 6) {
                        houndCritCount.put(player.getUuid(), 0);
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2, false, true, true));
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, false, true, true));
                        target.addStatusEffect(new StatusEffectInstance(ModEffects.PINNING, 60, 1, false, true, true));
                    }
                }
            }

            if (mat == ModArmorMaterials.DSHARD) {
                long now = player.getWorld().getTime();
                UUID targetId = target.getUuid();
                DecayData data = decayTargets.get(targetId);
                int currentStacks = (data != null && (now - data.lastHitTick()) < 160) ? data.stacks() : 0;
                if (currentStacks > 0) {
                    EntityAttributeInstance atk = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) {
                        atk.removeModifier(DMAN_BOOST_UUID);
                        atk.addTemporaryModifier(new EntityAttributeModifier(
                                DMAN_BOOST_UUID, "MNG Decay Boost",
                                currentStacks * 0.05,
                                EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                        pendingDmanRemoval.add(player.getUuid());
                    }
                }
                decayTargets.put(targetId, new DecayData(Math.min(5, currentStacks + 1), now));
            }

            if (mat == ModArmorMaterials.ESHARD) {
                UUID id = player.getUuid();
                float grudge = egoGrudge.getOrDefault(id, 0f);
                if (grudge > 0.1f) {
                    egoGrudge.remove(id);
                    EntityAttributeInstance atk = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) {
                        atk.removeModifier(EGO_GRUDGE_UUID);
                        atk.addTemporaryModifier(new EntityAttributeModifier(
                                EGO_GRUDGE_UUID, "MNG Grudge",
                                grudge, EntityAttributeModifier.Operation.ADDITION));
                        pendingEgoGrudgeRemoval.add(id);
                    }
                }
            }

            if (mat == ModArmorMaterials.CRSHARD && player.isSprinting()
                    && world instanceof ServerWorld sw) {
                sw.createExplosion(player,
                        target.getX(), target.getY() + 0.5, target.getZ(),
                        0.8f, World.ExplosionSourceType.NONE);
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (UUID id : pendingDmanRemoval) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p != null) {
                    EntityAttributeInstance atk = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) atk.removeModifier(DMAN_BOOST_UUID);
                }
            }
            pendingDmanRemoval.clear();

            for (UUID id : new HashSet<>(ratWearing)) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p == null || getMaskMaterial(p) != ModArmorMaterials.RATSHARD) {
                    if (p != null) {
                        EntityAttributeInstance maxHealth = p.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                        if (maxHealth != null) maxHealth.removeModifier(RAT_HEALTH_UUID);
                    }
                    ratWearing.remove(id);
                }
            }

            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld != null) {
                long now = overworld.getTime();
                decayTargets.entrySet().removeIf(e -> now - e.getValue().lastHitTick() > 160);
            }

            for (UUID id : pendingEgoGrudgeRemoval) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p != null) {
                    EntityAttributeInstance atk = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) atk.removeModifier(EGO_GRUDGE_UUID);
                }
            }
            pendingEgoGrudgeRemoval.clear();

            for (UUID id : new HashSet<>(rosenWearing)) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p == null || getMaskMaterial(p) != ModArmorMaterials.ROSENM) {
                    if (p != null) {
                        p.removeStatusEffect(StatusEffects.BLINDNESS);
                        if (rosenSpeedModifierActive.remove(id)) {
                            EntityAttributeInstance speed = p.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                            if (speed != null) speed.removeModifier(ROSEN_SPEED_UUID);
                        }
                    } else {
                        rosenSpeedModifierActive.remove(id);
                    }
                    rosenWearing.remove(id);
                    rosenStareTicks.remove(id);
                }
            }
        });
    }
}


