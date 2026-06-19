package net.vainnglory.masksnglory.world;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.vainnglory.masksnglory.MasksNGlory;

public class ModFeatures {
    public static final Feature<DefaultFeatureConfig> VERDANT_TREE =
            new VerdantTreeFeature(DefaultFeatureConfig.CODEC);

    public static final Feature<DefaultFeatureConfig> VERDANT_STONE =
            new VerdantStoneFeature(DefaultFeatureConfig.CODEC);

    public static final Feature<DefaultFeatureConfig> VERDANT_MONOLITH =
            new VerdantMonolithFeature(DefaultFeatureConfig.CODEC);

    public static void register() {
        Registry.register(Registries.FEATURE, new Identifier(MasksNGlory.MOD_ID, "verdant_tree"), VERDANT_TREE);
        Registry.register(Registries.FEATURE, new Identifier(MasksNGlory.MOD_ID, "verdant_stone"), VERDANT_STONE);
        Registry.register(Registries.FEATURE, new Identifier(MasksNGlory.MOD_ID, "verdant_monolith"), VERDANT_MONOLITH);
    }
}
