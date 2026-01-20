package net.vainnglory.masksnglory.util;

import net.vainnglory.masksnglory.util.FlashAttackPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {

    public static KeyBinding flashAttackKey;

    public static void register() {
        flashAttackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.masks-n-glory.flash_attack",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.masks-n-glory.keybinds"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (flashAttackKey.wasPressed()) {
                if (client.player != null) {
                    FlashAttackPacket.send();
                }
            }
        });
    }
}
