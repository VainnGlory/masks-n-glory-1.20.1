package net.vainnglory.masksnglory.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageTracker.class)
public class DeathMessageMixin {

    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void masksnglory$customDeathMessage(CallbackInfoReturnable<Text> cir) {
        LivingEntity entity = ((DamageTrackerAccessor) (Object) this).getEntity();
        DamageSource src = entity.getRecentDamageSource();
        if (src == null) return;
        if (!(src.getAttacker() instanceof PlayerEntity killer)) return;

        if (entity instanceof PlayerEntity dead && killer.getOffHandStack().isOf(ModItems.WARDEN)) {
            cir.setReturnValue(Text.literal(dead.getDisplayName().getString() + " has been locked away in a dark cell."));
            return;
        }

        ItemStack pan = killer.getMainHandStack();
        if (pan.getItem() instanceof GoldenPanItem) {
            if (pan.hasCustomName()) {
                cir.setReturnValue(Text.translatable("death.attack.pan.item",
                        entity.getDisplayName(), killer.getDisplayName(), pan.toHoverableText()));
            } else {
                cir.setReturnValue(Text.translatable("death.attack.pan.player",
                        entity.getDisplayName(), killer.getDisplayName()));
            }
            return;
        }

        if (EnchantmentHelper.getLevel(ModEnchantments.ASCENSION, killer.getEquippedStack(EquipmentSlot.CHEST)) > 0) {
            cir.setReturnValue(Text.literal(entity.getDisplayName().getString() + " was suffocated by _____"));
        }
    }
}
