package net.vainnglory.masksnglory.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public class ItemStackPlummetMixin {

    private static final UUID PLUMMET_REACH_ID = UUID.fromString("8f5cbfbc-0da6-47df-8726-1c827576bbb5");

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void masksnglory$plummetReach(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack self = (ItemStack)(Object)this;
        if (slot != EquipmentSlot.MAINHAND) return;
        if (!(self.getItem() instanceof GoldenPanItem)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.PLUMMET, self) <= 0) return;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(cir.getReturnValue());
        builder.put(
                ReachEntityAttributes.ATTACK_RANGE,
                new EntityAttributeModifier(PLUMMET_REACH_ID, "Plummet reach", 3.0, EntityAttributeModifier.Operation.ADDITION)
        );
        cir.setReturnValue(builder.build());
    }
}
