package net.vainnglory.masksnglory.cosmetic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class GoldenShardLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static final UUID OWNER_UUID = UUID.fromString("d1848a30-b4c9-4f64-817d-0d09377b125c");
    private final GoldenShardRenderer renderer = new GoldenShardRenderer();

    public GoldenShardLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity entity, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.getUuid().equals(OWNER_UUID)) return;

        renderer.prepForRender(entity, new ItemStack(GoldenShardItem.INSTANCE), EquipmentSlot.HEAD, this.getContextModel());
        renderer.defaultRender(matrices, GoldenShardItem.INSTANCE, vertexConsumers, null, null, 0, tickDelta, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }
}
