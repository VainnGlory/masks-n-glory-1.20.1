package net.vainnglory.masksnglory.entity.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.vainnglory.masksnglory.MasksNGlory;

@Environment(EnvType.CLIENT)
public class ShearProjectileRenderer extends EntityRenderer<ShearProjectileEntity> {
    private static final ModelIdentifier MODEL_ID =
            new ModelIdentifier(MasksNGlory.MOD_ID, "shear_projectile", "inventory");

    private final ItemRenderer itemRenderer;

    public ShearProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ShearProjectileEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        RibbonTrailRenderer.render(entity.getTrailPositions(), entity.getPos(), camPos, matrices, vertexConsumers, light, 110);

        BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(MODEL_ID);
        matrices.push();
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entity.getYaw(tickDelta) + 180f));
        matrices.scale(0.6f, 0.6f, 0.6f);
        itemRenderer.renderItem(new ItemStack(Items.SHEARS), ModelTransformationMode.NONE, false,
                matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ShearProjectileEntity entity) {
        return null;
    }
}
