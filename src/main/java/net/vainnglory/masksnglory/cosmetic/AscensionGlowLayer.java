package net.vainnglory.masksnglory.cosmetic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;

@Environment(EnvType.CLIENT)
public class AscensionGlowLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static final Identifier TEXTURE = new Identifier("masks-n-glory", "textures/entity/ascension_white.png");
    private static final int FULLBRIGHT = 15728880;
    private static final int SHELLS = 3;

    public AscensionGlowLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (EnchantmentHelper.getLevel(ModEnchantments.ASCENSION, chest) <= 0) return;
        if (player.isInvisible()) return;

        float pulse = 0.55f + 0.15f * MathHelper.sin((player.age + tickDelta) * 0.12f);

        for (int i = 0; i < SHELLS; i++) {
            float scale = 1.045f + i * 0.055f;
            float alpha = pulse * (0.30f - i * 0.085f);
            if (alpha <= 0.0f) continue;

            matrices.push();
            matrices.translate(0.0f, (scale - 1.0f) * 0.75f, 0.0f);
            matrices.scale(scale, scale, scale);

            VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(TEXTURE));
            this.getContextModel().render(matrices, consumer, FULLBRIGHT, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, alpha);

            matrices.pop();
        }
    }
}
