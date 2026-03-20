package net.vainnglory.masksnglory.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

public class ModWorldGeneration {

    public static void addFeaturesToBiomes() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInTheNether(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("masks-n-glory", "ancient_debris_boosted"))
        );
    }
}


