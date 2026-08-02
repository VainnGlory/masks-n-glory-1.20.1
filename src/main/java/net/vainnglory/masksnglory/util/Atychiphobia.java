package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.vainnglory.masksnglory.effect.ModEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Atychiphobia {

    public static final int DURATION_TICKS = 1200;
    public static final int STAGE_TICKS = 100;
    public static final int QTE_WINDOW_TICKS = 10;
    public static final int MAX_STAGE = 6;

    private static final Map<UUID, State> STATES = new HashMap<>();

    private static class State {
        int stage = 1;
        int ticksToRise = STAGE_TICKS;
        boolean windowOpen = false;
        boolean consumed = false;
        int lastMiningFatigue = -1;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    public static void apply(LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        player.addStatusEffect(new StatusEffectInstance(ModEffects.ATYCHIPHOBIA, DURATION_TICKS, 0, false, false, true));

        State state = STATES.computeIfAbsent(player.getUuid(), uuid -> new State());
        applyMiningFatigue(player, state, state.stage);
        AtychiphobiaPacket.sendState(player, state.stage);
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        if (!player.hasStatusEffect(ModEffects.ATYCHIPHOBIA)) {
            State existing = STATES.remove(player.getUuid());
            if (existing != null) {
                clearMiningFatigue(player, existing);
                AtychiphobiaPacket.sendState(player, 0);
            }
            return;
        }

        State state = STATES.computeIfAbsent(player.getUuid(), uuid -> new State());
        state.ticksToRise--;

        if (!state.windowOpen && state.ticksToRise <= QTE_WINDOW_TICKS) {
            state.windowOpen = true;
            state.consumed = false;
            AtychiphobiaPacket.sendQte(player);
        }

        if (state.ticksToRise <= 0) {
            state.stage = Math.min(state.stage + 1, MAX_STAGE);
            state.ticksToRise = STAGE_TICKS;
            state.windowOpen = false;
            applyMiningFatigue(player, state, state.stage);
            AtychiphobiaPacket.sendState(player, state.stage);
        }
    }

    public static boolean isWindowOpen(ServerPlayerEntity player) {
        State state = STATES.get(player.getUuid());
        return state != null && state.windowOpen && !state.consumed;
    }

    public static boolean tryConsumeQte(ServerPlayerEntity player) {
        State state = STATES.get(player.getUuid());
        if (state == null || !state.windowOpen || state.consumed) return false;

        state.consumed = true;
        state.windowOpen = false;
        state.stage = Math.max(1, state.stage - 1);
        state.ticksToRise = STAGE_TICKS;

        clearMiningFatigue(player, state);
        applyMiningFatigue(player, state, state.stage);
        AtychiphobiaPacket.sendState(player, state.stage);
        AtychiphobiaPacket.sendSuccess(player);
        return true;
    }

    private static void applyMiningFatigue(ServerPlayerEntity player, State state, int stage) {
        int amplifier = Math.min(2, stage / 2 - 1);
        if (amplifier < 0) {
            clearMiningFatigue(player, state);
            return;
        }

        state.lastMiningFatigue = amplifier;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, STAGE_TICKS + 40, amplifier, false, false, true));
    }

    private static void clearMiningFatigue(ServerPlayerEntity player, State state) {
        if (state.lastMiningFatigue < 0) return;

        StatusEffectInstance active = player.getStatusEffect(StatusEffects.MINING_FATIGUE);
        if (active != null && active.getAmplifier() == state.lastMiningFatigue) {
            player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
        state.lastMiningFatigue = -1;
    }
}