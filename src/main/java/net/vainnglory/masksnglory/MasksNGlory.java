package net.vainnglory.masksnglory;

import net.fabricmc.api.ModInitializer;

import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.enchantments.*;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.item.ModItemGroups;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.painting.ModPaintings;
import net.vainnglory.masksnglory.util.ModDamageTypes;
import net.vainnglory.masksnglory.util.ModLootTableModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MasksNGlory implements ModInitializer {
    public static final String MOD_ID = "masks-n-glory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModPaintings.registerPaintings();

        MasksNGlorySounds.initialize();

        ModLootTableModifier.modifyLootTables();
        ModEntities.registerModEntities();

        ModEnchantments.registerEnchantments();

        ModDamageTypes.initialize();

        SerialEnchantment.registerAttackCallback();
        SkullBreakerEnchantment.registerAttackCallback();
        SkullBreakerEnchantment.registerTickCallback();
        ImpactEnchantment.registerAttackCallback();
        ImpactEnchantment.registerTickCallback();
        GuillotineEnchantment.registerAttackCallback();
        ComboEnchantment.registerAttackCallback();
        ComboEnchantment.registerKillCallback();
        AntisepticEnchantment.registerTickCallback();
        SoulPhaseEnchantment.registerAttackCallback();

        RegisterMNGItems.registerPaleItems();

        LOGGER.info("Starting The 9/5");

        //thank you, @InfinityFarzad (https://modrinth.com/user/InfinityFarzad) for the "Pale Steel Greatsword" texture !
    }
}
