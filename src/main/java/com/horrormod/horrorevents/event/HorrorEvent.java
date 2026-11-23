package com.horrormod.horrorevents.event;

import net.minecraft.server.level.ServerPlayer;

/** Базовый класс события: id, категория, минимальный день, вес, длительность, кулдаун. */
public abstract class HorrorEvent {
    private final String id;
    private final EventCategory category;
    private final int minDay;
    private final int weight;
    private final int durationTicks;
    private final int cooldownTicks;
    private final boolean destructive;

    protected HorrorEvent(String id, EventCategory category, int minDay, int weight,
                          int durationSeconds, int cooldownSeconds, boolean destructive) {
        this.id = id;
        this.category = category;
        this.minDay = minDay;
        this.weight = weight;
        this.durationTicks = durationSeconds * 20;
        this.cooldownTicks = cooldownSeconds * 20;
        this.destructive = destructive;
    }

    public String id() { return id; }
    public EventCategory category() { return category; }
    public int minDay() { return minDay; }
    public int weight() { return weight; }
    public int durationTicks() { return durationTicks; }
    public int cooldownTicks() { return cooldownTicks; }
    public boolean destructive() { return destructive; }

    /** false — событие не выбирается случайно (запускается особым триггером). */
    public boolean randomTrigger() { return true; }

    public boolean canStart(ServerPlayer player) { return true; }

    public abstract void onStart(ServerPlayer player, ActiveEvent active);

    public void onTick(ServerPlayer player, ActiveEvent active) {}

    /** player может быть null, если игрок вышел. */
    public void onEnd(ServerPlayer player, ActiveEvent active) {}
}
