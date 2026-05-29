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
public class SheathLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static final UUID OWNER_UUID = UUID.fromString("f8951f83-bfaf-4a75-80bf-000824037387");
    private final SheathRenderer renderer = new SheathRenderer();

    public SheathLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity entity, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.getUuid().equals(OWNER_UUID)) return;

        renderer.prepForRender(entity, new ItemStack(SheathItem.INSTANCE), EquipmentSlot.HEAD, this.getContextModel());
        renderer.defaultRender(matrices, SheathItem.INSTANCE, vertexConsumers, null, null, 0, tickDelta, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }
}
