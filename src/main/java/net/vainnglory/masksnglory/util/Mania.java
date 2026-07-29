package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.enchantments.NotorietyEnchantment;
import net.vainnglory.masksnglory.item.custom.PrideItem;

public class Mania {

    private static final int REFRESH_DURATION = 30;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                int amplifier = getAmplifier(player.getMainHandStack());

                if (amplifier < 0) {
                    if (player.hasStatusEffect(ModEffects.MANIA)) {
                        player.removeStatusEffect(ModEffects.MANIA);
                    }
                    continue;
                }

                StatusEffectInstance current = player.getStatusEffect(ModEffects.MANIA);
                if (current == null || current.getAmplifier() != amplifier || current.getDuration() < REFRESH_DURATION - 5) {
                    player.addStatusEffect(new StatusEffectInstance(ModEffects.MANIA, REFRESH_DURATION, amplifier, false, false, true));
                }
            }
        });
    }

    public static int getKillCount(ItemStack stack) {
        if (!(stack.getItem() instanceof PrideItem)) return 0;
        if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, stack) <= 0) return 0;
        NbtList kills = NotorietyEnchantment.getKillList(stack);
        return kills.size();
    }

    public static int getAmplifier(ItemStack stack) {
        int names = getKillCount(stack);
        if (names < 2) return -1;
        return names / 3;
    }

    public static float getVolumeMultiplier(ItemStack stack) {
        int amplifier = getAmplifier(stack);
        if (amplifier <= 0) return 1.0F;
        return Math.max(0.15F, 1.0F - amplifier * 0.2F);
    }

    public static int getTintAlpha(ItemStack stack) {
        int names = getKillCount(stack);
        if (names < 2) return 0;
        return Math.min(170, 10 + (names - 2) * 7);
    }
}
