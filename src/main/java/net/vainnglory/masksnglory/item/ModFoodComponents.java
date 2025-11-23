package net.vainnglory.masksnglory.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent GILDED_SOUP = new FoodComponent.Builder().hunger(5).saturationModifier(1.5f)
            .statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200), 0.75f).build();


    public static final FoodComponent ECHO_CARAMEL = new FoodComponent.Builder().hunger(2).saturationModifier(2)
            .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 400), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400), 1.0f).alwaysEdible().build();

    public static final FoodComponent CAPPLE = new FoodComponent.Builder().hunger(4).saturationModifier(1.0F)
            .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 600), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600), 1.0f).alwaysEdible().build();

    public static final FoodComponent GCAPPLE = new FoodComponent.Builder().hunger(6).saturationModifier(1.4F)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 250, 1), 1.0F)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 4000, 0), 1.0F)
            .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 600), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600), 1.0f)
            .alwaysEdible()
            .build();

    public static final FoodComponent CHOGLOWBERRY = new FoodComponent.Builder().hunger(2).saturationModifier(0.5f)
            .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200), 0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200), 0.4f)
            .statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300), 0.4f)
            .statusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 500), 0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 500), 0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 100), 0.3f).alwaysEdible().build();

    public static final FoodComponent GILDED_HEART = new FoodComponent.Builder()
            .statusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100), 1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 0), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 0), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 3), 1.0f)
            .alwaysEdible()
            .build();

}
