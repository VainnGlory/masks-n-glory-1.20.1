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
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.AfterlifeEnchantment;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.ModRarities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlaiveItem extends SwordItem implements Vanishable, CustomHitSoundItem {
    private final float attackDamage;
    private final ModRarities rarity;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public GlaiveItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, ModRarities rarity) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.attackDamage = (float) attackDamage + toolMaterial.getAttackDamage();
        this.rarity = rarity;
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 0, EntityAttributeModifier.Operation.ADDITION)
        );
        builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.9F, EntityAttributeModifier.Operation.ADDITION)
        );
        this.attributeModifiers = builder.build();
    }


    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        if (EnchantmentHelper.getLevel(ModEnchantments.SOUL, stack) > 0) {
            return super.getName(stack).copy().styled(style -> style.withColor(0x6A5E45));
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
                MasksNGlorySounds.ITEM_RUSTED_HIT,
                player.getSoundCategory(),
                0.7F,
                (float) (1.0F + player.getRandom().nextGaussian() / 10f)
        );
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, stack) <= 0) {
            return TypedActionResult.pass(stack);
        }

        if (user.isSneaking()) {
            boolean acted = AfterlifeEnchantment.handleUse(user, stack, true);
            return acted ? TypedActionResult.success(stack, world.isClient) : TypedActionResult.pass(stack);
        } else {
            if (stack.getOrCreateNbt().getInt(AfterlifeEnchantment.SOULS_KEY) <= 0) {
                return TypedActionResult.pass(stack);
            }
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, stack) <= 0) return;
        int chargedTicks = getMaxUseTime(stack) - remainingUseTicks;
        if (chargedTicks < 20) return;
        AfterlifeEnchantment.handleUse(player, stack, false);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
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
        tooltip.add(Text.translatable("tooltip.masks-n-glory.glaive"));

        if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, stack) > 0) {
            NbtCompound nbt = stack.getOrCreateNbt();
            int mode = nbt.getInt(AfterlifeEnchantment.MODE_KEY);
            int souls = nbt.getInt(AfterlifeEnchantment.SOULS_KEY);
            int ravagers = nbt.getInt(AfterlifeEnchantment.RAVAGER_KEY);
            int illagers = nbt.getInt(AfterlifeEnchantment.ILLAGER_KEY);
            int undead = nbt.getInt(AfterlifeEnchantment.UNDEAD_KEY);
            int bandit = nbt.getInt(AfterlifeEnchantment.BANDIT_KEY);

            tooltip.add(Text.literal("Souls: " + souls + "/10").setStyle(Style.EMPTY.withColor(0x7B68EE)));
            tooltip.add(Text.literal((mode == AfterlifeEnchantment.MODE_RAVAGER ? "► " : "  ") + "Ravagers: " + ravagers + "/5").setStyle(Style.EMPTY.withColor(0x8B0000)));
            tooltip.add(Text.literal((mode == AfterlifeEnchantment.MODE_ILLAGER ? "► " : "  ") + "Illagers: " + illagers + "/5").setStyle(Style.EMPTY.withColor(0x2E8B57)));
            tooltip.add(Text.literal((mode == AfterlifeEnchantment.MODE_UNDEAD  ? "► " : "  ") + "Undead: " + undead + "/10").setStyle(Style.EMPTY.withColor(0x708090)));
            tooltip.add(Text.literal((mode == AfterlifeEnchantment.MODE_BANDIT ? "► " : "  ") + "Bandits: " + bandit + "/3").setStyle(Style.EMPTY.withColor(0xA0522D)));

        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public int getEnchantability()

    {

        return 1;

    }
}
