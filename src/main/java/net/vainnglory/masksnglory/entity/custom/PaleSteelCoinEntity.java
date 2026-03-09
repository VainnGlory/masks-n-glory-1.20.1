package net.vainnglory.masksnglory.entity.custom;

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

