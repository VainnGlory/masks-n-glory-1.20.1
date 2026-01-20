package net.vainnglory.masksnglory;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.vainnglory.masksnglory.datagen.ModModelProvider;
import net.vainnglory.masksnglory.datagen.ModPaintingVariantGenerator;
import net.vainnglory.masksnglory.item.ModItemTagProvider;


public class MasksNGloryDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModPaintingVariantGenerator::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModModelProvider::new);
	}
}


