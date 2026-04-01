package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class HeldItemFeatureRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At("HEAD"), cancellable = true)
    private void masksnglory$hideHeldItems(
            LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode,
            boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (entity != null && entity.hasStatusEffect(ModEffects.OFF_SCRIPT_FLAG)) {
            ci.cancel();
            return;
        }
        if (stack.hasNbt() && stack.getNbt().getBoolean("RemorseActive")
                && (renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
                || renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND
                || renderMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                || renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND)) {
            ci.cancel();
        }
    }
}
