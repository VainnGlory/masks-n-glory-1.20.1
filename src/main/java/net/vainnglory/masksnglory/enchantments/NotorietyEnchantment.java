package net.vainnglory.masksnglory.enchantments;

import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.PrideItem;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;

public class NotorietyEnchantment extends Enchantment {

    public static final String KILLS_KEY = "NotorietyKills";
    private static final float DAMAGE_PER_KILL = 0.5f;

    public NotorietyEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 10; }
    @Override public int getMaxPower(int level) { return 50; }
    @Override public boolean isTreasure() { return false; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return true; }
    @Override public boolean isAvailableForRandomSelection() { return true; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.PRIDE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    @Override
    public boolean canAccept(Enchantment other) {
        return !(other instanceof TemperEnchantment) && !(other instanceof IncumbentEnchantment) && super.canAccept(other);
    }

    public static float getBonusDamage(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return 0f;
        return nbt.getList(KILLS_KEY, NbtElement.STRING_TYPE).size() * DAMAGE_PER_KILL;
    }

    public static NbtList getKillList(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return new NbtList();
        return nbt.getList(KILLS_KEY, NbtElement.STRING_TYPE);
    }

    public static boolean addName(ServerPlayerEntity player, ItemStack weapon, String name) {
        NbtCompound nbt = weapon.getOrCreateNbt();
        NbtList kills = nbt.getList(KILLS_KEY, NbtElement.STRING_TYPE);

        for (int i = 0; i < kills.size(); i++) {
            if (kills.getString(i).equals(name)) return false;
        }

        Multimap<EntityAttribute, EntityAttributeModifier> oldModifiers =
                weapon.getAttributeModifiers(EquipmentSlot.MAINHAND);

        kills.add(NbtString.of(name));
        nbt.put(KILLS_KEY, kills);

        Multimap<EntityAttribute, EntityAttributeModifier> newModifiers =
                weapon.getAttributeModifiers(EquipmentSlot.MAINHAND);

        player.getAttributes().removeModifiers(oldModifiers);
        player.getAttributes().addTemporaryModifiers(newModifiers);

        int count = kills.size();
        float volume = Math.min(2.5F, 1.0F + (count - 1) * 0.1F);
        float pitch = Math.max(0.3F, 1.05F - (count - 1) * 0.04F + (float) player.getRandom().nextGaussian() * 0.03F);

        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                MasksNGlorySounds.ITEM_PRIDE_NOTORIETY_JINGLE,
                player.getSoundCategory(),
                volume,
                pitch
        );

        return true;
    }

    public static void registerCallbacks() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity killed)) return;
            if (!(damageSource.getAttacker() instanceof ServerPlayerEntity killer)) return;

            ItemStack weapon = killer.getMainHandStack();
            if (!(weapon.getItem() instanceof PrideItem)) return;
            if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, weapon) <= 0) return;

            addName(killer, weapon, killed.getName().getString());
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof PrideItem)) return;
            if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, weapon) <= 0) return;
            NbtCompound nbt = weapon.getNbt();
            if (nbt != null) nbt.remove(KILLS_KEY);
        });
    }
}
