package com.horrormod.horrorevents.data;

import com.horrormod.horrorevents.config.HorrorConfig;
import net.minecraft.server.MinecraftServer;

/** Расчёт игрового дня, тревоги и вероятности события. */
public final class AnxietyManager {
    private AnxietyManager() {}

    public static long currentDay(MinecraftServer server) {
        return server.overworld().getGameTime() / 24000L + 1L;
    }

    /** 0: дни 1-3, 1: 4-7, 2: 8-12, 3: 13-20, 4: 21+ */
    public static int stage(long day) {
        if (day <= 3) return 0;
        if (day <= 7) return 1;
        if (day <= 12) return 2;
        if (day <= 20) return 3;
        return 4;
    }

    public static void updateDay(MinecraftServer server) {
        HorrorWorldData d = HorrorWorldData.get(server);
        long day = currentDay(server);
        int max = HorrorConfig.MAX_ANXIETY.get();
        if (d.lastDay() <= 0) {
            d.setAnxiety((int) Math.min(max, (day - 1) * HorrorConfig.ANXIETY_PER_DAY.get()));
            d.setLastDay(day);
        } else if (day > d.lastDay()) {
            long gain = (day - d.lastDay()) * HorrorConfig.ANXIETY_PER_DAY.get();
            d.setAnxiety((int) Math.min(max, d.anxiety() + gain));
            d.setLastDay(day);
        }
    }

    public static void add(HorrorWorldData d, int amount) {
        d.setAnxiety(Math.min(HorrorConfig.MAX_ANXIETY.get(), d.anxiety() + amount));
    }

    /** Шанс события в процентах. */
    public static double chance(HorrorWorldData d, long day) {
        double base = HorrorConfig.BASE_CHANCE.get();
        double c = base + HorrorConfig.CHANCE_GROWTH_PER_DAY.get() * (day - 1);
        c *= 1.0 + 0.5 * d.anxiety() / (double) HorrorConfig.MAX_ANXIETY.get();
        return Math.min(c, base * HorrorConfig.MAX_CHANCE_MULTIPLIER.get());
    }
}
