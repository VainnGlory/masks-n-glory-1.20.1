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
import net.vainnglory.masksnglory.util.ModDamageTypes;


public class GuillotineEnchantment extends Enchantment {

    public GuillotineEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 7;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

    @Override
    public boolean canAccept(Enchantment other) {
        return !(other instanceof SerialEnchantment) &&
                !(other instanceof PactEnchantment) &&
                !(other instanceof LockoutEnchantment) &&
                !(other instanceof FearEnchantment);
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.RUSTED_SWORD) || stack.isOf(ModItems.PRIDE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient && entity instanceof LivingEntity target) {
                float cooldown = player.getAttackCooldownProgress(0.0f);
                if (cooldown >= 0.8f) {
                    ItemStack weapon = player.getMainHandStack();
                    int level = EnchantmentHelper.getLevel(ModEnchantments.DEATH, weapon);

                    if (level > 0 && target.getHealth() < 9.0f && isInterrupted(target)) {
                        target.damage(target.getDamageSources().create(ModDamageTypes.DEATH_DAMAGE, player, player), 100000.0f);
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private static boolean isInterrupted(LivingEntity target) {
        return target.isUsingItem();
    }
}
