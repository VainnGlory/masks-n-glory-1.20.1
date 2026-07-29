package net.vainnglory.masksnglory.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class LuneriSwapC2SPacket {
    public static final Identifier ID = new Identifier("masks-n-glory", "luneri_swap");

    @Environment(EnvType.CLIENT)
    public static void send() {
        ClientPlayNetworking.send(ID, PacketByteBufs.create());
    }

    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) ->
                server.execute(() -> LuneriSwap.trigger(player)));
    }
}
