package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AtychiphobiaPacket {

    public static final Identifier STATE_ID = new Identifier("masks-n-glory", "atychiphobia_state");
    public static final Identifier QTE_ID = new Identifier("masks-n-glory", "atychiphobia_qte");
    public static final Identifier SUCCESS_ID = new Identifier("masks-n-glory", "atychiphobia_success");

    public static void sendState(ServerPlayerEntity player, int stage) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(stage);
        ServerPlayNetworking.send(player, STATE_ID, buf);
    }

    public static void sendQte(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, QTE_ID, PacketByteBufs.empty());
    }

    public static void sendSuccess(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, SUCCESS_ID, PacketByteBufs.empty());
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(STATE_ID, (client, handler, buf, responseSender) -> {
            int stage = buf.readByte();
            client.execute(() -> AtychiphobiaOverlay.setStage(stage));
        });

        ClientPlayNetworking.registerGlobalReceiver(QTE_ID, (client, handler, buf, responseSender) ->
                client.execute(AtychiphobiaOverlay::triggerQte));

        ClientPlayNetworking.registerGlobalReceiver(SUCCESS_ID, (client, handler, buf, responseSender) ->
                client.execute(AtychiphobiaOverlay::onQteSuccess));
    }
}
