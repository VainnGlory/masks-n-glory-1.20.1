package net.vainnglory.masksnglory.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

import static net.vainnglory.masksnglory.MasksNGlory.MOD_ID;

public class ModDamageTypes {

    public static final RegistryKey<DamageType> SOUL_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MasksNGlory.MOD_ID, "soul"));
    public static final RegistryKey<DamageType> DEATH_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MasksNGlory.MOD_ID, "death"));
    public static final RegistryKey<DamageType> PAN_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MasksNGlory.MOD_ID, "pan"));
    public static final RegistryKey<DamageType> RUSTED_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MasksNGlory.MOD_ID, "rusted"));

    public static DamageSource pan(LivingEntity entity) {
        return entity.getDamageSources().create(PAN_DAMAGE); }

    public static DamageSource rusted(LivingEntity entity) {
        return entity.getDamageSources().create(RUSTED_DAMAGE); }

    private static RegistryKey<DamageType> of(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE,new Identifier(MOD_ID, name));
    }

    public static void initialize() {

        MasksNGlory.LOGGER.info("Registered Player's Souls");
    }
}
