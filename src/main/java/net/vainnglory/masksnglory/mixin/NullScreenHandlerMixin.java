package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.vainnglory.masksnglory.util.NullManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class NullScreenHandlerMixin {
    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void nullEffect$preventVoidSlotInteraction(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler self = (ScreenHandler)(Object)this;

        if (slotIndex >= 0 && slotIndex < self.slots.size()) {
            if (NullManager.isEffectVoidItem(self.slots.get(slotIndex).getStack())) {
                ci.cancel();
                return;
            }
        }

        if (actionType == SlotActionType.SWAP && button >= 0 && button < 9) {
            if (NullManager.isEffectVoidItem(player.getInventory().getStack(button))) {
                ci.cancel();
            }
        }
    }
}
