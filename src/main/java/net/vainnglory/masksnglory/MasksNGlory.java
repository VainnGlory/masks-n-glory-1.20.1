package net.vainnglory.masksnglory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.enchantments.*;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;
import net.vainnglory.masksnglory.entity.custom.SoulRavagerEntity;
import net.vainnglory.masksnglory.events.PlayerDeathEffects;
import net.vainnglory.masksnglory.item.ModItemGroups;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import net.vainnglory.masksnglory.painting.ModPaintings;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.*;
import net.vainnglory.masksnglory.world.ModWorldGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MasksNGlory implements ModInitializer {
    public static final String MOD_ID = "masks-n-glory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final GameRules.Key<GameRules.BooleanRule> DO_PROPERTY_DAMAGE = GameRuleRegistry.register(
            "doPropertyDamage", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

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
        AfterlifeEnchantment.registerCallbacks();
        RiskEnchantment.registerAttackCallback();
        PactEnchantment.registerAttackCallback();
        LockoutEnchantment.registerAttackCallback();

        RegisterMNGItems.registerPaleItems();

        ModEntityTypes.registerEntityTypes();
        FabricDefaultAttributeRegistry.register(ModEntityTypes.SOUL_RAVAGER_TYPE, SoulRavagerEntity.createAttributes());


        FlashAttackPacket.registerReceiver();

        MaskAbilityManager.registerCallbacks();

        ModWorldGeneration.addFeaturesToBiomes();

        ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> {
            UUID id = handler.player.getUuid();
            ActorManager.offScriptActive.remove(id);
            ActorManager.actorSneakTicks.remove(id);
            ActorManager.lastDamageTicks.remove(id);
            ActorManager.offScriptCooldowns.remove(id);
            ActorManager.sympathyInProgress.remove(id);
            MaskAbilityManager.clearPlayerData(id);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.hasStatusEffect(ModEffects.SEIZED)) {
                    player.setSprinting(false);
                }
            }
        });


        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PlayerDeathEffects.onPlayerDeath(player, damageSource);
                PlayerDeathEffects.onAnyEntityDeath(entity, damageSource);
            }
        });

        final Map<UUID, Integer> pinningAirTicks = new HashMap<>();
        final Set<UUID> pinningSlamming = new HashSet<>();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();
                if (!player.hasStatusEffect(ModEffects.PINNING)) {
                    pinningAirTicks.remove(id);
                    pinningSlamming.remove(id);
                    continue;
                }

                if (pinningSlamming.contains(id)) {
                    if (player.isOnGround()) {
                        pinningSlamming.remove(id);
                    } else {
                        player.fallDistance = 0f;
                    }
                }

                if (!player.isOnGround()) {
                    int ticks = pinningAirTicks.getOrDefault(id, 0) + 1;
                    if (ticks >= 20) {
                        if (player.getAbilities().flying) {
                            player.getAbilities().flying = false;
                            player.sendAbilitiesUpdate();
                        }
                        player.setVelocity(player.getVelocity().x, -3.0, player.getVelocity().z);
                        player.velocityModified = true;
                        pinningSlamming.add(id);
                        pinningAirTicks.remove(id);
                    } else {
                        pinningAirTicks.put(id, ticks);
                    }
                } else {
                    pinningAirTicks.remove(id);
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();

                boolean wearingPaleSet =
                        player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.PALE_HELMET) &&
                                player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.PALE_CHESTPLATE) &&
                                player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.PALE_LEGGINGS) &&
                                player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.PALE_BOOTS);

                if (wearingPaleSet && !player.hasStatusEffect(ModEffects.ACTOR)) {
                    player.addStatusEffect(new StatusEffectInstance(ModEffects.ACTOR, Integer.MAX_VALUE, 0, false, false, true));
                } else if (!wearingPaleSet && player.hasStatusEffect(ModEffects.ACTOR)) {
                    player.removeStatusEffect(ModEffects.ACTOR);
                    ActorManager.offScriptActive.remove(id);
                    ActorManager.actorSneakTicks.remove(id);
                    ActorManager.offScriptCooldowns.remove(id);
                    ActorManager.lastDamageTicks.remove(id);
                }

                if (!player.hasStatusEffect(ModEffects.ACTOR)) continue;

                if (ActorManager.offScriptCooldowns.getOrDefault(id, 0) > 0) {
                    ActorManager.offScriptCooldowns.merge(id, -1, Integer::sum);
                }

                if (ActorManager.offScriptActive.contains(id) && !player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                    ActorManager.offScriptActive.remove(id);
                    player.removeStatusEffect(ModEffects.OFF_SCRIPT_FLAG);
                }

                if (ActorManager.offScriptCooldowns.getOrDefault(id, 0) <= 0 && !ActorManager.offScriptActive.contains(id)) {
                    boolean inCombat = (server.getTicks() - ActorManager.lastDamageTicks.getOrDefault(id, 0L)) <= 100;
                    if (player.isSneaking() && inCombat) {
                        int sneakTicks = ActorManager.actorSneakTicks.merge(id, 1, Integer::sum);
                        if (sneakTicks >= 40) {
                            ActorManager.offScriptActive.add(id);
                            ActorManager.actorSneakTicks.remove(id);
                            ActorManager.offScriptCooldowns.put(id, 600);
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 100, 0, false, false, false));
                            player.addStatusEffect(new StatusEffectInstance(ModEffects.OFF_SCRIPT_FLAG, 100, 0, false, false, true));

                            StatusEffectInstance flagEffect = player.getStatusEffect(ModEffects.OFF_SCRIPT_FLAG);
                            if (flagEffect != null) {
                                EntityStatusEffectS2CPacket flagPacket = new EntityStatusEffectS2CPacket(player.getId(), flagEffect);
                                for (ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
                                    tracker.networkHandler.sendPacket(flagPacket);
                                }
                            }
                        }
                    } else {
                        ActorManager.actorSneakTicks.remove(id);
                    }
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();
            Set<UUID> shouldHaveEffect = new HashSet<>();

            for (ServerPlayerEntity player : allPlayers) {
                ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
                if (!helmet.isOf(ModItems.PALE_HELMET)) continue;
                if (EnchantmentHelper.getLevel(ModEnchantments.STUNT_DOUBLE, helmet) <= 0) continue;
                Vec3d pos = player.getPos();
                for (ServerPlayerEntity nearby : allPlayers) {
                    if (nearby.getWorld() != player.getWorld()) continue;
                    if (nearby.squaredDistanceTo(pos) <= 225.0) {
                        shouldHaveEffect.add(nearby.getUuid());
                    }
                }
            }

            for (ServerPlayerEntity player : allPlayers) {
                boolean has = player.hasStatusEffect(ModEffects.STUNT_DOUBLE);
                boolean should = shouldHaveEffect.contains(player.getUuid());
                if (should && !has) {
                    player.addStatusEffect(new StatusEffectInstance(ModEffects.STUNT_DOUBLE, Integer.MAX_VALUE, 0, false, false, true));
                } else if (!should && has) {
                    player.removeStatusEffect(ModEffects.STUNT_DOUBLE);
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(
                new Identifier("masks-n-glory", "cycle_glaive_mode"),
                (server, player, handler, buf, responseSender) -> server.execute(() -> {
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        net.minecraft.item.ItemStack s = player.getInventory().getStack(i);
                        if (s.getItem() instanceof net.vainnglory.masksnglory.item.custom.GlaiveItem
                                && net.minecraft.enchantment.EnchantmentHelper.getLevel(
                                net.vainnglory.masksnglory.enchantments.ModEnchantments.AFTERLIFE, s) > 0) {
                            AfterlifeEnchantment.cycleMode(s);
                            break;
                        }
                    }
                })
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> AfterlifeEnchantment.tickSummonedUndead(server));


        LOGGER.info("Starting The 9/5");

        //thank you, @InfinityFarzad (https://modrinth.com/user/InfinityFarzad) for the "Pale Steel Greatsword" texture !

        //Thank you Iron_fist for the code for the "Prideful Husk"'s ability
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