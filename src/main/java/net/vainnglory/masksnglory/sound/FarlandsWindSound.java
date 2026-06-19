package net.vainnglory.masksnglory.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;
import net.vainnglory.masksnglory.mixin.MusicTrackerAccessor;
import net.vainnglory.masksnglory.world.FarlandsHelper;

public class FarlandsWindSound extends AbstractSoundInstance implements TickableSoundInstance {
    private static final float BASE_VOLUME = 0.6f;
    private final PlayerEntity player;
    private boolean done = false;

    public FarlandsWindSound(PlayerEntity player) {
        super(MasksNGlorySounds.AMBIENT_FARLANDS_WIND, SoundCategory.AMBIENT, Random.create());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = BASE_VOLUME;
        this.pitch = 1.0f;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (player.isRemoved()
                || !FarlandsHelper.isInFarlands(player.getX(), player.getZ())
                || player.getY() < 30) {
            done = true;
            return;
        }
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        MinecraftClient client = MinecraftClient.getInstance();
        SoundInstance currentMusic = ((MusicTrackerAccessor) client.getMusicTracker()).masksnglory$getCurrentMusic();
        boolean musicPlaying = currentMusic != null && client.getSoundManager().isPlaying(currentMusic);
        this.volume = musicPlaying ? BASE_VOLUME * 0.4f : BASE_VOLUME;
    }

    @Override
    public boolean isDone() {
        return done;
    }
}