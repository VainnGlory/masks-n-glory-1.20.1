package net.vainnglory.masksnglory.util;

import net.minecraft.item.ItemStack;

import java.util.*;

public class ActorManager {

    public static final Set<UUID> offScriptActive = new HashSet<>();
    public static final Map<UUID, Integer> actorSneakTicks = new HashMap<>();
    public static final Map<UUID, Long> lastDamageTicks = new HashMap<>();
    public static final Map<UUID, Integer> offScriptCooldowns = new HashMap<>();
    public static final Set<UUID> sympathyInProgress = new HashSet<>();
}
