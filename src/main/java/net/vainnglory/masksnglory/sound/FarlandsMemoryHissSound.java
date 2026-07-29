package net.vainnglory.masksnglory.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;

public class FarlandsMemoryHissSound extends AbstractSoundInstance implements TickableSoundInstance {
    private static final float BASE_VOLUME = 0.28f;
    private final PlayerEntity player;
    private boolean done = false;

    public FarlandsMemoryHissSound(PlayerEntity player) {
        super(MasksNGlorySounds.AMBIENT_FARLANDS_MEMORY_HISS, SoundCategory.AMBIENT, Random.create());
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
        MinecraftClient client = MinecraftClient.getInstance();
        boolean wrongDimension = client.world == null || !client.world.getRegistryKey().equals(World.OVERWORLD);
        boolean shielded = FarlandsHelper.hasActiveAmalgam(player);
        if (player.isRemoved() || wrongDimension || shielded || !FarlandsHelper.isInInnerFarlands(player.getX(), player.getZ())) {
            done = true;
            return;
        }
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
