package net.vainnglory.masksnglory.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.vainnglory.masksnglory.MasksNGlory;

public class ModDimensions {
    public static final RegistryKey<World> VERDANT_MEMORY_KEY = RegistryKey.of(
            RegistryKeys.WORLD, new Identifier(MasksNGlory.MOD_ID, "verdant_memory")
    );
    public static final RegistryKey<DimensionType> VERDANT_MEMORY_TYPE_KEY = RegistryKey.of(
            RegistryKeys.DIMENSION_TYPE, new Identifier(MasksNGlory.MOD_ID, "verdant_memory")
    );
}
