package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.vainnglory.masksnglory.mixin.ProjectileOwnerAccessor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderPearlChunkLoader {

    public static final ChunkTicketType<ChunkPos> PEARL_TICKET =
            ChunkTicketType.create("masksnglory_ender_pearl", Comparator.comparingLong(ChunkPos::toLong), 100);

    private static final int TICKET_RADIUS = 2;
    private static final int SWEEP_INTERVAL = 100;
    private static final int STALE_TICKS = 200;

    private static final Map<UUID, Integer> lastSeen = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(EnderPearlChunkLoader::sweep);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                reticketFor(server, handler.player.getUuid()));
    }

    public static void onPearlTick(ServerWorld world, EnderPearlEntity pearl) {
        MinecraftServer server = world.getServer();
        UUID ownerId = resolveOwner(pearl);
        if (ownerId == null) return;
        if (server.getPlayerManager().getPlayer(ownerId) == null) return;

        ChunkPos chunkPos = new ChunkPos(pearl.getBlockPos());
        world.getChunkManager().addTicket(PEARL_TICKET, chunkPos, TICKET_RADIUS, chunkPos);

        PearlStasisState.getOrCreate(server)
                .put(pearl.getUuid(), ownerId, world.getRegistryKey(), chunkPos.x, chunkPos.z);
        lastSeen.put(pearl.getUuid(), server.getTicks());
    }

    private static UUID resolveOwner(EnderPearlEntity pearl) {
        UUID stored = ((ProjectileOwnerAccessor) pearl).masksnglory$getOwnerUuid();
        if (stored != null) return stored;
        Entity owner = pearl.getOwner();
        return owner == null ? null : owner.getUuid();
    }

    private static void reticketFor(MinecraftServer server, UUID ownerId) {
        PearlStasisState state = PearlStasisState.getOrCreate(server);
        int now = server.getTicks();
        for (PearlStasisState.PearlRecord record : state.forOwner(ownerId)) {
            ServerWorld world = server.getWorld(record.dimension);
            if (world == null) continue;
            ChunkPos chunkPos = new ChunkPos(record.chunkX, record.chunkZ);
            world.getChunkManager().addTicket(PEARL_TICKET, chunkPos, TICKET_RADIUS, chunkPos);
            lastSeen.put(record.pearlId, now);
        }
    }

    private static void sweep(MinecraftServer server) {
        if (server.getTicks() % SWEEP_INTERVAL != 0) return;

        PearlStasisState state = PearlStasisState.getOrCreate(server);
        int now = server.getTicks();

        for (PearlStasisState.PearlRecord record : state.all()) {
            if (server.getPlayerManager().getPlayer(record.ownerId) == null) continue;

            Integer seen = lastSeen.get(record.pearlId);
            if (seen == null) {
                lastSeen.put(record.pearlId, now);
                continue;
            }
            if (now - seen < STALE_TICKS) continue;

            state.remove(record.pearlId);
            lastSeen.remove(record.pearlId);
        }
    }
}
