package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class MaelstromEntityRenderer extends EntityRenderer<MaelstromEntity> {
    private final ItemRenderer itemRenderer;

    public MaelstromEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MaelstromEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        RibbonTrailRenderer.render(entity.getTrailPositions(), entity.getPos(), camPos, matrices, vertexConsumers, light, 220);

        matrices.push();
        if (entity.isRemorseStuck()) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
        } else {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.age + tickDelta) * 90.0F));
        }
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
        this.itemRenderer.renderItem(
                entity.asItemStack(),
                ModelTransformationMode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                entity.getId()
        );
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(MaelstromEntity entity) {
        return null;
    }
}
