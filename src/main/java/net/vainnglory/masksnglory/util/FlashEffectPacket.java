package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class FlashEffectPacket {

    public static final Identifier ID = new Identifier("masks-n-glory", "flash_effect");

    public static void send(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        ServerPlayNetworking.send(player, ID, buf);
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                if (client.player != null) {
                    FlashOverlayRenderer.triggerFlash();
                }
            });
        });
    }
}
