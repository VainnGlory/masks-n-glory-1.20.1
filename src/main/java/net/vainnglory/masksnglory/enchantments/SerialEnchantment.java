package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SerialEnchantment extends Enchantment {

    private static final Map<UUID, Integer> comboCount = new HashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();

    private static final long COMBO_TIMEOUT_MS = 2000;
    private static final int MAX_COMBO = 6;
    private static final int HEAL_START_COMBO = 4;
    private static final float MAX_BONUS_DAMAGE = 1.5f;
    private static final float MAX_BONUS_HEAL = 2.0f;

    public SerialEnchantment() {
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

    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.RUSTED_SWORD) || stack.isOf(ModItems.PRIDE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    private static int getCombo(PlayerEntity player) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        Long lastHit = lastHitTime.get(playerId);

        if (lastHit == null || (currentTime - lastHit) > COMBO_TIMEOUT_MS) {
            comboCount.put(playerId, 0);
        }

        return comboCount.getOrDefault(playerId, 0);
    }

    private static void incrementCombo(PlayerEntity player) {
        UUID playerId = player.getUuid();
        int currentCombo = comboCount.getOrDefault(playerId, 0);
        comboCount.put(playerId, Math.min(currentCombo + 1, MAX_COMBO));
        lastHitTime.put(playerId, System.currentTimeMillis());
    }

    private static float calculateBonusDamage(int combo) {
        float progress = Math.min((float) combo / MAX_COMBO, 1.0f);
        return MAX_BONUS_DAMAGE * progress;
    }

    private static float calculateHealAmount(int combo) {
        if (combo < HEAL_START_COMBO) {
            return 0f;
        }
        float progress = (float) (combo - HEAL_START_COMBO) / (MAX_COMBO - HEAL_START_COMBO);
        return MAX_BONUS_HEAL * progress;
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient && entity instanceof LivingEntity target) {
                float cooldown = player.getAttackCooldownProgress(0.0f);
                ItemStack weapon = player.getMainHandStack();
                int level = EnchantmentHelper.getLevel(ModEnchantments.SERIAL, weapon);

                if (level > 0 && cooldown >= 0.85f) {
                    incrementCombo(player);
                    int combo = getCombo(player);

                    float bonusDamage = calculateBonusDamage(combo);
                    float healAmount = calculateHealAmount(combo);

                    world.getServer().execute(() -> {
                        if (bonusDamage > 0) {
                            target.hurtTime = 0;
                            target.timeUntilRegen = 0;
                            target.damage(player.getDamageSources().playerAttack(player), bonusDamage);
                        }

                        if (healAmount > 0) {
                            player.heal(healAmount);
                        }
                    });
                }
            }
            return ActionResult.PASS;
        });
    }
}