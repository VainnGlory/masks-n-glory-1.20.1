package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class FarlandsElytraMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void masksnglory$cancelFarlandsElytraBoost(
            World world, PlayerEntity user, Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir
    ) {
        if (!world.isClient()
                && user.isFallFlying()
                && FarlandsHelper.isInFarlands(user.getX(), user.getZ())) {
            cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
        }
    }
}
