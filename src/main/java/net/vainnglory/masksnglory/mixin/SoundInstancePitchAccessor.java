package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.sound.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface SoundInstancePitchAccessor {
    @Accessor("pitch")
    void masksnglory$setPitch(float pitch);
}
