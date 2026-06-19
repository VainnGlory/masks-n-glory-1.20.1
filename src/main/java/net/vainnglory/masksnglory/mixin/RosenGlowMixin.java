package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class RosenGlowMixin {
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void rosenGlow(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        Entity self = (Entity)(Object)this;
        if (self == client.player) return;
        ItemStack helmet = client.player.getInventory().getArmorStack(3);
        if (!(helmet.getItem() instanceof ArmorItem armor)) return;
        if (armor.getMaterial() != ModArmorMaterials.ROSENM) return;
        if (client.player.distanceTo(self) <= 100.0f) {
            cir.setReturnValue(true);
        }
    }
}
