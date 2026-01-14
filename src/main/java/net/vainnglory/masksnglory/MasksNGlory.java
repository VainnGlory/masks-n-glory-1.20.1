package net.vainnglory.masksnglory;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.enchantments.*;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;
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

    public static final GameRules.Key<GameRules.BooleanRule> DO_PROPERTY_DAMAGE = GameRuleRegistry.register("doPropertyDamage", GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true));

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

        RegisterMNGItems.registerPaleItems();

        ModEntityTypes.registerEntityTypes();

        LOGGER.info("Starting The 9/5");

        //thank you, @InfinityFarzad (https://modrinth.com/user/InfinityFarzad) for the "Pale Steel Greatsword" texture !


        //Thank you Iron_fist for the code for the "Prideful Husk"'s ability'
        //link to his GitHub:
        //https://github.com/jayden-deason/Soulforged

        //MIT License

        //Copyright (c) 2025 Jayden Deason

        //Permission is hereby granted, free of charge, to any person obtaining a copy
        //of this software and associated documentation files (the "Software"), to deal
        //in the Software without restriction, including without limitation the rights
        //to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        //copies of the Software, and to permit persons to whom the Software is
        //furnished to do so, subject to the following conditions:

        //The above copyright notice and this permission notice shall be included in all
        //copies or substantial portions of the Software.

        //THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        //IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        //FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        //AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        //OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        //SOFTWARE.
    }
}
