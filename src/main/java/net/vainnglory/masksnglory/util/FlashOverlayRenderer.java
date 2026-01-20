package net.vainnglory.masksnglory.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

public class FlashOverlayRenderer {

    private static long flashStartTime = 0;
    private static final long FLASH_DURATION = 1000;
    private static final Identifier WHITE_TEXTURE = new Identifier("minecraft", "textures/misc/white.png");

    public static void register() {
        HudRenderCallback.EVENT.register(FlashOverlayRenderer::renderFlash);
    }

    public static void triggerFlash() {
        flashStartTime = System.currentTimeMillis();
    }

    private static void renderFlash(DrawContext context, float tickDelta) {
        if (flashStartTime == 0) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - flashStartTime;

        if (elapsed > FLASH_DURATION) {
            flashStartTime = 0;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        float progress = (float) elapsed / FLASH_DURATION;
        float alpha;

        if (progress < 0.1f) {
            alpha = progress / 0.1f;
        } else {
            alpha = 1.0f - ((progress - 0.1f) / 0.9f);
        }

        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        // Draw white overlay
        context.fill(0, 0, width, height, 0xFFFFFF | ((int)(alpha * 255) << 24));

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
