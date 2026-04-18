package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.TypedActionResult;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExceptionNotCaughtEnchantment extends Enchantment {
    private static final Map<UUID, Long> pendingExceptions = new HashMap<>();
    private static final long EXCEPTION_DURATION_MS = 10000L;

    public ExceptionNotCaughtEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) { return 15; }

    @Override
    public int getMaxPower(int level) { return 50; }

    @Override
    public int getMaxLevel() { return 1; }

    @Override
    public boolean isTreasure() { return true; }

    @Override
    public boolean isAvailableForEnchantedBookOffer() { return false; }

    @Override
    public boolean isAvailableForRandomSelection() { return false; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.NULL_KNIFE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void applyException(LivingEntity target) {
        if (!(target instanceof PlayerEntity player)) return;
        pendingExceptions.put(player.getUuid(), System.currentTimeMillis());
    }

    public static void cleanup(UUID id) {
        pendingExceptions.remove(id);
    }

    public static void registerCallbacks() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
            UUID id = player.getUuid();
            Long timestamp = pendingExceptions.get(id);
            if (timestamp != null && System.currentTimeMillis() - timestamp < EXCEPTION_DURATION_MS) {
                pendingExceptions.remove(id);
                ItemStack stack = player.getStackInHand(hand);
                player.getItemCooldownManager().set(stack.getItem(), 60);
                return TypedActionResult.fail(stack);
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            pendingExceptions.entrySet().removeIf(e -> now - e.getValue() > EXCEPTION_DURATION_MS);
        });
    }
}
