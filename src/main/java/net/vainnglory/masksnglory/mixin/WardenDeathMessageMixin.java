package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageTracker.class)
public class WardenDeathMessageMixin {

    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void masksnglory$overrideWardenDeathMessage(CallbackInfoReturnable<Text> cir) {
        LivingEntity entity = ((DamageTrackerAccessor) (Object) this).getEntity();
        if (!(entity instanceof PlayerEntity dead)) return;
        DamageSource src = dead.getRecentDamageSource();
        if (src == null) return;
        if (!(src.getAttacker() instanceof PlayerEntity killer)) return;
        ItemStack offhand = killer.getOffHandStack();
        if (!offhand.isOf(ModItems.WARDEN)) return;
        cir.setReturnValue(Text.literal(dead.getDisplayName().getString() + " has been locked away in a dark cell."));
    }
}
