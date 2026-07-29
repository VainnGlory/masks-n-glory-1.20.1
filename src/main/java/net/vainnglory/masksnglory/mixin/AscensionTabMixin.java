package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
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

@Mixin(PlayerListEntry.class)
public class AscensionTabMixin {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void masksnglory$ascensionTabName(CallbackInfoReturnable<Text> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        PlayerListEntry self = (PlayerListEntry)(Object) this;
        PlayerEntity player = client.world.getPlayerByUuid(self.getProfile().getId());
        if (player == null) return;
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (EnchantmentHelper.getLevel(ModEnchantments.ASCENSION, chest) <= 0) return;
        cir.setReturnValue(Text.literal("_____"));
    }
}
