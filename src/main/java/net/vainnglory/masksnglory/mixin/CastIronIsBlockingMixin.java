package net.vainnglory.masksnglory.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import net.vainnglory.masksnglory.util.CastIronManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class CastIronIsBlockingMixin {

    @Inject(method = "isBlocking", at = @At("RETURN"), cancellable = true)
    private void castIron$overrideBlocking(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof PlayerEntity player)) return;
        ItemStack weapon = player.getMainHandStack();
        if (!(weapon.getItem() instanceof GoldenPanItem)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, weapon) == 0) return;
        if (CastIronManager.isBlocking(player)) {
            cir.setReturnValue(true);
        }
    }
}
