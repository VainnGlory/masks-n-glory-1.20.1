package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.util.FarlandsMusicManager;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import net.vainnglory.masksnglory.world.ModDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicTracker.class)
public class FarlandsMusicTrackerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void masksnglory$preTickOverride(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return;

        if (client.world.getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) {
            MusicTrackerAccessor self = (MusicTrackerAccessor)(Object)this;
            SoundInstance current = self.masksnglory$getCurrentMusic();
            if (current != null) {
                client.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                self.masksnglory$setCurrentMusic(null);
            }
            ci.cancel();
            return;
        }

        if (!client.world.getRegistryKey().equals(World.OVERWORLD)) return;
        if (!FarlandsHelper.isInFarlands(client.player.getX(), client.player.getZ())) return;

        MusicTrackerAccessor self = (MusicTrackerAccessor)(Object)this;
        SoundInstance current = self.masksnglory$getCurrentMusic();

        if (FarlandsMusicManager.isFarlandsMusic(current)) return;

        if (current != null) {
            client.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
            self.masksnglory$setCurrentMusic(null);
        }
    }
}
