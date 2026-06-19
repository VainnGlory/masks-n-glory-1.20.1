package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class FarlandsMusicTypeMixin {
    private static RegistryEntry<SoundEvent> entryA = null;
    private static RegistryEntry<SoundEvent> entryB = null;
    private static boolean nextIsA = true;
    private static SoundInstance prevCurrent = null;

    @Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
    private void masksnglory$farlandsMusicType(CallbackInfoReturnable<MusicSound> cir) {
        MinecraftClient client = (MinecraftClient)(Object)this;
        if (client.player == null || client.world == null) {
            prevCurrent = null;
            return;
        }
        if (!client.world.getRegistryKey().equals(World.OVERWORLD)) {
            prevCurrent = null;
            return;
        }
        if (!FarlandsHelper.isInFarlands(client.player.getX(), client.player.getZ())) {
            prevCurrent = null;
            return;
        }
        if (entryA == null) {
            Registries.SOUND_EVENT.getEntry(
                    RegistryKey.of(RegistryKeys.SOUND_EVENT, new Identifier("masks-n-glory", "music.farlands.a"))
            ).ifPresent(e -> entryA = e);
        }
        if (entryB == null) {
            Registries.SOUND_EVENT.getEntry(
                    RegistryKey.of(RegistryKeys.SOUND_EVENT, new Identifier("masks-n-glory", "music.farlands.b"))
            ).ifPresent(e -> entryB = e);
        }
        if (entryA == null || entryB == null) return;

        MusicTrackerAccessor accessor = (MusicTrackerAccessor) client.getMusicTracker();
        SoundInstance current = accessor.masksnglory$getCurrentMusic();

        if (prevCurrent != null && current == null) {
            Identifier prevId = prevCurrent.getId();
            if (prevId.equals(entryA.value().getId()) || prevId.equals(entryB.value().getId())) {
                nextIsA = !nextIsA;
            }
        }
        prevCurrent = current;

        if (current != null) {
            Identifier currentId = current.getId();
            if (currentId.equals(entryA.value().getId())) {
                cir.setReturnValue(new MusicSound(entryA, 3600, 3600, true));
                return;
            }
            if (currentId.equals(entryB.value().getId())) {
                cir.setReturnValue(new MusicSound(entryB, 3600, 3600, true));
                return;
            }
        }

        RegistryEntry<SoundEvent> next = nextIsA ? entryA : entryB;
        cir.setReturnValue(new MusicSound(next, 3600, 3600, true));
    }
}