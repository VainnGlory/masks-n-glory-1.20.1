package net.vainnglory.masksnglory.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilNullKnifeMixin {

    @Shadow private int repairItemUsage;
    @Shadow public Property levelCost;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void masksnglory$nullKnifeAnvilRecipe(CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler)(Object)this;

        ItemStack left = self.slots.get(0).getStack();
        ItemStack right = self.slots.get(1).getStack();

        boolean leftIsNullSword = left.isOf(Items.IRON_SWORD)
                && left.hasCustomName()
                && left.getName().getString().equals("null");
        boolean rightIsVoid = right.isOf(ModItems.GLORIOUS);

        if (!leftIsNullSword || !rightIsVoid) return;

        self.slots.get(2).setStack(new ItemStack(ModItems.NULL_KNIFE));
        this.levelCost.set(10);
        this.repairItemUsage = 1;
        ci.cancel();
    }
}
