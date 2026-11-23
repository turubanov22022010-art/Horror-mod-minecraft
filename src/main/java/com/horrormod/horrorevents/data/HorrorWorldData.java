package com.horrormod.horrorevents.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/** Сохраняемое состояние мода: тревога, последний день, кулдауны событий. */
public class HorrorWorldData extends SavedData {
    public static final String NAME = "horror_events";

    private int anxiety;
    private long lastDay;
    private int eventsTriggered;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public static SavedData.Factory<HorrorWorldData> factory() {
        return new SavedData.Factory<>(HorrorWorldData::new, HorrorWorldData::load, null);
    }

    public static HorrorWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), NAME);
    }

    public static HorrorWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        HorrorWorldData d = new HorrorWorldData();
        d.anxiety = tag.getInt("anxiety");
        d.lastDay = tag.getLong("lastDay");
        d.eventsTriggered = tag.getInt("eventsTriggered");
        CompoundTag cd = tag.getCompound("cooldowns");
        for (String k : cd.getAllKeys()) d.cooldowns.put(k, cd.getLong(k));
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("anxiety", anxiety);
        tag.putLong("lastDay", lastDay);
        tag.putInt("eventsTriggered", eventsTriggered);
        CompoundTag cd = new CompoundTag();
        cooldowns.forEach(cd::putLong);
        tag.put("cooldowns", cd);
        return tag;
    }

    public int anxiety() { return anxiety; }
    public void setAnxiety(int v) { anxiety = Math.max(0, v); setDirty(); }
    public long lastDay() { return lastDay; }
    public void setLastDay(long d) { lastDay = d; setDirty(); }
    public int eventsTriggered() { return eventsTriggered; }
    public void countEvent() { eventsTriggered++; setDirty(); }

    public boolean isReady(String id, long gameTime) {
        return cooldowns.getOrDefault(id, 0L) <= gameTime;
    }

    public void setCooldown(String id, long readyAt) {
        cooldowns.put(id, readyAt);
        setDirty();
    }

    public void reset() {
        anxiety = 0;
        lastDay = 0;
        eventsTriggered = 0;
        cooldowns.clear();
        setDirty();
    }
}
