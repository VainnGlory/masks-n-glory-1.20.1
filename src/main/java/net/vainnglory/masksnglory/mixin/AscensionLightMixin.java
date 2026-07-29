package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public class AscensionLightMixin {

    @Inject(method = "getLight", at = @At("RETURN"), cancellable = true)
    private void masksnglory$ascensionFullbright(Entity entity, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof PlayerEntity player)) return;
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (EnchantmentHelper.getLevel(ModEnchantments.ASCENSION, chest) <= 0) return;
        cir.setReturnValue(LightmapTextureManager.pack(15, 15));
    }
}