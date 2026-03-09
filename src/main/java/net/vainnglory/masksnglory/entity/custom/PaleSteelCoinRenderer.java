package net.vainnglory.masksnglory.entity.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.vainnglory.masksnglory.item.ModItems;

@Environment(EnvType.CLIENT)
public class PaleSteelCoinRenderer extends EntityRenderer<PaleSteelCoinEntity> {

    private final ItemRenderer itemRenderer;

    public PaleSteelCoinRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(PaleSteelCoinEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        float flip = (entity.age + tickDelta) * 15.0F;
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(flip));

        matrices.scale(0.5F, 0.5F, 0.5F);

        this.itemRenderer.renderItem(
                new ItemStack(ModItems.PALE_STEEL_COIN),
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
    public Identifier getTexture(PaleSteelCoinEntity entity) {
        return null;
    }
}


