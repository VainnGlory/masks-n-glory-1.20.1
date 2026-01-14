package net.vainnglory.masksnglory.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.vainnglory.masksnglory.util.ModDeathSource;

@Mixin(LivingEntity.class)
public abstract class DeathMixin extends Entity implements Attackable {
    public DeathMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void mask$modKillSource(LivingEntity instance, DamageSource source, float amount, Operation<Void> original) {
        if (source.getAttacker() instanceof LivingEntity living && living.getMainHandStack().getItem() instanceof ModDeathSource deathSource) {
            original.call(instance, deathSource.getKillSource(instance), amount);
        } else {
            original.call(instance, source, amount);
        }
    }
}
