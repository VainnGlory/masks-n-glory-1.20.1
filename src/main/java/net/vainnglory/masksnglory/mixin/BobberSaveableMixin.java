package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class BobberSaveableMixin {

    @Inject(method = "isSaveable", at = @At("HEAD"), cancellable = true)
    private void masksnglory$keepFishingBobbers(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == EntityType.FISHING_BOBBER) {
            cir.setReturnValue(true);
        }
    }
}
