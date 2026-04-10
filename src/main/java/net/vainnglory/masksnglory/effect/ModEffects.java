package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static final StatusEffect PINNING = new PinningEffect();
    public static final StatusEffect BLEEDING = new BleedingEffect();
    public static final StatusEffect ACTOR = new ActorEffect();
    public static final StatusEffect OFF_SCRIPT_FLAG = new OffScriptFlagEffect();
    public static final StatusEffect STUNT_DOUBLE = new StuntDoubleEffect();
    public static final StatusEffect ROTTING = new RottingEffect();
    public static final StatusEffect SEIZED = new SeizedEffect();
    public static final StatusEffect SUGAR_RUSH = new SugarRushEffect();
    public static final StatusEffect SPITE = new SpiteEffect();

    public static void registerEffects() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "pinning"), PINNING);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "bleeding"), BLEEDING);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "actor"), ACTOR);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "off_script_flag"), OFF_SCRIPT_FLAG);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "stunt_double"), STUNT_DOUBLE);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "rotting"), ROTTING);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "seized"), SEIZED);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "sugar_rush"), SUGAR_RUSH);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("masks-n-glory", "spite"), SPITE);
    }
}
