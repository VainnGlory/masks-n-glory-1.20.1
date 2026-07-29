package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.vainnglory.masksnglory.util.Mania;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemManiaMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onPlay(SoundInstance sound, CallbackInfo ci) {
        if (!(sound instanceof AbstractSoundInstance)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float multiplier = Mania.getVolumeMultiplier(client.player.getMainHandStack());
        if (multiplier >= 1.0F) return;

        SoundInstanceVolumeAccessor accessor = (SoundInstanceVolumeAccessor) sound;
        accessor.masksnglory$setVolume(accessor.masksnglory$getVolume() * multiplier);
    }
}
