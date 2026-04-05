package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.entity.custom.SoulProjectileEntity;
import net.vainnglory.masksnglory.entity.custom.SoulRavagerEntity;
import net.vainnglory.masksnglory.item.custom.GlaiveItem;

import java.util.*;

public class AfterlifeEnchantment extends Enchantment {

    public static final String SOULS_KEY = "AfterlifeSouls";
    public static final String RAVAGER_KEY = "AfterlifeRavagers";
    public static final String ILLAGER_KEY = "AfterlifeIllagers";
    public static final String UNDEAD_KEY = "AfterlifeUndead";
    public static final String UNDEAD_TYPES_KEY = "AfterlifeUndeadTypes";
    public static final String MODE_KEY = "AfterlifeMode";
    public static final String BANDIT_KEY = "AfterlifeBandits";


    public static final int MODE_RAVAGER = 0;
    public static final int MODE_ILLAGER = 1;
    public static final int MODE_UNDEAD = 2;
    private static final int SOUL_CAP = 10;
    public static final int MODE_BANDIT = 3;

    private static final WeakHashMap<PlayerEntity, LivingEntity> lastTargets = new WeakHashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> critCounts = new HashMap<>();

    public static final Map<UUID, Integer> summonedRemainingTicks = new HashMap<>();
    public static final Map<UUID, UUID>  minionOwners = new HashMap<>();

    public AfterlifeEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 30; }
    @Override public int getMaxPower(int level) { return 50; }
    @Override public boolean isTreasure() { return false; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return true; }
    @Override public boolean isAvailableForRandomSelection() { return true; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) { return stack.getItem() instanceof GlaiveItem; }

    @Override
    public boolean canAccept(Enchantment other) {
        return !(other instanceof RiskEnchantment) && super.canAccept(other);
    }

    public static LivingEntity getLastTarget(PlayerEntity player) { return lastTargets.get(player); }

    public static void cycleMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(MODE_KEY, (nbt.getInt(MODE_KEY) + 1) % 4);
    }

    public static void registerCallbacks() {

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || !(entity instanceof LivingEntity target)) return ActionResult.PASS;
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof GlaiveItem)) return ActionResult.PASS;
            if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, weapon) <= 0) return ActionResult.PASS;

            lastTargets.put(player, target);

            SoulRavagerEntity ravager = SoulRavagerEntity.getActiveRavager(player.getUuid());
            if (ravager != null) ravager.setTarget(target);

            if (target instanceof PlayerEntity) {
                boolean isCrit = player.fallDistance > 0.0f
                        && !player.isOnGround() && !player.isClimbing()
                        && !player.isTouchingWater()
                        && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                        && !player.hasVehicle();

                if (isCrit) {
                    Map<UUID, Integer> hits = critCounts.computeIfAbsent(player.getUuid(), k -> new HashMap<>());
                    int count = hits.merge(target.getUuid(), 1, Integer::sum);
                    if (count >= 2) {
                        NbtCompound nbt = weapon.getOrCreateNbt();
                        nbt.putInt(SOULS_KEY, Math.min(SOUL_CAP, nbt.getInt(SOULS_KEY) + 1));
                        hits.remove(target.getUuid());
                    }
                }
            }
            return ActionResult.PASS;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((killed, damageSource) -> {
            if (!(damageSource.getAttacker() instanceof PlayerEntity player)) return;
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof GlaiveItem)) return;
            if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, weapon) <= 0) return;

            NbtCompound nbt = weapon.getOrCreateNbt();

            nbt.putInt(SOULS_KEY, Math.min(SOUL_CAP, nbt.getInt(SOULS_KEY) + 1));

            if (killed instanceof RavagerEntity && !(killed instanceof SoulRavagerEntity)) {
                nbt.putInt(RAVAGER_KEY, Math.min(5, nbt.getInt(RAVAGER_KEY) + 1));
            }

            if (killed instanceof IllagerEntity) {
                nbt.putInt(ILLAGER_KEY, Math.min(5, nbt.getInt(ILLAGER_KEY) + 1));
            }

            if (killed.getGroup() == EntityGroup.UNDEAD && !(killed instanceof SoulRavagerEntity)) {
                nbt.putInt(UNDEAD_KEY, Math.min(5, nbt.getInt(UNDEAD_KEY) + 1));
                String typeId = Registries.ENTITY_TYPE.getId(killed.getType()).toString();
                NbtList typeList = nbt.getList(UNDEAD_TYPES_KEY, NbtElement.STRING_TYPE);
                boolean tracked = false;
                for (int i = 0; i < typeList.size(); i++) {
                    if (typeList.getString(i).equals(typeId)) { tracked = true; break; }
                }
                if (!tracked) typeList.add(NbtString.of(typeId));
                nbt.put(UNDEAD_TYPES_KEY, typeList);
            }

            if (killed instanceof HorseEntity) {
                nbt.putInt(BANDIT_KEY, Math.min(3, nbt.getInt(BANDIT_KEY) + 1));
            }


            if (player.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.SCULK_SOUL,
                        killed.getX(), killed.getY() + killed.getHeight() / 2.0, killed.getZ(),
                        12, 0.3, 0.3, 0.3, 0.04);
            }
        });
    }

    public static boolean handleUse(PlayerEntity player, ItemStack stack, boolean sneaking) {
        if (player.getWorld().isClient) return false;
        NbtCompound nbt = stack.getOrCreateNbt();

        if (sneaking) {
            int mode = nbt.getInt(MODE_KEY);
            return switch (mode) {
                case MODE_RAVAGER -> summonRavager(player, stack);
                case MODE_ILLAGER -> fireIllagerFangs(player, stack);
                case MODE_UNDEAD -> summonUndead(player, stack);
                case MODE_BANDIT -> summonBanditHorse(player, stack);
                default -> false;
            };
        } else {
            int souls = nbt.getInt(SOULS_KEY);
            if (souls <= 0) return false;

            LivingEntity target = getLastTarget(player);
            if (target == null || target.isDead() || target.isRemoved()) target = findClosestTarget(player);

            UUID targetUUID = target != null ? target.getUuid() : null;
            SoulProjectileEntity proj = new SoulProjectileEntity(player.getWorld(), player, targetUUID);
            double dx = player.getRotationVec(1.0f).x;
            double dz = player.getRotationVec(1.0f).z;
            proj.setPosition(player.getX() + dx * 0.8, player.getEyeY() - 0.1, player.getZ() + dz * 0.8);
            proj.setVelocity(player, player.getPitch(), player.getYaw(), 0f, 1.5f, 0f);
            player.getWorld().spawnEntity(proj);

            nbt.putInt(SOULS_KEY, souls - 1);
            return true;
        }
    }

    private static boolean summonRavager(PlayerEntity player, ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        int count = nbt.getInt(RAVAGER_KEY);
        if (count <= 0) return false;

        SoulRavagerEntity ravager = new SoulRavagerEntity(player.getWorld(), player);
        ravager.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0f);
        ravager.initialize((ServerWorldAccess) player.getWorld(),
                player.getWorld().getLocalDifficulty(player.getBlockPos()),
                SpawnReason.SPAWN_EGG, null, null);
        player.getWorld().spawnEntity(ravager);
        nbt.putInt(RAVAGER_KEY, count - 1);
        return true;
    }

    private static boolean fireIllagerFangs(PlayerEntity player, ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        int count = nbt.getInt(ILLAGER_KEY);
        if (count <= 0) return false;

        LivingEntity target = getLastTarget(player);
        if (target == null || target.isDead() || target.isRemoved()) target = findClosestTarget(player);
        if (target == null) return false;

        World world = player.getWorld();
        Vec3d from = player.getPos();
        Vec3d to = target.getPos();
        Vec3d dir = to.subtract(from);
        double dist = dir.length();
        if (dist < 0.5) return false;
        Vec3d norm = dir.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-norm.x, norm.z));

        for (int i = 0; i < 5; i++) {
            double t = Math.min((i + 1.0) * (dist / 5.0), dist - 0.3);
            double x = from.x + norm.x * t;
            double y = target.getY();
            double z = from.z + norm.z * t;
            EvokerFangsEntity fang = new EvokerFangsEntity(world, x, y, z, yaw, i * 3, player);
            world.spawnEntity(fang);
        }

        nbt.putInt(ILLAGER_KEY, count - 1);
        return true;
    }

    private static boolean summonUndead(PlayerEntity player, ItemStack stack) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        NbtCompound nbt = stack.getOrCreateNbt();
        int count = nbt.getInt(UNDEAD_KEY);
        if (count <= 0) return false;

        NbtList typeList = nbt.getList(UNDEAD_TYPES_KEY, NbtElement.STRING_TYPE);
        if (typeList.isEmpty()) return false;

        String typeId = typeList.getString(player.getRandom().nextInt(typeList.size()));
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(new Identifier(typeId));
        if (entityType == null) return false;

        Entity entity = entityType.create(serverWorld);
        if (!(entity instanceof MobEntity mob)) return false;

        mob.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);

        if (mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(mob.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 2.0);
        }
        mob.setHealth(mob.getMaxHealth());

        if (mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
            mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5);
        }

        serverWorld.spawnEntity(mob);
        summonedRemainingTicks.put(mob.getUuid(), 300);
        minionOwners.put(mob.getUuid(), player.getUuid());

        serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                mob.getX(), mob.getY() + 1, mob.getZ(), 10, 0.4, 0.4, 0.4, 0.04);

        nbt.putInt(UNDEAD_KEY, count - 1);
        return true;
    }

    public static void tickSummonedUndead(MinecraftServer server) {
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : new HashMap<>(summonedRemainingTicks).entrySet()) {
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                for (ServerWorld world : server.getWorlds()) {
                    Entity e = world.getEntity(entry.getKey());
                    if (e != null) {
                        world.spawnParticles(ParticleTypes.SCULK_SOUL,
                                e.getX(), e.getY() + 1, e.getZ(), 20, 0.6, 0.6, 0.6, 0.05);
                        e.discard();
                        break;
                    }
                }
                toRemove.add(entry.getKey());
            } else {
                summonedRemainingTicks.put(entry.getKey(), remaining);
            }
        }

        toRemove.forEach(uuid -> { summonedRemainingTicks.remove(uuid); minionOwners.remove(uuid); });

        for (ServerWorld world : server.getWorlds()) {
            for (Map.Entry<UUID, UUID> entry : minionOwners.entrySet()) {
                Entity minion = world.getEntity(entry.getKey());
                if (minion instanceof MobEntity mob) {
                    LivingEntity t = mob.getTarget();
                    if (t != null && t.getUuid().equals(entry.getValue())) mob.setTarget(null);
                }
            }
        }
    }

    private static LivingEntity findClosestTarget(PlayerEntity player) {
        List<PlayerEntity> players = player.getWorld().getEntitiesByClass(PlayerEntity.class,
                player.getBoundingBox().expand(24), p -> p != player && !p.isDead());
        if (!players.isEmpty())
            return players.stream().min(Comparator.comparingDouble(p -> p.squaredDistanceTo(player))).orElse(null);
        List<LivingEntity> mobs = player.getWorld().getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(24), e -> e != player && !e.isDead() && !(e instanceof PlayerEntity));
        return mobs.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player))).orElse(null);
    }

    private static boolean summonBanditHorse(PlayerEntity player, ItemStack stack) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
        NbtCompound nbt = stack.getOrCreateNbt();
        int count = nbt.getInt(BANDIT_KEY);
        if (count <= 0) return false;

        HorseEntity horse = EntityType.HORSE.create(serverWorld);
        if (horse == null) return false;

        NbtCompound saddleNbt = new NbtCompound();
        saddleNbt.putString("id", "minecraft:saddle");
        saddleNbt.putByte("Count", (byte) 1);
        NbtCompound horsePatch = new NbtCompound();
        horsePatch.put("SaddleItem", saddleNbt);
        horsePatch.putBoolean("Tame", true);
        horse.readNbt(horsePatch);

        horse.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0);

        if (horse.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            horse.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(60.0);
        }
        horse.setHealth(60.0f);
        if (horse.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED) != null) {
            horse.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.45);
        }

        serverWorld.spawnEntity(horse);
        nbt.putInt(BANDIT_KEY, count - 1);
        return true;
    }


}
