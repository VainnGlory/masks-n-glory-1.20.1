package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class RatMaskDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void masksnglory$ratMaskName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity)(Object) this;
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (!helmet.isOf(ModItems.RAT_MASK)) return;
        cir.setReturnValue(Text.literal("rat"));
    }
}
