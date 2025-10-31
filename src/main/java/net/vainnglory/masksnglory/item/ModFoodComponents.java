package net.vainnglory.masksnglory.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent GILDED_SOUP = new FoodComponent.Builder().hunger(5).saturationModifier(1.5f)
            .statusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200), 0.75f).build();


    public static final FoodComponent ECHO_CARAMEL = new FoodComponent.Builder().hunger(2).saturationModifier(2)
            .statusEffect(new StatusEffectInstance(StatusEffects.HASTE, 300), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300), 1.0f).alwaysEdible().build();
}
