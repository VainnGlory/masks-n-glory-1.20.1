package net.vainnglory.masksnglory.util;

import net.vainnglory.masksnglory.item.custom.RetributionHelmet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FlashAttackPacket {

    public static final Identifier ID = new Identifier("masks-n-glory", "flash_attack");

    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> handleFlashAttack(player));
        });
    }

    private static void handleFlashAttack(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);

        if (!(helmet.getItem() instanceof RetributionHelmet)) {
            player.sendMessage(Text.literal("You must wear the Golden Retribution Helmet!"), true);
            return;
        }

        if (RetributionHelmet.canActivate(helmet)) {
            RetributionHelmet.activateFlash(player, helmet);
            player.sendMessage(Text.literal("FlashBanged!"), true);
        } else {
            float stored = RetributionHelmet.getStoredDamage(helmet);
            player.sendMessage(
                    Text.literal(String.format("Not enough stored damage! (%.1f/20.0)", stored)),
                    true
            );
        }
    }
}
