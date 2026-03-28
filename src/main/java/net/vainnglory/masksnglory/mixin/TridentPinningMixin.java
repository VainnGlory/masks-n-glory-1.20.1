package net.vainnglory.masksnglory.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public class TridentPinningMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockRiptideChargeIfPinned(World world, PlayerEntity user, Hand hand,
                                                        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (user.hasStatusEffect(ModEffects.PINNING)) {
            ItemStack stack = user.getStackInHand(hand);
            if (EnchantmentHelper.getRiptide(stack) > 0) {
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockRiptideLaunchIfPinned(ItemStack stack, World world,
                                                        LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user.hasStatusEffect(ModEffects.PINNING)) {
            if (EnchantmentHelper.getRiptide(stack) > 0) {
                ci.cancel();
            }
        }
    }
}
