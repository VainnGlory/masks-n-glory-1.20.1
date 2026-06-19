package net.vainnglory.masksnglory.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.PanickEdlyCarvedWoodenSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilPanickEdlyMixin {

    @Shadow private int repairItemUsage;
    @Shadow public Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void masksnglory$panickEdlyAnvilRecipe(CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;

        ItemStack left = self.slots.get(0).getStack();
        ItemStack right = self.slots.get(1).getStack();

        if (!(left.getItem() instanceof PanickEdlyCarvedWoodenSword)) return;
        if (!PanickEdlyCarvedWoodenSword.hasInserted(left)) return;
        if (right.getItem() != Items.STONE) return;

        self.slots.get(2).setStack(new ItemStack(ModItems.PANICKEDLY_CARVED_WOODEN_SWORD));
        this.levelCost.set(1);
        this.repairItemUsage = 1;
        ci.cancel();
    }
}
