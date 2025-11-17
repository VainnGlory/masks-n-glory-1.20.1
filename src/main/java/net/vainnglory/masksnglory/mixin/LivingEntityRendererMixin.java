package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {


    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void modifyNametagVisibility(T entity, CallbackInfoReturnable cir) {
        boolean hasPaleSword = false;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = client.player;
        boolean bl = !entity.isInvisibleTo(clientPlayerEntity);

        if (entity instanceof PlayerEntity player) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                if (player.getInventory().getStack(i).isOf(ModItems.RUSTED_SWORD)) {
                    hasPaleSword = true;
                }
            }
        }
        cir.setReturnValue(hasPaleSword && MinecraftClient.isHudEnabled() && entity != client.getCameraEntity() && bl && !entity.hasPassengers());
    }
}
