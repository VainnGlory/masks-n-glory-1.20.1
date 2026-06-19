package net.vainnglory.masksnglory.entity.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.ModDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WatcherEntity extends PathAwareEntity {

    private static final TrackedData<Optional<UUID>> OWNER_UUID =
            DataTracker.registerData(WatcherEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private static final Map<UUID, UUID> PLAYER_TO_WATCHER = new HashMap<>();
    private static final List<PendingRespawn> PENDING_RESPAWNS = new ArrayList<>();

    public WatcherEntity(EntityType<? extends WatcherEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new WatcherOrbitGoal(this));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    public void setOwnerUuid(UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.of(uuid));
    }

    public Optional<UUID> getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID);
    }

    @Nullable
    public PlayerEntity getOwnerEntity() {
        return getOwnerUuid().map(uuid -> this.getWorld().getPlayerByUuid(uuid)).orElse(null);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getOwnerUuid().ifPresent(uuid -> nbt.putUuid("OwnerUuid", uuid));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("OwnerUuid")) {
            this.dataTracker.set(OWNER_UUID, Optional.of(nbt.getUuid("OwnerUuid")));
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!(source.getAttacker() instanceof PlayerEntity attacker)) return false;
        Optional<UUID> ownerUuid = getOwnerUuid();
        if (ownerUuid.isEmpty() || !ownerUuid.get().equals(attacker.getUuid())) return false;

        if (!this.getWorld().isClient) {
            PLAYER_TO_WATCHER.remove(ownerUuid.get());
            PENDING_RESPAWNS.add(new PendingRespawn(
                    (ServerWorld) this.getWorld(), this.getX(), this.getZ(), ownerUuid.get(), 1200
            ));
        }
        this.discard();
        return true;
    }

    @Override
    public int getXpToDrop() {
        return 0;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PENDING_RESPAWNS.removeIf(r -> {
                r.ticks--;
                if (r.ticks > 0) return false;
                ServerWorld verdant = server.getWorld(ModDimensions.VERDANT_MEMORY_KEY);
                if (verdant == null) return true;
                ServerPlayerEntity owner = server.getPlayerManager().getPlayer(r.ownerUuid);
                if (owner == null || !owner.getWorld().getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) {
                    return true;
                }
                spawnWatcherNear(verdant, owner, r.ownerUuid);
                return true;
            });

            if (server.getTicks() % 100 != 0) return;

            ServerWorld verdant = server.getWorld(ModDimensions.VERDANT_MEMORY_KEY);
            if (verdant == null) return;

            Set<UUID> playersInDim = new HashSet<>();
            for (ServerPlayerEntity player : verdant.getPlayers()) {
                playersInDim.add(player.getUuid());
            }

            PLAYER_TO_WATCHER.entrySet().removeIf(entry -> {
                if (playersInDim.contains(entry.getKey())) return false;
                Entity w = verdant.getEntity(entry.getValue());
                if (w != null) w.discard();
                return true;
            });

            for (ServerPlayerEntity player : verdant.getPlayers()) {
                UUID playerUuid = player.getUuid();
                boolean pendingRespawn = PENDING_RESPAWNS.stream().anyMatch(r -> r.ownerUuid.equals(playerUuid));
                if (pendingRespawn) continue;
                UUID watcherUuid = PLAYER_TO_WATCHER.get(playerUuid);
                if (watcherUuid != null && verdant.getEntity(watcherUuid) != null) continue;
                spawnWatcherNear(verdant, player, playerUuid);
            }
        });
    }

    private static void spawnWatcherNear(ServerWorld world, ServerPlayerEntity player, UUID ownerUuid) {
        double angle = world.getRandom().nextDouble() * Math.PI * 2;
        double dist = 24 + world.getRandom().nextDouble() * 4;
        double spawnX = player.getX() + Math.cos(angle) * dist;
        double spawnZ = player.getZ() + Math.sin(angle) * dist;
        double spawnY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) spawnX, (int) spawnZ);
        WatcherEntity watcher = ModEntityTypes.WATCHER_TYPE.create(world);
        if (watcher == null) return;
        watcher.setOwnerUuid(ownerUuid);
        watcher.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0, 0);
        world.spawnEntity(watcher);
        PLAYER_TO_WATCHER.put(ownerUuid, watcher.getUuid());
    }

    static class PendingRespawn {
        final ServerWorld world;
        final double x, z;
        final UUID ownerUuid;
        int ticks;

        PendingRespawn(ServerWorld world, double x, double z, UUID ownerUuid, int ticks) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.ownerUuid = ownerUuid;
            this.ticks = ticks;
        }
    }

    static class WatcherOrbitGoal extends Goal {
        private static final double IDEAL_DIST = 25.0;
        private static final double PANIC_DIST = 5.0;
        private static final double FLEE_DIST = 20.0;
        private static final double APPROACH_DIST = 30.0;
        private static final double TP_RADIUS = 32.0;

        private final WatcherEntity watcher;
        private PlayerEntity owner;
        private int cooldown = 0;

        WatcherOrbitGoal(WatcherEntity watcher) {
            this.watcher = watcher;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            owner = watcher.getOwnerEntity();
            return owner != null;
        }

        @Override
        public boolean shouldContinue() {
            owner = watcher.getOwnerEntity();
            return owner != null && owner.isAlive();
        }

        @Override
        public void stop() {
            watcher.getNavigation().stop();
            owner = null;
        }

        @Override
        public void tick() {
            if (owner == null) return;
            double dist = watcher.distanceTo(owner);

            if (dist < PANIC_DIST) {
                double angle = watcher.getRandom().nextDouble() * Math.PI * 2;
                double tpX = owner.getX() + Math.cos(angle) * TP_RADIUS;
                double tpZ = owner.getZ() + Math.sin(angle) * TP_RADIUS;
                double tpY = watcher.getWorld().getTopY(
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) tpX, (int) tpZ
                );
                watcher.refreshPositionAndAngles(tpX, tpY, tpZ, watcher.getYaw(), watcher.getPitch());
                watcher.getNavigation().stop();
                cooldown = 0;
                return;
            }

            if (--cooldown > 0) return;
            cooldown = 10;

            Vec3d dirFromOwner = watcher.getPos().subtract(owner.getPos());
            double horizontal = Math.sqrt(dirFromOwner.x * dirFromOwner.x + dirFromOwner.z * dirFromOwner.z);
            Vec3d flatDir = horizontal > 0.001
                    ? new Vec3d(dirFromOwner.x / horizontal, 0, dirFromOwner.z / horizontal)
                    : new Vec3d(1, 0, 0);

            if (dist < FLEE_DIST) {
                double speed = dist < 12 ? 1.4 : 1.1;
                double targetX = owner.getX() + flatDir.x * (IDEAL_DIST + 4);
                double targetZ = owner.getZ() + flatDir.z * (IDEAL_DIST + 4);
                double targetY = watcher.getWorld().getTopY(
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) targetX, (int) targetZ
                );
                watcher.getNavigation().startMovingTo(targetX, targetY, targetZ, speed);
            } else if (dist > APPROACH_DIST) {
                double targetX = owner.getX() + flatDir.x * IDEAL_DIST;
                double targetZ = owner.getZ() + flatDir.z * IDEAL_DIST;
                double targetY = watcher.getWorld().getTopY(
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) targetX, (int) targetZ
                );
                if (dist > 48) {
                    watcher.refreshPositionAndAngles(targetX, targetY, targetZ, watcher.getYaw(), watcher.getPitch());
                    watcher.getNavigation().stop();
                    cooldown = 0;
                } else {
                    watcher.getNavigation().startMovingTo(targetX, targetY, targetZ, 0.7);
                }
            } else {
                watcher.getNavigation().stop();
            }
        }
    }
}