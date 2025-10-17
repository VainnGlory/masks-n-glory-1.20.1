package net.vainnglory.masksnglory.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.vainnglory.masksnglory.MasksNGlory;

import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    ESHARD("eshard", 25, new int[] { 3, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.ESHARD)),
    DSHARD("dshard", 25, new int[] { 3, 8, 6, 3 }, 19,
    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.DSHARD)),
    MASKS("masks", 25, new int[] { 3, 8, 6, 3 }, 19,
    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.MASKS)),
    HMASKS("hmasks", 25, new int[] { 1, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.HMASKS)),
    GMASKS("gmasks", 25, new int[] { 1, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.GMASKS)),
    SMASKS("smasks", 25, new int[] { 1, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.SMASKS)),
    KMASKS("kmasks", 25, new int[] { 1, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.KMASKS)),
    EMASKS("emasks", 25, new int[] { 1, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.EMASKS)),
    NMASKS("nmasks", 25, new int[] { 1, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.NMASKS)),
    OSHARD("oshard", 25, new int[] { 3, 8, 6, 3 }, 19,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.OSHARD)),
    PSHARD("pshard", 25, new int[] { 3, 8, 6, 3 }, 19,
    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2f, 0.1f, () -> Ingredient.ofItems(ModItems.PSHARD));


    private final String name;
    private final int[] protectionAmounts;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] BASE_DURABILITY = {11, 16, 15, 13 };

    ModArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantability, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.protectionAmounts = protectionAmounts;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        return BASE_DURABILITY[type.ordinal()] * this.enchantability;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return protectionAmounts[type.ordinal()];
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return MasksNGlory.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
