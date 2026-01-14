package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class MaelstromEntityRenderer extends EntityRenderer<MaelstromEntity> {
    private final ItemRenderer itemRenderer;

    public MaelstromEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MaelstromEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();


        float rotation = (entity.age + tickDelta) * 90.0F; // Adjust speed here
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));


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
