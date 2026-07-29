package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.world.ServerWorld;
import net.vainnglory.masksnglory.util.StasisBobber;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberFreezeMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void masksnglory$freezeWhenOwnerAway(CallbackInfo ci) {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object) this;
        if (!(bobber.getWorld() instanceof ServerWorld)) return;

        StasisBobber.relink(bobber);

        if (StasisBobber.shouldFreeze(bobber)) {
            ci.cancel();
        }
    }
}
