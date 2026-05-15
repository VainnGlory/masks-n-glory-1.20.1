package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GreaseEffectPacket {

    public static final Identifier START_ID = new Identifier("masks-n-glory", "grease_start");
    public static final Identifier STOP_ID = new Identifier("masks-n-glory", "grease_stop");

    public static void sendStart(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, START_ID, PacketByteBufs.empty());
    }

    public static void sendStop(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, STOP_ID, PacketByteBufs.empty());
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(START_ID, (client, handler, buf, responseSender) ->
                client.execute(() -> GreaseClientState.setGreased(true)));
        ClientPlayNetworking.registerGlobalReceiver(STOP_ID, (client, handler, buf, responseSender) ->
                client.execute(() -> GreaseClientState.setGreased(false)));
    }
}
