package net.vainnglory.masksnglory.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PearlStasisState extends PersistentState {

    public static class PearlRecord {
        public final UUID pearlId;
        public final UUID ownerId;
        public final RegistryKey<World> dimension;
        public final int chunkX;
        public final int chunkZ;

        public PearlRecord(UUID pearlId, UUID ownerId, RegistryKey<World> dimension, int chunkX, int chunkZ) {
            this.pearlId = pearlId;
            this.ownerId = ownerId;
            this.dimension = dimension;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    private final Map<UUID, PearlRecord> records = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (PearlRecord record : records.values()) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("Pearl", record.pearlId);
            entry.putUuid("Owner", record.ownerId);
            entry.putString("Dimension", record.dimension.getValue().toString());
            entry.putInt("ChunkX", record.chunkX);
            entry.putInt("ChunkZ", record.chunkZ);
            list.add(entry);
        }
        nbt.put("Pearls", list);
        return nbt;
    }

    public static PearlStasisState fromNbt(NbtCompound nbt) {
        PearlStasisState state = new PearlStasisState();
        NbtList list = nbt.getList("Pearls", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            if (!entry.containsUuid("Pearl") || !entry.containsUuid("Owner")) continue;
            UUID pearlId = entry.getUuid("Pearl");
            UUID ownerId = entry.getUuid("Owner");
            RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, new Identifier(entry.getString("Dimension")));
            state.records.put(pearlId, new PearlRecord(pearlId, ownerId, dimension, entry.getInt("ChunkX"), entry.getInt("ChunkZ")));
        }
        return state;
    }

    public static PearlStasisState getOrCreate(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(
                PearlStasisState::fromNbt,
                PearlStasisState::new,
                "pearl_stasis"
        );
    }

    public void put(UUID pearlId, UUID ownerId, RegistryKey<World> dimension, int chunkX, int chunkZ) {
        PearlRecord existing = records.get(pearlId);
        if (existing != null && existing.chunkX == chunkX && existing.chunkZ == chunkZ
                && existing.dimension.equals(dimension)) {
            return;
        }
        records.put(pearlId, new PearlRecord(pearlId, ownerId, dimension, chunkX, chunkZ));
        markDirty();
    }

    public void remove(UUID pearlId) {
        if (records.remove(pearlId) != null) {
            markDirty();
        }
    }

    public List<PearlRecord> forOwner(UUID ownerId) {
        List<PearlRecord> out = new ArrayList<>();
        for (PearlRecord record : records.values()) {
            if (record.ownerId.equals(ownerId)) out.add(record);
        }
        return out;
    }

    public List<PearlRecord> all() {
        return new ArrayList<>(records.values());
    }
}
