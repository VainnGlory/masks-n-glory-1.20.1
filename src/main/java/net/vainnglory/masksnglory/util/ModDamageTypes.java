package net.vainnglory.masksnglory.util;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

public class ModDamageTypes {

    public static final RegistryKey<DamageType> SOUL_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MasksNGlory.MOD_ID, "soul"));

    public static void initialize() {

        MasksNGlory.LOGGER.info("Registered Player's Souls");
    }
}
