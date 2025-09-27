package net.vainnglory.masksnglory;

import net.fabricmc.api.ModInitializer;

import net.vainnglory.masksnglory.item.ModItemGroups;
import net.vainnglory.masksnglory.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasksNGlory implements ModInitializer {
	public static final String MOD_ID = "masks-n-glory";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();

		LOGGER.info("Hello Fabric world!");
	}
}