package net.vainnglory.masksnglory.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.vainnglory.masksnglory.datagen.provider.PaintingTagProvider;
import net.vainnglory.masksnglory.painting.ModPaintings;

import java.util.concurrent.CompletableFuture;

public class ModPaintingVariantGenerator extends PaintingTagProvider {
    public ModPaintingVariantGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(PaintingVariantTags.PLACEABLE)
                .add(ModPaintings.HUSK)
                .add(ModPaintings.STOP)
                .add(ModPaintings.RATS);

    }
}
