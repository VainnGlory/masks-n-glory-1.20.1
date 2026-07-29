package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.world.ServerWorld;
import net.vainnglory.masksnglory.util.EnderPearlChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public class EnderPearlChunkTicketMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void masksnglory$loadChunks(CallbackInfo ci) {
        EnderPearlEntity pearl = (EnderPearlEntity)(Object) this;
        if (!(pearl.getWorld() instanceof ServerWorld serverWorld)) return;
        EnderPearlChunkLoader.onPearlTick(serverWorld, pearl);
    }
}