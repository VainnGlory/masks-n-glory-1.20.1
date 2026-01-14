package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.NuggetItem;

public class NuggetEntity extends ThrownItemEntity {
    public NuggetEntity(EntityType<? extends SnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public NuggetEntity(World world, LivingEntity owner) {
        super(EntityType.SNOWBALL, owner, world);
    }

    public NuggetEntity(World world, double x, double y, double z) {
        super(EntityType.SNOWBALL, x, y, z, world);
    }

    protected Item getDefaultItem() {
        return ModItems.NUGGET;
    }

    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getItem();
        return (itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack));
    }

    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();

            for(int i = 0; i < 8; ++i) {
                this.getWorld().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if(getStack().getItem() instanceof NuggetItem)
            NuggetItem.doItemExplosion(getStack(), getWorld(), entityHitResult.getEntity());
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            if(getStack().getItem() instanceof NuggetItem)
                NuggetItem.doItemExplosion(getStack(), getWorld(), this);
            this.getWorld().sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }
}
