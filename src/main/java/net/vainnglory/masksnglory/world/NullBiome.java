package net.vainnglory.masksnglory.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.vainnglory.masksnglory.MasksNGlory;

public class NullBiome {
    public static final RegistryKey<Biome> KEY = RegistryKey.of(RegistryKeys.BIOME, new Identifier(MasksNGlory.MOD_ID, "null"));
}
