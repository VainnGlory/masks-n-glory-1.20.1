package net.vainnglory.masksnglory;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntity;
import net.vainnglory.masksnglory.item.ModItemGroups;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.util.ModLootTableModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.*;


public class MasksNGlory implements ModInitializer {
	public static final String MOD_ID = "masks-n-glory";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();

        ModLootTableModifier.modifyLootTables();
        ModEntities.registerModEntities();


		LOGGER.info("Starting The 9/5");
	}
}