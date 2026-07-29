package net.vainnglory.masksnglory.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final DefaultParticleType NULL_EFFECT = FabricParticleTypes.simple();
    public static final DefaultParticleType MANIA_EFFECT = FabricParticleTypes.simple();

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("masks-n-glory", "null_effect"), NULL_EFFECT);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("masks-n-glory", "mania_effect"), MANIA_EFFECT);
    }
}
