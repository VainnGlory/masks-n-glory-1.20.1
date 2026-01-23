package net.vainnglory.masksnglory.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class FlashAttackC2SPacket {

    public static final Identifier ID = new Identifier("masks-n-glory", "flash_attack");

    public static void send() {
        ClientPlayNetworking.send(ID, PacketByteBufs.create());
    }
}
