package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import net.vainnglory.masksnglory.world.FarlandsNullManager;

public class NullOverlay {

    public static void register() {
        HudRenderCallback.EVENT.register(NullOverlay::render);
    }

    private static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (!FarlandsHelper.isInFarlands(client.player.getX(), client.player.getZ())) return;

        StatusEffectInstance effect = client.player.getStatusEffect(ModEffects.NULL_EFFECT);
        if (effect == null || effect.getAmplifier() < FarlandsNullManager.AMP_STAGE1) return;

        int amplifier = effect.getAmplifier();
        float alpha;
        if (amplifier >= FarlandsNullManager.AMP_STAGE3) {
            alpha = 0.50f;
        } else if (amplifier >= FarlandsNullManager.AMP_STAGE2) {
            alpha = 0.30f;
        } else {
            alpha = 0.15f;
        }

        int a = (int) (alpha * 255);
        int color = (a << 24) | 0x808080;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        context.fill(0, 0, width, height, color);
    }
}
