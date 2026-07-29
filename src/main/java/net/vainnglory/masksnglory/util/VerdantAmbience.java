package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.sound.VerdantAmbientSound;
import net.vainnglory.masksnglory.world.ModDimensions;

public class VerdantAmbience {
    private static SoundInstance currentSound = null;
    private static int delayTicks = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            SoundManager sm = client.getSoundManager();

            if (!client.world.getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) {
                if (currentSound != null && sm.isPlaying(currentSound)) {
                    sm.stop(currentSound);
                }
                currentSound = null;
                delayTicks = 0;
                return;
            }

            if (currentSound != null && sm.isPlaying(currentSound)) return;

            if (delayTicks > 0) {
                delayTicks--;
                return;
            }

            delayTicks = 60 + client.player.getRandom().nextInt(100);

            int roll = client.player.getRandom().nextInt(5);
            SoundEvent next;
            if (roll == 0) {
                next = MasksNGlorySounds.AMBIENT_VERDANT_CHEERS;
            } else if (roll <= 2) {
                next = MasksNGlorySounds.AMBIENT_VERDANT_BIRD1;
            } else {
                next = MasksNGlorySounds.AMBIENT_VERDANT_BIRD2;
            }

            currentSound = new VerdantAmbientSound(next);
            sm.play(currentSound);
        });
    }
}
