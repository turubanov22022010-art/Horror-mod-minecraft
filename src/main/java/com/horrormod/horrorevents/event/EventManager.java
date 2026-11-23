package com.horrormod.horrorevents.event;

import com.horrormod.horrorevents.HorrorEvents;
import com.horrormod.horrorevents.config.EventConfig;
import com.horrormod.horrorevents.config.HorrorConfig;
import com.horrormod.horrorevents.data.AnxietyManager;
import com.horrormod.horrorevents.data.HorrorWorldData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

/** Запуск, тик и завершение событий. Одновременно работает только одно событие. */
public final class EventManager {
    private static ActiveEvent active;
    private static int checkTimer;
    private static int dayTimer;

    private EventManager() {}

    public static ActiveEvent active() { return active; }

    public static void onServerTick(ServerTickEvent.Post e) {
        MinecraftServer server = e.getServer();
        if (!HorrorConfig.GLOBAL_ENABLED.get()) {
            stop(server);
            return;
        }
        if (++dayTimer >= 100) {
            dayTimer = 0;
            AnxietyManager.updateDay(server);
        }
        if (active != null) {
            tickActive(server);
            return;
        }
        if (++checkTimer >= HorrorConfig.CHECK_INTERVAL_SECONDS.get() * 20) {
            checkTimer = 0;
            attempt(server);
        }
    }

    public static void onServerStopping(ServerStoppingEvent e) {
        stop(e.getServer());
        checkTimer = 0;
    }

    public static void onWakeUp(PlayerWakeUpEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || active != null) return;
        if (!HorrorConfig.GLOBAL_ENABLED.get()) return;
        MinecraftServer server = p.getServer();
        if (server == null) return;
        HorrorEvent ev = EventRegistry.get("post_sleep_stalker");
        if (ev == null || !EventConfig.isEnabled(ev.id())) return;
        long day = AnxietyManager.currentDay(server);
        HorrorWorldData d = HorrorWorldData.get(server);
        if (day < ev.minDay() || !d.isReady(ev.id(), server.overworld().getGameTime())) return;
        if (p.getRandom().nextDouble() < 0.35 && eligible(p)) start(server, p, ev);
    }

    private static boolean eligible(ServerPlayer p) {
        return p.isAlive() && !p.isSpectator() && p.level().dimension() == Level.OVERWORLD;
    }

    private static void attempt(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        ServerPlayer p = players.get(server.overworld().random.nextInt(players.size()));
        if (!eligible(p)) return;
        HorrorWorldData d = HorrorWorldData.get(server);
        long day = AnxietyManager.currentDay(server);
        if (p.getRandom().nextDouble() * 100.0 >= AnxietyManager.chance(d, day)) return;

        long now = server.overworld().getGameTime();
        List<HorrorEvent> candidates = new ArrayList<>();
        int total = 0;
        for (HorrorEvent ev : EventRegistry.all()) {
            if (!ev.randomTrigger() || !EventConfig.isEnabled(ev.id())) continue;
            if (ev.destructive() && !HorrorConfig.DESTRUCTIVE_ENABLED.get()) continue;
            if (day < ev.minDay() || !d.isReady(ev.id(), now) || !ev.canStart(p)) continue;
            candidates.add(ev);
            total += ev.weight();
        }
        if (candidates.isEmpty() || total <= 0) return;
        int roll = p.getRandom().nextInt(total);
        for (HorrorEvent ev : candidates) {
            roll -= ev.weight();
            if (roll < 0) {
                start(server, p, ev);
                return;
            }
        }
    }

    /** Принудительный запуск (команда). Текущее событие останавливается. */
    public static void forceStart(MinecraftServer server, ServerPlayer p, HorrorEvent ev) {
        stop(server);
        start(server, p, ev);
    }

    private static void start(MinecraftServer server, ServerPlayer p, HorrorEvent ev) {
        ActiveEvent a = new ActiveEvent(ev, p.getUUID(), p.serverLevel());
        active = a;
        HorrorWorldData d = HorrorWorldData.get(server);
        d.setCooldown(ev.id(), server.overworld().getGameTime() + ev.cooldownTicks());
        d.countEvent();
        AnxietyManager.add(d, 10);
        try {
            ev.onStart(p, a);
        } catch (Exception ex) {
            HorrorEvents.LOGGER.error("Ошибка запуска события {}", ev.id(), ex);
            a.finish();
        }
    }

    private static void tickActive(MinecraftServer server) {
        ActiveEvent a = active;
        ServerPlayer p = server.getPlayerList().getPlayer(a.playerId);
        if (p == null || !eligible(p) || a.isFinished() || a.age >= a.event.durationTicks()) {
            stop(server);
            return;
        }
        try {
            a.event.onTick(p, a);
        } catch (Exception ex) {
            HorrorEvents.LOGGER.error("Ошибка в событии {}", a.event.id(), ex);
            a.finish();
        }
        a.age++;
    }

    public static boolean stop(MinecraftServer server) {
        if (active == null) return false;
        ActiveEvent a = active;
        active = null;
        ServerPlayer p = server.getPlayerList().getPlayer(a.playerId);
        try {
            a.event.onEnd(p, a);
        } catch (Exception ex) {
            HorrorEvents.LOGGER.error("Ошибка завершения события {}", a.event.id(), ex);
        }
        a.discardEntities();
        return true;
    }
}
