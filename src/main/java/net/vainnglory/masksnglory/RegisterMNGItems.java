package net.vainnglory.masksnglory;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static net.vainnglory.masksnglory.MasksNGlory.*;
import static net.vainnglory.masksnglory.item.ModItems.*;

public class RegisterMNGItems {

    private static <T extends Item> void registerItem(String name, T item) {
            Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), (Item) item);
        }

        private static void registerMNGItems() {
            registerItem("pale_upgrade_template", PALE_TEMPLATE);

        }


        public static void registerPaleItems() {
            registerMNGItems();
            LOGGER.info("Registering Peak Items for " + MOD_ID);
        }
    }
