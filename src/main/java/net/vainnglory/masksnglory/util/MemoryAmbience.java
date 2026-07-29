package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.mixin.MusicTrackerAccessor;
import net.vainnglory.masksnglory.mixin.SoundInstancePitchAccessor;
import net.vainnglory.masksnglory.sound.FarlandsMemoryHissSound;
import net.vainnglory.masksnglory.world.FarlandsHelper;

public class MemoryAmbience {
    private static FarlandsMemoryHissSound hissSound = null;
    private static float wobbleTime = 0.0f;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            if (!client.world.getRegistryKey().equals(World.OVERWORLD)) return;

            PlayerEntity player = client.player;
            SoundManager sm = client.getSoundManager();

            if (!FarlandsHelper.isInInnerFarlands(player.getX(), player.getZ())) {
                wobbleTime = 0.0f;
                return;
            }

            if (FarlandsHelper.hasActiveAmalgam(player)) {
                wobbleTime = 0.0f;
                return;
            }

            if (hissSound == null || !sm.isPlaying(hissSound)) {
                hissSound = new FarlandsMemoryHissSound(player);
                sm.play(hissSound);
            }

            wobbleTime += 1.0f;
            MusicTrackerAccessor tracker = (MusicTrackerAccessor) client.getMusicTracker();
            SoundInstance current = tracker.masksnglory$getCurrentMusic();
            if (current != null && FarlandsMusic.isFarlandsMusic(current) && sm.isPlaying(current)) {
                float wobble = (float) (Math.sin(wobbleTime * 0.015) * 0.014 + Math.sin(wobbleTime * 0.047) * 0.006);
                ((SoundInstancePitchAccessor) current).masksnglory$setPitch(1.0f + wobble);
            }
        });
    }
}
