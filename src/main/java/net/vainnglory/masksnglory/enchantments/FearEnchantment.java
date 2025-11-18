package net.vainnglory.masksnglory.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.RustedSwordItem;

public class FearEnchantment extends Enchantment {

    public FearEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 5;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }
    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.RUSTED_SWORD) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        if (target instanceof LivingEntity targetEntity) {
            targetEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120 * level, 0, false, false, false)); // L'aura visibile dura 10 secondi
        }
    }
}
