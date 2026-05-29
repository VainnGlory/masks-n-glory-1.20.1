package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

public class RibbonTrailRenderer {
    private static final Identifier TEXTURE = new Identifier("masks-n-glory", "ribbon_trail_shared");
    private static boolean textureRegistered = false;

    private static void ensureTexture() {
        if (textureRegistered) return;
        textureRegistered = true;
        NativeImage gradient = new NativeImage(NativeImage.Format.RGBA, 16, 1, false);
        for (int x = 0; x < 16; x++) {
            float u = (x + 0.5f) / 16.0f;
            float edge = Math.min(u * 4.0f, (1.0f - u) * 4.0f);
            int a = (int)(Math.min(1.0f, edge * edge) * 255);
            gradient.setColor(x, 0, (a << 24) | 0x00FFFFFF);
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(
                TEXTURE, new NativeImageBackedTexture(gradient));
    }

    public static void render(List<Vec3d> trail, Vec3d entityPos, Vec3d camPos,
                              MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                              int light, int maxAlpha) {
        if (trail.size() < 2) return;
        ensureTexture();

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Vec3d[] rights = new Vec3d[trail.size()];
        for (int i = 0; i < trail.size(); i++) {
            Vec3d tangent;
            if (i == 0) {
                tangent = trail.get(0).subtract(trail.get(1));
            } else if (i == trail.size() - 1) {
                tangent = trail.get(trail.size() - 2).subtract(trail.get(trail.size() - 1));
            } else {
                tangent = trail.get(i - 1).subtract(trail.get(i + 1));
            }
            if (tangent.lengthSquared() < 1e-6) {
                rights[i] = i > 0 ? rights[i - 1] : new Vec3d(1, 0, 0);
                continue;
            }
            tangent = tangent.normalize();
            Vec3d toCam = camPos.subtract(trail.get(i)).normalize();
            Vec3d right = tangent.crossProduct(toCam);
            if (right.lengthSquared() < 1e-6) {
                rights[i] = i > 0 ? rights[i - 1] : new Vec3d(1, 0, 0);
                continue;
            }
            rights[i] = right.normalize();
        }

        for (int i = 1; i < rights.length; i++) {
            if (rights[i - 1] != null && rights[i].dotProduct(rights[i - 1]) < 0) {
                rights[i] = rights[i].negate();
            }
        }

        for (int i = 0; i < trail.size() - 1; i++) {
            Vec3d p1 = trail.get(i).subtract(entityPos);
            Vec3d p2 = trail.get(i + 1).subtract(entityPos);
            Vec3d r1 = rights[i];
            Vec3d r2 = rights[i + 1];

            float t1 = 1.0f - (float) i / trail.size();
            float t2 = 1.0f - (float) (i + 1) / trail.size();
            int a1 = (int)(t1 * t1 * maxAlpha);
            int a2 = (int)(t2 * t2 * maxAlpha);
            float w1 = 0.12f * t1;
            float w2 = 0.12f * t2;

            addVertex(buffer, matrix, p1, r1,  w1, 0.0f, a1, light);
            addVertex(buffer, matrix, p1, r1, -w1, 1.0f, a1, light);
            addVertex(buffer, matrix, p2, r2, -w2, 1.0f, a2, light);
            addVertex(buffer, matrix, p2, r2,  w2, 0.0f, a2, light);
        }
    }

    private static void addVertex(VertexConsumer buffer, Matrix4f matrix, Vec3d base, Vec3d right,
                                  float w, float u, int alpha, int light) {
        buffer.vertex(matrix,
                        (float)(base.x + right.x * w),
                        (float)(base.y + right.y * w),
                        (float)(base.z + right.z * w))
                .color(255, 255, 255, alpha)
                .texture(u, 0.5f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 1, 0)
                .next();
    }
}
