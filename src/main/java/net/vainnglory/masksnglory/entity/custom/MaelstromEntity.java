package net.vainnglory.masksnglory.entity.custom;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private static final TrackedData<Boolean> SURGE =
            DataTracker.registerData(MaelstromEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final Map<UUID, MaelstromEntity> activeRemorseEntities = new HashMap<>();
    private static final Map<UUID, MaelstromEntity> activeSurgeEntities = new HashMap<>();
    private final Deque<Vec3d> trailPositions = new ArrayDeque<>();
    private static final int TRAIL_MAX = 24;

    private ItemStack swordStack;
    private Vec3d startPos;
    private int ticksInAir = 0;

    private UUID homingTargetUUID;

    private UUID stuckTargetUUID;
    private int remorseTimer = 0;
    private int boostCooldown = 0;
    private int ownerSelectedSlot = -1;

    private final Set<UUID> nearbyFireworkIds = new HashSet<>();

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
        this.dataTracker.startTracking(SURGE, false);
    }

    public boolean isReturning() { return this.dataTracker.get(RETURNING); }
    public void setReturning(boolean v) { this.dataTracker.set(RETURNING, v); }
    public boolean isHoming() { return this.dataTracker.get(HOMING); }
    public void setHoming(boolean v) { this.dataTracker.set(HOMING, v); }
    public boolean isRemorse() { return this.dataTracker.get(REMORSE); }
    public void setRemorse(boolean v) { this.dataTracker.set(REMORSE, v); }
    public boolean isRemorseStuck() { return this.dataTracker.get(REMORSE_STUCK); }
    public void setRemorseStuck(boolean v) { this.dataTracker.set(REMORSE_STUCK, v); }
    public boolean isSurge() { return this.dataTracker.get(SURGE); }
    public void setSurge(boolean v) { this.dataTracker.set(SURGE, v); }

    public void setHomingTargetUUID(UUID uuid) { this.homingTargetUUID = uuid; }
    public void setOwnerSelectedSlot(int slot) { this.ownerSelectedSlot = slot; }

    public static MaelstromEntity getActiveRemorseEntity(UUID ownerUUID) {
        MaelstromEntity entity = activeRemorseEntities.get(ownerUUID);
        if (entity != null && entity.isRemoved()) {
            activeRemorseEntities.remove(ownerUUID);
            return null;
        }
        return entity;
    }

    public static MaelstromEntity getActiveSurgeEntity(UUID ownerUUID) {
        MaelstromEntity entity = activeSurgeEntities.get(ownerUUID);
        if (entity != null && entity.isRemoved()) {
            activeSurgeEntities.remove(ownerUUID);
            return null;
        }
        return entity;
    }

    public List<Vec3d> getTrailPositions() {
        return new ArrayList<>(trailPositions);
    }

    public static void registerSurgeEntity(UUID ownerUUID, MaelstromEntity entity) {
        activeSurgeEntities.put(ownerUUID, entity);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.getOwner() != null) {
            activeRemorseEntities.remove(this.getOwner().getUuid());
            activeSurgeEntities.remove(this.getOwner().getUuid());
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

    public boolean triggerBoost(PlayerEntity player) {
        if (this.getWorld().isClient) return false;
        if (boostCooldown > 0) return false;

        Vec3d lookDir = player.getRotationVec(1.0F);
        Vec3d boostDir = lookDir;

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Vec3d eyePos = player.getEyePos();
            LivingEntity nearest = null;
            double bestDot = Math.cos(Math.toRadians(30));

            for (LivingEntity e : serverWorld.getEntitiesByClass(LivingEntity.class,
                    new Box(eyePos.x, eyePos.y, eyePos.z, eyePos.x, eyePos.y, eyePos.z).expand(20),
                    e -> e != player && !e.isDead())) {
                Vec3d toEntity = e.getEyePos().subtract(eyePos).normalize();
                double dot = toEntity.dotProduct(lookDir);
                if (dot > bestDot) {
                    bestDot = dot;
                    nearest = e;
                }
            }

            if (nearest != null) {
                Vec3d targetCenter = nearest.getPos().add(0, nearest.getHeight() * 0.5, 0);
                boostDir = targetCenter.subtract(this.getPos()).normalize();
            }
        }

        double boostSpeed = Math.min(Math.max(this.getVelocity().length(), 2.5) + 1.0, 4.0);
        this.setVelocity(boostDir.multiply(boostSpeed));
        this.noClip = false;
        setReturning(false);

        ticksInAir = 0;
        boostCooldown = 40;

        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 0.8F, 1.5F);

        return true;
    }

    @Override
    public void tick() {
        if (this.getWorld().isClient) {
            trailPositions.addFirst(this.getPos());
            if (trailPositions.size() > TRAIL_MAX) trailPositions.removeLast();
        }
        if (startPos == null) {
            startPos = this.getPos();
        }

        if (!this.getWorld().isClient && isRemorse() && this.getOwner() instanceof PlayerEntity owner) {
            activeRemorseEntities.putIfAbsent(owner.getUuid(), this);
        }

        if (!this.getWorld().isClient && isSurge() && this.getOwner() instanceof PlayerEntity owner) {
            activeSurgeEntities.putIfAbsent(owner.getUuid(), this);

            if (ticksInAir == 0 && owner instanceof ServerPlayerEntity spe && ownerSelectedSlot >= 0) {
                ItemStack phantom = swordStack.copy();
                phantom.setCount(1);
                phantom.getOrCreateNbt().putBoolean("SurgeActive", true);
                phantom.getOrCreateNbt().putInt("CustomModelData", 1);
                owner.getInventory().setStack(ownerSelectedSlot, phantom);
                spe.networkHandler.sendPacket(
                        new ScreenHandlerSlotUpdateS2CPacket(-2, 0, ownerSelectedSlot, phantom));
            }

            if (owner.isUsingItem() && owner.getActiveItem().isOf(ModItems.PALE_SWORD)
                    && boostCooldown == 0) {
                triggerBoost(owner);
                owner.stopUsingItem();
            }
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
                    if (remorseTimer >= 600) {
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

        if (boostCooldown > 0) boostCooldown--;

        if (!this.getWorld().isClient && !isReturning() && !isRemorseStuck()) {
            checkForFireworkExplosion();
        }

        if (isHoming() && !isReturning() && !this.getWorld().isClient) {
            applyHomingGuidance();
        }

        if (!isReturning()) {
            if (isSurge() && this.getOwner() instanceof PlayerEntity surgeOwner) {
                if (distanceTo(surgeOwner.getPos()) > 50 || ticksInAir > 200) {
                    setReturning(true);
                }
            } else {
                int maxDist  = isHoming() ? 50 : 35;
                int maxTicks = isHoming() ? 100 : 40;
                if (startPos != null && (distanceTo(startPos) > maxDist || ticksInAir > maxTicks)) {
                    setReturning(true);
                }
            }
        }

        if (isReturning()) {
            this.noClip = true;
        }

        if (isReturning() && !this.getWorld().isClient && this.getOwner() == null) {
            this.discard();
            return;
        }

        if (isSurge() && !isReturning() && this.getOwner() instanceof PlayerEntity surgeOwner) {
            Vec3d lookDir = surgeOwner.getRotationVec(1.0F);
            Vec3d vel = this.getVelocity();
            double speed = vel.length();
            if (speed > 0.1) {
                Vec3d steered = vel.normalize().multiply(0.85).add(lookDir.multiply(0.15)).normalize();
                this.setVelocity(steered.multiply(speed / 0.99));
            }
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
                    if (!isRemorse() && !isSurge()) {
                        if (!owner.getInventory().insertStack(swordStack)) {
                            owner.dropItem(swordStack, false);
                        }
                    } else if (isRemorse()) {
                        for (int i = 0; i < owner.getInventory().size(); i++) {
                            ItemStack s = owner.getInventory().getStack(i);
                            if (s.hasNbt() && s.getNbt().getBoolean("RemorseActive")) {
                                s.getNbt().remove("RemorseActive");
                                owner.getItemCooldownManager().set(s.getItem(), 100);
                                break;
                            }
                        }
                    } else {
                        if (ownerSelectedSlot >= 0) {
                            owner.getInventory().setStack(ownerSelectedSlot, swordStack);
                            owner.getItemCooldownManager().set(swordStack.getItem(), 120);
                            if (owner instanceof ServerPlayerEntity spe) {
                                spe.networkHandler.sendPacket(
                                        new ScreenHandlerSlotUpdateS2CPacket(-2, 0, ownerSelectedSlot, swordStack));
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

    private void activateFireworkHoming() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        Entity target = findEntityByUUID(homingTargetUUID);
        boolean targetInvalid = target == null || target.isRemoved()
                || (target instanceof LivingEntity le && le.isDead());

        if (targetInvalid) {
            Entity owner = this.getOwner();
            double searchX = owner != null ? owner.getX() : this.getX();
            double searchY = owner != null ? owner.getY() : this.getY();
            double searchZ = owner != null ? owner.getZ() : this.getZ();

            PlayerEntity nearestPlayer = serverWorld.getClosestPlayer(
                    searchX, searchY, searchZ, 64,
                    p -> p != this.getOwner() && !p.isSpectator());

            if (nearestPlayer != null) {
                homingTargetUUID = nearestPlayer.getUuid();
            } else {
                LivingEntity nearestMob = null;
                double closestDist = Double.MAX_VALUE;
                for (LivingEntity e : serverWorld.getEntitiesByClass(LivingEntity.class,
                        new Box(searchX, searchY, searchZ, searchX, searchY, searchZ).expand(64),
                        e -> !(e instanceof PlayerEntity) && e != this.getOwner() && !e.isDead())) {
                    double dist = e.squaredDistanceTo(searchX, searchY, searchZ);
                    if (dist < closestDist) {
                        closestDist = dist;
                        nearestMob = e;
                    }
                }
                if (nearestMob != null) homingTargetUUID = nearestMob.getUuid();
            }
        }

        if (homingTargetUUID == null) return;

        setHoming(true);
        startPos = this.getPos();
        ticksInAir = 0;

        Entity finalTarget = findEntityByUUID(homingTargetUUID);
        if (finalTarget != null) {
            double targetY = finalTarget instanceof LivingEntity le
                    ? finalTarget.getY() + le.getHeight() / 2.0
                    : finalTarget.getY();
            Vec3d toTarget = new Vec3d(
                    finalTarget.getX() - this.getX(),
                    targetY - this.getY(),
                    finalTarget.getZ() - this.getZ()
            ).normalize();
            double speed = Math.max(this.getVelocity().length(), 1.5);
            this.setVelocity(toTarget.multiply(speed));
        }

        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ITEM_CROSSBOW_HIT, SoundCategory.PLAYERS, 1.0f, 1.8f);
    }

    private void checkForFireworkExplosion() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        Set<UUID> currentIds = new HashSet<>();
        serverWorld.getEntitiesByClass(
                FireworkRocketEntity.class,
                this.getBoundingBox().expand(6.0),
                f -> f.wasShotAtAngle() && !f.isRemoved()
        ).forEach(f -> currentIds.add(f.getUuid()));

        for (UUID id : nearbyFireworkIds) {
            if (!currentIds.contains(id)) {
                activateFireworkHoming();
                nearbyFireworkIds.clear();
                return;
            }
        }

        nearbyFireworkIds.clear();
        nearbyFireworkIds.addAll(currentIds);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (this.getWorld().isClient) return;

        Entity hit = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        if (!(hit instanceof LivingEntity target) || hit == owner) return;

        if (isRemorse() && !isReturning() && !isRemorseStuck()) {
            target.damage(this.getDamageSources().trident(this, owner), 5.0F);
            stuckTargetUUID = target.getUuid();
            setRemorseStuck(true);
            this.setVelocity(Vec3d.ZERO);
            this.noClip = true;
            if (owner != null) {
                activeRemorseEntities.put(owner.getUuid(), this);
            }
            return;
        }

        if (isHoming() && !isReturning()) {
            target.damage(this.getDamageSources().trident(this, owner), 4.0F);
            target.addStatusEffect(new StatusEffectInstance(
                    ModEffects.PINNING, 300, 0, false, true, true));
            setReturning(true);
            this.noClip = true;
            return;
        }

        if (isSurge() && !isReturning()) {
            target.damage(this.getDamageSources().trident(this, owner), 10.0F);
            setReturning(true);
            this.noClip = true;
            return;
        }

        target.damage(this.getDamageSources().trident(this, owner), 8.0F);

        if (isReturning()) {
            this.noClip = true;
        } else {
            this.noClip = false;
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (this.getWorld().isClient) return;
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
        nbt.putBoolean("Surge", isSurge());
        nbt.putInt("TicksInAir", ticksInAir);
        nbt.putInt("RemorseTimer", remorseTimer);
        nbt.putInt("BoostCooldown", boostCooldown);
        nbt.putInt("OwnerSelectedSlot", ownerSelectedSlot);
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
        setSurge(nbt.getBoolean("Surge"));
        ticksInAir = nbt.getInt("TicksInAir");
        remorseTimer = nbt.getInt("RemorseTimer");
        boostCooldown = nbt.getInt("BoostCooldown");
        if (nbt.contains("OwnerSelectedSlot")) ownerSelectedSlot = nbt.getInt("OwnerSelectedSlot");
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
