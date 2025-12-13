package net.vainnglory.masksnglory.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ModArmorItem extends ArmorItem {
    private static final Map<ArmorMaterial, StatusEffectInstance> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<ArmorMaterial, StatusEffectInstance>())
                    .put(ModArmorMaterials.ESHARD, new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1,
                            false, false, true))
                    .put(ModArmorMaterials.OSHARD, new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 1,
                            false, false, true))
                    .put(ModArmorMaterials.PSHARD, new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 11000, 1,
                            false, false, true))
                    .put(ModArmorMaterials.TSHARD, new StatusEffectInstance(StatusEffects.ABSORPTION, 300, 3,
                            false, false, true))
                    .put(ModArmorMaterials.HSSHARD, new StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 3,
                            false, false, true))
                    .put(ModArmorMaterials.HMASKS, new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 400, 0,
                            false, false, true))
                    .put(ModArmorMaterials.GMASKS, new StatusEffectInstance(StatusEffects.BAD_OMEN, 400, 0,
                            false, false, true))
                    .put(ModArmorMaterials.SMASKS, new StatusEffectInstance(StatusEffects.HASTE, 600, 0,
                            false, false, true))
                    .put(ModArmorMaterials.KMASKS, new StatusEffectInstance(StatusEffects.ABSORPTION, 550, 2,
                            false, false, true))
                    .put(ModArmorMaterials.EMASKS, new StatusEffectInstance(StatusEffects.NIGHT_VISION, 5000, 0,
                            false, false, true))
                    .put(ModArmorMaterials.NMASKS, new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 1000, 0,
                            false, false, true))
                    .put(ModArmorMaterials.DOSHARD, new StatusEffectInstance(StatusEffects.SPEED, 600, 4,
                            false, false, true))
                    .put(ModArmorMaterials.CSHARD, new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0,
                            false, false, true))
                    .put(ModArmorMaterials.CRSHARD, new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0,
                            false, false, true))
                    .put(ModArmorMaterials.HHSHARD, new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1,
                            false, false, true))
                    .put(ModArmorMaterials.DVSHARD, new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1,
                            false, false, true))
                    .put(ModArmorMaterials.STSHARD, new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0,
                            false, false, true))
                    .put(ModArmorMaterials.DSHARD, new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1,
                            false, false, true)).build();


    public ModArmorItem(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(!world.isClient()) {
            if(entity instanceof PlayerEntity player && hasFullSuitOfArmorOn(player)) {
                evaluateArmorEffects(player);
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void evaluateArmorEffects(PlayerEntity player) {
        for (Map.Entry<ArmorMaterial, StatusEffectInstance> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
            ArmorMaterial mapArmorMaterial = entry.getKey();
            StatusEffectInstance mapStatusEffect = entry.getValue();

            if(hasCorrectArmorOn(mapArmorMaterial, player)) {
                addStatusEffectForMaterial(player, mapArmorMaterial, mapStatusEffect);
            }
        }
    }

    private void addStatusEffectForMaterial(PlayerEntity player, ArmorMaterial mapArmorMaterial, StatusEffectInstance mapStatusEffect) {
        boolean hasPlayerEffect = player.hasStatusEffect(mapStatusEffect.getEffectType());

        if(hasCorrectArmorOn(mapArmorMaterial, player) && !hasPlayerEffect) {
            player.addStatusEffect(new StatusEffectInstance(mapStatusEffect));
        }
    }

    private boolean hasFullSuitOfArmorOn(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);

        return !helmet.isEmpty();
    }

    private boolean hasCorrectArmorOn(ArmorMaterial material, PlayerEntity player) {
        for (ItemStack armorStack: player.getInventory().armor) {
            if (!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }
        ArmorItem helmet = ((ArmorItem)player.getInventory().getArmorStack(3).getItem());

        return helmet.getMaterial() == material;
    }
}