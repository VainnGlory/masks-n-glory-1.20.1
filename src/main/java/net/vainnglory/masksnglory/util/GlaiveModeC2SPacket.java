package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.enchantments.AfterlifeEnchantment;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.custom.GlaiveItem;

public class GlaiveModeC2SPacket {
    public static final Identifier ID = new Identifier("masks-n-glory", "cycle_glaive_mode");

    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) ->
                server.execute(() -> {
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (stack.getItem() instanceof GlaiveItem
                                && EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, stack) > 0) {
                            AfterlifeEnchantment.cycleMode(stack);
                            break;
                        }
                    }
                }));
    }
}
