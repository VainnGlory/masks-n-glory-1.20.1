package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.sound.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface SoundInstanceVolumeAccessor {
    @Accessor("volume")
    void masksnglory$setVolume(float volume);
}