package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.mixin.MusicTrackerAccessor;
import net.vainnglory.masksnglory.mixin.SoundInstanceVolumeAccessor;
import net.vainnglory.masksnglory.world.FarlandsHelper;

public class FarlandsMusicManager {
    private static boolean wasInFarlands = false;
    private static SoundInstance fadingSound = null;
    private static float fadeMultiplier = 1.0f;
    private static final float FADE_SPEED = 0.01f;

    public static boolean isFarlandsMusic(SoundInstance sound) {
        if (sound == null) return false;
        Identifier id = sound.getId();
        return "masks-n-glory".equals(id.getNamespace()) &&
                (id.getPath().equals("music.farlands.a") || id.getPath().equals("music.farlands.b"));
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            boolean inFarlands = client.world.getRegistryKey().equals(World.OVERWORLD)
                    && FarlandsHelper.isInFarlands(client.player.getX(), client.player.getZ());
            MusicTrackerAccessor tracker = (MusicTrackerAccessor) client.getMusicTracker();

            if (inFarlands) {
                if (fadingSound != null) {
                    ((SoundInstanceVolumeAccessor) fadingSound).masksnglory$setVolume(1.0f);
                    fadingSound = null;
                }
                fadeMultiplier = 1.0f;
                if (!wasInFarlands) {
                    client.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                    tracker.masksnglory$setCurrentMusic(null);
                    tracker.masksnglory$setTimeUntilNextSong(0);
                }
            } else {
                if (wasInFarlands) {
                    SoundInstance current = tracker.masksnglory$getCurrentMusic();
                    if (isFarlandsMusic(current) && client.getSoundManager().isPlaying(current)) {
                        fadingSound = current;
                        fadeMultiplier = 1.0f;
                    }
                } else if (fadingSound != null) {
                    fadeMultiplier -= FADE_SPEED;
                    if (fadeMultiplier <= 0.0f) {
                        if (client.getSoundManager().isPlaying(fadingSound)) {
                            client.getSoundManager().stop(fadingSound);
                        }
                        fadingSound = null;
                        fadeMultiplier = 1.0f;
                    } else {
                        ((SoundInstanceVolumeAccessor) fadingSound).masksnglory$setVolume(fadeMultiplier);
                    }
                }
            }

            wasInFarlands = inFarlands;
        });
    }
}