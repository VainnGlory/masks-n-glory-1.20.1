package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageTracker.class)
public class PanDeathMessageMixin {

    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void masksnglory$overridePanDeathMessage(CallbackInfoReturnable<Text> cir) {
        LivingEntity entity = ((DamageTrackerAccessor)(Object)this).getEntity();
        DamageSource src = entity.getRecentDamageSource();
        if (src == null) return;
        if (!(src.getAttacker() instanceof PlayerEntity killer)) return;
        if (!(killer.getMainHandStack().getItem() instanceof GoldenPanItem)) return;
        cir.setReturnValue(Text.translatable("death.attack.pan.player",
                entity.getDisplayName(), killer.getDisplayName()));
    }
}
