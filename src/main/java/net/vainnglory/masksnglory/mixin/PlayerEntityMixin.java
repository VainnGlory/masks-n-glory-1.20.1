package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.custom.CustomHitSoundItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (this.getAttackCooldownProgress(0.5F) > 0.9F) {
            if (this.getMainHandStack().getItem() instanceof CustomHitSoundItem customHitSoundItem) {
                customHitSoundItem.playHitSound((PlayerEntity) (Object) this);
            }
        }
    }
}

