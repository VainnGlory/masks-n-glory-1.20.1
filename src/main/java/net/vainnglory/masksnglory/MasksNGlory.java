package net.vainnglory.masksnglory;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.enchantments.*;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;
import net.vainnglory.masksnglory.events.PlayerDeathEffects;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.item.ModItemGroups;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.painting.ModPaintings;
import net.vainnglory.masksnglory.util.*;
import net.vainnglory.masksnglory.world.ModWorldGeneration;
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
        ModEffects.registerEffects();

        ModDamageTypes.initialize();

        SerialEnchantment.registerAttackCallback();
        GoldenPanItem.registerCallbacks();
        GuillotineEnchantment.registerAttackCallback();
        ComboEnchantment.registerAttackCallback();
        ComboEnchantment.registerKillCallback();
        AntisepticEnchantment.registerTickCallback();

        RegisterMNGItems.registerPaleItems();

        ModEntityTypes.registerEntityTypes();

        FlashAttackPacket.registerReceiver();

        MaskAbilityManager.registerCallbacks();

        ModWorldGeneration.addFeaturesToBiomes();


        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PlayerDeathEffects.onPlayerDeath(player, damageSource);
                PlayerDeathEffects.onAnyEntityDeath(entity, damageSource);
            }
        });

        final Map<UUID, Integer> pinningAirTicks = new HashMap<>();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();

                if (!player.hasStatusEffect(ModEffects.PINNING)) {
                    pinningAirTicks.remove(id);
                    continue;
                }

                if (player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                    player.sendAbilitiesUpdate();
                    player.setVelocity(player.getVelocity().x, -3.0, player.getVelocity().z);
                    player.velocityModified = true;
                    pinningAirTicks.remove(id);
                    continue;
                }

                if (!player.isOnGround()) {
                    int ticks = pinningAirTicks.getOrDefault(id, 0) + 1;
                    if (ticks >= 40) {
                        player.setVelocity(player.getVelocity().x, -3.0, player.getVelocity().z);
                        player.velocityModified = true;
                        pinningAirTicks.remove(id);
                    } else {
                        pinningAirTicks.put(id, ticks);
                    }
                } else {
                    pinningAirTicks.remove(id);
                }
            }
        });







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
