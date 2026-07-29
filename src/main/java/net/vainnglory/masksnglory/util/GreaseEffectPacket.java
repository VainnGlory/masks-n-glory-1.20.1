package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GreaseEffectPacket {

    public static final Identifier START_ID = new Identifier("masks-n-glory", "grease_start");
    public static final Identifier STOP_ID = new Identifier("masks-n-glory", "grease_stop");

    public static void sendStart(ServerPlayerEntity player, int stacks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(stacks);
        ServerPlayNetworking.send(player, START_ID, buf);
    }

    public static void sendStop(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, STOP_ID, PacketByteBufs.empty());
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(START_ID, (client, handler, buf, responseSender) -> {
            int stacks = buf.readInt();
            client.execute(() -> {
                if (client.player != null && !GreaseClientState.isGreased()) {
                    GreaseClientState.captureOriginalStepHeight(client.player.getStepHeight());
                }
                GreaseClientState.setGreased(true, stacks);
                if (client.player != null) {
                    client.player.setStepHeight(Grease.GREASE_STEP_HEIGHT);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(STOP_ID, (client, handler, buf, responseSender) ->
                client.execute(() -> {
                    GreaseClientState.setGreased(false, 0);
                    if (client.player != null) {
                        client.player.setStepHeight(GreaseClientState.getOriginalStepHeight());
                    }
                    GreaseClientState.resetCapturedStepHeight();
                }));
    }
}