package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.custom.AshChargeItem;
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
    private void masksnglory$cancelAshChargeFallDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getWorld().isClient && source.equals(this.getWorld().getDamageSources().fall())) {
            if ((Object) this instanceof PlayerEntity player && AshChargeItem.isFallImmune(player)) {
                AshChargeItem.onPlayerLanded(player);
                cir.setReturnValue(false);
            }
        }
    }
}
