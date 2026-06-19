package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ArmorFeatureRenderer.class)
public class RosenArmorLayerMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

    private static final Identifier ROSEN_LAYER_2 = new Identifier("masks-n-glory", "textures/models/armor/rosenm_layer_1.png");

    @Inject(method = "renderArmor", at = @At("TAIL"))
    private void masksnglory$rosenInnerLayer(
            MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            T entity, EquipmentSlot slot, int light, A armorModel, CallbackInfo ci) {
        if (slot != EquipmentSlot.HEAD) return;
        ItemStack helmet = entity.getEquippedStack(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof ArmorItem armorItem)) return;
        if (armorItem.getMaterial() != ModArmorMaterials.ROSENM) return;
        armorModel.head.render(matrices,
                vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(ROSEN_LAYER_2)),
                light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
    }
}
