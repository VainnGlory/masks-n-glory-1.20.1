package net.vainnglory.masksnglory.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.ModDamageTypes;
import net.vainnglory.masksnglory.util.ModRarities;
import net.vainnglory.masksnglory.util.ModDeathSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.WeakHashMap;

public class GoldenPanItem extends SwordItem implements Vanishable, CustomHitSoundItem, ModDeathSource {
    private static final WeakHashMap<PlayerEntity, Float> fallStarts = new WeakHashMap<>();

    private static final float MIN_FALL_DISTANCE = 2.5f;
    private static final float SKULL_CRATER_THRESHOLD = 12.0f;

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

    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        if (EnchantmentHelper.getLevel(ModEnchantments.SKULL, stack) > 0) {
            return super.getName(stack).copy().styled(style -> style.withColor(0xE3BCF5));
        }
        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
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
    public DamageSource getKillSource(LivingEntity livingEntity) {
        return ModDamageTypes.pan(livingEntity);
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
        super.appendTooltip(stack, world, tooltip, context);
    }

    public int getEnchantability() {
        return 1;
    }

    public static void registerCallbacks() {

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                ItemStack weapon = player.getMainHandStack();
                if (!(weapon.getItem() instanceof GoldenPanItem)) {
                    fallStarts.remove(player);
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
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient || !(entity instanceof LivingEntity target)) {
                return ActionResult.PASS;
            }
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof GoldenPanItem)) {
                return ActionResult.PASS;
            }

            Float startY = fallStarts.remove(player);
            if (startY == null) return ActionResult.PASS;

            float fallDistance = Math.max(0f, startY - (float) player.getY());
            if (fallDistance < MIN_FALL_DISTANCE) return ActionResult.PASS;

            float cooldown = player.getAttackCooldownProgress(0.5f);
            if (cooldown <= 0.65f) return ActionResult.PASS;

            int skullLevel = EnchantmentHelper.getLevel(ModEnchantments.SKULL, weapon);

            final float baseFallBonus = fallDistance * 0.18f;
            final float skullBonus    = skullLevel > 0 ? fallDistance * 2.50f * skullLevel + 8.0f : 0f;
            final float totalBonus    = baseFallBonus + skullBonus;

            world.getServer().execute(() -> {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                if (target.isAlive()) {
                    target.hurtTime = 0;
                    target.timeUntilRegen = 0;
                    target.damage(world.getDamageSources().playerAttack(player), totalBonus);
                }
            });

            float baseDamage  = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
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
                }
            }

            if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

            serverWorld.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x, pos.y + 0.5, pos.z,
                    18,
                    0.4, 0.3, 0.4,
                    0.02
            );

            float launchVelocity = Math.min(fallDistance * 0.50f, 1.5f);
            player.setVelocity(player.getVelocity().x, launchVelocity, player.getVelocity().z);
            player.velocityModified = true;

            if (skullLevel > 0 && fallDistance >= SKULL_CRATER_THRESHOLD) {
                BlockPos belowTarget = target.getBlockPos().down();
                BlockState groundBlock = serverWorld.getBlockState(belowTarget);
                if (groundBlock.isAir()) {
                    groundBlock = Blocks.STONE.getDefaultState();
                }

                BlockStateParticleEffect fallbackParticle = new BlockStateParticleEffect(ParticleTypes.BLOCK, groundBlock);

                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if (dist < 1.5 || dist > 3.5) continue;

                        BlockPos samplePos = belowTarget.add(dx, 0, dz);
                        BlockState sampleBlock = serverWorld.getBlockState(samplePos);
                        BlockStateParticleEffect localParticle = sampleBlock.isAir()
                                ? fallbackParticle
                                : new BlockStateParticleEffect(ParticleTypes.BLOCK, sampleBlock);

                        serverWorld.spawnParticles(
                                localParticle,
                                pos.x + dx, pos.y + 0.1, pos.z + dz,
                                6,
                                0.3, 0.1, 0.3,
                                0.12
                        );
                    }
                }

                serverWorld.playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_ANVIL_LAND,
                        SoundCategory.PLAYERS,
                        3.5f,
                        0.85f
                );
            }

            return ActionResult.PASS;
        });
    }
}



