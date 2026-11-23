package com.horrormod.horrorevents.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Общая конфигурация мода (ModConfigSpec). */
public final class HorrorConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue GLOBAL_ENABLED;
    public static final ModConfigSpec.BooleanValue DESTRUCTIVE_ENABLED;
    public static final ModConfigSpec.DoubleValue BASE_CHANCE;
    public static final ModConfigSpec.DoubleValue CHANCE_GROWTH_PER_DAY;
    public static final ModConfigSpec.DoubleValue MAX_CHANCE_MULTIPLIER;
    public static final ModConfigSpec.IntValue CHECK_INTERVAL_SECONDS;
    public static final ModConfigSpec.IntValue ANXIETY_PER_DAY;
    public static final ModConfigSpec.IntValue MAX_ANXIETY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        GLOBAL_ENABLED = b.comment("Общий выключатель мода").define("globalEnabled", true);
        DESTRUCTIVE_ENABLED = b.comment("Разрешить события, которые удаляют предметы и меняют блоки")
                .define("destructiveEventsEnabled", true);
        BASE_CHANCE = b.comment("Начальный шанс события при проверке, %")
                .defineInRange("baseChance", 5.0, 0.0, 100.0);
        CHANCE_GROWTH_PER_DAY = b.comment("Прирост шанса за игровой день, п.п.")
                .defineInRange("chanceGrowthPerDay", 1.5, 0.0, 100.0);
        MAX_CHANCE_MULTIPLIER = b.comment("Максимальный шанс = baseChance * это значение")
                .defineInRange("maxChanceMultiplier", 5.0, 1.0, 100.0);
        CHECK_INTERVAL_SECONDS = b.comment("Интервал проверки шанса, секунд")
                .defineInRange("checkIntervalSeconds", 120, 5, 36000);
        ANXIETY_PER_DAY = b.comment("Прирост тревоги за игровой день")
                .defineInRange("anxietyPerDay", 5, 0, 1000);
        MAX_ANXIETY = b.comment("Максимальная тревога")
                .defineInRange("maxAnxiety", 1000, 1, 100000);
        b.push("events");
        EventConfig.build(b);
        b.pop();
        SPEC = b.build();
    }

    private HorrorConfig() {}
}
