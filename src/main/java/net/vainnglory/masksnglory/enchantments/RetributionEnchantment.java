package net.vainnglory.masksnglory.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.vainnglory.masksnglory.item.custom.RetributionHelmet;

public class RetributionEnchantment extends Enchantment {

    public RetributionEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMinPower(int level) {
        return 30;
    }

    @Override
    public int getMaxPower(int level) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof RetributionHelmet || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    protected boolean canAccept(Enchantment other) {

        return !(other instanceof UndeadArmyEnchantment) && super.canAccept(other);
    }
}
