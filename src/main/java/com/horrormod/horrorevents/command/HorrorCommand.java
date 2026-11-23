package com.horrormod.horrorevents.command;

import com.horrormod.horrorevents.config.HorrorConfig;
import com.horrormod.horrorevents.data.AnxietyManager;
import com.horrormod.horrorevents.data.HorrorWorldData;
import com.horrormod.horrorevents.event.EventManager;
import com.horrormod.horrorevents.event.EventRegistry;
import com.horrormod.horrorevents.event.HorrorEvent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /horror list | trigger <id> | stop | anxiety get/set | reset  (OP 2). */
public final class HorrorCommand {
    private static final SuggestionProvider<CommandSourceStack> IDS =
            (ctx, b) -> SharedSuggestionProvider.suggest(EventRegistry.ids(), b);

    private HorrorCommand() {}

    public static void onRegister(RegisterCommandsEvent e) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("horror")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("list").executes(HorrorCommand::list))
                .then(Commands.literal("trigger").then(Commands.argument("id", StringArgumentType.word())
                        .suggests(IDS).executes(HorrorCommand::trigger)))
                .then(Commands.literal("stop").executes(HorrorCommand::stop))
                .then(Commands.literal("anxiety")
                        .then(Commands.literal("get").executes(HorrorCommand::anxietyGet))
                        .then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer(0, 100000))
                                .executes(HorrorCommand::anxietySet))))
                .then(Commands.literal("reset").executes(HorrorCommand::reset));
        e.getDispatcher().register(root);
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder();
        for (HorrorEvent ev : EventRegistry.all()) {
            sb.append("\n[").append(ev.category().name().toLowerCase()).append("] ")
                    .append(ev.id()).append(" (день ").append(ev.minDay()).append("+)");
        }
        c.getSource().sendSuccess(() -> Component.literal("События:" + sb), false);
        return EventRegistry.all().size();
    }

    private static int trigger(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String id = StringArgumentType.getString(c, "id");
        HorrorEvent ev = EventRegistry.get(id);
        if (ev == null) {
            c.getSource().sendFailure(Component.translatable("horror_events.command.no_event", id));
            return 0;
        }
        ServerPlayer p = c.getSource().getPlayerOrException();
        EventManager.forceStart(c.getSource().getServer(), p, ev);
        c.getSource().sendSuccess(() -> Component.translatable("horror_events.command.started", id), true);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> c) {
        EventManager.stop(c.getSource().getServer());
        c.getSource().sendSuccess(() -> Component.translatable("horror_events.command.stopped"), true);
        return 1;
    }

    private static int anxietyGet(CommandContext<CommandSourceStack> c) {
        MinecraftServer s = c.getSource().getServer();
        HorrorWorldData d = HorrorWorldData.get(s);
        c.getSource().sendSuccess(() -> Component.translatable("horror_events.command.anxiety",
                d.anxiety(), HorrorConfig.MAX_ANXIETY.get(), AnxietyManager.currentDay(s)), false);
        return d.anxiety();
    }

    private static int anxietySet(CommandContext<CommandSourceStack> c) {
        MinecraftServer s = c.getSource().getServer();
        HorrorWorldData d = HorrorWorldData.get(s);
        d.setAnxiety(Math.min(HorrorConfig.MAX_ANXIETY.get(), IntegerArgumentType.getInteger(c, "value")));
        return anxietyGet(c);
    }

    private static int reset(CommandContext<CommandSourceStack> c) {
        MinecraftServer s = c.getSource().getServer();
        EventManager.stop(s);
        HorrorWorldData.get(s).reset();
        AnxietyManager.updateDay(s);
        c.getSource().sendSuccess(() -> Component.translatable("horror_events.command.reset"), true);
        return 1;
    }
}
