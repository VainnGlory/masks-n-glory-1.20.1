package net.vainnglory.masksnglory.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ShearsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ShearsStackMixin {
    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void increaseMaxCount(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof ShearsItem) {
            cir.setReturnValue(16);
        }
    }
}
