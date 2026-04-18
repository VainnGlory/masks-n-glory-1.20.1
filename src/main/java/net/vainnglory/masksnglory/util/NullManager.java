package net.vainnglory.masksnglory.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NullManager {
    public static final String EFFECT_VOID_TAG = "NullEffectItem";
    public static final String ORIGINAL_ITEM_TAG = "OriginalItem";
    public static final String OWNER_UUID_TAG = "NullOwner";

    private static final Map<UUID, Long> expireTimes = new HashMap<>();
    private static long currentWorldTime = 0;

    public static void tick(long worldTime) {
        currentWorldTime = worldTime;
    }

    public static boolean isAffected(UUID uuid) {
        return expireTimes.containsKey(uuid);
    }

    public static boolean needsRestoration(UUID uuid, long worldTime) {
        Long expire = expireTimes.get(uuid);
        return expire != null && worldTime >= expire;
    }

    public static void cleanup(UUID uuid) {
        expireTimes.remove(uuid);
    }

    public static void applyEffect(ServerPlayerEntity player, int durationTicks) {
        List<Integer> eligible = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && !isEffectVoidItem(stack)) {
                eligible.add(i);
            }
        }

        Collections.shuffle(eligible);
        int count = Math.min(2, eligible.size());
        if (count == 0) return;

        for (int i = 0; i < count; i++) {
            int slot = eligible.get(i);
            ItemStack original = player.getInventory().getStack(slot);

            ItemStack voidItem = new ItemStack(ModItems.GLORIOUS);
            NbtCompound nbt = voidItem.getOrCreateNbt();
            nbt.putBoolean(EFFECT_VOID_TAG, true);
            nbt.putUuid(OWNER_UUID_TAG, player.getUuid());
            NbtCompound itemNbt = new NbtCompound();
            original.writeNbt(itemNbt);
            nbt.put(ORIGINAL_ITEM_TAG, itemNbt);

            player.getInventory().setStack(slot, voidItem);
        }

        expireTimes.put(player.getUuid(), currentWorldTime + durationTicks);
    }

    public static void restoreItems(ServerPlayerEntity player) {
        expireTimes.remove(player.getUuid());
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isOwnedEffectVoidItem(stack, player.getUuid())) {
                NbtCompound nbt = stack.getNbt();
                NbtCompound itemNbt = nbt.getCompound(ORIGINAL_ITEM_TAG);
                ItemStack restored = ItemStack.fromNbt(itemNbt);
                player.getInventory().setStack(i, restored);
            }
        }
    }

    public static boolean isEffectVoidItem(ItemStack stack) {
        if (!stack.isOf(ModItems.GLORIOUS)) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(EFFECT_VOID_TAG);
    }

    private static boolean isOwnedEffectVoidItem(ItemStack stack, UUID owner) {
        if (!isEffectVoidItem(stack)) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.containsUuid(OWNER_UUID_TAG) && nbt.getUuid(OWNER_UUID_TAG).equals(owner);
    }
}
