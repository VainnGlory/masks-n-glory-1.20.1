package net.vainnglory.masksnglory.sound;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

public class VerdantAmbientSound extends AbstractSoundInstance {
    public VerdantAmbientSound(SoundEvent sound) {
        super(sound, SoundCategory.AMBIENT, Random.create());
        this.repeat = false;
        this.repeatDelay = 0;
        this.volume = 0.28f;
        this.pitch = 1.0f;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
    }
}
