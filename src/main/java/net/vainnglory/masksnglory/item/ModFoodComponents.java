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

    public static final FoodComponent CHOGLOWBERRY = new FoodComponent.Builder().hunger(2).saturationModifier(0.5f)
            .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200), 0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200), 0.4f)
            .statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300), 0.4f)
            .statusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 500), 0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 500), 0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 100), 0.3f).alwaysEdible().build();
}
