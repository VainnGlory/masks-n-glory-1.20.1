package net.vainnglory.masksnglory.entity.custom;

import net.vainnglory.masksnglory.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MaelstromEntity extends PersistentProjectileEntity {
    private static final TrackedData<Boolean> RETURNING = DataTracker.registerData(MaelstromEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int MAX_DISTANCE = 35; // blocks before returning
    private ItemStack swordStack;
    private Vec3d startPos;
    private int ticksInAir = 0;

    public MaelstromEntity(EntityType<? extends MaelstromEntity> entityType, World world) {
        super(entityType, world);
        this.swordStack = new ItemStack(ModItems.MAELSTROM);
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
    }

    public boolean isReturning() {
        return this.dataTracker.get(RETURNING);
    }

    public void setReturning(boolean returning) {
        this.dataTracker.set(RETURNING, returning);
    }

    @Override
    public void tick() {


        if (startPos == null) {
            startPos = this.getPos();
        }

        this.inGround = false;

        if (isReturning()) {
            this.noClip = true;
        }

        ticksInAir++;


        if (!isReturning() && startPos != null && (this.distanceTo(startPos) > MAX_DISTANCE || ticksInAir > 40)) {
            setReturning(true);
        }


        if (isReturning() && this.getOwner() instanceof PlayerEntity owner) {
            if (!this.getWorld().isClient) {

                Vec3d currentPos = this.getPos();
                this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.5),
                                entity -> entity instanceof LivingEntity && entity != this.getOwner())
                        .forEach(entity -> {
                            entity.damage(this.getDamageSources().trident(this, this.getOwner()), 8.0F);
                        });

                Vec3d ownerPos = owner.getPos().add(0, owner.getStandingEyeHeight() / 2, 0);

                Vec3d direction = ownerPos.subtract(currentPos).normalize();

                double speed = 0.8;
                this.setVelocity(direction.multiply(speed));


                if (this.distanceTo(owner) < 2.0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 1.0F, 1.0F);
                    if (!owner.getInventory().insertStack(swordStack)) {
                        owner.dropItem(swordStack, false);
                    }
                    this.discard();
                    return;
                }
            }
        }

        super.tick();
    }

    private double distanceTo(Vec3d pos) {
        return this.getPos().distanceTo(pos);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!this.getWorld().isClient) {

            entityHitResult.getEntity().damage(this.getDamageSources().trident(this, this.getOwner()), 8.0F);

            if (isReturning() && this.getOwner() instanceof PlayerEntity owner) {

                this.noClip = true;

            } else {

                this.noClip = false;
            }
        }
    }

    @Override
    protected void onBlockHit(net.minecraft.util.hit.BlockHitResult blockHitResult) {

        if (isReturning()) {
            return;
        }

        this.setVelocity(this.getVelocity().multiply(-0.25));
        setReturning(true);
    }

    @Override
    public boolean shouldRender(double distance) {

        return true;
    }

    @Override
    protected ItemStack asItemStack() {
        return swordStack.copy();
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("SwordStack", swordStack.writeNbt(new NbtCompound()));
        nbt.putBoolean("Returning", isReturning());
        nbt.putInt("TicksInAir", ticksInAir);
        if (startPos != null) {
            nbt.putDouble("StartX", startPos.x);
            nbt.putDouble("StartY", startPos.y);
            nbt.putDouble("StartZ", startPos.z);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("SwordStack")) {
            this.swordStack = ItemStack.fromNbt(nbt.getCompound("SwordStack"));
        }
        setReturning(nbt.getBoolean("Returning"));
        ticksInAir = nbt.getInt("TicksInAir");
        if (nbt.contains("StartX")) {
            this.startPos = new Vec3d(
                    nbt.getDouble("StartX"),
                    nbt.getDouble("StartY"),
                    nbt.getDouble("StartZ")
            );
        }
    }
}