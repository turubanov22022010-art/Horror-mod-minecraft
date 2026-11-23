package com.horrormod.horrorevents.config;

import com.horrormod.horrorevents.event.EventRegistry;
import com.horrormod.horrorevents.event.HorrorEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/** Отдельный переключатель для каждого из 33 событий. */
public final class EventConfig {
    private static final Map<String, ModConfigSpec.BooleanValue> ENABLED = new LinkedHashMap<>();

    private EventConfig() {}

    static void build(ModConfigSpec.Builder b) {
        for (HorrorEvent e : EventRegistry.all()) {
            ENABLED.put(e.id(), b.comment("Включить событие " + e.id()).define(e.id(), true));
        }
    }

    public static boolean isEnabled(String id) {
        ModConfigSpec.BooleanValue v = ENABLED.get(id);
        return v == null || v.get();
    }
}
