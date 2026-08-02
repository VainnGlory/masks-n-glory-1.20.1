package net.vainnglory.masksnglory.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class AtychiphobiaOverlay {

    private static final Identifier QUICKTIME_1 = new Identifier("masks-n-glory", "textures/gui/quicktime1.png");
    private static final Identifier QUICKTIME_2 = new Identifier("masks-n-glory", "textures/gui/quicktime2.png");
    private static final int TEXTURE_SIZE = 128;
    private static final long FLASH_DURATION_MS = 500;
    private static final long FRAME_MS = 60;
    private static final int EDGE_STEPS = 24;

    private static int stage = 0;
    private static long flashStart = 0;

    public static void register() {
        HudRenderCallback.EVENT.register(AtychiphobiaOverlay::render);
    }

    public static void setStage(int newStage) {
        stage = newStage;
        if (newStage <= 0) {
            flashStart = 0;
        }
    }

    public static void triggerQte() {
        flashStart = System.currentTimeMillis();
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.BLOCK_BELL_USE, 1.7f));
    }

    public static void onQteSuccess() {
        flashStart = 0;
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.BLOCK_CONDUIT_ACTIVATE, 1.4f));
    }

    private static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        if (stage > 0) {
            drawVignette(context, width, height);
        }
        drawQte(context, width, height);
    }

    private static void drawVignette(DrawContext context, int width, int height) {
        float progress = (float) (stage - 1) / (Atychiphobia.MAX_STAGE - 1);
        float coverage = 0.10f + 0.32f * progress;
        float peak = 0.55f + 0.45f * progress;

        int insetX = Math.max(EDGE_STEPS, (int) (width * coverage));
        int insetY = Math.max(EDGE_STEPS, (int) (height * coverage));
        int stepX = Math.max(1, insetX / EDGE_STEPS);
        int stepY = Math.max(1, insetY / EDGE_STEPS);

        for (int i = 0; i < EDGE_STEPS; i++) {
            float t = (float) i / EDGE_STEPS;
            float falloff = (1.0f - t) * (1.0f - t);
            int alpha = (int) (255 * peak * falloff);
            if (alpha <= 0) continue;
            int color = alpha << 24;

            int yTop = i * stepY;
            context.fill(0, yTop, width, yTop + stepY, color);
            int yBottom = height - (i + 1) * stepY;
            context.fill(0, yBottom, width, yBottom + stepY, color);

            int xLeft = i * stepX;
            context.fill(xLeft, 0, xLeft + stepX, height, color);
            int xRight = width - (i + 1) * stepX;
            context.fill(xRight, 0, xRight + stepX, height, color);
        }
    }

    private static void drawQte(DrawContext context, int width, int height) {
        if (flashStart == 0) return;

        long elapsed = System.currentTimeMillis() - flashStart;
        if (elapsed > FLASH_DURATION_MS) {
            flashStart = 0;
            return;
        }

        Identifier texture = (elapsed / FRAME_MS) % 2 == 0 ? QUICKTIME_1 : QUICKTIME_2;
        int x = (width - TEXTURE_SIZE) / 2;
        int y = (height - TEXTURE_SIZE) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.drawTexture(texture, x, y, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        RenderSystem.disableBlend();
    }
}
