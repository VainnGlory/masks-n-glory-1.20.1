package net.vainnglory.masksnglory.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Vanishable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntity;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.ModRarities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaleSwordItem extends SwordItem implements Vanishable, CustomHitSoundItem {
    private final float attackDamage;
    private final ModRarities rarity;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public PaleSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed,
                         Settings settings, ModRarities rarity) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.attackDamage = (float) attackDamage + toolMaterial.getAttackDamage();
        this.rarity = rarity;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder =
                ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier",
                        9, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier",
                        -3F, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    public float getAttackDamage() { return this.attackDamage; }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                MasksNGlorySounds.ITEM_PALE_HIT, player.getSoundCategory(),
                0.7F, (float) (1.0F + player.getRandom().nextGaussian() / 10f));
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isOf(Blocks.COBWEB)) return 15.0F;
        return state.isIn(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        if (attacker instanceof PlayerEntity
                && EnchantmentHelper.getLevel(ModEnchantments.HOMING, stack) > 0) {
            stack.getOrCreateNbt().putUuid("HomingTargetUUID", target.getUuid());
        }
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state,
                            BlockPos pos, LivingEntity miner) {
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
    public void appendTooltip(ItemStack stack, @Nullable World world,
     List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.masks-n-glory.pale_sword"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            int remorseLevel = EnchantmentHelper.getLevel(ModEnchantments.REMORSE, itemStack);
            int homingLevel  = EnchantmentHelper.getLevel(ModEnchantments.HOMING, itemStack);

            if (remorseLevel > 0) {
                MaelstromEntity existing = MaelstromEntity.getActiveRemorseEntity(user.getUuid());
                if (existing != null) {
                    if (existing.isRemorseStuck()) {
                        if (user.isSneaking()) {
                            existing.triggerTpRecall(user);
                        } else {
                            existing.triggerForcefulRecall(user);
                        }
                        return TypedActionResult.success(itemStack, false);
                    }
                    return TypedActionResult.fail(itemStack);
                }
            } else {
                if (user.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                    return TypedActionResult.fail(itemStack);
                }
            }

            MaelstromEntity maelstrom = new MaelstromEntity(world, user, itemStack);
            maelstrom.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 3.5F, 1.0F);
            maelstrom.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;

            if (homingLevel > 0) {
                maelstrom.setHoming(true);
                NbtCompound nbt = itemStack.getNbt();
                if (nbt != null && nbt.containsUuid("HomingTargetUUID")) {
                    maelstrom.setHomingTargetUUID(nbt.getUuid("HomingTargetUUID"));
                }
            }

            if (remorseLevel > 0) {
                maelstrom.setRemorse(true);
                itemStack.getOrCreateNbt().putBoolean("RemorseActive", true);
            }

            world.spawnEntity(maelstrom);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);

            if (remorseLevel == 0 && !user.getAbilities().creativeMode) {
                itemStack.decrement(1);
                user.getItemCooldownManager().set(this, homingLevel > 0 ? 500 : 100);
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }


    @Override
    public int getEnchantability() { return 1; }
}

