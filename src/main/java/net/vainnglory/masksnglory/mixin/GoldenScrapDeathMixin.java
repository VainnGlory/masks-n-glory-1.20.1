package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.util.GoldenScrapManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class GoldenScrapDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void masksnglory$onScrapFarmDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        if (!(damageSource.getAttacker() instanceof ServerPlayerEntity killer)) return;
        if (!GoldenScrapManager.wouldQualify(killer, victim)) return;

        GoldenScrapManager.preQualifyKill(victim.getUuid(), killer.getUuid());

        victim.totalExperience = 0;
        victim.experienceLevel = 0;
        victim.experienceProgress = 0.0f;

        if (!GoldenScrapManager.hasFoodItems(victim)) {
            GoldenScrapManager.markForHealthPenalty(victim.getUuid());
        }
    }
}
