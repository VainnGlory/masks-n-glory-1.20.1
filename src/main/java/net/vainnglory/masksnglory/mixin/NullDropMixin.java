package net.vainnglory.masksnglory.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.vainnglory.masksnglory.util.NullManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class NullDropMixin {

    @Shadow
    public net.minecraft.server.network.ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void masksnglory$blockNullEffectDrop(PlayerActionC2SPacket packet, CallbackInfo ci) {
        PlayerActionC2SPacket.Action action = packet.getAction();
        if (action != PlayerActionC2SPacket.Action.DROP_ITEM && action != PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) return;

        ItemStack selected = this.player.getInventory().getMainHandStack();
        if (NullManager.isEffectVoidItem(selected)) {
            ci.cancel();
        }
    }
}
