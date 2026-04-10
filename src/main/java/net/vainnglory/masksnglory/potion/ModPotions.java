package net.vainnglory.masksnglory.potion;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.effect.ModEffects;

public class ModPotions {
    public static Potion SPIT;
    public static Potion SPITE;

    public static void registerPotions() {
        SPIT = Registry.register(
                Registries.POTION,
                new Identifier("masks-n-glory", "spit"),
                new Potion("masks_n_glory.spit")
        );
        SPITE = Registry.register(
                Registries.POTION,
                new Identifier("masks-n-glory", "spite"),
                new Potion("masks_n_glory.spite", new StatusEffectInstance(ModEffects.SPITE, 2400, 0))
        );

        BrewingRecipeRegistry.registerPotionRecipe(SPIT, Items.SPIDER_EYE, SPITE);
    }
}
