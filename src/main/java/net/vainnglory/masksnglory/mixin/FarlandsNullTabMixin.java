package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import net.vainnglory.masksnglory.world.FarlandsNullManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class FarlandsNullTabMixin {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void masksnglory$nullTabName(CallbackInfoReturnable<Text> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        PlayerListEntry self = (PlayerListEntry)(Object)this;
        if (!self.getProfile().getId().equals(client.player.getUuid())) return;
        if (!FarlandsHelper.isInFarlands(client.player.getX(), client.player.getZ())) return;
        StatusEffectInstance effect = client.player.getStatusEffect(ModEffects.NULL_EFFECT);
        if (effect == null || effect.getAmplifier() < FarlandsNullManager.AMP_STAGE1) return;
        cir.setReturnValue(Text.literal("null").formatted(Formatting.GRAY));
    }
}
