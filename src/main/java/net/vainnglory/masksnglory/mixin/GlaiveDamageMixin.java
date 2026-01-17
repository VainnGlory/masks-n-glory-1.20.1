package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.vainnglory.masksnglory.item.custom.GlaiveItem;
import net.vainnglory.masksnglory.util.ModDamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class GlaiveDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;


        if (source.getAttacker() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.getItem() instanceof GlaiveItem glaive) {

                cir.setReturnValue(false);


                float vanillaDamageWithCooldown = amount;


                float cooldownMultiplier = Math.min(1.0F, vanillaDamageWithCooldown / 1.0F);
                float damage = 6.0F * cooldownMultiplier;


                entity.damage(entity.getDamageSources().create(ModDamageTypes.SOUL_DAMAGE), damage);
                cir.setReturnValue(true);
            }
        }
    }
}