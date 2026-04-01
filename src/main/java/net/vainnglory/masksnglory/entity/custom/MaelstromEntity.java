package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaelstromEntity extends PersistentProjectileEntity {

    private static final TrackedData<Boolean> RETURNING =
            DataTracker.registerData(MaelstromEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HOMING =
            DataTracker.registerData(MaelstromEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> REMORSE =
            DataTracker.registerData(MaelstromEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> REMORSE_STUCK =
            DataTracker.registerData(MaelstromEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final Map<UUID, MaelstromEntity> activeRemorseEntities = new HashMap<>();

    private ItemStack swordStack;
    private Vec3d startPos;
    private int ticksInAir = 0;

    private UUID homingTargetUUID;

    private UUID stuckTargetUUID;
    private int remorseTimer = 0;

    public MaelstromEntity(EntityType<? extends MaelstromEntity> entityType, World world) {
        super(entityType, world);
        this.swordStack = new ItemStack(ModItems.PALE_SWORD);
    }

    public MaelstromEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntityTypes.MAELSTROM_ENTITY_ENTITY_TYPE, owner, world);
        this.swordStack = stack.copy();
        this.startPos = owner.getPos();
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(RETURNING, false);
        this.dataTracker.startTracking(HOMING, false);
        this.dataTracker.startTracking(REMORSE, false);
        this.dataTracker.startTracking(REMORSE_STUCK, false);
    }

    public boolean isReturning() { return this.dataTracker.get(RETURNING);}
    public void setReturning(boolean v) { this.dataTracker.set(RETURNING, v);}
    public boolean isHoming() { return this.dataTracker.get(HOMING);}
    public void setHoming(boolean v){ this.dataTracker.set(HOMING, v);}
    public boolean isRemorse() { return this.dataTracker.get(REMORSE);}
    public void setRemorse(boolean v){ this.dataTracker.set(REMORSE, v);}
    public boolean isRemorseStuck() { return this.dataTracker.get(REMORSE_STUCK);}
    public void setRemorseStuck(boolean v){ this.dataTracker.set(REMORSE_STUCK, v);}

    public void setHomingTargetUUID(UUID uuid) { this.homingTargetUUID = uuid; }

    public static MaelstromEntity getActiveRemorseEntity(UUID ownerUUID) {
        MaelstromEntity entity = activeRemorseEntities.get(ownerUUID);
        if (entity != null && entity.isRemoved()) {
            activeRemorseEntities.remove(ownerUUID);
            return null;
        }
        return entity;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.getOwner() != null) {
            activeRemorseEntities.remove(this.getOwner().getUuid());
        }
        super.remove(reason);
    }

    public void triggerForcefulRecall(PlayerEntity player) {
        if (this.getWorld().isClient) return;
        LivingEntity target = findLivingEntityByUUID(stuckTargetUUID);
        if (target != null && !target.isRemoved()) {
            target.damage(this.getDamageSources().trident(this, player), 2.0F);
            target.addStatusEffect(new StatusEffectInstance(
                    ModEffects.BLEEDING, 200, 0, false, true, true));
        }
        setRemorseStuck(false);
        setReturning(true);
        this.noClip = true;
    }

    public void triggerTpRecall(PlayerEntity player) {
        if (this.getWorld().isClient) return;
        LivingEntity target = findLivingEntityByUUID(stuckTargetUUID);
        if (target != null && !target.isRemoved()) {
            player.teleport(target.getX(), target.getY(), target.getZ());
            target.damage(this.getDamageSources().trident(this, player), 2.0F);
        }
        setRemorseStuck(false);
        setReturning(true);
        this.noClip = true;
    }

    @Override
    public void tick() {
        if (startPos == null) {
            startPos = this.getPos();
        }

        if (!this.getWorld().isClient && isRemorse() && this.getOwner() instanceof PlayerEntity owner) {
            activeRemorseEntities.putIfAbsent(owner.getUuid(), this);
        }

        if (isRemorseStuck()) {
            this.noClip = true;
            this.inGround = false;
            this.setVelocity(Vec3d.ZERO);

            if (!this.getWorld().isClient) {
                if (this.getOwner() instanceof PlayerEntity owner) {
                    activeRemorseEntities.putIfAbsent(owner.getUuid(), this);
                }

                LivingEntity stuckTarget = findLivingEntityByUUID(stuckTargetUUID);
                if (stuckTarget == null || stuckTarget.isRemoved() || stuckTarget.isDead()) {
                    setRemorseStuck(false);
                    setReturning(true);
                } else {
                    this.setPosition(
                            stuckTarget.getX(),
                            stuckTarget.getY() + stuckTarget.getHeight() * 0.5,
                            stuckTarget.getZ()
                    );
                    remorseTimer++;
                    if (remorseTimer >= 600) { // 30 seconds
                        setRemorseStuck(false);
                        setReturning(true);
                    }
                }
            }

            super.tick();
            return;
        }
        this.inGround = false;
        ticksInAir++;

        if (isHoming() && !isReturning() && !this.getWorld().isClient) {
            applyHomingGuidance();
        }

        int maxDist  = isHoming() ? 50 : 35;
        int maxTicks = isHoming() ? 100 : 40;
        if (!isReturning() && startPos != null &&
                (distanceTo(startPos) > maxDist || ticksInAir > maxTicks)) {
            setReturning(true);
        }

        if (isReturning()) {
            this.noClip = true;
        }

        if (isReturning() && this.getOwner() instanceof PlayerEntity owner) {
            if (!this.getWorld().isClient) {
                float returnDamage = isHoming() ? 4.0F : 8.0F;

                this.getWorld()
                        .getOtherEntities(this, this.getBoundingBox().expand(0.5),
                                e -> e instanceof LivingEntity && e != this.getOwner())
                        .forEach(e -> e.damage(
                                this.getDamageSources().trident(this, this.getOwner()),
                                returnDamage));

                Vec3d ownerPos = owner.getPos().add(0, owner.getStandingEyeHeight() / 2.0, 0);
                Vec3d direction = ownerPos.subtract(this.getPos()).normalize();
                this.setVelocity(direction.multiply(0.8));

                if (this.distanceTo(owner) < 2.0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 1.0F, 1.0F);
                    if (!isRemorse()) {
                        if (!owner.getInventory().insertStack(swordStack)) {
                            owner.dropItem(swordStack, false);
                        }
                    } else {
                        for (int i = 0; i < owner.getInventory().size(); i++) {
                            ItemStack s = owner.getInventory().getStack(i);
                            if (s.hasNbt() && s.getNbt().getBoolean("RemorseActive")) {
                                s.getNbt().remove("RemorseActive");
                                owner.getItemCooldownManager().set(s.getItem(), 100);
                                break;
                            }
                        }
                    }
                    this.discard();
                    return;
                }
            }
        }

        super.tick();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (this.getWorld().isClient) return;

        Entity hit = entityHitResult.getEntity();
        if (!(hit instanceof LivingEntity target) || hit == this.getOwner()) return;

        if (isRemorse() && !isReturning() && !isRemorseStuck()) {
            target.damage(this.getDamageSources().trident(this, this.getOwner()), 5.0F);
            stuckTargetUUID = target.getUuid();
            setRemorseStuck(true);
            this.setVelocity(Vec3d.ZERO);
            this.noClip = true;
            if (this.getOwner() != null) {
                activeRemorseEntities.put(this.getOwner().getUuid(), this);
            }
            return;
        }

        if (isHoming() && !isReturning()) {
            target.damage(this.getDamageSources().trident(this, this.getOwner()), 4.0F);
            target.addStatusEffect(new StatusEffectInstance(
                    ModEffects.PINNING, 300, 0, false, true, true));
            setReturning(true);
            this.noClip = true;
            return;
        }

        target.damage(this.getDamageSources().trident(this, this.getOwner()), 8.0F);

        if (isReturning()) {
            this.noClip = true;
        } else {
            this.noClip = false;
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (isReturning() || isRemorseStuck()) return;
        this.setVelocity(this.getVelocity().multiply(-0.25));
        setReturning(true);
    }

    private void applyHomingGuidance() {
        Entity target = findEntityByUUID(homingTargetUUID);
        if (!(target instanceof LivingEntity livingTarget) || target.isRemoved()) return;
        if (distanceTo(target.getPos()) > 50) return;

        Vec3d toTarget = target.getPos()
                .add(0, livingTarget.getHeight() / 2.0, 0)
                .subtract(this.getPos())
                .normalize();

        Vec3d currentVel = this.getVelocity();
        double speed = currentVel.length();
        if (speed < 0.01) return;

        Vec3d newDir = currentVel.normalize().multiply(0.85).add(toTarget.multiply(0.15)).normalize();
        this.setVelocity(newDir.multiply(speed));
    }

    private Entity findEntityByUUID(UUID uuid) {
        if (uuid == null) return null;
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            return serverWorld.getEntity(uuid);
        }
        return null;
    }

    private LivingEntity findLivingEntityByUUID(UUID uuid) {
        Entity e = findEntityByUUID(uuid);
        return e instanceof LivingEntity living ? living : null;
    }

    private double distanceTo(Vec3d pos) {
        return this.getPos().distanceTo(pos);
    }

    @Override
    public boolean shouldRender(double distance) { return true; }

    @Override
    protected ItemStack asItemStack() { return swordStack.copy(); }

    @Override
    protected boolean tryPickup(PlayerEntity player) { return false; }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("SwordStack", swordStack.writeNbt(new NbtCompound()));
        nbt.putBoolean("Returning", isReturning());
        nbt.putBoolean("Homing", isHoming());
        nbt.putBoolean("Remorse", isRemorse());
        nbt.putBoolean("RemorseStuck", isRemorseStuck());
        nbt.putInt("TicksInAir", ticksInAir);
        nbt.putInt("RemorseTimer", remorseTimer);
        if (startPos != null) {
            nbt.putDouble("StartX", startPos.x);
            nbt.putDouble("StartY", startPos.y);
            nbt.putDouble("StartZ", startPos.z);
        }
        if (homingTargetUUID != null) nbt.putUuid("HomingTargetUUID", homingTargetUUID);
        if (stuckTargetUUID != null)  nbt.putUuid("StuckTargetUUID", stuckTargetUUID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("SwordStack")) {
            this.swordStack = ItemStack.fromNbt(nbt.getCompound("SwordStack"));
        }
        setReturning(nbt.getBoolean("Returning"));
        setHoming(nbt.getBoolean("Homing"));
        setRemorse(nbt.getBoolean("Remorse"));
        setRemorseStuck(nbt.getBoolean("RemorseStuck"));
        ticksInAir = nbt.getInt("TicksInAir");
        remorseTimer = nbt.getInt("RemorseTimer");
        if (nbt.contains("StartX")) {
            this.startPos = new Vec3d(
            nbt.getDouble("StartX"),
            nbt.getDouble("StartY"),
            nbt.getDouble("StartZ"));
        }
        if (nbt.containsUuid("HomingTargetUUID")) homingTargetUUID = nbt.getUuid("HomingTargetUUID");
        if (nbt.containsUuid("StuckTargetUUID"))  stuckTargetUUID  = nbt.getUuid("StuckTargetUUID");
    }
}