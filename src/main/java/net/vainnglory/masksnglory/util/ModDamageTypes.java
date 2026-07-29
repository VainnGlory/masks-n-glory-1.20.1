package net.vainnglory.masksnglory.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

import static net.vainnglory.masksnglory.MasksNGlory.MOD_ID;

public class ModDamageTypes {

    public static final RegistryKey<DamageType> SOUL_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "soul"));
    public static final RegistryKey<DamageType> DEATH_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "death"));
    public static final RegistryKey<DamageType> PAN_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "pan"));
    public static final RegistryKey<DamageType> RUSTED_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "rusted"));
    public static final RegistryKey<DamageType> EGO_LAST_WORD =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "ego_last_word"));
    public static final RegistryKey<DamageType> BLEEDING_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "bleeding"));
    public static final RegistryKey<DamageType> SHEAR_PROJECTILE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "shear_projectile"));
    public static final RegistryKey<DamageType> MANIA_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "mania"));
    public static final RegistryKey<DamageType> GLITCH_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "glitch"));

    public static DamageSource pan(LivingEntity attacker, LivingEntity target) {
        return target.getDamageSources().create(PAN_DAMAGE, attacker, attacker);
    }

    public static DamageSource rusted(LivingEntity attacker, LivingEntity target) {
        return target.getDamageSources().create(RUSTED_DAMAGE, attacker, attacker);
    }

    public static DamageSource egoLastWord(LivingEntity entity) {
        return entity.getDamageSources().create(EGO_LAST_WORD); }

    public static DamageSource bleeding(LivingEntity entity) {
        return entity.getDamageSources().create(BLEEDING_DAMAGE); }

    public static DamageSource shearProjectile(LivingEntity target) {
        return target.getDamageSources().create(SHEAR_PROJECTILE);
    }

    public static DamageSource shearProjectile(PlayerEntity attacker, LivingEntity target) {
        return target.getDamageSources().create(SHEAR_PROJECTILE, attacker, attacker);
    }

    public static DamageSource mania(LivingEntity entity) {
        return entity.getDamageSources().create(MANIA_DAMAGE);
    }

    public static DamageSource glitch(LivingEntity entity) {
        return entity.getDamageSources().create(GLITCH_DAMAGE);
    }

    public static void initialize() {
        MasksNGlory.LOGGER.info("Registered Player's Souls");
    }
}