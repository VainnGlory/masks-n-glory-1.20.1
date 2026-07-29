package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.sound.FarlandsHumSound;
import net.vainnglory.masksnglory.sound.FarlandsWindSound;
import net.vainnglory.masksnglory.world.FarlandsHelper;

public class FarlandsAmbience {
    private static FarlandsWindSound windSound = null;
    private static FarlandsHumSound humSound = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            if (!client.world.getRegistryKey().equals(World.OVERWORLD)) return;
            PlayerEntity player = client.player;
            SoundManager sm = client.getSoundManager();
            boolean inFarlands = FarlandsHelper.isInFarlands(player.getX(), player.getZ());
            boolean aboveY30 = player.getY() >= 30;

            if (inFarlands && aboveY30 && (windSound == null || !sm.isPlaying(windSound))) {
                windSound = new FarlandsWindSound(player);
                sm.play(windSound);
            }

            if (inFarlands && !aboveY30 && (humSound == null || !sm.isPlaying(humSound))) {
                humSound = new FarlandsHumSound(player);
                sm.play(humSound);
            }
        });
    }
}
