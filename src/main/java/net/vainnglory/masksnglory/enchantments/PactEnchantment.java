package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;

public class PactEnchantment extends Enchantment {

    private static final float DAMAGE_PER_EMPTY_SLOT = 1.5f;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public PactEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 15;
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
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.RUSTED_SWORD) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            ItemStack weapon = player.getMainHandStack();
            if (EnchantmentHelper.getLevel(ModEnchantments.PACT, weapon) <= 0) return ActionResult.PASS;

            int emptySlots = 0;
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                if (player.getEquippedStack(slot).isEmpty()) {
                    emptySlots++;
                }
            }

            if (emptySlots == 0) return ActionResult.PASS;

            float bonusDamage = emptySlots * DAMAGE_PER_EMPTY_SLOT;

            world.getServer().execute(() -> {
                target.hurtTime = 0;
                target.timeUntilRegen = 0;
                target.damage(player.getDamageSources().playerAttack(player), bonusDamage);
            });

            return ActionResult.PASS;
        });
    }
}
