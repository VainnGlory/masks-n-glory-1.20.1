package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModItems;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
    public abstract class EffectMixin extends Entity {
    public EffectMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", cancellable = true)
    private void addStatusEffect(StatusEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;


        if (entity instanceof PlayerEntity player) {
            boolean fullPale =
                    player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.PALE_HELMET) &&
                            player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.PALE_CHESTPLATE) &&
                            player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.PALE_LEGGINGS) &&
                            player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.PALE_BOOTS);

            if (fullPale) {
                StatusEffectInstance newEffect = new StatusEffectInstance(
                        effect.getEffectType(),
                        effect.getDuration(),
                        Math.min(effect.getAmplifier() + 1, 255),
                        effect.isAmbient(),
                        effect.shouldShowParticles(),
                        effect.shouldShowIcon()
                );

                boolean applied = entity.getActiveStatusEffects().put(effect.getEffectType(), newEffect) == null;

                if (applied) {
                    ((LivingEntityAccessor) player).invokeOnStatusEffectApplied(newEffect, source);
                    cir.setReturnValue(true);
                } else if (effect.upgrade(effect)) {
                    ((LivingEntityAccessor) player).invokeOnStatusEffectUpgraded(newEffect, true, source);
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}

