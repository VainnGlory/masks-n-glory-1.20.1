package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.util.ModDamageTypes;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SoulProjectileEntity extends PersistentProjectileEntity {

    private UUID targetUUID;
    private int lifetime = 0;

    public SoulProjectileEntity(EntityType<? extends SoulProjectileEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public SoulProjectileEntity(World world, LivingEntity owner, UUID targetUUID) {
        super(ModEntityTypes.SOUL_PROJECTILE_TYPE, owner, world);
        this.targetUUID = targetUUID;
        this.setNoGravity(true);
    }

    public void setTargetUUID(UUID uuid) { this.targetUUID = uuid; }

    @Override
    public void tick() {
        this.inGround = false;
        lifetime++;

        if (getWorld().isClient) {
            super.tick();
            return;
        }

        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                    getX(), getY(), getZ(),
                    2, 0.1, 0.1, 0.1, 0.02);
        }

        LivingEntity target = findTarget();
        if (target != null) {
            Vec3d toTarget = target.getPos().add(0, target.getHeight() / 2.0, 0)
                    .subtract(getPos()).normalize();
            Vec3d current = getVelocity().normalize();
            Vec3d steered = current.multiply(0.8).add(toTarget.multiply(0.2)).normalize().multiply(1.4);
            setVelocity(steered);
        }

        if (lifetime > 80) {
            discard();
            return;
        }

        super.tick();
    }

    private LivingEntity findTarget() {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return null;

        if (targetUUID != null) {
            Entity stored = serverWorld.getEntity(targetUUID);
            if (stored instanceof LivingEntity living && !living.isDead() && !living.isRemoved()) {
                return living;
            }
        }

        Vec3d pos = getPos();
        List<PlayerEntity> players = getWorld().getEntitiesByClass(PlayerEntity.class,
                getBoundingBox().expand(30), p -> p != getOwner() && !p.isDead());
        if (!players.isEmpty()) {
            return players.stream().min(Comparator.comparingDouble(p -> p.squaredDistanceTo(pos))).orElse(null);
        }

        List<LivingEntity> entities = getWorld().getEntitiesByClass(LivingEntity.class,
                getBoundingBox().expand(30), e -> e != getOwner() && !e.isDead() && !(e instanceof PlayerEntity));
        return entities.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(pos))).orElse(null);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (getWorld().isClient) return;
        Entity hit = entityHitResult.getEntity();
        if (hit == getOwner() || !(hit instanceof LivingEntity target)) return;

        target.damage(target.getDamageSources().create(ModDamageTypes.SOUL_DAMAGE, getOwner()), 4.0f);
        discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        discard();
    }

    @Override
    public boolean shouldRender(double distance) { return false; }

    @Override
    protected ItemStack asItemStack() { return new ItemStack(Items.AIR); }

    @Override
    protected boolean tryPickup(PlayerEntity player) { return false; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Lifetime", lifetime);
        if (targetUUID != null) nbt.putUuid("TargetUUID", targetUUID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        lifetime = nbt.getInt("Lifetime");
        if (nbt.containsUuid("TargetUUID")) targetUUID = nbt.getUuid("TargetUUID");
    }
}
