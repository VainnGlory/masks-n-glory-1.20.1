package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class PeripheralRenderMixin {

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void masksnglory$peripheralHide(Entity entity, double x, double y, double z,
                                            float yaw, float tickDelta, MatrixStack matrices,
                                            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entity == client.player) return;
        if (!(entity instanceof PlayerEntity player)) return;

        boolean hasPeripheral = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack s = player.getEquippedStack(slot);
            if (EnchantmentHelper.getLevel(ModEnchantments.PERIPHERAL, s) > 0) {
                hasPeripheral = true;
                break;
            }
        }
        if (!hasPeripheral) return;

        Vec3d lookDir = Vec3d.fromPolar(
                client.gameRenderer.getCamera().getPitch(),
                client.gameRenderer.getCamera().getYaw());
        Vec3d toEntity = new Vec3d(x, y + player.getHeight() * 0.5, z).normalize();

        double angle = Math.toDegrees(Math.acos(
                Math.max(-1.0, Math.min(1.0, lookDir.dotProduct(toEntity)))));

        if (angle < 50.0) {
            ci.cancel();
        }
    }
}
