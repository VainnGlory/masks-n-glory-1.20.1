package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.custom.AmalgamItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class AmalgamNullImmunityMixin {

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockNullEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect.getEffectType() != ModEffects.NULL_EFFECT) return;
        if (!((Object)this instanceof PlayerEntity player)) return;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() instanceof AmalgamItem && stack.getDamage() < stack.getMaxDamage()) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}