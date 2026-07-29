package net.vainnglory.masksnglory.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.enchantments.NotorietyEnchantment;
import net.vainnglory.masksnglory.item.custom.PrideItem;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NotorietyCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("notoriety")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(literal("add")
                            .then(argument("name", StringArgumentType.word())
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        ServerPlayerEntity player = source.getPlayer();
                                        if (player == null) {
                                            source.sendError(Text.literal("This command can only be run by a player."));
                                            return 0;
                                        }

                                        ItemStack weapon = player.getMainHandStack();
                                        if (!(weapon.getItem() instanceof PrideItem)) {
                                            source.sendError(Text.literal("You must be holding a Prideful Husk."));
                                            return 0;
                                        }

                                        if (EnchantmentHelper.getLevel(ModEnchantments.NOTORIETY, weapon) <= 0) {
                                            source.sendError(Text.literal("Your Prideful Husk must have the Notoriety enchantment."));
                                            return 0;
                                        }

                                        String name = StringArgumentType.getString(context, "name");
                                        boolean added = NotorietyEnchantment.addName(player, weapon, name);

                                        if (added) {
                                            source.sendFeedback(() -> Text.literal("Added \"" + name + "\" to your sword's Notoriety list."), false);
                                        } else {
                                            source.sendError(Text.literal("\"" + name + "\" is already on your sword's Notoriety list."));
                                        }
                                        return 1;
                                    }))));
        });
    }
}
