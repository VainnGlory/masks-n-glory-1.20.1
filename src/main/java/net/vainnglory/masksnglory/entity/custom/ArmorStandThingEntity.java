package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ArmorStandThingEntity extends HostileEntity implements GeoEntity {

    private static final TrackedData<Boolean> ACTIVE =
            DataTracker.registerData(ArmorStandThingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int proximityTicks = 0;
    private int wailCooldown = 0;

    public ArmorStandThingEntity(EntityType<? extends ArmorStandThingEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 22.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ACTIVE, false);
    }

    public boolean isActive() {
        return this.dataTracker.get(ACTIVE);
    }

    private void setActive(boolean value) {
        this.dataTracker.set(ACTIVE, value);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SpiderNavigation(this, world);
    }

    @Override
    public boolean isClimbing() {
        return isActive() && this.horizontalCollision;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, false) {
            @Override public boolean canStart() { return isActive() && super.canStart(); }
            @Override public boolean shouldContinue() { return isActive() && super.shouldContinue(); }
        });
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f) {
            @Override public boolean canStart() { return isActive() && super.canStart(); }
        });
        this.goalSelector.add(3, new LookAroundGoal(this) {
            @Override public boolean canStart() { return isActive() && super.canStart(); }
        });

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true) {
            @Override public boolean canStart() { return isActive() && super.canStart(); }
            @Override public boolean shouldContinue() { return isActive() && super.shouldContinue(); }
        });
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ARMOR_STAND_HIT;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!isActive() && !getWorld().isClient) {
            activate();
        }
        boolean result = super.damage(source, amount);
        this.hurtTime = 0;
        this.maxHurtTime = 0;
        if (result && getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()),
                    getX(), getY() + getHeight() / 2.0, getZ(),
                    12, 0.3, 0.4, 0.3, 0.1);
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        if (!isActive()) {
            boolean playerNearby = !getWorld().getEntitiesByClass(
                    PlayerEntity.class,
                    getBoundingBox().expand(10),
                    p -> !p.isSpectator() && !p.isCreative() && !p.isDead()
            ).isEmpty();

            if (playerNearby) {
                proximityTicks++;
                if (proximityTicks >= 200) activate();
            } else {
                proximityTicks = 0;
            }
        } else {
            if (wailCooldown <= 0) {
                getWorld().playSound(null, getX(), getY(), getZ(),
                        MasksNGlorySounds.ENTITY_ARMOR_STAND_THING_WAIL,
                        SoundCategory.HOSTILE, 1.0f, 1.0f);
                wailCooldown = 80;
            } else {
                wailCooldown--;
            }

            if (horizontalCollision && !isOnGround()) {
                Vec3d vel = getVelocity();
                if (vel.y < 0.55) {
                    setVelocity(vel.x, 0.55, vel.z);
                }
            }
        }
    }

    private void activate() {
        setActive(true);
        proximityTicks = 0;
        wailCooldown = 0;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!isActive() && !getWorld().isClient) {
            activate();
        }
        return ActionResult.PASS;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("ArmorStandActive", isActive());
        nbt.putInt("ProximityTicks", proximityTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setActive(nbt.getBoolean("ArmorStandActive"));
        proximityTicks = nbt.getInt("ProximityTicks");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (isActive()) {
                state.getController().setAnimation(
                        RawAnimation.begin().then("animation.armor_stand.chase", Animation.LoopType.LOOP));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
