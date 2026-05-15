package net.vainnglory.masksnglory.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.BoneKnifeParryManager;
import net.vainnglory.masksnglory.util.CastIronManager;
import net.vainnglory.masksnglory.util.GreaseManager;
import net.vainnglory.masksnglory.util.ModDamageTypes;
import net.vainnglory.masksnglory.util.ModRarities;
import net.vainnglory.masksnglory.util.ModDeathSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GoldenPanItem extends SwordItem implements Vanishable, CustomHitSoundItem, ModDeathSource {
    private static final WeakHashMap<PlayerEntity, Float> fallStarts = new WeakHashMap<>();
    private static final Queue<Runnable> pendingSlams = new ConcurrentLinkedQueue<>();
    private static final Queue<Runnable> pendingKillChecks = new ConcurrentLinkedQueue<>();
    private static final WeakHashMap<PlayerEntity, Integer> lastHurtTime = new WeakHashMap<>();
    private static final WeakHashMap<PlayerEntity, Integer> hitsTaken = new WeakHashMap<>();

    private static final float MIN_FALL_DISTANCE = 2.5f;
    private static final float SLAM_SOUND_THRESHOLD = 11.0f;

    private static final String DENTS_KEY = "MNG_Dents";
    private static final String LAST_REPAIR_KEY = "MNG_LastRepairTick";
    private static final int MAX_DENTS = 5;
    private static final long TICKS_PER_REPAIR = 500L;
    private static final long TICKS_PER_REPAIR_WATER = 100L;
    private static final float DENT_PENALTY = 0.08f;
    private static final int HITS_TO_REPAIR = 2;

    private final float attackDamage;
    private final ModRarities rarity;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public GoldenPanItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, ModRarities rarity) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.attackDamage = (float) attackDamage + toolMaterial.getAttackDamage();
        this.rarity = rarity;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 5, EntityAttributeModifier.Operation.ADDITION)
        );
        builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -1.8F, EntityAttributeModifier.Operation.ADDITION)
        );
        this.attributeModifiers = builder.build();
    }

    public static int getDents(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? MathHelper.clamp(nbt.getInt(DENTS_KEY), 0, MAX_DENTS) : 0;
    }

    private static void setDents(ItemStack stack, int dents) {
        stack.getOrCreateNbt().putInt(DENTS_KEY, MathHelper.clamp(dents, 0, MAX_DENTS));
    }

    private static long getLastRepairTick(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? nbt.getLong(LAST_REPAIR_KEY) : 0L;
    }

    private static void setLastRepairTick(ItemStack stack, long tick) {
        stack.getOrCreateNbt().putLong(LAST_REPAIR_KEY, tick);
    }

    private static float getDentMultiplier(ItemStack stack) {
        return 1.0f - (getDents(stack) * DENT_PENALTY);
    }

    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        if (EnchantmentHelper.getLevel(ModEnchantments.SKULL, stack) > 0) {
            return super.getName(stack).copy().styled(style -> style.withColor(0xA33E43));
        }
        if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, stack) > 0) {
            return super.getName(stack).copy().styled(style -> style.withColor(0x450C21));
        }
        if (EnchantmentHelper.getLevel(ModEnchantments.GREASE, stack) > 0) {
            return super.getName(stack).copy().styled(style -> style.withColor(0x75C3D1));
        }
        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                MasksNGlorySounds.ITEM_PAN_HIT,
                player.getSoundCategory(),
                1F,
                (float) (1.0F + player.getRandom().nextGaussian() / 10f)
        );
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isOf(Blocks.COBWEB)) {
            return 15.0F;
        } else {
            return state.isIn(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return super.postHit(stack, target, attacker);
    }

    @Override
    public DamageSource getKillSource(LivingEntity attacker, LivingEntity target) {
        return ModDamageTypes.pan(attacker, target);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getHardness(world, pos) != 0.0F) {
            stack.damage(2, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return state.isOf(Blocks.COBWEB);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.masks-n-glory.golden_pan"));
        int dents = getDents(stack);
        if (dents > 0) {
            String label;
            int color;
            if (dents <= 2) {
                label = "Slightly Dented (" + dents + "/" + MAX_DENTS + ")";
                color = 0xFFD966;
            } else if (dents <= 4) {
                label = "Dented (" + dents + "/" + MAX_DENTS + ")";
                color = 0xFF8C00;
            } else {
                label = "Heavily Dented (" + dents + "/" + MAX_DENTS + ")";
                color = 0xFF3333;
            }
            tooltip.add(Text.literal(label).setStyle(Style.EMPTY.withColor(color)));
        }
        if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, stack) > 0) {
            int charge = CastIronManager.readChargeFromWeapon(stack);
            if (charge > 0) {
                tooltip.add(Text.literal("Shield Charge: " + charge + "/5")
                        .setStyle(Style.EMPTY.withColor(0x888888)));
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, stack) > 0) {
            if (!world.isClient) {
                CastIronManager.setBlocking(user, true);
            }
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        if (EnchantmentHelper.getLevel(ModEnchantments.GREASE, stack) > 0 && user.isSneaking()) {
            if (!world.isClient) {
                GreaseManager.applyGrease(user);
            }
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            CastIronManager.setBlocking(player, false);
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, stack) > 0) {
            return UseAction.BLOCK;
        }
        return UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;

    }

    private static void spawnGroundParticles(ServerWorld serverWorld, Vec3d pos, float totalBonus) {
        BlockPos belowTarget = BlockPos.ofFloored(pos.x, pos.y, pos.z).down();
        BlockState groundBlock = serverWorld.getBlockState(belowTarget);
        if (groundBlock.isAir()) {
            groundBlock = Blocks.STONE.getDefaultState();
        }
        BlockStateParticleEffect fallbackParticle = new BlockStateParticleEffect(ParticleTypes.BLOCK, groundBlock);

        int particlesPerPos = MathHelper.clamp((int)(totalBonus * 0.8f), 3, 20);
        float maxRadius = MathHelper.clamp(totalBonus * 0.2f, 1.5f, 4.5f);
        int radiusCeil = (int) maxRadius + 1;

        for (int dx = -radiusCeil; dx <= radiusCeil; dx++) {
            for (int dz = -radiusCeil; dz <= radiusCeil; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > maxRadius) continue;

                BlockPos samplePos = belowTarget.add(dx, 0, dz);
                BlockState sampleBlock = serverWorld.getBlockState(samplePos);
                BlockStateParticleEffect localParticle = sampleBlock.isAir()
                        ? fallbackParticle
                        : new BlockStateParticleEffect(ParticleTypes.BLOCK, sampleBlock);

                serverWorld.spawnParticles(
                        localParticle,
                        pos.x + dx, pos.y + 0.1, pos.z + dz,
                        particlesPerPos,
                        0.3, 0.1, 0.3,
                        0.15
                );
            }
        }
    }

    public static void registerCallbacks() {

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Runnable task;
            while ((task = pendingSlams.poll()) != null) {
                task.run();
            }

            Runnable killCheck;
            while ((killCheck = pendingKillChecks.poll()) != null) {
                killCheck.run();
            }

            for (var player : server.getPlayerManager().getPlayerList()) {
                ItemStack weapon = player.getMainHandStack();
                if (!(weapon.getItem() instanceof GoldenPanItem)) {
                    fallStarts.remove(player);
                    lastHurtTime.remove(player);
                    hitsTaken.remove(player);
                    continue;
                }

                boolean falling = !player.isOnGround() && player.getVelocity().y < -0.08;
                if (falling && !fallStarts.containsKey(player)) {
                    fallStarts.put(player, (float) player.getY());
                }
                if (player.isOnGround()) {
                    fallStarts.remove(player);
                }
                if (player.fallDistance > 0) {
                    player.fallDistance = 0;
                }

                int dents = getDents(weapon);
                if (dents > 0) {
                    long worldTime = player.getServerWorld().getTime();
                    long lastRepair = getLastRepairTick(weapon);
                    long interval = player.isTouchingWater() ? TICKS_PER_REPAIR_WATER : TICKS_PER_REPAIR;
                    if (lastRepair == 0L) {
                        setLastRepairTick(weapon, worldTime);
                    } else if (worldTime - lastRepair >= interval) {
                        setDents(weapon, dents - 1);
                        setLastRepairTick(weapon, worldTime);
                    }
                }

                int prevHurt = lastHurtTime.getOrDefault(player, 0);
                int currHurt = player.hurtTime;
                lastHurtTime.put(player, currHurt);

                if (prevHurt == 0 && currHurt > 0) {
                    int currentDents = getDents(weapon);
                    if (currentDents > 0) {
                        int hits = hitsTaken.getOrDefault(player, 0) + 1;
                        if (hits >= HITS_TO_REPAIR) {
                            setDents(weapon, currentDents - 1);
                            setLastRepairTick(weapon, player.getServerWorld().getTime());
                            hitsTaken.put(player, 0);
                            player.getServerWorld().playSound(
                                    null,
                                    player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.BLOCK_ANVIL_USE,
                                    player.getSoundCategory(),
                                    0.6f, 1.3f
                            );
                        } else {
                            hitsTaken.put(player, hits);
                        }
                    }
                }
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));

            ItemStack heldItem = player.getStackInHand(hand);
            if (heldItem.getItem() != Items.EXPERIENCE_BOTTLE)
                return TypedActionResult.pass(heldItem);

            ItemStack mainHand = player.getMainHandStack();
            if (!(mainHand.getItem() instanceof GoldenPanItem))
                return TypedActionResult.pass(heldItem);

            int dents = getDents(mainHand);
            if (dents == 0) return TypedActionResult.pass(heldItem);

            if (!(world instanceof ServerWorld serverWorld)) return TypedActionResult.pass(heldItem);

            setDents(mainHand, dents - 1);
            setLastRepairTick(mainHand, serverWorld.getTime());

            if (!player.isCreative()) heldItem.decrement(1);

            serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, player.getSoundCategory(), 1f, 1f);

            return TypedActionResult.success(heldItem);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient || !(entity instanceof LivingEntity target)) {
                return ActionResult.PASS;
            }
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof GoldenPanItem)) {
                return ActionResult.PASS;
            }
            if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

            if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, weapon) > 0
                    && CastIronManager.isBlocking(player)) {
                return ActionResult.PASS;
            }

            Float startY = fallStarts.remove(player);
            if (startY == null) {
                final LivingEntity capturedTarget = target;
                final ItemStack capturedWeapon = weapon;

                if (EnchantmentHelper.getLevel(ModEnchantments.GREASE, weapon) > 0
                        && capturedTarget instanceof PlayerEntity cp && GreaseManager.isGreased(cp)) {
                    pendingSlams.offer(() -> {
                        if (capturedTarget.isAlive()) {
                            capturedTarget.hurtTime = 0;
                            capturedTarget.timeUntilRegen = 0;
                            capturedTarget.damage(world.getDamageSources().playerAttack(player), 1.0f);
                        }
                    });
                }

                if (EnchantmentHelper.getLevel(ModEnchantments.GREASE, weapon) > 0) {
                    GreaseManager.applyGrease(capturedTarget);
                }

                pendingKillChecks.offer(() -> {
                    if (!capturedTarget.isAlive()) {
                        int curDents = getDents(capturedWeapon);
                        if (curDents > 0) {
                            setDents(capturedWeapon, curDents - 2);
                            setLastRepairTick(capturedWeapon, serverWorld.getTime());
                            serverWorld.playSound(
                                    null,
                                    capturedTarget.getX(), capturedTarget.getY(), capturedTarget.getZ(),
                                    SoundEvents.BLOCK_ANVIL_USE,
                                    SoundCategory.PLAYERS,
                                    0.6f, 1.3f
                            );
                        }
                    }
                });
                return ActionResult.PASS;
            }

            float fallDistance = Math.max(0f, startY - (float) player.getY());
            if (fallDistance < MIN_FALL_DISTANCE) return ActionResult.PASS;

            float cooldown = player.getAttackCooldownProgress(0.5f);
            if (cooldown <= 0.65f) return ActionResult.PASS;

            int skullLevel = EnchantmentHelper.getLevel(ModEnchantments.SKULL, weapon);
            float dentMultiplier = getDentMultiplier(weapon);

            final float baseFallBonus = fallDistance * 0.7f;
            final float skullBonus    = skullLevel > 0 ? fallDistance * 2.50f * skullLevel + 8.0f : 0f;
            final float totalBonus    = (baseFallBonus + skullBonus) * dentMultiplier;

            int currentDents = getDents(weapon);
            if (currentDents < MAX_DENTS) {
                setDents(weapon, currentDents + 1);
            }
            setLastRepairTick(weapon, serverWorld.getTime());

            final float capturedFallDistance = fallDistance;

            if (EnchantmentHelper.getLevel(ModEnchantments.GREASE, weapon) > 0) {
                GreaseManager.applyGrease(target);
            }

            pendingSlams.offer(() -> {
                if (target.isAlive()) {
                    if (BoneKnifeParryManager.tryPanParry(target, player, world, totalBonus)) {
                        return;
                    }
                    target.hurtTime = 0;
                    target.timeUntilRegen = 0;
                    float greasedBonus = (EnchantmentHelper.getLevel(ModEnchantments.GREASE, weapon) > 0
                            && target instanceof PlayerEntity gp && GreaseManager.isGreased(gp)) ? 1.0f : 0.0f;
                    target.damage(world.getDamageSources().playerAttack(player), totalBonus + greasedBonus);

                    if (!target.isAlive()) {
                        int curDents = getDents(weapon);
                        if (curDents > 0) {
                            setDents(weapon, curDents - 2);
                            setLastRepairTick(weapon, serverWorld.getTime());
                            serverWorld.playSound(
                                    null,
                                    target.getX(), target.getY(), target.getZ(),
                                    SoundEvents.BLOCK_ANVIL_USE,
                                    SoundCategory.PLAYERS,
                                    0.6f, 1.3f
                            );
                        }
                    }
                }
            });

            float baseDamage   = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float enchantBonus = EnchantmentHelper.getAttackDamage(weapon, target.getGroup());
            float shockwaveDamage = (baseDamage + enchantBonus) * 0.80f;
            if (skullLevel > 0) {
                shockwaveDamage += fallDistance * 0.10f * skullLevel * 0.90f;
            }

            DamageSource source = world.getDamageSources().playerAttack(player);
            Vec3d pos = target.getPos();

            Box area = new Box(pos.add(-3, -1, -3), pos.add(3, 2, 3));
            List<LivingEntity> nearby = world.getNonSpectatingEntities(LivingEntity.class, area);
            for (LivingEntity near : nearby) {
                if (near != target && near != player && near.isAlive()) {
                    near.damage(source, shockwaveDamage);
                    Vec3d toNear = near.getPos().subtract(pos);
                    double dist = toNear.length();
                    if (dist > 0) {
                        Vec3d knockDir = toNear.normalize();
                        float knockStrength = Math.min(totalBonus * 0.04f, 1.8f);
                        near.addVelocity(
                                knockDir.x * knockStrength,
                                0.2f + knockStrength * 0.1f,
                                knockDir.z * knockStrength
                        );
                        near.velocityModified = true;
                    }
                }
            }

            serverWorld.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x, pos.y + 0.5, pos.z,
                    18, 0.4, 0.3, 0.4, 0.02
            );

            float launchVelocity = Math.min(fallDistance * 0.50f, 1.5f);
            player.setVelocity(player.getVelocity().x, launchVelocity, player.getVelocity().z);
            player.velocityModified = true;

            if (capturedFallDistance >= SLAM_SOUND_THRESHOLD) {
                serverWorld.playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_ANVIL_LAND,
                        SoundCategory.PLAYERS,
                        6.0f, 0.85f
                );
            }

            spawnGroundParticles(serverWorld, pos, totalBonus);

            return ActionResult.PASS;
        });
    }
}