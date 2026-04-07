package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.vainnglory.masksnglory.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class SugarRushHudMixin {

    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void masksnglory$hideSugarRushHearts(DrawContext context, @Coerce Object type, int x, int y, int v, boolean blinking, boolean half, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.hasStatusEffect(ModEffects.SUGAR_RUSH)) {
            ci.cancel();
        }
    }
}

