package net.vainnglory.masksnglory.particle;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.vainnglory.masksnglory.effect.ModEffects;

public class ManiaParticles {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientWorld world = client.world;
            if (world == null) return;

            for (PlayerEntity player : world.getPlayers()) {
                if (!player.hasStatusEffect(ModEffects.MANIA)) continue;
                if (world.getRandom().nextFloat() >= 0.25F) continue;

                double x = player.getX() + (world.getRandom().nextDouble() - 0.5) * player.getWidth();
                double y = player.getY() + world.getRandom().nextDouble() * player.getHeight();
                double z = player.getZ() + (world.getRandom().nextDouble() - 0.5) * player.getWidth();

                world.addParticle(ModParticles.MANIA_EFFECT, x, y, z, 0.0, 0.0, 0.0);
            }
        });
    }
}
