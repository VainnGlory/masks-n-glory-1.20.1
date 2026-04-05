package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.custom.GlaiveItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public class GlaiveInventoryCycleMixin {

    @Shadow @Nullable protected Slot focusedSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void masksnglory$cycleGlaiveMode(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 1 || focusedSlot == null) return;
        if (!(focusedSlot.getStack().getItem() instanceof GlaiveItem)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, focusedSlot.getStack()) <= 0) return;

        ClientPlayNetworking.send(new Identifier("masks-n-glory", "cycle_glaive_mode"), PacketByteBufs.empty());
        cir.setReturnValue(true);
    }
}
