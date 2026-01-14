package net.vainnglory.masksnglory.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.ModRarities;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrideItem extends SwordItem implements Vanishable, CustomHitSoundItem {
    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    private static final String NBT_EFFECTS = "ImbuedEffects";
    private static final String NBT_HIT_COUNTER = "HitCounter";

    private final int maxHits;
    private final float attackDamage;
    private final ModRarities rarity;

    public PrideItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, ModRarities rarity, int maxHits) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.attackDamage = (float) attackDamage + toolMaterial.getAttackDamage();
        this.rarity = rarity;
        this.maxHits = maxHits;
    }


    private List<StatusEffectInstance> getEffects(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(NBT_EFFECTS, NbtElement.LIST_TYPE)) {
            NbtList effectsList = nbt.getList(NBT_EFFECTS, NbtElement.COMPOUND_TYPE);
            List<StatusEffectInstance> effects = new ArrayList<>();
            for (int i = 0; i < effectsList.size(); i++) {
                NbtCompound effectCompound = effectsList.getCompound(i);
                StatusEffectInstance effect = StatusEffectInstance.fromNbt(effectCompound);
                if (effect != null) {
                    effects.add(effect);
                }
            }
            return effects.isEmpty() ? null : effects;
        }
        return null;
    }

    private void setEffects(ItemStack stack, List<StatusEffectInstance> effects) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (effects != null && !effects.isEmpty()) {
            NbtList effectsList = new NbtList();
            for (StatusEffectInstance effect : effects) {
                NbtCompound effectCompound = new NbtCompound();
                effect.writeNbt(effectCompound);
                effectsList.add(effectCompound);
            }
            nbt.put(NBT_EFFECTS, effectsList);
        } else {
            nbt.remove(NBT_EFFECTS);
        }
    }

    private int getHitCounter(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? nbt.getInt(NBT_HIT_COUNTER) : 0;
    }

    private void setHitCounter(ItemStack stack, int counter) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (counter > 0) {
            nbt.putInt(NBT_HIT_COUNTER, counter);
        } else {
            nbt.remove(NBT_HIT_COUNTER);
        }
    }

    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.playSound(MasksNGlorySounds.ITEM_PRIDE_HIT, 0.5F, (float) (1.0F + player.getRandom().nextGaussian() / 10f));
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
        List<StatusEffectInstance> effects = getEffects(stack);
        int hitCounter = getHitCounter(stack);

        if (hitCounter < this.maxHits && effects != null && !effects.isEmpty()) {
            hitCounter++;
            setHitCounter(stack, hitCounter);

            for (StatusEffectInstance effect : effects) {
                target.addStatusEffect(new StatusEffectInstance(
                        effect.getEffectType(),
                        effect.getDuration(),
                        effect.getAmplifier() + 1
                ));
            }
        } else {

            setHitCounter(stack, 0);
            setEffects(stack, null);
        }
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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack mainHandItem = user.getStackInHand(hand);
        ItemStack offHandItem = user.getOffHandStack();

        if (offHandItem.getItem() instanceof PotionItem) {
            List<StatusEffectInstance> potionEffects = PotionUtil.getPotionEffects(offHandItem);

            if (!world.isClient) {
                setEffects(mainHandItem, potionEffects);
                setHitCounter(mainHandItem, 0);
            }

            if (!user.getAbilities().creativeMode) {
                offHandItem.decrement(1);
                if (offHandItem.isEmpty()) {
                    user.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE));
                } else {
                    user.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
            return new TypedActionResult<>(ActionResult.SUCCESS, mainHandItem);
        }
        return super.use(world, user, hand);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return state.isOf(Blocks.COBWEB);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        List<StatusEffectInstance> effects = getEffects(stack);
        int hitCounter = getHitCounter(stack);

        if (effects != null && !effects.isEmpty() && (this.maxHits - hitCounter) > 0) {
            MutableText imbuedHits = Text.translatable(
                    "masks-n-glory.tooltip.pride.imbued_hits",
                    nf.format(maxHits - hitCounter)
            );
            tooltip.add(imbuedHits.fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));

            List<StatusEffectInstance> modifiedEffects = effects.stream()
                    .map(effect -> new StatusEffectInstance(
                            effect.getEffectType(),
                            effect.getDuration(),
                            effect.getAmplifier() + 1
                    ))
                    .collect(Collectors.toList());

            PotionUtil.buildTooltip(modifiedEffects, tooltip, 1.0F);
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public int getEnchantability() {
        return 1;
    }
}
