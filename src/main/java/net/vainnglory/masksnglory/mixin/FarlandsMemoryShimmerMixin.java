package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class FarlandsMemoryShimmerMixin {

    @Inject(method = "bobView", at = @At("TAIL"))
    private void masksnglory$farlandsMemoryShimmer(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!client.world.getRegistryKey().equals(World.OVERWORLD)) return;
        if (!FarlandsHelper.isInInnerFarlands(client.player.getX(), client.player.getZ())) return;
        if (FarlandsHelper.hasActiveAmalgam(client.player)) return;

        float time = client.player.age + tickDelta;
        float sway = (float) (Math.sin(time * 0.023) * 0.35 + Math.sin(time * 0.011) * 0.5);
        float lift = (float) (Math.cos(time * 0.017) * 0.25);

        matrices.translate(sway * 0.0045f, lift * 0.003f, 0.0f);
    }
}
