package net.vainnglory.masksnglory.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {

    public static Enchantment FEAR = new FearEnchantment();
    public static Enchantment STUNNED = new DiscombobulatedEnchantment();
    public static Enchantment BLUNT = new BluntEnchantment();
    public static Enchantment LETHAL = new LethalEnchantment();
    public static Enchantment SERIAL = new SerialEnchantment();
    public static Enchantment SKULL = new SkullBreakerEnchantment();
    public static Enchantment IMPACT = new ImpactEnchantment();
    public static Enchantment DEATH = new GuillotineEnchantment();
    public static Enchantment COMBO = new ComboEnchantment();
    public static Enchantment ANTISEPTIC = new AntisepticEnchantment();
    public static Enchantment SOUL = new SoulPhaseEnchantment();
    public static Enchantment RET = new RetributionEnchantment();
    public static Enchantment UNDEAD = new UndeadArmyEnchantment();


    public static void registerEnchantments(){
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "fear"), FEAR);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "discombobulated"), STUNNED);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "blunt"), BLUNT);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "lethal"), LETHAL);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "serial"), SERIAL);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "skull"), SKULL);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "death"), DEATH);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "impact"), IMPACT);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "combo"), COMBO);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "antiseptic"), ANTISEPTIC);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "soul"), SOUL);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "ret"), RET);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "undead"), UNDEAD);

        }



    }

