package net.vainnglory.masksnglory.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import net.vainnglory.masksnglory.world.FarlandsNullManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class FarlandsPortalMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockFarlandsPortal(
            BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci
    ) {
        if (world.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        StatusEffectInstance effect = player.getStatusEffect(ModEffects.NULL_EFFECT);
        if (effect == null || effect.getAmplifier() < FarlandsNullManager.AMP_STAGE1) return;
        if (FarlandsHelper.isInFarlands(player.getX(), player.getZ())) {
            ci.cancel();
        }
    }
}
