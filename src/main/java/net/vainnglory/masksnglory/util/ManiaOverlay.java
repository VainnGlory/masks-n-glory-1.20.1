package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ManiaOverlay {

    public static void register() {
        HudRenderCallback.EVENT.register(ManiaOverlay::render);
    }

    private static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack held = client.player.getMainHandStack();
        int alpha = Mania.getTintAlpha(held);
        if (alpha <= 0) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        context.fill(0, 0, width, height, (alpha << 24) | 0xC00000);
    }
}
