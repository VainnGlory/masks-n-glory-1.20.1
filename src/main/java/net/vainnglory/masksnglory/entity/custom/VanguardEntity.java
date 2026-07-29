package net.vainnglory.masksnglory.entity.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.Parry;
import net.vainnglory.masksnglory.util.ModDamageTypes;
import net.vainnglory.masksnglory.world.ModDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VanguardEntity extends PathAwareEntity {

    private static final TrackedData<Optional<UUID>> SKIN_UUID =
            DataTracker.registerData(VanguardEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<BlockPos> HOME_POS =
            DataTracker.registerData(VanguardEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    private static final Map<VanguardVariant, Integer> RESPAWN_COOLDOWNS = new EnumMap<>(VanguardVariant.class);
    private static final int RESPAWN_TICKS = 600;
    private static final int CHECK_INTERVAL = 40;
    private static final int FOOD_COOLDOWN_TICKS = 300;
    private static final float SHIELD_BLOCK_CHANCE = 0.35F;
    private static boolean spawnChunkForced = false;
    private static int warmupTicks = 100;

    private VanguardVariant variant;
    private int goldenApplesLeft;
    private int steaksLeft;
    private int foodCooldown;
    private boolean usedTotem;
    private final List<PendingBlockRevert> pendingReverts = new ArrayList<>();

    public VanguardEntity(EntityType<? extends VanguardEntity> type, World world) {
        super(type, world);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 16.0F);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SKIN_UUID, Optional.empty());
        this.dataTracker.startTracking(HOME_POS, BlockPos.ORIGIN);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new VanguardEatGoal(this));
        this.goalSelector.add(2, new VanguardAbilityGoal(this));
        this.goalSelector.add(2, new VanguardSkullSlamGoal(this));
        this.goalSelector.add(3, new VanguardCombatGoal(this));
        this.goalSelector.add(4, new GoHomeGoal(this));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 20.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0, 0.001F));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 3.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (foodCooldown > 0) foodCooldown--;

        if (!this.getWorld().isClient && !pendingReverts.isEmpty()) {
            pendingReverts.removeIf(revert -> {
                revert.ticks--;
                if (revert.ticks > 0) return false;
                if (this.getWorld().getBlockState(revert.pos).equals(revert.placedState)) {
                    this.getWorld().setBlockState(revert.pos, revert.revertState);
                }
                return true;
            });
        }
    }

    @Override
    public Text getName() {
        return Text.literal("Elite Vanguard");
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.variant == VanguardVariant.VANGUARD_FOUR
                && source.getAttacker() instanceof LivingEntity
                && !source.isIn(DamageTypeTags.BYPASSES_SHIELD)
                && this.getRandom().nextFloat() < SHIELD_BLOCK_CHANCE) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ITEM_SHIELD_BLOCK, this.getSoundCategory(), 1.0F, 1.0F);
            amount *= 0.2F;
        }

        if (this.variant == VanguardVariant.VANGUARD_THREE && !this.usedTotem
                && this.isAlive() && amount >= this.getHealth()) {
            this.usedTotem = true;
            this.setHealth(this.getMaxHealth() * 0.5f);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ITEM_TOTEM_USE, this.getSoundCategory(), 1.0F, 1.0F);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getBodyY(0.5), this.getZ(),
                        30, 0.5, 0.5, 0.5, 0.3);
            }
            return false;
        }

        return super.damage(source, amount);
    }

    public void initialize(VanguardVariant variant, BlockPos homePos) {
        this.variant = variant;
        this.dataTracker.set(SKIN_UUID, Optional.of(variant.skinUuid));
        this.dataTracker.set(HOME_POS, homePos);
        this.goldenApplesLeft = variant.goldenApples;
        this.steaksLeft = variant.cookedSteaks;

        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(variant.maxHealth);
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(variant.movementSpeed);
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(variant.attackDamage);
        this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(variant.knockbackResistance);
        this.setHealth(this.getMaxHealth());

        equipArmor(variant);
        equipMainWeapon();
        if (variant == VanguardVariant.VANGUARD_FOUR) {
            this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            this.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0f);
        }
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.LEGS, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.FEET, 0.0f);
    }

    public void equipSword() {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.RUSTED_SWORD));
    }

    public void equipMainWeapon() {
        if (this.variant == VanguardVariant.VANGUARD_FIVE) {
            equipSkullBreakerPan();
        } else {
            equipSword();
        }
    }

    private void equipSkullBreakerPan() {
        ItemStack pan = new ItemStack(ModItems.GOLDEN_PAN);
        pan.addEnchantment(ModEnchantments.SKULL, 1);
        this.equipStack(EquipmentSlot.MAINHAND, pan);
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ASH_CHARGE));
        this.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0f);
    }

    private void equipArmor(VanguardVariant variant) {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack chest = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack legs = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);

        applyTrim(helmet, variant.helmetPattern, variant.helmetMaterial);
        applyTrim(chest, variant.chestPattern, variant.chestMaterial);
        applyTrim(legs, variant.legsPattern, variant.legsMaterial);
        applyTrim(boots, variant.bootsPattern, variant.bootsMaterial);

        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chest);
        this.equipStack(EquipmentSlot.LEGS, legs);
        this.equipStack(EquipmentSlot.FEET, boots);
    }

    private void applyTrim(ItemStack stack, RegistryKey<ArmorTrimPattern> patternKey, RegistryKey<ArmorTrimMaterial> materialKey) {
        Optional<RegistryEntry.Reference<ArmorTrimPattern>> pattern =
                this.getWorld().getRegistryManager().get(RegistryKeys.TRIM_PATTERN).getEntry(patternKey);
        Optional<RegistryEntry.Reference<ArmorTrimMaterial>> material =
                this.getWorld().getRegistryManager().get(RegistryKeys.TRIM_MATERIAL).getEntry(materialKey);
        if (pattern.isEmpty() || material.isEmpty()) return;
        ArmorTrim.apply(this.getWorld().getRegistryManager(), stack, new ArmorTrim(material.get(), pattern.get()));
    }

    public Optional<UUID> getSkinUuid() {
        return this.dataTracker.get(SKIN_UUID);
    }

    public BlockPos getHomePos() {
        return this.dataTracker.get(HOME_POS);
    }

    @Nullable
    public VanguardVariant getVariant() {
        return this.variant;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getHurtSound(DamageSource source) {
        if (this.variant == VanguardVariant.VANGUARD_THREE) {
            int pick = this.getRandom().nextInt(3);
            if (pick == 0) return MasksNGlorySounds.ENTITY_VANGUARD_DMAN_HIT_1;
            if (pick == 1) return MasksNGlorySounds.ENTITY_VANGUARD_DMAN_HIT_2;
            return MasksNGlorySounds.ENTITY_VANGUARD_DMAN_HIT_3;
        }
        return super.getHurtSound(source);
    }

    @Override
    protected net.minecraft.sound.SoundEvent getDeathSound() {
        if (this.variant == VanguardVariant.VANGUARD_THREE) {
            return MasksNGlorySounds.ENTITY_VANGUARD_DMAN_DEATH;
        }
        return super.getDeathSound();
    }

    public boolean canEat() {
        return foodCooldown <= 0 && (goldenApplesLeft > 0 || steaksLeft > 0);
    }

    public void eatNextFood() {
        if (goldenApplesLeft > 0 && this.getHealth() < this.getMaxHealth() * 0.25f) {
            goldenApplesLeft--;
            this.heal(4.0f);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 0));
        } else if (steaksLeft > 0) {
            steaksLeft--;
            this.heal(8.0f);
        } else if (goldenApplesLeft > 0) {
            goldenApplesLeft--;
            this.heal(4.0f);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 0));
        }
        foodCooldown = FOOD_COOLDOWN_TICKS;
    }

    public void scheduleRevert(BlockPos pos, BlockState placedState, BlockState revertState, int ticks) {
        pendingReverts.add(new PendingBlockRevert(pos, placedState, revertState, ticks));
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public int getXpToDrop() {
        return 0;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient && this.variant != null) {
            RESPAWN_COOLDOWNS.put(this.variant, RESPAWN_TICKS);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.variant != null) nbt.putString("VanguardVariant", this.variant.name());
        nbt.putInt("GoldenApplesLeft", goldenApplesLeft);
        nbt.putInt("SteaksLeft", steaksLeft);
        nbt.putInt("FoodCooldown", foodCooldown);
        nbt.putBoolean("UsedTotem", usedTotem);
        BlockPos home = getHomePos();
        nbt.putInt("HomeX", home.getX());
        nbt.putInt("HomeY", home.getY());
        nbt.putInt("HomeZ", home.getZ());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("VanguardVariant")) {
            try {
                this.variant = VanguardVariant.valueOf(nbt.getString("VanguardVariant"));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (this.variant != null) {
            this.dataTracker.set(SKIN_UUID, Optional.of(this.variant.skinUuid));
        }
        if (nbt.contains("GoldenApplesLeft")) goldenApplesLeft = nbt.getInt("GoldenApplesLeft");
        if (nbt.contains("SteaksLeft")) steaksLeft = nbt.getInt("SteaksLeft");
        if (nbt.contains("FoodCooldown")) foodCooldown = nbt.getInt("FoodCooldown");
        if (nbt.contains("UsedTotem")) usedTotem = nbt.getBoolean("UsedTotem");
        if (nbt.contains("HomeX")) {
            this.dataTracker.set(HOME_POS, new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ")));
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;
            ServerWorld verdant = server.getWorld(ModDimensions.VERDANT_MEMORY_KEY);
            if (verdant == null) return;

            if (!spawnChunkForced) {
                verdant.setChunkForced(VanguardVariant.HOME_POS.getX() >> 4, VanguardVariant.HOME_POS.getZ() >> 4, true);
                spawnChunkForced = true;
                return;
            }

            if (warmupTicks > 0) {
                warmupTicks -= CHECK_INTERVAL;
                return;
            }

            for (VanguardVariant variant : VanguardVariant.values()) {
                Integer cooldown = RESPAWN_COOLDOWNS.get(variant);
                if (cooldown != null) {
                    int remaining = cooldown - CHECK_INTERVAL;
                    if (remaining > 0) {
                        RESPAWN_COOLDOWNS.put(variant, remaining);
                        continue;
                    }
                    RESPAWN_COOLDOWNS.remove(variant);
                }

                boolean alive = !verdant.getEntitiesByType(ModEntityTypes.VANGUARD_TYPE, e -> e.getVariant() == variant).isEmpty();
                if (!alive) spawn(verdant, variant);
            }
        });
    }

    private static void spawn(ServerWorld world, VanguardVariant variant) {
        int x = VanguardVariant.HOME_POS.getX();
        int z = VanguardVariant.HOME_POS.getZ();
        int y = VanguardVariant.HOME_POS.getY();
        world.getChunk(x >> 4, z >> 4);
        BlockPos spawnPos = new BlockPos(x, y, z);

        VanguardEntity vanguard = ModEntityTypes.VANGUARD_TYPE.create(world);
        if (vanguard == null) return;
        vanguard.initialize(variant, spawnPos);
        vanguard.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        world.spawnEntity(vanguard);
    }

    static class PendingBlockRevert {
        final BlockPos pos;
        final BlockState placedState;
        final BlockState revertState;
        int ticks;

        PendingBlockRevert(BlockPos pos, BlockState placedState, BlockState revertState, int ticks) {
            this.pos = pos;
            this.placedState = placedState;
            this.revertState = revertState;
            this.ticks = ticks;
        }
    }

    static class VanguardEatGoal extends Goal {
        private static final double RETREAT_SPEED = 1.6;
        private static final double RETREAT_DISTANCE = 3.0;
        private static final float EAT_DECISION_CHANCE = 0.4F;
        private static final int EAT_DECISION_INTERVAL = 40;

        private final VanguardEntity vanguard;
        private int eatTicks;
        private int decisionCooldown;
        private double fleeX;
        private double fleeZ;

        VanguardEatGoal(VanguardEntity vanguard) {
            this.vanguard = vanguard;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (!vanguard.canEat()) return false;
            if (vanguard.getHealth() >= vanguard.getMaxHealth() * 0.4f) return false;

            if (decisionCooldown > 0) {
                decisionCooldown--;
                return false;
            }
            decisionCooldown = EAT_DECISION_INTERVAL;
            return vanguard.getRandom().nextFloat() < EAT_DECISION_CHANCE;
        }

        @Override
        public boolean shouldContinue() {
            return eatTicks > 0;
        }

        @Override
        public void start() {
            eatTicks = 32;
            vanguard.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.COOKED_BEEF));
            vanguard.setSprinting(true);

            LivingEntity threat = vanguard.getTarget();
            if (threat != null) {
                double dx = vanguard.getX() - threat.getX();
                double dz = vanguard.getZ() - threat.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.001) {
                    fleeX = vanguard.getX() + (dx / dist) * RETREAT_DISTANCE;
                    fleeZ = vanguard.getZ() + (dz / dist) * RETREAT_DISTANCE;
                } else {
                    fleeX = vanguard.getX();
                    fleeZ = vanguard.getZ();
                }
            } else {
                fleeX = vanguard.getX();
                fleeZ = vanguard.getZ();
            }
        }

        @Override
        public void stop() {
            vanguard.equipMainWeapon();
            vanguard.setSprinting(false);
            vanguard.getNavigation().stop();
        }

        @Override
        public void tick() {
            eatTicks--;

            if (vanguard.getNavigation().isIdle()) {
                vanguard.getNavigation().startMovingTo(fleeX, vanguard.getY(), fleeZ, RETREAT_SPEED);
            }

            LivingEntity threat = vanguard.getTarget();
            if (threat != null) {
                vanguard.getLookControl().lookAt(threat, 30.0f, 30.0f);
            }

            if (eatTicks % 8 == 0) {
                float pitch = 1.0f + (vanguard.getRandom().nextFloat() - 0.5f) * 0.2f;
                vanguard.getWorld().playSound(null, vanguard.getX(), vanguard.getY(), vanguard.getZ(),
                        SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.HOSTILE, 1.0f, pitch);
            }
            if (eatTicks <= 0) {
                vanguard.eatNextFood();
            }
        }
    }

    static class VanguardAbilityGoal extends Goal {
        private static final int COOLDOWN_TICKS = 600;
        private static final int TRAP_RANGE_SQ = 16;

        private final VanguardEntity vanguard;
        private int cooldown;

        VanguardAbilityGoal(VanguardEntity vanguard) {
            this.vanguard = vanguard;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            VanguardVariant variant = vanguard.getVariant();
            if (variant != VanguardVariant.VANGUARD_ONE && variant != VanguardVariant.VANGUARD_TWO) return false;

            LivingEntity target = vanguard.getTarget();
            return target != null && target.isAlive() && vanguard.squaredDistanceTo(target) <= TRAP_RANGE_SQ;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            VanguardVariant variant = vanguard.getVariant();
            if (variant == VanguardVariant.VANGUARD_ONE) {
                placeCobweb();
            } else if (variant == VanguardVariant.VANGUARD_TWO) {
                placeFlashLava();
            }
            cooldown = COOLDOWN_TICKS;
        }

        private void placeCobweb() {
            LivingEntity target = vanguard.getTarget();
            if (target == null) return;
            BlockPos pos = target.getBlockPos();
            BlockState current = vanguard.getWorld().getBlockState(pos);
            if (!current.isAir()) return;

            BlockState webState = Blocks.COBWEB.getDefaultState();
            vanguard.getWorld().setBlockState(pos, webState);
            vanguard.getWorld().playSound(null, pos, webState.getSoundGroup().getPlaceSound(), SoundCategory.HOSTILE, 1.0f, 1.0f);
        }

        private void placeFlashLava() {
            LivingEntity target = vanguard.getTarget();
            if (target == null) return;
            BlockPos pos = target.getBlockPos().down();
            BlockState original = vanguard.getWorld().getBlockState(pos);
            BlockState lavaState = Blocks.LAVA.getDefaultState();

            vanguard.getWorld().setBlockState(pos, lavaState);
            vanguard.getWorld().playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.HOSTILE, 1.0f, 1.0f);
            vanguard.scheduleRevert(pos, lavaState, original, 10);
        }
    }

    static class VanguardSkullSlamGoal extends Goal {
        private static final int COOLDOWN_TICKS = 120;
        private static final double ABILITY_RANGE_SQ = 100.0;
        private static final double LAUNCH_VERTICAL = 1.75;
        private static final double HOMING_SPEED = 0.9;
        private static final float MIN_FALL_DISTANCE = 2.5f;
        private static final int MAX_AIRBORNE_TICKS = 60;
        private static final double SLAM_RADIUS = 4.0;

        private final VanguardEntity vanguard;
        private LivingEntity target;
        private int cooldown;
        private boolean airborne;
        private double peakY;
        private int airborneTicks;

        VanguardSkullSlamGoal(VanguardEntity vanguard) {
            this.vanguard = vanguard;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            if (vanguard.getVariant() != VanguardVariant.VANGUARD_FIVE) return false;
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            LivingEntity t = vanguard.getTarget();
            if (t == null || !t.isAlive()) return false;
            return vanguard.squaredDistanceTo(t) <= ABILITY_RANGE_SQ;
        }

        @Override
        public boolean shouldContinue() {
            return airborne;
        }

        @Override
        public void start() {
            target = vanguard.getTarget();
            airborne = true;
            airborneTicks = 0;
            peakY = vanguard.getY();

            vanguard.setVelocity(vanguard.getVelocity().x, LAUNCH_VERTICAL, vanguard.getVelocity().z);
            vanguard.velocityModified = true;
            vanguard.getLookControl().lookAt(target, 30.0f, 30.0f);
            steerTowardTarget();

            World world = vanguard.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                double px = vanguard.getX();
                double py = vanguard.getY();
                double pz = vanguard.getZ();
                for (int i = 0; i < 30; i++) {
                    double offsetX = (vanguard.getRandom().nextDouble() - 0.5) * 1.2;
                    double offsetZ = (vanguard.getRandom().nextDouble() - 0.5) * 1.2;
                    serverWorld.spawnParticles(ParticleTypes.FLAME,
                            px + offsetX, py, pz + offsetZ,
                            1, 0.1, 0.2, 0.1, 0.02);
                }
            }
            world.playSound(null, vanguard.getX(), vanguard.getY(), vanguard.getZ(),
                    SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0f, 0.8f);
        }

        @Override
        public void stop() {
            airborne = false;
            target = null;
            cooldown = COOLDOWN_TICKS;
        }

        @Override
        public void tick() {
            airborneTicks++;
            peakY = Math.max(peakY, vanguard.getY());
            if (vanguard.fallDistance > 0) {
                vanguard.fallDistance = 0;
            }

            if (target != null) {
                vanguard.getLookControl().lookAt(target, 30.0f, 30.0f);
                steerTowardTarget();
            }

            if (vanguard.isOnGround() && airborneTicks > 3) {
                performSlam();
                airborne = false;
                return;
            }

            if (airborneTicks >= MAX_AIRBORNE_TICKS) {
                airborne = false;
            }
        }

        private void steerTowardTarget() {
            if (target == null) return;
            double dx = target.getX() - vanguard.getX();
            double dz = target.getZ() - vanguard.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= 0.001) return;

            double dirX = dx / dist;
            double dirZ = dz / dist;
            double horizontalSpeed = Math.min(dist * 0.3, HOMING_SPEED);
            Vec3d vel = vanguard.getVelocity();
            vanguard.setVelocity(dirX * horizontalSpeed, vel.y, dirZ * horizontalSpeed);
            vanguard.velocityModified = true;
        }

        private void performSlam() {
            World world = vanguard.getWorld();
            if (!(world instanceof ServerWorld serverWorld)) return;

            float fallDistance = (float) Math.max(0.0, peakY - vanguard.getY());
            if (fallDistance < MIN_FALL_DISTANCE) return;

            float baseFallBonus = fallDistance * 0.7f;
            float skullBonus = fallDistance * 2.5f + 8.0f;
            float totalBonus = baseFallBonus + skullBonus;

            Vec3d pos = vanguard.getPos();
            Box area = new Box(pos.add(-SLAM_RADIUS, -1, -SLAM_RADIUS), pos.add(SLAM_RADIUS, 2, SLAM_RADIUS));
            List<LivingEntity> nearby = world.getNonSpectatingEntities(LivingEntity.class, area);

            boolean hitSomething = false;

            for (LivingEntity near : nearby) {
                if (near == vanguard || !near.isAlive()) continue;

                if (Parry.tryPanParry(near, vanguard, world, totalBonus)) {
                    hitSomething = true;
                    continue;
                }

                near.damage(ModDamageTypes.pan(vanguard, near), totalBonus);
                hitSomething = true;

                Vec3d toNear = near.getPos().subtract(pos);
                double dist = toNear.length();
                if (dist > 0.001) {
                    Vec3d knockDir = toNear.normalize();
                    float knockStrength = Math.min(totalBonus * 0.04f, 1.8f);
                    near.addVelocity(knockDir.x * knockStrength, 0.2f + knockStrength * 0.1f, knockDir.z * knockStrength);
                    near.velocityModified = true;
                }
            }

            if (!hitSomething) return;

            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y + 0.5, pos.z, 18, 0.4, 0.3, 0.4, 0.02);
            serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 4.0f, 0.85f);
        }
    }

    static class VanguardCombatGoal extends Goal {
        private static final double ATTACK_RANGE_SQ = 9.0;
        private final VanguardEntity vanguard;
        private LivingEntity target;
        private int attackCooldown;
        private int jumpAttackDelay;
        private int strafeDirectionChangeTicks;
        private int strafeDirection = 1;

        VanguardCombatGoal(VanguardEntity vanguard) {
            this.vanguard = vanguard;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            LivingEntity t = vanguard.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void start() {
            target = vanguard.getTarget();
            jumpAttackDelay = 0;
        }

        @Override
        public void stop() {
            target = null;
            vanguard.getNavigation().stop();
            vanguard.setSprinting(false);
        }

        @Override
        public void tick() {
            target = vanguard.getTarget();
            if (target == null) return;

            vanguard.getLookControl().lookAt(target, 30.0f, 30.0f);
            double distSq = vanguard.squaredDistanceTo(target);

            if (distSq > ATTACK_RANGE_SQ) {
                vanguard.setSprinting(true);
                double angle = Math.toRadians((vanguard.getId() % 8) * 45.0);
                double approachX = target.getX() + Math.cos(angle) * 2.0;
                double approachZ = target.getZ() + Math.sin(angle) * 2.0;
                vanguard.getNavigation().startMovingTo(approachX, target.getY(), approachZ, 1.0);

                if (vanguard.horizontalCollision && vanguard.getRandom().nextInt(4) == 0) {
                    vanguard.getJumpControl().setActive();
                }
            } else {
                vanguard.setSprinting(false);

                if (--strafeDirectionChangeTicks <= 0) {
                    strafeDirectionChangeTicks = 20 + vanguard.getRandom().nextInt(20);
                    strafeDirection = vanguard.getRandom().nextBoolean() ? 1 : -1;
                }

                double dx = target.getX() - vanguard.getX();
                double dz = target.getZ() - vanguard.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.001) {
                    double perpX = -dz / dist;
                    double perpZ = dx / dist;
                    double strafeX = vanguard.getX() + perpX * strafeDirection * 2.5;
                    double strafeZ = vanguard.getZ() + perpZ * strafeDirection * 2.5;
                    vanguard.getNavigation().startMovingTo(strafeX, target.getY(), strafeZ, 0.8);
                }
            }

            if (jumpAttackDelay > 0) {
                jumpAttackDelay--;
                if (jumpAttackDelay == 0) {
                    performAttack(true);
                }
                return;
            }

            if (attackCooldown > 0) attackCooldown--;
            if (distSq <= ATTACK_RANGE_SQ && attackCooldown <= 0) {
                attackCooldown = 20;
                if (vanguard.isOnGround() && vanguard.getRandom().nextInt(2) == 0) {
                    vanguard.getJumpControl().setActive();
                    jumpAttackDelay = 6;
                } else {
                    performAttack(vanguard.fallDistance > 0.0f && !vanguard.isOnGround());
                }
            }
        }

        private void performAttack(boolean crit) {
            if (target == null) return;
            vanguard.swingHand(Hand.MAIN_HAND);

            if (crit) {
                EntityAttributeInstance dmgAttr = vanguard.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                double base = dmgAttr.getBaseValue();
                dmgAttr.setBaseValue(base * 1.5);
                vanguard.tryAttack(target);
                dmgAttr.setBaseValue(base);

                vanguard.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.0f, 1.0f);
                if (vanguard.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.5), target.getZ(),
                            10, 0.3, 0.3, 0.3, 0.1);
                }
            } else {
                vanguard.tryAttack(target);
            }

            net.minecraft.sound.SoundEvent hitSound = vanguard.getMainHandStack().isOf(ModItems.GOLDEN_PAN)
                    ? MasksNGlorySounds.ITEM_PAN_HIT
                    : MasksNGlorySounds.ITEM_RUSTED_HIT;

            vanguard.getWorld().playSound(null, vanguard.getX(), vanguard.getY(), vanguard.getZ(),
                    hitSound, vanguard.getSoundCategory(), 0.7F,
                    (float) (1.0F + vanguard.getRandom().nextGaussian() / 10f));
        }
    }

    static class GoHomeGoal extends Goal {
        private static final double LEASH_DIST = 24.0;
        private static final double TP_DIST = 40.0;
        private static final int STUCK_THRESHOLD = 100;
        private static final double HOME_ROAM_RADIUS = 6.0;

        private final VanguardEntity vanguard;
        private double lastDistSq;
        private int stuckTicks;
        private double offsetX;
        private double offsetZ;

        GoHomeGoal(VanguardEntity vanguard) {
            this.vanguard = vanguard;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return vanguard.getTarget() == null && homeDistance() > LEASH_DIST;
        }

        @Override
        public boolean shouldContinue() {
            return vanguard.getTarget() == null && homeDistance() > 4.0;
        }

        @Override
        public void start() {
            stuckTicks = 0;
            lastDistSq = Double.MAX_VALUE;
            double angle = vanguard.getRandom().nextDouble() * Math.PI * 2;
            double radius = vanguard.getRandom().nextDouble() * HOME_ROAM_RADIUS;
            offsetX = Math.cos(angle) * radius;
            offsetZ = Math.sin(angle) * radius;
        }

        @Override
        public void stop() {
            vanguard.getNavigation().stop();
        }

        @Override
        public void tick() {
            BlockPos home = vanguard.getHomePos();
            double targetX = home.getX() + 0.5 + offsetX;
            double targetZ = home.getZ() + 0.5 + offsetZ;
            double distSq = vanguard.squaredDistanceTo(targetX, home.getY(), targetZ);

            if (Math.sqrt(distSq) > TP_DIST) {
                if (distSq >= lastDistSq - 1.0) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                }
                lastDistSq = distSq;

                if (stuckTicks > STUCK_THRESHOLD) {
                    vanguard.refreshPositionAndAngles(targetX, home.getY(), targetZ, vanguard.getYaw(), vanguard.getPitch());
                    vanguard.getNavigation().stop();
                    vanguard.getWorld().playSound(null, home, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0f, 1.0f);
                    stuckTicks = 0;
                    return;
                }
            } else {
                stuckTicks = 0;
                lastDistSq = distSq;
            }

            if (vanguard.getNavigation().isIdle()) {
                vanguard.getNavigation().startMovingTo(targetX, home.getY(), targetZ, 1.0);
            }
        }

        private double homeDistance() {
            return Math.sqrt(vanguard.getBlockPos().getSquaredDistance(vanguard.getHomePos()));
        }
    }
}
