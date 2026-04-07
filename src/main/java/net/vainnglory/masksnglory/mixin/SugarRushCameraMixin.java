package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.RotationAxis;
import net.vainnglory.masksnglory.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class SugarRushCameraMixin {

    @Inject(method = "bobView", at = @At("TAIL"))
    private void masksnglory$sugarRushVibrate(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        StatusEffectInstance effect = client.player.getStatusEffect(ModEffects.SUGAR_RUSH);
        if (effect == null) return;

        float base = effect.getAmplifier() + 1;
        float intensity = base * base * 0.9f;
        float time = client.player.age + tickDelta;
        float shakeX = (float)(Math.sin(time * 0.85) * intensity + Math.sin(time * 1.9) * intensity * 0.5f);
        float shakeY = (float)(Math.cos(time * 1.2) * intensity + Math.cos(time * 2.3) * intensity * 0.4f);

        matrices.translate(shakeX * 0.022f, shakeY * 0.016f, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(shakeX * 2.8f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(shakeY * 1.2f));
    }
}

