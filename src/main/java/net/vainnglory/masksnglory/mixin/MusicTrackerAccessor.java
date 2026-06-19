package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MusicTracker.class)
public interface MusicTrackerAccessor {
    @Accessor("current")
    @Nullable SoundInstance masksnglory$getCurrentMusic();

    @Accessor("current")
    void masksnglory$setCurrentMusic(@Nullable SoundInstance sound);

    @Accessor("timeUntilNextSong")
    void masksnglory$setTimeUntilNextSong(int value);
}
