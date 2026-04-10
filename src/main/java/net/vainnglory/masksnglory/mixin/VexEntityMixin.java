package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.custom.RetributionHelmet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(VexEntity.class)
public class VexEntityMixin {
    @Unique
    private int masksnglory$spiteCountdown = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void masksnglory$spiteTargeting(CallbackInfo ci) {
        VexEntity self = (VexEntity)(Object)this;

        if (self.getWorld().isClient()) return;
        if (!RetributionHelmet.SPITE_VEXES.contains(self.getUuid())) return;

        if (!self.isAlive()) {
            RetributionHelmet.SPITE_VEXES.remove(self.getUuid());
            return;
        }

        LivingEntity currentTarget = self.getTarget();
        boolean alreadyOnSpiteTarget = currentTarget != null
                && currentTarget.isAlive()
                && currentTarget.hasStatusEffect(ModEffects.SPITE);

        if (alreadyOnSpiteTarget) return;

        masksnglory$spiteCountdown--;
        if (masksnglory$spiteCountdown <= 0) {
            masksnglory$spiteCountdown = 10;

            ServerWorld world = (ServerWorld) self.getWorld();
            List<LivingEntity> candidates = world.getEntitiesByClass(
                    LivingEntity.class,
                    self.getBoundingBox().expand(30),
                    e -> e.isAlive() && e.hasStatusEffect(ModEffects.SPITE)
            );

            LivingEntity target = candidates.stream()
                    .filter(e -> e instanceof PlayerEntity)
                    .min(Comparator.comparingDouble(self::squaredDistanceTo))
                    .orElse(null);

            if (target == null) {
                target = candidates.stream()
                        .min(Comparator.comparingDouble(self::squaredDistanceTo))
                        .orElse(null);
            }

            self.setTarget(target);
        }
    }
}
