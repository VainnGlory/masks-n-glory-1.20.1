package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.PrideItem;

import java.util.ArrayList;
import java.util.List;

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

    private record PendingDamage(LivingEntity target, PlayerEntity attacker, float bonus) {}

    public static void registerCallbacks() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity killed)) return;
            if (!(damageSource.getAttacker() instanceof ServerPlayerEntity killer)) return;

            ItemStack weapon = killer.getMainHandStack();
            if (!(weapon.getItem() instanceof PrideItem)) return;
            if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, weapon) <= 0) return;

            NbtCompound nbt = weapon.getOrCreateNbt();
            NbtList kills = nbt.getList(KILLS_KEY, NbtElement.STRING_TYPE);
            String name = killed.getName().getString();

            for (int i = 0; i < kills.size(); i++) {
                if (kills.getString(i).equals(name)) return;
            }

            kills.add(NbtString.of(name));
            nbt.put(KILLS_KEY, kills);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof PrideItem)) return;
            if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, weapon) <= 0) return;
            NbtCompound nbt = weapon.getNbt();
            if (nbt != null) nbt.remove(KILLS_KEY);
        });

        List<PendingDamage> pendingDamages = new ArrayList<>();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof PrideItem)) return ActionResult.PASS;
            if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, weapon) <= 0) return ActionResult.PASS;
            float bonus = getBonusDamage(weapon);
            if (bonus <= 0) return ActionResult.PASS;
            pendingDamages.add(new PendingDamage(target, player, bonus));
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (pendingDamages.isEmpty()) return;
            for (PendingDamage pd : pendingDamages) {
                if (!pd.target().isAlive()) continue;
                pd.target().hurtTime = 0;
                pd.target().timeUntilRegen = 0;
                pd.target().damage(pd.target().getDamageSources().playerAttack(pd.attacker()), pd.bonus());
            }
            pendingDamages.clear();
        });
    }
}
