package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.vainnglory.masksnglory.util.StasisBobber;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public class FishingBobberRemoteUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void masksnglory$remoteRetrieve(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity bobber = (FishingBobberEntity)(Object) this;
        if (!(bobber.getWorld() instanceof ServerWorld)) return;
        if (bobber.getPlayerOwner() != null) return;

        ServerPlayerEntity owner = StasisBobber.onlineOwner(bobber);
        if (owner == null) return;

        int damage = bobber.isOnGround() ? 2 : 0;
        owner.fishHook = null;
        bobber.discard();
        cir.setReturnValue(damage);
    }
}