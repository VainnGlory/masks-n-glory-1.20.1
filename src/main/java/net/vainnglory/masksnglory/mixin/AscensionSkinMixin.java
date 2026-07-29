package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public class AscensionSkinMixin {

    private static final Identifier ASCENSION_TEXTURE = new Identifier("masks-n-glory", "textures/entity/ascension_white.png");

    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void masksnglory$ascensionSkin(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (EnchantmentHelper.getLevel(ModEnchantments.ASCENSION, chest) <= 0) return;
        cir.setReturnValue(ASCENSION_TEXTURE);
    }
}
