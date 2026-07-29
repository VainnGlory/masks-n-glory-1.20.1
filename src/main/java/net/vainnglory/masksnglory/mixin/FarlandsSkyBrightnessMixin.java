package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import net.vainnglory.masksnglory.world.ModDimensions;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class FarlandsSkyBrightnessMixin {

    private static final Vec3d VERDANT_DAY = new Vec3d(132.0 / 255.0, 250.0 / 255.0, 211.0 / 255.0);
    private static final Vec3d VERDANT_NIGHT = new Vec3d(255.0 / 255.0, 239.0 / 255.0, 161.0 / 255.0);

    @Redirect(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;")
    )
    private Vec3d masksnglory$farlandsSkyColor(ClientWorld world, Vec3d cameraPos, float tickDelta) {
        Vec3d original = world.getSkyColor(cameraPos, tickDelta);
        float skyAngle = world.getSkyAngle(tickDelta);
        double t = MathHelper.clamp(MathHelper.cos(skyAngle * ((float) Math.PI * 2.0f)) * 2.0f + 0.5f, 0.0f, 1.0f);

        if (world.getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) {
            return new Vec3d(
                    VERDANT_DAY.x * t + VERDANT_NIGHT.x * (1.0 - t),
                    VERDANT_DAY.y * t + VERDANT_NIGHT.y * (1.0 - t),
                    VERDANT_DAY.z * t + VERDANT_NIGHT.z * (1.0 - t)
            );
        }

        if (!world.getRegistryKey().equals(World.OVERWORLD)) return original;
        if (!FarlandsHelper.isInFarlands(cameraPos.x, cameraPos.z)) return original;

        float naturalBrightness = MathHelper.clamp(MathHelper.cos(skyAngle * ((float) Math.PI * 2.0f)) * 2.0f + 0.5f, 0.0f, 1.0f);
        double ft = Math.min(1.0, Math.max(0.0, naturalBrightness / 0.3));
        double nightR = 219.0 / 255.0, nightG = 212.0 / 255.0, nightB = 220.0 / 255.0;
        double tintedR = nightR + (original.x - nightR) * ft;
        double tintedG = nightG + (original.y - nightG) * ft;
        double tintedB = nightB + (original.z - nightB) * ft;
        double strength = 0.7;
        return new Vec3d(
                original.x + (tintedR - original.x) * strength,
                original.y + (tintedG - original.y) * strength,
                original.z + (tintedB - original.z) * strength
        );
    }
}
