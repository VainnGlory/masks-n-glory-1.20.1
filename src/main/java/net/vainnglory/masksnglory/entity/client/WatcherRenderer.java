package net.vainnglory.masksnglory.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.entity.custom.WatcherEntity;

public class WatcherRenderer extends MobEntityRenderer<WatcherEntity, PlayerEntityModel<WatcherEntity>> {
    private static final Identifier TEXTURE = new Identifier("masks-n-glory", "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(WatcherEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(WatcherEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (entity.getOwnerUuid().isEmpty()) return;
        if (!entity.getOwnerUuid().get().equals(client.player.getUuid())) return;
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
