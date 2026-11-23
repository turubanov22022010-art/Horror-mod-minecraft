package com.horrormod.horrorevents.event;

import com.horrormod.horrorevents.event.glitch.GlitchEvents;
import com.horrormod.horrorevents.event.psychological.PsychologicalEvents;
import com.horrormod.horrorevents.event.stalker.StalkerEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Единый реестр всех 33 событий. */
public final class EventRegistry {
    private static final Map<String, HorrorEvent> EVENTS = new LinkedHashMap<>();

    static {
        PsychologicalEvents.registerAll(EventRegistry::register);
        StalkerEvents.registerAll(EventRegistry::register);
        GlitchEvents.registerAll(EventRegistry::register);
    }

    private EventRegistry() {}

    private static void register(HorrorEvent e) {
        EVENTS.put(e.id(), e);
    }

    public static Collection<HorrorEvent> all() { return EVENTS.values(); }
    public static HorrorEvent get(String id) { return EVENTS.get(id); }
    public static List<String> ids() { return new ArrayList<>(EVENTS.keySet()); }
}
