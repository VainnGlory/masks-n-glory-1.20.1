package net.vainnglory.masksnglory.mixin;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ElytraPinningMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onClientCommand", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockElytraIfPinned(ClientCommandC2SPacket packet, CallbackInfo ci) {
        if (packet.getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING
                && this.player.hasStatusEffect(ModEffects.PINNING)) {
            ci.cancel();
        }
    }
}
