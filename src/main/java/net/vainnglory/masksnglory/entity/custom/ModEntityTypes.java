package net.vainnglory.masksnglory.entity.custom;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

public class ModEntityTypes {
    public static final EntityType<MaelstromEntity> MAELSTROM_ENTITY_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MasksNGlory.MOD_ID, "pale_sword"),
            FabricEntityTypeBuilder.<MaelstromEntity>create(SpawnGroup.MISC, MaelstromEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<PaleSteelCoinEntity> PALE_STEEL_COIN_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MasksNGlory.MOD_ID, "pale_steel_coin"),
            FabricEntityTypeBuilder.<PaleSteelCoinEntity>create(SpawnGroup.MISC, PaleSteelCoinEntity::new)
                    .dimensions(EntityDimensions.fixed(0.4F, 0.4F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<SoulProjectileEntity> SOUL_PROJECTILE_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MasksNGlory.MOD_ID, "soul_projectile"),
            FabricEntityTypeBuilder.<SoulProjectileEntity>create(SpawnGroup.MISC, SoulProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.3F, 0.3F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<SoulRavagerEntity> SOUL_RAVAGER_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MasksNGlory.MOD_ID, "soul_ravager"),
            FabricEntityTypeBuilder.<SoulRavagerEntity>create(SpawnGroup.MONSTER, SoulRavagerEntity::new)
                    .dimensions(EntityDimensions.fixed(1.95F, 2.2F))
                    .trackRangeBlocks(80)
                    .trackedUpdateRate(3)
                    .build()
    );


    public static void registerEntityTypes() {}

    public static void registerModEntities() {
        MasksNGlory.LOGGER.info("Registering Entities for " + MasksNGlory.MOD_ID);
    }
}
