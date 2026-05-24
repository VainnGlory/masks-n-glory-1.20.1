package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.util.ModDamageTypes;

import java.util.*;

public class ShearProjectileEntity extends Entity {

    private static final int MAX_LIFETIME = 100;
    private static final float GRAVITY = 0.01f;
    private static final float ORBIT_RADIUS = 2.5f;
    private static final float ORBIT_SPEED = 0.18f;
    private static final int ORBIT_DURATION = 200;
    private static final int ORBIT_HIT_COOLDOWN = 10;
    private static final int ROD_CHECK_INTERVAL = 2;
    private static final float PROJECTILE_DAMAGE = 3.5f;
    private static final float ORBIT_DAMAGE = 2.5f;

    private Vec3d velocity = Vec3d.ZERO;
    private int lifetime = 0;
    private UUID ownerUUID = null;

    private BlockPos orbitPos = null;
    private float orbitAngle = 0f;
    private int orbitTicksLeft = 0;

    private final Set<UUID> hitThisFlight = new HashSet<>();
    private final Map<UUID, Integer> orbitHitCooldowns = new HashMap<>();

    public ShearProjectileEntity(EntityType<? extends ShearProjectileEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void setOwner(PlayerEntity player) {
        this.ownerUUID = player.getUuid();
    }

    public void setShootingVelocity(Vec3d vel) {
        this.velocity = vel;
    }

    private PlayerEntity getOwnerPlayer() {
        if (ownerUUID == null || this.getWorld().isClient) return null;
        return ((ServerWorld) this.getWorld()).getServer().getPlayerManager().getPlayerList()
                .stream().filter(p -> p.getUuid().equals(ownerUUID)).findFirst().orElse(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        lifetime++;

        if (orbitPos != null) {
            tickOrbit();
            return;
        }

        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (lifetime % ROD_CHECK_INTERVAL == 0) {
            checkForLightningRod();
            if (orbitPos != null) return;
        }

        velocity = new Vec3d(velocity.x, velocity.y - GRAVITY, velocity.z);
        this.setVelocity(velocity);

        Vec3d pos = this.getPos();
        Vec3d nextPos = pos.add(velocity);

        if (collidesWithBlock(pos, nextPos)) {
            this.discard();
            return;
        }

        Box sweepBox = new Box(
                Math.min(pos.x, nextPos.x) - 0.25, Math.min(pos.y, nextPos.y) - 0.25, Math.min(pos.z, nextPos.z) - 0.25,
                Math.max(pos.x, nextPos.x) + 0.25, Math.max(pos.y, nextPos.y) + 0.25, Math.max(pos.z, nextPos.z) + 0.25
        );
        PlayerEntity owner = getOwnerPlayer();
        List<LivingEntity> targets = this.getWorld().getNonSpectatingEntities(LivingEntity.class, sweepBox);
        for (LivingEntity target : targets) {
            if (ownerUUID != null && target.getUuid().equals(ownerUUID)) continue;
            if (hitThisFlight.contains(target.getUuid())) continue;
            hitThisFlight.add(target.getUuid());
            applyHit(target, owner);
        }

        this.setPosition(nextPos);

        double dx = velocity.x;
        double dz = velocity.z;
        if (dx != 0 || dz != 0) {
            this.setYaw((float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90f);
        }
    }

    private static boolean isPassable(BlockState state) {
        if (state.isAir()) return true;
        if (state.isIn(BlockTags.LEAVES)) return true;
        if (state.isIn(BlockTags.SMALL_FLOWERS)) return true;
        if (state.isIn(BlockTags.TALL_FLOWERS)) return true;
        if (state.isOf(Blocks.GRASS) || state.isOf(Blocks.TALL_GRASS)) return true;
        if (state.isOf(Blocks.FERN) || state.isOf(Blocks.LARGE_FERN)) return true;
        return false;
    }

    private boolean collidesWithBlock(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from);
        double len = dir.length();
        if (len == 0) return false;
        Vec3d step = dir.normalize().multiply(0.3);
        int steps = (int) Math.ceil(len / 0.3);
        Vec3d start = from.add(dir.normalize().multiply(0.1));
        for (int i = 0; i <= steps; i++) {
            Vec3d point = (i == steps) ? to : start.add(step.multiply(i));
            BlockState state = this.getWorld().getBlockState(BlockPos.ofFloored(point));
            if (!isPassable(state) && !state.getCollisionShape(this.getWorld(), BlockPos.ofFloored(point)).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void applyHit(LivingEntity target, PlayerEntity owner) {
        World w = this.getWorld();
        target.hurtTime = 0;
        target.timeUntilRegen = 0;
        DamageSource source = owner != null
                ? ModDamageTypes.shearProjectile(owner, target)
                : ModDamageTypes.shearProjectile(target);
        target.damage(source, orbitPos == null ? PROJECTILE_DAMAGE : ORBIT_DAMAGE);
        target.addStatusEffect(new StatusEffectInstance(ModEffects.BLEEDING, 100, 0, false, true, true));
        w.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS,
                1.0f, 0.9f + w.random.nextFloat() * 0.2f);
    }

    private void checkForLightningRod() {
        World w = this.getWorld();
        BlockPos myPos = this.getBlockPos();
        for (BlockPos check : BlockPos.iterateOutwards(myPos, 5, 5, 5)) {
            if (w.getBlockState(check).isOf(Blocks.LIGHTNING_ROD)) {
                BlockPos rodPos = check.toImmutable();
                long count = w.getEntitiesByClass(
                        ShearProjectileEntity.class,
                        new Box(check).expand(ORBIT_RADIUS + 1.0),
                        e -> rodPos.equals(e.orbitPos)
                ).size();
                if (count >= 20) continue;

                orbitPos = rodPos;
                orbitAngle = (float) Math.atan2(getZ() - (check.getZ() + 0.5), getX() - (check.getX() + 0.5));
                orbitTicksLeft = ORBIT_DURATION;
                velocity = Vec3d.ZERO;
                this.setVelocity(Vec3d.ZERO);
                return;
            }
        }
    }

    private void tickOrbit() {
        World w = this.getWorld();

        if (!w.getBlockState(orbitPos).isOf(Blocks.LIGHTNING_ROD)) {
            this.discard();
            return;
        }

        orbitAngle += ORBIT_SPEED;
        double cx = orbitPos.getX() + 0.5;
        double cy = orbitPos.getY() + 1.0;
        double cz = orbitPos.getZ() + 0.5;

        double newX = cx + Math.cos(orbitAngle) * ORBIT_RADIUS;
        double newZ = cz + Math.sin(orbitAngle) * ORBIT_RADIUS;
        this.setPosition(newX, cy, newZ);
        this.setYaw((float) (orbitAngle * (180.0 / Math.PI)));

        Box hitBox = new Box(newX - 0.6, cy - 0.6, newZ - 0.6, newX + 0.6, cy + 0.6, newZ + 0.6);
        PlayerEntity owner = getOwnerPlayer();
        List<LivingEntity> nearby = w.getNonSpectatingEntities(LivingEntity.class, hitBox);
        for (LivingEntity target : nearby) {
            if (ownerUUID != null && target.getUuid().equals(ownerUUID)) continue;
            int cd = orbitHitCooldowns.getOrDefault(target.getUuid(), 0);
            if (cd > 0) {
                orbitHitCooldowns.put(target.getUuid(), cd - 1);
                continue;
            }
            applyHit(target, owner);
            orbitHitCooldowns.put(target.getUuid(), ORBIT_HIT_COOLDOWN);
        }

        if (w instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, newX, cy, newZ, 1, 0, 0, 0, 0);
        }

        BlockPos below = orbitPos.down();
        if (!w.getBlockState(below).isOf(Blocks.REDSTONE_BLOCK)) {
            orbitTicksLeft--;
            if (orbitTicksLeft <= 0) this.discard();
        }
    }

    @Override
    protected void initDataTracker() {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        velocity = new Vec3d(nbt.getDouble("VelX"), nbt.getDouble("VelY"), nbt.getDouble("VelZ"));
        lifetime = nbt.getInt("Lifetime");
        if (nbt.containsUuid("OwnerUUID")) ownerUUID = nbt.getUuid("OwnerUUID");
        if (nbt.contains("OrbitX")) {
            orbitPos = new BlockPos(nbt.getInt("OrbitX"), nbt.getInt("OrbitY"), nbt.getInt("OrbitZ"));
            orbitAngle = nbt.getFloat("OrbitAngle");
            orbitTicksLeft = nbt.getInt("OrbitTicksLeft");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putDouble("VelX", velocity.x);
        nbt.putDouble("VelY", velocity.y);
        nbt.putDouble("VelZ", velocity.z);
        nbt.putInt("Lifetime", lifetime);
        if (ownerUUID != null) nbt.putUuid("OwnerUUID", ownerUUID);
        if (orbitPos != null) {
            nbt.putInt("OrbitX", orbitPos.getX());
            nbt.putInt("OrbitY", orbitPos.getY());
            nbt.putInt("OrbitZ", orbitPos.getZ());
            nbt.putFloat("OrbitAngle", orbitAngle);
            nbt.putInt("OrbitTicksLeft", orbitTicksLeft);
        }
    }
}