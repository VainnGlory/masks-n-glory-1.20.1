package net.vainnglory.masksnglory.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.vainnglory.masksnglory.world.FarlandsBiomeCache;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class FarlandsBiomeMixin {

    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void masksnglory$farlandsBiome(
            int x, int y, int z,
            MultiNoiseUtil.MultiNoiseSampler noise,
            CallbackInfoReturnable<RegistryEntry<Biome>> cir
    ) {
        if (FarlandsBiomeCache.nullBiomeEntry == null) return;
        if (Math.abs(x) > (FarlandsHelper.THRESHOLD >> 2) || Math.abs(z) > (FarlandsHelper.THRESHOLD >> 2)) {
            cir.setReturnValue(FarlandsBiomeCache.nullBiomeEntry);
        }
    }
}
