package net.vainnglory.masksnglory.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import net.vainnglory.masksnglory.util.ModRarities;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public class ModArmorItem extends ArmorItem {
    private final ModRarities rarity;


    private static final Map<ArmorMaterial, List<StatusEffectInstance>> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<ArmorMaterial, List<StatusEffectInstance>>())

                    .put(ModArmorMaterials.ESHARD, List.of(
                            new StatusEffectInstance(StatusEffects.UNLUCK, 150, 254, false, false, true)
                    ))

                    .put(ModArmorMaterials.OSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 1, false, false, true)
                    ))

                    .put(ModArmorMaterials.PSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 11000, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.TSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.ABSORPTION, 300, 3, false, false, true),
                            new StatusEffectInstance(StatusEffects.HUNGER, 100, 10, false, false, true)
                    ))

                    .put(ModArmorMaterials.HSSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 3, false, false, true),
                            new StatusEffectInstance(StatusEffects.HUNGER, 400, 2, false, false, true)
                    ))

                    .put(ModArmorMaterials.HMASKS, List.of(
                            new StatusEffectInstance(StatusEffects.GLOWING, 400, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 400, 1, false, false, true)
                    ))

                    .put(ModArmorMaterials.GMASKS, List.of(
                            new StatusEffectInstance(StatusEffects.BAD_OMEN, 400, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.HUNGER, 150, 254, false, false, true)
                    ))

                    .put(ModArmorMaterials.SMASKS, List.of(
                            new StatusEffectInstance(StatusEffects.HASTE, 600, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.UNLUCK, 600, 1, false, false, true),
                            new StatusEffectInstance(StatusEffects.REGENERATION, 600, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.KMASKS, List.of(
                            new StatusEffectInstance(StatusEffects.ABSORPTION, 550, 2, false, false, true),
                            new StatusEffectInstance(StatusEffects.SLOWNESS, 150, 1, false, false, true)
                    ))

                    .put(ModArmorMaterials.EMASKS, List.of(
                            new StatusEffectInstance(StatusEffects.NIGHT_VISION, 5000, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.HASTE, 150, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.NMASKS, List.of(
                            new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 1000, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.LUCK, 1000, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 1000, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.DOSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.SPEED, 600, 4, false, false, true)
                    ))

                    .put(ModArmorMaterials.CSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.CRSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.HHSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1, false, false, true)
                    ))

                    .put(ModArmorMaterials.DVSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1, false, false, true)
                    ))

                    .put(ModArmorMaterials.STSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0, false, false, true),
                            new StatusEffectInstance(StatusEffects.HUNGER, 400, 0, false, false, true)
                    ))

                    .put(ModArmorMaterials.DSHARD, List.of(
                            new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1, false, false, true),
                            new StatusEffectInstance(StatusEffects.HUNGER, 400, 4, false, false, true)
                    ))
                    .build();


    public ModArmorItem(ArmorMaterial material, Type type, Settings settings, ModRarities rarity) {
        super(material, type, settings);
        this.rarity = rarity;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);

        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(!world.isClient()) {
            if(entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if(hasHelmetEquipped(player)) {
                    evaluateArmorEffects(player);
                }
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void evaluateArmorEffects(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);

        if(helmet.getItem() instanceof ArmorItem) {
            ArmorItem helmetItem = (ArmorItem) helmet.getItem();
            ArmorMaterial helmetMaterial = helmetItem.getMaterial();

            List<StatusEffectInstance> effectInstances = MATERIAL_TO_EFFECT_MAP.get(helmetMaterial);

            if(effectInstances != null) {

                for(StatusEffectInstance effect : effectInstances) {
                    addStatusEffectForMaterial(player, effect);
                }
            }
        }
    }

    private void addStatusEffectForMaterial(PlayerEntity player, StatusEffectInstance mapStatusEffect) {
        boolean hasPlayerEffect = player.hasStatusEffect(mapStatusEffect.getEffectType());

        if(!hasPlayerEffect) {
            player.addStatusEffect(new StatusEffectInstance(mapStatusEffect));
        }
    }

    private boolean hasHelmetEquipped(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);
        return !helmet.isEmpty();
    }
}