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


    public static void registerEnchantments(){
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "fear"), FEAR);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "discombobulated"), STUNNED);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "blunt"), BLUNT);
        Registry.register(Registries.ENCHANTMENT, new Identifier("masks-n-glory", "lethal"), LETHAL);


    }
}
