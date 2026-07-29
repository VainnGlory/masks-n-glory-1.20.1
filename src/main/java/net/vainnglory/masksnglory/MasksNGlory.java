package net.vainnglory.masksnglory;

import net.vainnglory.masksnglory.block.custom.GlitchBlock;
import net.vainnglory.masksnglory.particle.ModParticles;
import net.vainnglory.masksnglory.util.CastleSpawner;
import net.vainnglory.masksnglory.util.StabShotManager;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.vainnglory.masksnglory.entity.custom.*;
import net.vainnglory.masksnglory.world.ModDimensions;
import net.vainnglory.masksnglory.world.ModFeatures;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.custom.AmalgamItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.TypedActionResult;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.util.Blackout;
import net.vainnglory.masksnglory.util.BlackoutC2SPacket;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.text.Text;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.enchantments.*;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.events.PlayerDeathEffects;
import net.vainnglory.masksnglory.item.ModItemGroups;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import net.vainnglory.masksnglory.item.custom.NullKnifeItem;
import net.vainnglory.masksnglory.painting.ModPaintings;
import net.vainnglory.masksnglory.potion.ModPotions;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.*;
import net.vainnglory.masksnglory.world.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MasksNGlory implements ModInitializer {
    public static final String MOD_ID = "masks-n-glory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, Long> lastPlayerChunk = new HashMap<>();
    private static final Map<UUID, Integer> honeyDrinkStart = new HashMap<>();

    public static final Map<UUID, Integer> verdantPortalTicks = new HashMap<>();
    public static final Map<UUID, Long> verdantPortalLastTick = new HashMap<>();

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
        ModFeatures.register();

        MasksNGlorySounds.initialize();

        ModLootTableModifier.modifyLootTables();
        ModEntities.registerModEntities();

        ModEnchantments.registerEnchantments();
        ModEffects.registerEffects();
        ModParticles.registerParticles();
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
        NotorietyCommand.register();
        Mania.register();
        CastIron.register();
        Grease.register();

        RegisterMNGItems.registerPaleItems();

        ModEntityTypes.registerEntityTypes();
        FabricDefaultAttributeRegistry.register(ModEntityTypes.SOUL_RAVAGER_TYPE, SoulRavagerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntityTypes.ARMOR_STAND_THING_TYPE, ArmorStandThingEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntityTypes.WATCHER_TYPE, WatcherEntity.createAttributes());
        WatcherEntity.register();
        FabricDefaultAttributeRegistry.register(ModEntityTypes.VANGUARD_TYPE, VanguardEntity.createAttributes());
        VanguardEntity.register();
        CastleSpawner.register();
        MineshaftVaults.register();
        GlitchBlock.register();
        StabShotManager.register();
        NukeShotManager.register();
        EnderPearlChunkLoader.register();
        FlashAttackPacket.registerReceiver();

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
            ItemStack held = player.getStackInHand(hand);

            if (held.isOf(Items.MILK_BUCKET) && player.hasStatusEffect(ModEffects.INSOMNIA)) {
                return TypedActionResult.fail(held);
            }

            if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(held);
            if (!held.isOf(Items.GLASS_BOTTLE)) return TypedActionResult.pass(held);
            if (player.getY() >= 30) return TypedActionResult.pass(held);
            if (!FarlandsHelper.isInInnerFarlands(player.getX(), player.getZ())) return TypedActionResult.pass(held);
            if (!player.getAbilities().creativeMode) held.decrement(1);
            player.giveItemStack(new ItemStack(ModItems.ORIGINAL_AIR_BOTTLE));
            return TypedActionResult.success(player.getStackInHand(hand));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ItemStack amalgam = null;
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() instanceof AmalgamItem && stack.getDamage() < stack.getMaxDamage()) {
                        amalgam = stack;
                        break;
                    }
                }
                if (amalgam == null) continue;

                boolean justBroke = false;
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    if (slot == EquipmentSlot.FEET && !player.isOnGround()) continue;
                    if (amalgam.getDamage() >= amalgam.getMaxDamage()) break;
                    ItemStack armor = player.getEquippedStack(slot);
                    if (armor.isEmpty() || !armor.isDamageable() || armor.getDamage() <= 0) continue;
                    armor.setDamage(armor.getDamage() - 1);
                    int prev = amalgam.getDamage();
                    amalgam.setDamage(Math.min(prev + 1, amalgam.getMaxDamage()));
                    if (!justBroke && amalgam.getDamage() >= amalgam.getMaxDamage() && prev < amalgam.getMaxDamage()) {
                        justBroke = true;
                    }
                }
                if (justBroke) {
                    player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 2.0f, 1.0f);
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!player.hasStatusEffect(ModEffects.NULL_EFFECT)) continue;
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() instanceof AmalgamItem && stack.getDamage() < stack.getMaxDamage()) {
                        player.removeStatusEffect(ModEffects.NULL_EFFECT);
                        break;
                    }
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!player.getWorld().getRegistryKey().equals(World.OVERWORLD)) continue;
                if (player.getY() >= player.getWorld().getBottomY()) continue;
                double px = player.getX(), pz = player.getZ();
                if (Math.abs(px) < 70000 && Math.abs(pz) < 70000) continue;
                ServerWorld verdant = server.getWorld(ModDimensions.VERDANT_MEMORY_KEY);
                if (verdant == null) continue;
                FarlandsNullManager.cleanup(player.getUuid());
                player.removeStatusEffect(ModEffects.NULL_EFFECT);
                if (px >= 70566.5 && px <= 70569.5 && pz >= 7454.5 && pz <= 7457.5) {
                    player.teleport(verdant, 0, 600, 0, player.getYaw(), player.getPitch());
                } else {
                    player.teleport(verdant, px, 128, pz, player.getYaw(), player.getPitch());
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> FarlandsPortal.tick(server));

        MaskAbilities.registerCallbacks();

        ModWorldGeneration.addFeaturesToBiomes();

        BlackoutC2SPacket.registerReceiver();

        GlaiveModeC2SPacket.registerReceiver();
        LuneriSwapC2SPacket.registerReceiver();
        ServerTickEvents.END_SERVER_TICK.register(server -> AfterlifeEnchantment.tickSummonedUndead(server));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> {
            UUID id = handler.player.getUuid();
            ActorManager.offScriptActive.remove(id);
            ActorManager.actorSneakTicks.remove(id);
            ActorManager.lastDamageTicks.remove(id);
            ActorManager.offScriptCooldowns.remove(id);
            ActorManager.sympathyInProgress.remove(id);
            MaskAbilities.clearPlayerData(id);
            TemperEnchantment.cleanup(id);
            IncumbentEnchantment.cleanup(id);
            NullManager.cleanup(id);
            NullKnifeItem.cleanup(id);
            ExceptionNotCaughtEnchantment.cleanup(id);
            GoldenScrap.cleanupOnDisconnect(id);
            GoldenScrap.pauseHealthPenalty(id);
            ActorManager.offScriptActive.remove(id);
            ActorManager.actorSneakTicks.remove(id);
            ActorManager.lastDamageTicks.remove(id);
            ActorManager.offScriptCooldowns.remove(id);
            ActorManager.sympathyInProgress.remove(id);
            ActorManager.offScriptAddedInvis.remove(id);
            CastIron.setBlocking(handler.player, false);
            FarlandsNullManager.cleanup(id);
            lastPlayerChunk.remove(id);
            honeyDrinkStart.remove(id);
            MasksNGlory.verdantPortalTicks.remove(id);
            MasksNGlory.verdantPortalLastTick.remove(id);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            GoldenScrap.resumeHealthPenalty(handler.player.getUuid(), handler.player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID id = oldPlayer.getUuid();
            NullManager.restoreItems(oldPlayer);
            NullManager.cleanup(id);
            NullKnifeItem.cleanup(id);
            ExceptionNotCaughtEnchantment.cleanup(id);
            FarlandsNullManager.cleanup(id);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.hasStatusEffect(ModEffects.SEIZED)) {
                    player.setSprinting(false);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> Blackout.onServerStart(server));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            VerdantTreeFeature.templateManager = server.getStructureTemplateManager();
            VerdantTreeFeature.templateManager = server.getStructureTemplateManager();
            VerdantStoneFeature.templateManager = server.getStructureTemplateManager();
            VerdantMonolithFeature.templateManager = server.getStructureTemplateManager();
            FarlandsBiomeCache.nullBiomeEntry = server.getRegistryManager()
                    .get(RegistryKeys.BIOME)
                    .getEntry(NullBiome.KEY)
                    .orElse(null);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PlayerDeathEffects.onPlayerDeath(player, damageSource);
                PlayerDeathEffects.onAnyEntityDeath(entity, damageSource);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player
                    && damageSource.getAttacker() instanceof ArmorStandThingEntity
                    && entity.getWorld() instanceof ServerWorld serverWorld) {
                ArmorStandThingEntity spawned = new ArmorStandThingEntity(ModEntityTypes.ARMOR_STAND_THING_TYPE, serverWorld);
                spawned.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0, 0);
                serverWorld.spawnEntity(spawned);
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
            FarlandsNullManager.tick(server);
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                FarlandsNullManager.onWakeUp(player);
            }
        });

        EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
            if (player.hasStatusEffect(ModEffects.INSOMNIA)) {
                return PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW;
            }
            return null;
        });

        EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) ->
                !player.getWorld().getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY));

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            if (!entity.getWorld().getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) return;
            player.getServer().execute(() -> {
                BlockPos spawnPos = player.getSpawnPointPosition();
                RegistryKey<World> spawnDim = player.getSpawnPointDimension();
                ServerWorld targetWorld;
                double tx, ty, tz;
                if (spawnPos != null && !spawnDim.equals(ModDimensions.VERDANT_MEMORY_KEY)) {
                    targetWorld = player.getServer().getWorld(spawnDim);
                    if (targetWorld == null) targetWorld = player.getServer().getOverworld();
                    tx = spawnPos.getX() + 0.5;
                    ty = spawnPos.getY() + 1.0;
                    tz = spawnPos.getZ() + 0.5;
                } else {
                    targetWorld = player.getServer().getOverworld();
                    BlockPos worldSpawn = targetWorld.getSpawnPos();
                    tx = worldSpawn.getX() + 0.5;
                    ty = worldSpawn.getY();
                    tz = worldSpawn.getZ() + 0.5;
                }
                FabricDimensions.teleport(player, targetWorld, new TeleportTarget(
                        new Vec3d(tx, ty, tz),
                        Vec3d.ZERO,
                        player.getYaw(),
                        player.getPitch()
                ));
            });
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> Blackout.tick(server));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                GoldenScrap.tickHealthPenalties(server);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();

                if (!player.hasStatusEffect(ModEffects.INSOMNIA)) {
                    honeyDrinkStart.remove(id);
                    continue;
                }

                if (player.isUsingItem() && player.getActiveItem().isOf(Items.HONEY_BOTTLE)) {
                    honeyDrinkStart.putIfAbsent(id, server.getTicks());
                } else if (honeyDrinkStart.containsKey(id)) {
                    int startTick = honeyDrinkStart.remove(id);
                    if (server.getTicks() - startTick >= 30
                            && !FarlandsHelper.isInFarlands(player.getX(), player.getZ())) {
                        player.removeStatusEffect(ModEffects.INSOMNIA);
                    }
                }
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
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
                    double px = player.getX(), pz = player.getZ();

                    if (FarlandsHelper.isInFarlands(px, pz)) {
                        Advancement enterAdv = server.getAdvancementLoader().get(new Identifier("masks-n-glory", "farlands_enter"));
                        if (enterAdv != null && !player.getAdvancementTracker().getProgress(enterAdv).isDone()) {
                            player.getAdvancementTracker().grantCriterion(enterAdv, "entered_farlands");
                        }
                    }

                    if (FarlandsHelper.isInFarlands(px, pz)) {
                        ServerWorld world = (ServerWorld) player.getWorld();
                        BlockPos center = player.getBlockPos();

                        Advancement houseAdv = server.getAdvancementLoader().get(new Identifier("masks-n-glory", "farlands_house"));
                        if (houseAdv != null && !player.getAdvancementTracker().getProgress(houseAdv).isDone()) {
                            for (BlockPos pos : BlockPos.iterate(center.add(-8, -8, -8), center.add(8, 8, 8))) {
                                if (world.getBlockState(pos).isOf(Blocks.COBWEB)) {
                                    player.getAdvancementTracker().grantCriterion(houseAdv, "approached_house");
                                    break;
                                }
                            }
                        }

                        Advancement portalAdv = server.getAdvancementLoader().get(new Identifier("masks-n-glory", "farlands_portal"));
                        if (portalAdv != null && !player.getAdvancementTracker().getProgress(portalAdv).isDone()) {
                            for (BlockPos pos : BlockPos.iterate(center.add(-8, -8, -8), center.add(8, 8, 8))) {
                                if (world.getBlockState(pos).isOf(Blocks.BARRIER)) {
                                    player.getAdvancementTracker().grantCriterion(portalAdv, "approached_portal");
                                    break;
                                }
                            }
                        }
                    }

                    Advancement vanguardsAdv = server.getAdvancementLoader().get(new Identifier("masks-n-glory", "vanguards_failure"));
                    if (vanguardsAdv != null && !player.getAdvancementTracker().getProgress(vanguardsAdv).isDone()) {
                        double dx = px - 70560, dz = pz - 7461;
                        if (dx * dx + dz * dz <= 200.0 * 200.0) {
                            player.getAdvancementTracker().grantCriterion(vanguardsAdv, "entered_area");
                        }
                    }
                }

                if (player.getWorld().getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) {
                    Advancement goodAdv = server.getAdvancementLoader().get(new Identifier("masks-n-glory", "good_as_new"));
                    if (goodAdv != null && !player.getAdvancementTracker().getProgress(goodAdv).isDone()) {
                        double dx = player.getX(), dz = player.getZ();
                        if (dx * dx + dz * dz <= 200.0 * 200.0) {
                            player.getAdvancementTracker().grantCriterion(goodAdv, "entered_area");
                        }
                    }
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!player.getWorld().getRegistryKey().equals(World.OVERWORLD)) continue;
                double px = player.getX(), pz = player.getZ();

                if (FarlandsHelper.isInFarlands(px, pz)) {
                    ServerWorld world = (ServerWorld) player.getWorld();
                    BlockPos center = player.getBlockPos();

                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            int cx = player.getChunkPos().x + dx;
                            int cz = player.getChunkPos().z + dz;
                            int startX = cx << 4;
                            int startZ = cz << 4;

                            long houseSeed = ((long) startX * 123456789L) ^ ((long) startZ * 987654321L);
                            net.minecraft.util.math.random.Random houseRandom = net.minecraft.util.math.random.Random.create(houseSeed);

                            if (houseRandom.nextInt(500) != 0) continue;

                            int houseX = startX + houseRandom.nextInt(10);
                            int houseZ = startZ + houseRandom.nextInt(10);

                            int houseY = -1;
                            for (int y = world.getTopY() - 1; y >= world.getBottomY(); y--) {
                                if (FarlandsHelper.isSolid(houseX + 3, y, houseZ + 2)) {
                                    houseY = y + 1;
                                    break;
                                }
                            }
                            if (houseY < world.getBottomY() + 5 || houseY > world.getTopY() - 10) continue;

                            BlockPos spawnPos = new BlockPos(houseX + 4, houseY + 1, houseZ + 3);
                            List<ArmorStandThingEntity> existing = world.getEntitiesByClass(
                                    ArmorStandThingEntity.class, new Box(spawnPos).expand(10), e -> true);
                            if (!existing.isEmpty()) continue;

                            ArmorStandThingEntity entity = new ArmorStandThingEntity(
                                    ModEntityTypes.ARMOR_STAND_THING_TYPE, world);
                            entity.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 90, 0);
                            world.spawnEntity(entity);
                        }
                    }
                }
            }
        });

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

            int leatherCost = 5;

            Box searchBox = endermite.getBoundingBox().expand(2.0);
            List<ItemEntity> nearbyItems = world.getEntitiesByType(EntityType.ITEM, searchBox, e -> true);

            int leatherNeeded = leatherCost;
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

            int toConsume = leatherCost;
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
                GoldenScrap.recordDamage(dealer.getUuid(), victim.getUuid(), amount);
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity victim) {
                if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                    GoldenScrap.handleKill(killer, victim);
                }
                if (GoldenScrap.isMarkedForHealthPenalty(victim.getUuid())) {
                    GoldenScrap.applyHealthPenalty(victim);
                    GoldenScrap.startHealthPenaltyTimer(victim.getUuid());
                }
                GoldenScrap.resetProgress(victim.getUuid());
                GoldenScrap.cleanupAfterDeath(victim.getUuid());
            }
        });

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