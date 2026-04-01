package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import net.vainnglory.masksnglory.item.custom.AshChargeItem;
import net.vainnglory.masksnglory.util.MaskAbilityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FallDamageMixin extends Entity {

    public FallDamageMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void masksnglory$cancelAshChargeFallDamage(DamageSource source, float amount,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (!this.getWorld().isClient && source.equals(this.getWorld().getDamageSources().fall())) {
            if ((Object) this instanceof PlayerEntity player && AshChargeItem.isFallImmune(player)) {
                AshChargeItem.onPlayerLanded(player);
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void masksnglory$hsAcrobat(DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient) return;
        if (!source.equals(this.getWorld().getDamageSources().fall())) return;
        if (!((Object) this instanceof PlayerEntity player)) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.HSSHARD) return;

        float damagePortion = Math.min(5f, amount * 0.5f);
        float knockbackMult = 0.4f + amount * 0.06f;

        Box area = new Box(player.getBlockPos()).expand(4.0);
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player)) {
            Vec3d dir = entity.getPos().subtract(player.getPos()).normalize();
            entity.setVelocity(entity.getVelocity().add(dir.x * knockbackMult, 0.4, dir.z * knockbackMult));
            if (entity instanceof ServerPlayerEntity targetSp) {
                targetSp.velocityModified = true;
            }
            entity.damage(world.getDamageSources().fall(), damagePortion);
        }

        cir.setReturnValue(false);
    }
}

