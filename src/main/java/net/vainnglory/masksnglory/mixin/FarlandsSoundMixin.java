package net.vainnglory.masksnglory.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSoundInstance.class)
public class FarlandsSoundMixin {

    @Shadow protected SoundCategory category;

    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void masksnglory$muffleFarlandsSound(CallbackInfoReturnable<Float> cir) {
        if (category == SoundCategory.AMBIENT || category == SoundCategory.MUSIC) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        if (!FarlandsHelper.isInFarlands(client.player.getX(), client.player.getZ())) return;
        cir.setReturnValue(cir.getReturnValue() * 0.25f);
    }
}
