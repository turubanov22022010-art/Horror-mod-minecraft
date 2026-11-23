package com.horrormod.horrorevents.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Состояние одного запущенного события. */
public final class ActiveEvent {
    public final HorrorEvent event;
    public final UUID playerId;
    public final ServerLevel level;
    public int age;
    private boolean finished;
    public final List<Entity> entities = new ArrayList<>();
    public final Map<String, Object> vars = new HashMap<>();

    public ActiveEvent(HorrorEvent event, UUID playerId, ServerLevel level) {
        this.event = event;
        this.playerId = playerId;
        this.level = level;
    }

    public void track(Entity e) { entities.add(e); }
    public void finish() { finished = true; }
    public boolean isFinished() { return finished; }

    public boolean hasLiveEntities() {
        entities.removeIf(Entity::isRemoved);
        return !entities.isEmpty();
    }

    public void discardEntities() {
        for (Entity e : entities) if (!e.isRemoved()) e.discard();
        entities.clear();
    }
}
