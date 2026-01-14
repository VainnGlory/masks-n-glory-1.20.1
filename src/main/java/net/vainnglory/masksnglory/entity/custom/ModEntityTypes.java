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
            new Identifier(MasksNGlory.MOD_ID, "maelstrom"),
            FabricEntityTypeBuilder.<MaelstromEntity>create(SpawnGroup.MISC, MaelstromEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void registerEntityTypes() {
    }
    public static void registerModEntities() {
        MasksNGlory.LOGGER.info("Registering Entities for " + MasksNGlory.MOD_ID);
    }

}
