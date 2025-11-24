package net.vainnglory.masksnglory.entity;


import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntity;

public class ModEntities {

    public static final EntityType<MaelstromEntity> MAELSTROME = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(MasksNGlory.MOD_ID, "maelstrom"),
            FabricEntityTypeBuilder.<MaelstromEntity>create(SpawnGroup.MISC, MaelstromEntity::new)
                    .dimensions(EntityDimensions.fixed(1f, 1f)).build());


    public static void registerModEntities() {
        MasksNGlory.LOGGER.info("Registering Entities for " + MasksNGlory.MOD_ID);
    }

}
