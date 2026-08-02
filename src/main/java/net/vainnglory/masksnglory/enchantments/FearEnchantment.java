package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.util.Atychiphobia;

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
        return stack.isOf(ModItems.RUSTED_SWORD) || stack.isOf(ModItems.PRIDE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        if (user.getWorld().isClient) return;

        if (target instanceof LivingEntity targetEntity) {
            Atychiphobia.apply(targetEntity);
        }
        Atychiphobia.apply(user);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
            if (!Atychiphobia.isWindowOpen(serverPlayer)) return ActionResult.PASS;

            float bonus = (float) serverPlayer.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            Atychiphobia.tryConsumeQte(serverPlayer);

            world.getServer().execute(() -> {
                target.hurtTime = 0;
                target.timeUntilRegen = 0;
                target.damage(serverPlayer.getDamageSources().playerAttack(serverPlayer), bonus);
            });

            return ActionResult.PASS;
        });
    }
}
