package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static final StatusEffect PINNING = new PinningEffect();
    public static final StatusEffect BLEEDING = new BleedingEffect();

    public static void registerEffects() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "pinning"), PINNING);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "bleeding"), BLEEDING);
    }
}
