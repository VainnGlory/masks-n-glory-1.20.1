package net.vainnglory.masksnglory.mixin;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilPeripheralMixin {

    @Shadow private String newItemName;
    @Shadow private Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void masksnglory$peripheralRecipe(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler)(Object)this;
        ItemStack left = handler.getSlot(0).getStack();

        if (left.isEmpty()) return;
        if (!left.isOf(ModItems.GLORIOUS)) return;
        if (!"enchanted book".equals(this.newItemName)) return;

        ItemStack output = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(output, new EnchantmentLevelEntry(ModEnchantments.PERIPHERAL, 1));

        handler.getSlot(2).setStack(output);
        this.levelCost.set(1);
        handler.sendContentUpdates();

        ci.cancel();
    }
}
