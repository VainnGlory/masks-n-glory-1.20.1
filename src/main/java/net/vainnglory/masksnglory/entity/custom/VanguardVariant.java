package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.vainnglory.masksnglory.world.ModDimensions;

import java.util.UUID;

public enum VanguardVariant {
    VANGUARD_ONE(UUID.fromString("d1848a30-b4c9-4f64-817d-0d09377b125c"),
            ArmorTrimPatterns.SPIRE, ArmorTrimMaterials.EMERALD,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.EYE, ArmorTrimMaterials.EMERALD,
            120.0, 0.33, 5.0, 0.6, 5, 20),

    VANGUARD_TWO(UUID.fromString("ee77d450-64e4-40bc-a70d-78bcf91ffebe"),
            ArmorTrimPatterns.DUNE, ArmorTrimMaterials.REDSTONE,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.EYE, ArmorTrimMaterials.REDSTONE,
            95.0, 0.30, 4.0, 0.45, 1, 10),

    VANGUARD_THREE(UUID.fromString("3af214f5-7f9a-464f-9240-dc96148d8bd4"),
            ArmorTrimPatterns.DUNE, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.EYE, ArmorTrimMaterials.QUARTZ,
            75.0, 0.28, 3.0, 0.3, 0, 10),

    VANGUARD_FOUR(UUID.fromString("206810d4-14cd-4600-84d9-5f5325c81b54"),
            ArmorTrimPatterns.DUNE, ArmorTrimMaterials.GOLD,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.EYE, ArmorTrimMaterials.GOLD,
            120.0, 0.33, 5.0, 0.6, 5, 20),

    VANGUARD_FIVE(UUID.fromString("f8951f83-bfaf-4a75-80bf-000824037387"),
            ArmorTrimPatterns.DUNE, ArmorTrimMaterials.AMETHYST,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.SNOUT, ArmorTrimMaterials.QUARTZ,
            ArmorTrimPatterns.EYE, ArmorTrimMaterials.AMETHYST,
            95.0, 0.30, 4.0, 0.45, 1, 10);

    public static final BlockPos HOME_POS = new BlockPos(0, 81, 0);
    public static final RegistryKey<net.minecraft.world.World> HOME_DIMENSION = ModDimensions.VERDANT_MEMORY_KEY;

    public final UUID skinUuid;
    public final RegistryKey<ArmorTrimPattern> chestPattern;
    public final RegistryKey<ArmorTrimMaterial> chestMaterial;
    public final RegistryKey<ArmorTrimPattern> helmetPattern;
    public final RegistryKey<ArmorTrimMaterial> helmetMaterial;
    public final RegistryKey<ArmorTrimPattern> legsPattern;
    public final RegistryKey<ArmorTrimMaterial> legsMaterial;
    public final RegistryKey<ArmorTrimPattern> bootsPattern;
    public final RegistryKey<ArmorTrimMaterial> bootsMaterial;
    public final double maxHealth;
    public final double movementSpeed;
    public final double attackDamage;
    public final double knockbackResistance;
    public final int goldenApples;
    public final int cookedSteaks;

    VanguardVariant(UUID skinUuid,
                    RegistryKey<ArmorTrimPattern> chestPattern, RegistryKey<ArmorTrimMaterial> chestMaterial,
                    RegistryKey<ArmorTrimPattern> helmetPattern, RegistryKey<ArmorTrimMaterial> helmetMaterial,
                    RegistryKey<ArmorTrimPattern> legsPattern, RegistryKey<ArmorTrimMaterial> legsMaterial,
                    RegistryKey<ArmorTrimPattern> bootsPattern, RegistryKey<ArmorTrimMaterial> bootsMaterial,
                    double maxHealth, double movementSpeed, double attackDamage, double knockbackResistance,
                    int goldenApples, int cookedSteaks) {
        this.skinUuid = skinUuid;
        this.chestPattern = chestPattern;
        this.chestMaterial = chestMaterial;
        this.helmetPattern = helmetPattern;
        this.helmetMaterial = helmetMaterial;
        this.legsPattern = legsPattern;
        this.legsMaterial = legsMaterial;
        this.bootsPattern = bootsPattern;
        this.bootsMaterial = bootsMaterial;
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.attackDamage = attackDamage;
        this.knockbackResistance = knockbackResistance;
        this.goldenApples = goldenApples;
        this.cookedSteaks = cookedSteaks;
    }
}
