package net.vainnglory.masksnglory.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class CastlePersistentState extends PersistentState {
    public boolean placed = false;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("Placed", placed);
        return nbt;
    }

    public static CastlePersistentState fromNbt(NbtCompound nbt) {
        CastlePersistentState s = new CastlePersistentState();
        s.placed = nbt.getBoolean("Placed");
        return s;
    }

    public static CastlePersistentState getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                CastlePersistentState::fromNbt,
                CastlePersistentState::new,
                "castle_spawner"
        );
    }

    public static CastlePersistentState getOrCreateRuined(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                CastlePersistentState::fromNbt,
                CastlePersistentState::new,
                "ruined_castle_spawner"
        );
    }

    public static CastlePersistentState getOrCreateMineshaftVault(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                CastlePersistentState::fromNbt,
                CastlePersistentState::new,
                "mineshaft_vault_spawner"
        );
    }
}
