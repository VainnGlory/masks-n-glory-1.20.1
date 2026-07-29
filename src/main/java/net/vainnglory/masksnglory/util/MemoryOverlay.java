package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;

public class MemoryOverlay {
    private static final Random RNG = Random.create();
    private static final int VIGNETTE_COLOR = 0x2A1B12;
    private static int[] grainX = new int[0];
    private static int[] grainY = new int[0];
    private static int grainSeedTick = -1;

    public static void register() {
        HudRenderCallback.EVENT.register(MemoryOverlay::render);
    }

    private static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!client.world.getRegistryKey().equals(World.OVERWORLD)) return;
        if (!FarlandsHelper.isInInnerFarlands(client.player.getX(), client.player.getZ())) return;
        if (FarlandsHelper.hasActiveAmalgam(client.player)) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int age = client.player.age;

        float flicker = (float) (Math.sin(age * 0.2) * 0.01 + Math.sin(age * 0.53) * 0.007);
        int tintAlpha = (int) Math.max(0, Math.min(255, 20 + flicker * 255));
        context.fill(0, 0, width, height, (tintAlpha << 24) | 0xC9A15A);

        int edgeY = height / 5;
        int edgeX = width / 6;
        context.fillGradient(0, 0, width, edgeY, (0x60 << 24) | VIGNETTE_COLOR, 0x00000000);
        context.fillGradient(0, height - edgeY, width, height, 0x00000000, (0x60 << 24) | VIGNETTE_COLOR);

        int columns = 20;
        int stripWidth = edgeX / columns;
        for (int i = 0; i < columns; i++) {
            float t = (float) i / columns;
            int alpha = (int) (0x48 * (1.0f - t));
            int color = (alpha << 24) | VIGNETTE_COLOR;
            int xLeft = i * stripWidth;
            context.fill(xLeft, 0, xLeft + stripWidth + 1, height, color);
            int xRight = width - edgeX + (columns - 1 - i) * stripWidth;
            context.fill(xRight, 0, xRight + stripWidth + 1, height, color);
        }

        int seedTick = age / 4;
        if (seedTick != grainSeedTick) {
            grainSeedTick = seedTick;
            int count = 20;
            grainX = new int[count];
            grainY = new int[count];
            for (int i = 0; i < count; i++) {
                grainX[i] = RNG.nextInt(width);
                grainY[i] = RNG.nextInt(height);
            }
        }
        for (int i = 0; i < grainX.length; i++) {
            context.fill(grainX[i], grainY[i], grainX[i] + 1, grainY[i] + 1, 0x14FFF3D6);
        }
    }
}
