package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import net.vainnglory.masksnglory.item.custom.CustomHitSoundItem;
import net.vainnglory.masksnglory.util.ActorManager;
import net.vainnglory.masksnglory.util.MaskAbilityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("WrongEntityDataParameterClass")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract float getAttackCooldownProgress(float baseTime);

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F"))
    private void masksnglory$PlayCustomHitSound(Entity target, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            if (this.getAttackCooldownProgress(0.5F) > 0.9F) {
                if (this.getMainHandStack().getItem() instanceof CustomHitSoundItem customHitSoundItem) {
                    customHitSoundItem.playHitSound((PlayerEntity) (Object) this);
                }
            }
        }
    }

    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void masksnglory$togCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.getWorld().isClient &&
                MaskAbilityManager.getMaskMaterial(player) == ModArmorMaterials.TSHARD) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockRemorseAttack(Entity target, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        ItemStack mainHand = self.getMainHandStack();
        if (mainHand.hasNbt() && mainHand.getNbt().getBoolean("RemorseActive")) {
            ci.cancel();
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void masksnglory$offScriptProjectileImmunity(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!self.getWorld().isClient && ActorManager.offScriptActive.contains(self.getUuid())) {
            if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                cir.setReturnValue(true);
            }
        }
    }
}