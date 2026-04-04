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
        if (source.getTypeRegistryEntry().matchesKey(ModDamageTypes.SOUL_DAMAGE)) return;

        LivingEntity entity = (LivingEntity) (Object) this;

        if (source.getAttacker() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.getItem() instanceof GlaiveItem) {
                cir.setReturnValue(false);

                float cooldown = player.getAttackCooldownProgress(0.5f);
                float damage = 3.0F * cooldown;

                if (damage > 0.1f) {
                    DamageSource soulDamage = entity.getDamageSources().create(ModDamageTypes.SOUL_DAMAGE, player);
                    entity.damage(soulDamage, damage);
                    cir.setReturnValue(true);
                }
            }
        }
    }
}