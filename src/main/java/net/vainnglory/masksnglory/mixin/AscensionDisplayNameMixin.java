package net.vainnglory.masksnglory.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class AscensionDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void masksnglory$ascensionBlankName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity)(Object) this;
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (EnchantmentHelper.getLevel(ModEnchantments.ASCENSION, chest) <= 0) return;
        cir.setReturnValue(Text.literal("_____"));
    }
}
