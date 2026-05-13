package net.vainnglory.masksnglory.item.custom;

import net.minecraft.screen.slot.Slot;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HuntersSatchelItem extends Item {

    private static final int ARMOR_COOLDOWN_TICKS = 400;
    private static final int POTION_MAX = 15;
    private static final int DEFAULT_SLOTS = 9;

    public HuntersSatchelItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private SatchelMode getMode(ItemStack stack) {
        if (!stack.hasCustomName()) return SatchelMode.DEFAULT;
        String name = stack.getName().getString().toLowerCase();
        if (name.contains("potion")) return SatchelMode.POTION;
        if (name.contains("armor")) return SatchelMode.ARMOR;
        return SatchelMode.DEFAULT;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x1E756B)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack satchel = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.pass(satchel);

        return switch (getMode(satchel)) {
            case ARMOR -> useArmor(player, satchel);
            case POTION -> usePotion(player, satchel);
            default -> openGui(player, satchel);
        };
    }

    private TypedActionResult<ItemStack> openGui(PlayerEntity player, ItemStack satchel) {
        final SimpleInventory inv = loadInventory(satchel);
        inv.addListener(sender -> saveInventory(satchel, inv));
        player.playSound(SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new SatchelScreenHandler(syncId, playerInv, inv, player),
                Text.literal("Hunter's Satchel")
        ));
        return TypedActionResult.success(satchel);
    }

    private SimpleInventory loadInventory(ItemStack satchel) {
        SimpleInventory inv = new SimpleInventory(DEFAULT_SLOTS) {
            @Override
            public boolean isValid(int slot, ItemStack stack) {
                return !(stack.getItem() instanceof HuntersSatchelItem);
            }
        };
        NbtList list = satchel.getOrCreateNbt().getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size() && i < DEFAULT_SLOTS; i++) {
            inv.setStack(i, ItemStack.fromNbt(list.getCompound(i)));
        }
        return inv;
    }

    private void saveInventory(ItemStack satchel, SimpleInventory inv) {
        NbtList list = new NbtList();
        for (int i = 0; i < inv.size(); i++) {
            NbtCompound c = new NbtCompound();
            inv.getStack(i).writeNbt(c);
            list.add(c);
        }
        satchel.getOrCreateNbt().put("Items", list);
    }

    private TypedActionResult<ItemStack> usePotion(PlayerEntity player, ItemStack satchel) {
        NbtCompound nbt = satchel.getOrCreateNbt();

        if (player.isSneaking()) {
            int count = nbt.getInt("PotionCount");
            String storedKey = nbt.getString("PotionKey");
            boolean inserted = false;

            for (int i = 0; i < player.getInventory().size() && count < POTION_MAX; i++) {
                ItemStack slot = player.getInventory().getStack(i);
                if (!isPotion(slot)) continue;

                String slotKey = getPotionKey(slot);
                if (!storedKey.isEmpty() && !storedKey.equals(slotKey)) continue;

                if (storedKey.isEmpty()) {
                    ItemStack template = slot.copy();
                    template.setCount(1);
                    NbtCompound templateNbt = new NbtCompound();
                    template.writeNbt(templateNbt);
                    nbt.put("StoredPotion", templateNbt);
                    nbt.putString("PotionKey", slotKey);
                    storedKey = slotKey;
                }

                int canAdd = Math.min(slot.getCount(), POTION_MAX - count);
                count += canAdd;
                slot.decrement(canAdd);
                nbt.putInt("PotionCount", count);
                inserted = true;
            }

            if (inserted) {
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return TypedActionResult.success(satchel);
        } else {
            int count = nbt.getInt("PotionCount");
            if (count <= 0) return TypedActionResult.fail(satchel);

            ItemStack potion = ItemStack.fromNbt(nbt.getCompound("StoredPotion"));
            potion.setCount(1);
            if (!player.getInventory().insertStack(potion)) {
                player.dropItem(potion, false);
            }

            nbt.putInt("PotionCount", count - 1);
            if (count - 1 == 0) {
                nbt.remove("StoredPotion");
                nbt.remove("PotionKey");
            }

            player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return TypedActionResult.success(satchel);
        }
    }

    private TypedActionResult<ItemStack> useArmor(PlayerEntity player, ItemStack satchel) {
        if (player.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(satchel);
        }

        NbtCompound nbt = satchel.getOrCreateNbt();
        NbtList stored = nbt.getList("StoredArmor", NbtElement.COMPOUND_TYPE);
        boolean hasStored = !stored.isEmpty();
        NbtList newStored = new NbtList();

        for (int i = 0; i < 4; i++) {
            ItemStack equipped = player.getInventory().getArmorStack(i);
            ItemStack swapIn = hasStored ? ItemStack.fromNbt(stored.getCompound(i)) : ItemStack.EMPTY;

            NbtCompound entry = new NbtCompound();
            equipped.writeNbt(entry);
            newStored.add(entry);

            player.getInventory().armor.set(i, swapIn.copy());
        }

        nbt.put("StoredArmor", newStored);
        player.getItemCooldownManager().set(this, ARMOR_COOLDOWN_TICKS);
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        return TypedActionResult.success(satchel);
    }

    private boolean isPotion(ItemStack stack) {
        return stack.getItem() == Items.POTION
                || stack.getItem() == Items.SPLASH_POTION
                || stack.getItem() == Items.LINGERING_POTION;
    }

    private String getPotionKey(ItemStack stack) {
        return stack.getItem().toString() + "|" + PotionUtil.getPotion(stack).toString();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        switch (getMode(stack)) {
            case DEFAULT -> {
                int used = 0;
                if (nbt != null) {
                    NbtList list = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
                    for (int i = 0; i < list.size(); i++) {
                        if (!ItemStack.fromNbt(list.getCompound(i)).isEmpty()) used++;
                    }
                }
                tooltip.add(Text.literal("§7" + used + "/" + DEFAULT_SLOTS + " slots used"));
            }
            case POTION -> {
                int count = nbt != null ? nbt.getInt("PotionCount") : 0;
                tooltip.add(Text.literal("§7" + count + "/" + POTION_MAX + " potions"));
            }
            case ARMOR -> {
                boolean has = nbt != null && !nbt.getList("StoredArmor", NbtElement.COMPOUND_TYPE).isEmpty();
                tooltip.add(Text.literal(has ? "§7Armor set stored" : "§7No armor stored"));
            }
        }
    }

    private static class SatchelScreenHandler extends GenericContainerScreenHandler {
        private final PlayerEntity player;

        SatchelScreenHandler(int syncId, PlayerInventory playerInv, SimpleInventory inv, PlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X1, syncId, playerInv, inv, 1);
            this.player = player;
            for (int i = 0; i < DEFAULT_SLOTS; i++) {
                Slot old = this.slots.get(i);
                final int slotIndex = i;
                this.slots.set(slotIndex, new Slot(inv, slotIndex, old.x, old.y) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return !(stack.getItem() instanceof HuntersSatchelItem);
                    }
                });
            }
        }

        @Override
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);
            player.playSound(SoundEvents.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private enum SatchelMode { DEFAULT, POTION, ARMOR }
}