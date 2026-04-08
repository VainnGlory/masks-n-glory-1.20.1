package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IncumbentEnchantment extends Enchantment {

    private static final int MAX_BONUS = 3;
    private static final long RESET_MS = 5000L;
    private static final Map<UUID, Map<UUID, Integer>> tradeCount = new HashMap<>();
    private static final Map<UUID, Map<UUID, Long>> lastTradeTime = new HashMap<>();

    public IncumbentEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 10; }
    @Override public int getMaxPower(int level) { return 50; }
    @Override public boolean isTreasure() { return false; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return true; }
    @Override public boolean isAvailableForRandomSelection() { return true; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.PRIDE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    @Override
    public boolean canAccept(Enchantment other) {
        return !(other instanceof TemperEnchantment) && !(other instanceof NotorietyEnchantment) && super.canAccept(other);
    }

    private static void recordTrade(UUID incumbentId, UUID opponentId) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> times = lastTradeTime.computeIfAbsent(incumbentId, k -> new HashMap<>());
        Map<UUID, Integer> counts = tradeCount.computeIfAbsent(incumbentId, k -> new HashMap<>());
        Long last = times.get(opponentId);
        if (last != null && (now - last) > RESET_MS) {
            counts.put(opponentId, 0);
        }
        int current = counts.getOrDefault(opponentId, 0);
        counts.put(opponentId, Math.min(MAX_BONUS, current + 1));
        times.put(opponentId, now);
    }

    private static int getBonus(UUID incumbentId, UUID opponentId) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> times = lastTradeTime.getOrDefault(incumbentId, Collections.emptyMap());
        Long last = times.get(opponentId);
        if (last == null || (now - last) > RESET_MS) return 0;
        return tradeCount.getOrDefault(incumbentId, Collections.emptyMap()).getOrDefault(opponentId, 0);
    }

    public static void cleanup(UUID id) {
        tradeCount.remove(id);
        lastTradeTime.remove(id);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!(entity instanceof PlayerEntity target)) return ActionResult.PASS;

            ItemStack attackerWeapon = player.getMainHandStack();
            boolean attackerHasIncumbent = EnchantmentHelper.getLevel(ModEnchantments.INCUMBENT, attackerWeapon) > 0;

            ItemStack targetWeapon = target.getMainHandStack();
            boolean targetHasIncumbent = EnchantmentHelper.getLevel(ModEnchantments.INCUMBENT, targetWeapon) > 0;

            if (attackerHasIncumbent) {
                recordTrade(player.getUuid(), target.getUuid());
                int bonus = getBonus(player.getUuid(), target.getUuid());
                if (bonus > 0) {
                    world.getServer().execute(() -> {
                        if (!target.isAlive()) return;
                        target.hurtTime = 0;
                        target.timeUntilRegen = 0;
                        target.damage(target.getDamageSources().playerAttack(player), (float) bonus);
                    });
                }
            }

            if (targetHasIncumbent) {
                recordTrade(target.getUuid(), player.getUuid());
            }

            return ActionResult.PASS;
        });
    }
}
