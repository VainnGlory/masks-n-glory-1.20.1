package net.vainnglory.masksnglory.util;

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
import net.vainnglory.masksnglory.item.ModArmorMaterials;

import java.util.*;

public class MaskAbilityManager {

    private record DecayData(int stacks, long lastHitTick) {}
    private record StillnessData(Vec3d lastPos, long stillSince) {}

    private static final UUID HOUND_BOOST_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f01234567890");
    private static final UUID DMAN_BOOST_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-012345678901");
    private static final UUID STONEI_ARMOR_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-def0-123456789012");

    private static final Map<UUID, Set<UUID>> eyeMaskGlowed    = new HashMap<>();
    private static final Set<UUID> togWasEating = new HashSet<>();
    private static final Map<UUID, UUID> houndLastAttacker = new HashMap<>();
    private static final Map<UUID, UUID> houndGlowedEntity = new HashMap<>();
    private static final Map<UUID, DecayData> decayTargets = new HashMap<>();
    private static final Map<UUID, StillnessData> stoneiData = new HashMap<>();
    private static final Set<UUID> pendingHoundRemoval = new HashSet<>();
    private static final Set<UUID> pendingDmanRemoval = new HashSet<>();
    private static final Map<UUID, Long> ojiLastHit = new HashMap<>();
    private static final Set<UUID> ojiGuard = new HashSet<>();
    private static final Set<UUID> corvGuard = new HashSet<>();
    private static final Map<UUID, Long> nullSneakStart = new HashMap<>();
    private static final Map<UUID, Long> nullCooldown = new HashMap<>();
    private static final Set<UUID> nullNetherTravelers = new HashSet<>();


    public static ArmorMaterial getMaskMaterial(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem armor)) return null;
        return armor.getMaterial();
    }

    public static void recordHoundAttacker(UUID playerUUID, UUID attackerUUID) {
        houndLastAttacker.put(playerUUID, attackerUUID);
    }

    public static boolean isOjiFirstHit(UUID id, long currentTime) {
        return currentTime - ojiLastHit.getOrDefault(id, 0L) > 80;
    }
    public static void ojiRecordHit(UUID id, long time) { ojiLastHit.put(id, time); }
    public static boolean ojiEnterGuard(UUID id) { return ojiGuard.add(id); }
    public static void ojiExitGuard(UUID id) { ojiGuard.remove(id); }

    public static boolean corvEnterGuard(UUID id) { return corvGuard.add(id); }
    public static void corvExitGuard(UUID id) { corvGuard.remove(id); }

    public static void tick(PlayerEntity player, ArmorMaterial material) {

        if (material == ModArmorMaterials.EMASKS) {
            tickEye(player);
        } else if (eyeMaskGlowed.containsKey(player.getUuid())) {
            cleanupEye(player);
        }

        if (material == ModArmorMaterials.STSHARD) {
            tickStonei(player);
        } else if (stoneiData.containsKey(player.getUuid())) {
            EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (armor != null) armor.removeModifier(STONEI_ARMOR_UUID);
            stoneiData.remove(player.getUuid());
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
        }

        if (material == ModArmorMaterials.NMASKS) {
            tickNull(player);
        } else {
            nullSneakStart.remove(player.getUuid());
        }

        if (material == ModArmorMaterials.GMASKS) tickGrin(player);
        if (material == ModArmorMaterials.DOSHARD) tickDog(player);
        if (material == ModArmorMaterials.TSHARD) tickTog(player);
        if (material == ModArmorMaterials.KMASKS) tickKnight(player);
        if (material == ModArmorMaterials.DVSHARD) tickDave(player);
        if (material == ModArmorMaterials.CSHARD) tickCorv(player);
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

    private static void triggerNullTP(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        UUID id = player.getUuid();
        long now = player.getWorld().getTime();

        Long lastTp = nullCooldown.get(id);
        if (lastTp != null && now - lastTp < 2400) return;
        nullCooldown.put(id, now);

        if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
            ServerWorld nether = server.getWorld(World.NETHER);
            if (nether == null) return;
            BlockPos spawn = server.getOverworld().getSpawnPos();
            double netherX = spawn.getX() / 8.0;
            double netherZ = spawn.getZ() / 8.0;

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
            nullNetherTravelers.add(id);
            player.teleport(nether, netherX, 128.0, netherZ, Set.of(), player.getYaw(), player.getPitch());
            nether.playSound(null, netherX, 128.0, netherZ,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);

        } else if (player.getWorld().getRegistryKey() == World.NETHER
                && nullNetherTravelers.contains(id)) {
            ServerWorld overworld = server.getOverworld();
            BlockPos spawn = overworld.getSpawnPos();
            int surfaceY = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    spawn.getX(), spawn.getZ());

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
            nullNetherTravelers.remove(id);
            player.teleport(overworld, spawn.getX() + 0.5, surfaceY, spawn.getZ() + 0.5,
                    Set.of(), player.getYaw(), player.getPitch());
            overworld.playSound(null, spawn.getX() + 0.5, surfaceY, spawn.getZ() + 0.5,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);
        }
    }

    private static void tickGrin(PlayerEntity player) {
        if (!player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BAD_OMEN, 400, 0, false, false, true));
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
        Set<UUID> prevGlowed = eyeMaskGlowed.getOrDefault(playerId, new HashSet<>());
        Set<UUID> nowGlowed  = new HashSet<>();

        Box area = new Box(player.getBlockPos()).expand(12);
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player)) {
            entity.setGlowing(true);
            nowGlowed.add(entity.getUuid());
        }
        for (UUID uid : prevGlowed) {
            if (!nowGlowed.contains(uid)) {
                Entity gone = world.getEntity(uid);
                if (gone != null) gone.setGlowing(false);
            }
        }
        eyeMaskGlowed.put(playerId, nowGlowed);
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
        boolean isEatingFood = player.isUsingItem() && player.getActiveItem().isFood();
        if (!isEatingFood && togWasEating.remove(player.getUuid())) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH, 80, 0, false, true, true));
        } else if (isEatingFood) {
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
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,    120, 1, false, true, true));
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
        UUID playerId    = player.getUuid();
        UUID prevGlowedId = houndGlowedEntity.get(playerId);
        UUID attackerId  = houndLastAttacker.get(playerId);

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
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        UUID playerId   = player.getUuid();
        Vec3d currentPos = player.getPos();
        long currentTime = world.getTime();

        EntityAttributeInstance armorAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armorAttr == null) return;

        StillnessData data   = stoneiData.get(playerId);
        boolean moved = data == null || currentPos.squaredDistanceTo(data.lastPos()) > 0.001;

        if (moved) {
            stoneiData.put(playerId, new StillnessData(currentPos, currentTime));
            armorAttr.removeModifier(STONEI_ARMOR_UUID);
        } else {
            long stillTicks = currentTime - data.stillSince();
            double scale    = Math.min(1.0, stillTicks / 60.0);
            armorAttr.removeModifier(STONEI_ARMOR_UUID);
            if (scale > 0.01) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        STONEI_ARMOR_UUID, "MNG Fortress",
                        scale * 10.0, EntityAttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static void tickCorv(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        Vec3d eyePos = player.getEyePos();
        Vec3d look   = player.getRotationVec(1.0f);

        Box searchBox = new Box(eyePos, eyePos.add(look.multiply(16.0))).expand(1.0);
        LivingEntity found  = null;
        double closest = Double.MAX_VALUE;

        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, searchBox, e -> e != player)) {
            Vec3d toEntity = entity.getEyePos().subtract(eyePos);
            double dot = toEntity.normalize().dotProduct(look);
            if (dot > 0.97) {
                double dist = eyePos.squaredDistanceTo(entity.getEyePos());
                if (dist < closest) {
                    closest = dist;
                    found   = entity;
                }
            }
        }

        if (found != null) {
            String bar = String.format("%.1f / %.1f ❤", found.getHealth(), found.getMaxHealth());
            sp.sendMessage(Text.literal(bar).formatted(Formatting.RED), true);
        }
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
                float dmg = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                player.heal(dmg * 0.15f);
            }

            if (mat == ModArmorMaterials.HHSHARD) {
                float missingHearts = (target.getMaxHealth() - target.getHealth()) / 2.0f;
                if (missingHearts > 0) {
                    EntityAttributeInstance atk = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) {
                        atk.removeModifier(HOUND_BOOST_UUID);
                        atk.addTemporaryModifier(new EntityAttributeModifier(
                                HOUND_BOOST_UUID, "MNG Hound Bonus",
                                missingHearts * 0.10,
                                EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                        pendingHoundRemoval.add(player.getUuid());
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
            if (mat == ModArmorMaterials.ESHARD && player instanceof ServerPlayerEntity sp
                    && player.isSprinting()) {
                target.damage(world.getDamageSources().playerAttack(sp), 2.0f);
                target.takeKnockback(1.5,
                        player.getX() - target.getX(),
                        player.getZ() - target.getZ());
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
            for (UUID id : pendingHoundRemoval) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p != null) {
                    EntityAttributeInstance atk = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) atk.removeModifier(HOUND_BOOST_UUID);
                }
            }
            pendingHoundRemoval.clear();

            for (UUID id : pendingDmanRemoval) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p != null) {
                    EntityAttributeInstance atk = p.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (atk != null) atk.removeModifier(DMAN_BOOST_UUID);
                }
            }
            pendingDmanRemoval.clear();

            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld != null) {
                long now = overworld.getTime();
                decayTargets.entrySet().removeIf(e -> now - e.getValue().lastHitTick() > 160);
            }
        });
    }
}



