package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulRavagerEntity extends RavagerEntity {

    private UUID ownerUUID;
    private int deathTimer = 0;
    private static final int LIFETIME_TICKS = 160;

    private static final Map<UUID, SoulRavagerEntity> activeRavagers = new HashMap<>();

    public SoulRavagerEntity(EntityType<? extends RavagerEntity> type, World world) {
        super(type, world);
    }

    public SoulRavagerEntity(World world, PlayerEntity owner) {
        super(ModEntityTypes.SOUL_RAVAGER_TYPE, world);
        this.ownerUUID = owner.getUuid();
        activeRavagers.put(ownerUUID, this);
    }

    public static SoulRavagerEntity getActiveRavager(UUID ownerUUID) {
        SoulRavagerEntity ravager = activeRavagers.get(ownerUUID);
        if (ravager != null && (ravager.isRemoved() || ravager.isDead())) {
            activeRavagers.remove(ownerUUID);
            return null;
        }
        return ravager;
    }


    @Override
    public void remove(RemovalReason reason) {
        if (ownerUUID != null) activeRavagers.remove(ownerUUID);
        super.remove(reason);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.75);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(3, new FollowOwnerGoal());
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true,
                player -> !player.getUuid().equals(this.ownerUUID)));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, LivingEntity.class, false,
                entity -> entity instanceof HostileEntity && !(entity instanceof SoulRavagerEntity)));
    }

    private class FollowOwnerGoal extends Goal {
        private PlayerEntity owner;
        private int updateTimer;

        public FollowOwnerGoal() {
            setControls(java.util.EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (ownerUUID == null) return false;
            if (getWorld().isClient) return false;
            Entity e = ((ServerWorld) getWorld()).getEntity(ownerUUID);
            if (!(e instanceof PlayerEntity p)) return false;
            owner = p;
            return squaredDistanceTo(owner) > 16.0;
        }

        @Override
        public boolean shouldContinue() {
            return owner != null && !owner.isDead() && squaredDistanceTo(owner) > 9.0;
        }

        @Override
        public void start() { updateTimer = 0; }

        @Override
        public void stop() { getNavigation().stop(); owner = null; }

        @Override
        public void tick() {
            if (owner == null) return;
            if (++updateTimer % 10 == 0) {
                getNavigation().startMovingTo(owner, 1.4);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClient) return;

        if (getWorld() instanceof ServerWorld serverWorld && age % 2 == 0) {
            serverWorld.spawnParticles(ParticleTypes.SOUL,
                    getX(), getY() + 0.5, getZ(),
                    4, 0.4, 0.3, 0.4, 0.01);
            serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY() + 0.2, getZ(),
                    2, 0.3, 0.1, 0.3, 0.005);
        }

        deathTimer++;
        if (deathTimer >= LIFETIME_TICKS) {
            if (getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                        getX(), getY() + 1.0, getZ(),
                        20, 0.8, 0.8, 0.8, 0.05);
            }
            removeAllPassengers();
            discard();
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof PlayerEntity player
                && player.getUuid().equals(ownerUUID)
                && getPassengerList().isEmpty();
    }

    @Override
    public boolean isImmobile() {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SoulDeathTimer", deathTimer);
        if (ownerUUID != null) nbt.putUuid("OwnerUUID", ownerUUID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        deathTimer = nbt.getInt("SoulDeathTimer");
        if (nbt.containsUuid("OwnerUUID")) ownerUUID = nbt.getUuid("OwnerUUID");
        if (ownerUUID != null) activeRavagers.put(ownerUUID, this);
    }
}
