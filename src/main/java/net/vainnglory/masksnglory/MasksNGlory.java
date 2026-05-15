package net.vainnglory.masksnglory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.util.BlackoutAbilityManager;
import net.vainnglory.masksnglory.util.BlackoutC2SPacket;
import net.vainnglory.masksnglory.util.GoldenScrapManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.text.Text;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
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
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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
import net.vainnglory.masksnglory.item.custom.NullKnifeItem;
import net.vainnglory.masksnglory.painting.ModPaintings;
import net.vainnglory.masksnglory.potion.ModPotions;
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

    public static final GameRules.Key<GameRules.BooleanRule> EGO_ONLY = GameRuleRegistry.register(
            "EgoOnly", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

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
        ModPotions.registerPotions();

        ModDamageTypes.initialize();

        NullKnifeItem.registerCallbacks();
        ExceptionNotCaughtEnchantment.registerCallbacks();

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
        TemperEnchantment.registerAttackCallback();
        TemperEnchantment.registerTickCallback();
        IncumbentEnchantment.registerAttackCallback();
        NotorietyEnchantment.registerCallbacks();
        CastIronManager.register();
        GreaseManager.register();

        RegisterMNGItems.registerPaleItems();

        ModEntityTypes.registerEntityTypes();
        FabricDefaultAttributeRegistry.register(ModEntityTypes.SOUL_RAVAGER_TYPE, SoulRavagerEntity.createAttributes());


        FlashAttackPacket.registerReceiver();

        MaskAbilityManager.registerCallbacks();

        ModWorldGeneration.addFeaturesToBiomes();

        BlackoutC2SPacket.registerReceiver();

        ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> {
            UUID id = handler.player.getUuid();
            ActorManager.offScriptActive.remove(id);
            ActorManager.actorSneakTicks.remove(id);
            ActorManager.lastDamageTicks.remove(id);
            ActorManager.offScriptCooldowns.remove(id);
            ActorManager.sympathyInProgress.remove(id);
            MaskAbilityManager.clearPlayerData(id);
            TemperEnchantment.cleanup(id);
            IncumbentEnchantment.cleanup(id);
            NullManager.cleanup(id);
            NullKnifeItem.cleanup(id);
            ExceptionNotCaughtEnchantment.cleanup(id);
            GoldenScrapManager.cleanupOnDisconnect(id);
            GoldenScrapManager.pauseHealthPenalty(id);
            CastIronManager.setBlocking(handler.player, false);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            GoldenScrapManager.resumeHealthPenalty(handler.player.getUuid(), handler.player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID id = oldPlayer.getUuid();
            NullManager.restoreItems(oldPlayer);
            NullManager.cleanup(id);
            NullKnifeItem.cleanup(id);
            ExceptionNotCaughtEnchantment.cleanup(id);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.hasStatusEffect(ModEffects.SEIZED)) {
                    player.setSprinting(false);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> BlackoutAbilityManager.onServerStart(server));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PlayerDeathEffects.onPlayerDeath(player, damageSource);
                PlayerDeathEffects.onAnyEntityDeath(entity, damageSource);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long worldTime = server.getOverworld().getTime();
            NullManager.tick(worldTime);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();
                if (NullManager.needsRestoration(id, worldTime)) {
                    NullManager.restoreItems(player);
                    player.removeStatusEffect(ModEffects.NULL_EFFECT);
                } else if (!NullManager.isAffected(id) && worldTime % 20 == 0) {
                    NullManager.restoreItems(player);
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> BlackoutAbilityManager.tick(server));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                GoldenScrapManager.tickHealthPenalties(server);
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
                        int amplifier = player.getStatusEffect(ModEffects.PINNING).getAmplifier();
                        switch (amplifier) {
                            case 0 -> player.fallDistance = 0f;
                            case 1 -> player.fallDistance = Math.min(player.fallDistance, 5f);
                            case 2 -> player.fallDistance = Math.min(player.fallDistance, 10f);
                            default -> {}
                        }
                    }
                }

                if (!player.isOnGround()) {
                    int amplifier = player.getStatusEffect(ModEffects.PINNING).getAmplifier();
                    int threshold = Math.max(1, 20 - amplifier * 7);
                    int ticks = pinningAirTicks.getOrDefault(id, 0) + 1;
                    if (ticks >= threshold) {
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
                            if (!player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 100, 0, false, false, false));
                            }
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

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LlamaEntity && hand == Hand.MAIN_HAND && !world.isClient()) {
                ItemStack held = player.getStackInHand(hand);
                if (held.isOf(Items.GLASS_BOTTLE)) {
                    if (!player.getAbilities().creativeMode) {
                        held.decrement(1);
                    }
                    player.giveItemStack(PotionUtil.setPotion(new ItemStack(Items.POTION), ModPotions.SPIT));
                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(entity instanceof EndermiteEntity endermite)) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);
            if (!held.isOf(Items.ENDER_EYE)) return ActionResult.PASS;

            if (world.getGameRules().getBoolean(MasksNGlory.EGO_ONLY)) {
                UUID egoUUID = UUID.fromString("d1848a30-b4c9-4f64-817d-0d09377b125c");
                if (!player.getUuid().equals(egoUUID)) {
                    player.sendMessage(Text.literal("your knowledge is far too low to understand this."), true);
                    return ActionResult.FAIL;
                }
            }

            Box searchBox = endermite.getBoundingBox().expand(2.0);
            List<ItemEntity> nearbyItems = world.getEntitiesByType(EntityType.ITEM, searchBox, e -> true);

            int leatherNeeded = 4;
            boolean ingotNeeded = true;
            List<ItemEntity> leatherEntities = new ArrayList<>();
            ItemEntity ingotEntity = null;

            for (ItemEntity itemEntity : nearbyItems) {
                ItemStack stack = itemEntity.getStack();
                if (leatherNeeded > 0 && stack.isOf(Items.LEATHER)) {
                    leatherEntities.add(itemEntity);
                    leatherNeeded -= stack.getCount();
                } else if (ingotNeeded && stack.isOf(ModItems.RUSTED)) {
                    ingotEntity = itemEntity;
                    ingotNeeded = false;
                }
            }

            if (leatherNeeded > 0 || ingotNeeded) return ActionResult.PASS;

            int toConsume = 4;
            for (ItemEntity itemEntity : leatherEntities) {
                if (toConsume <= 0) break;
                ItemStack stack = itemEntity.getStack();
                int take = Math.min(stack.getCount(), toConsume);
                if (take >= stack.getCount()) {
                    itemEntity.discard();
                } else {
                    stack.decrement(take);
                }
                toConsume -= take;
            }

            ItemStack ingotStack = ingotEntity.getStack();
            if (ingotStack.getCount() == 1) {
                ingotEntity.discard();
            } else {
                ingotStack.decrement(1);
            }

            if (!player.getAbilities().creativeMode) {
                held.decrement(1);
            }

            ItemEntity satchelDrop = new ItemEntity(world, endermite.getX(), endermite.getY() + 0.5, endermite.getZ(), new ItemStack(ModItems.HUNTERS_SATCHEL));
            world.spawnEntity(satchelDrop);

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.PORTAL, endermite.getX(), endermite.getY() + 0.5, endermite.getZ(), 40, 0.3, 0.3, 0.3, 0.15);
                serverWorld.playSound(null, endermite.getBlockPos(), SoundEvents.ENTITY_ENDERMITE_DEATH, SoundCategory.NEUTRAL, 1.5f, 0.8f);
            }

            endermite.discard();
            return ActionResult.SUCCESS;
        });


        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            ItemStack handItem = player.getStackInHand(hand);
            if (!handItem.isOf(Items.BLAZE_POWDER)) return ActionResult.PASS;
            BlockPos blockPos = hitResult.getBlockPos();
            if (!world.getBlockState(blockPos).isOf(Blocks.SCULK_SHRIEKER)) return ActionResult.PASS;

            List<ItemEntity> nearbyItems = world.getEntitiesByClass(
                    ItemEntity.class,
                    new Box(blockPos).expand(2.0),
                    e -> !e.isRemoved()
            );

            ItemEntity vanillaBone = null;
            ItemEntity boneAlloyIngot = null;
            for (ItemEntity ie : nearbyItems) {
                if (ie.getStack().isOf(Items.BONE) && vanillaBone == null) vanillaBone = ie;
                else if (ie.getStack().isOf(ModItems.BONE) && boneAlloyIngot == null) boneAlloyIngot = ie;
            }

            if (vanillaBone == null || boneAlloyIngot == null) return ActionResult.PASS;

            vanillaBone.discard();
            boneAlloyIngot.discard();
            if (!player.getAbilities().creativeMode) handItem.decrement(1);

            ItemStack warden = new ItemStack(ModItems.WARDEN);
            if (!player.getInventory().insertStack(warden)) {
                player.dropItem(warden, false);
            }

            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.SCULK_SOUL,
                        blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5,
                        20, 0.5, 0.5, 0.5, 0.04);
                sw.spawnParticles(ParticleTypes.SOUL,
                        blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3, 0.02);
            }

            return ActionResult.SUCCESS;
        });


        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.hasStatusEffect(ModEffects.WARDEN) && player.isTouchingWater()) {
                    player.removeStatusEffect(ModEffects.WARDEN);
                    player.removeStatusEffect(ModEffects.PINNING);
                    player.removeStatusEffect(ModEffects.SEIZED);
                    player.removeStatusEffect(StatusEffects.SLOWNESS);
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity victim && source.getAttacker() instanceof ServerPlayerEntity dealer) {
                GoldenScrapManager.recordDamage(dealer.getUuid(), victim.getUuid(), amount);
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity victim) {
                if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                    GoldenScrapManager.handleKill(killer, victim);
                }
                if (GoldenScrapManager.isMarkedForHealthPenalty(victim.getUuid())) {
                    GoldenScrapManager.applyHealthPenalty(victim);
                    GoldenScrapManager.startHealthPenaltyTimer(victim.getUuid());
                }
                GoldenScrapManager.resetProgress(victim.getUuid());
                GoldenScrapManager.cleanupAfterDeath(victim.getUuid());
            }
        });


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