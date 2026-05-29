package net.vainnglory.masksnglory.entity.custom;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModItems;

public class PaleSteelCoinEntity extends Entity {

    private static final double GRAVITY = 0.015;
    private static final double DRAG = 0.99;
    private final Deque<Vec3d> trailPositions = new ArrayDeque<>();
    private static final int TRAIL_MAX = 24;

    public PaleSteelCoinEntity(EntityType<? extends PaleSteelCoinEntity> type, World world) {
        super(type, world);
    }

    public PaleSteelCoinEntity(World world, LivingEntity thrower) {
        super(ModEntityTypes.PALE_STEEL_COIN_ENTITY_TYPE, world);
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (this.getWorld().isClient) {
            trailPositions.addFirst(this.getPos());
            if (trailPositions.size() > TRAIL_MAX) trailPositions.removeLast();
        }
        super.tick();

        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.x * DRAG, vel.y - GRAVITY, vel.z * DRAG);

        this.move(MovementType.SELF, this.getVelocity());

        if (!this.getWorld().isClient) {
            if (this.isOnGround()) {
                dropCoin();
                return;
            }
            if (this.age > 600) {
                dropCoin();
            }
        }
    }

    public List<Vec3d> getTrailPositions() {
        return new ArrayList<>(trailPositions);
    }

    private void dropCoin() {
        ItemEntity itemEntity = new ItemEntity(
                this.getWorld(),
                this.getX(), this.getY(), this.getZ(),
                new ItemStack(ModItems.PALE_STEEL_COIN)
        );
        itemEntity.setPickupDelay(10);
        this.getWorld().spawnEntity(itemEntity);
        this.discard();
    }
}

