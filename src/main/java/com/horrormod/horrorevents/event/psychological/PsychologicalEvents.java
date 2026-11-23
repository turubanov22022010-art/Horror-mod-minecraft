package com.horrormod.horrorevents.event.psychological;

import com.horrormod.horrorevents.event.ActiveEvent;
import com.horrormod.horrorevents.event.EventCategory;
import com.horrormod.horrorevents.event.EventUtil;
import com.horrormod.horrorevents.event.HorrorEvent;
import com.horrormod.horrorevents.network.ClientEffectPayload;
import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/** 11 психологических событий. Каждое — отдельный класс. */
public final class PsychologicalEvents {
    private PsychologicalEvents() {}

    public static void registerAll(Consumer<HorrorEvent> r) {
        r.accept(new FakeSteps());
        r.accept(new FakeBreathing());
        r.accept(new FakeJoinMessage());
        r.accept(new ExtinguishedTorches());
        r.accept(new TimeDistortion());
        r.accept(new DoorCreak());
        r.accept(new LightFlicker());
        r.accept(new UndergroundDigging());
        r.accept(new CameraShake());
        r.accept(new FakeCreeper());
        r.accept(new FakeLeaveMessage());
    }

    /** Шаги за спиной: приближаются и затихают. */
    public static final class FakeSteps extends HorrorEvent {
        public FakeSteps() { super("fake_steps", EventCategory.PSYCHOLOGICAL, 2, 10, 12, 300, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {}
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age % 14 == 0 && a.age < 200) {
                Vec3 pos = EventUtil.behind(p, Math.max(2.5, 7.0 - a.age / 35.0));
                EventUtil.sound(p, ModSounds.FAKE_STEPS, pos, 0.9F, 0.85F + p.getRandom().nextFloat() * 0.2F);
            }
        }
    }

    /** Чужое дыхание рядом. */
    public static final class FakeBreathing extends HorrorEvent {
        public FakeBreathing() { super("fake_breathing", EventCategory.PSYCHOLOGICAL, 3, 8, 15, 400, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {}
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age % 50 == 10) EventUtil.sound(p, ModSounds.FAKE_BREATHING, EventUtil.behind(p, 1.3), 0.7F, 0.8F);
        }
    }

    /** Сообщение о подключении несуществующего игрока. */
    public static final class FakeJoinMessage extends HorrorEvent {
        public FakeJoinMessage() { super("fake_join_message", EventCategory.PSYCHOLOGICAL, 2, 8, 3, 600, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            String name = EventUtil.randomName(p.getRandom());
            p.sendSystemMessage(Component.translatable("multiplayer.player.joined", Component.literal(name))
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    /** Факелы рядом с игроком гаснут. */
    public static final class ExtinguishedTorches extends HorrorEvent {
        public ExtinguishedTorches() { super("extinguished_torches", EventCategory.PSYCHOLOGICAL, 5, 6, 3, 600, true); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            BlockPos c = p.blockPosition();
            int count = 0;
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-14, -6, -14), c.offset(14, 6, 14))) {
                BlockState s = l.getBlockState(pos);
                if (s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH) || s.is(Blocks.SOUL_TORCH) || s.is(Blocks.SOUL_WALL_TORCH)) {
                    l.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    l.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
                    count++;
                }
            }
            if (count == 0) a.finish();
        }
    }

    /** Сбой времени: время суток прыгает и возвращается. */
    public static final class TimeDistortion extends HorrorEvent {
        public TimeDistortion() { super("time_distortion", EventCategory.PSYCHOLOGICAL, 6, 5, 8, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            long t = a.level.getDayTime();
            a.vars.put("t", t);
            a.level.setDayTime(t + 5000L + p.getRandom().nextInt(4000));
            EventUtil.effect(p, ClientEffectPayload.FLICKER, 40);
            EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.6F, 1.0F);
        }
        @Override public void onEnd(ServerPlayer p, ActiveEvent a) {
            Object t = a.vars.get("t");
            if (t instanceof Long base) a.level.setDayTime(base + a.age);
        }
    }

    /** Скрип двери: ближайшая дверь открывается или закрывается сама. */
    public static final class DoorCreak extends HorrorEvent {
        public DoorCreak() { super("door_creak", EventCategory.PSYCHOLOGICAL, 3, 9, 3, 300, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            BlockPos c = p.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-12, -3, -12), c.offset(12, 3, 12))) {
                BlockState s = l.getBlockState(pos);
                if (s.getBlock() instanceof DoorBlock door && s.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                    door.setOpen(p, l, s, pos.immutable(), !s.getValue(DoorBlock.OPEN));
                    EventUtil.sound(p, ModSounds.DOOR_CREAK, Vec3.atCenterOf(pos), 1.0F, 0.9F);
                    return;
                }
            }
            EventUtil.sound(p, ModSounds.DOOR_CREAK, EventUtil.behind(p, 6), 0.8F, 0.8F);
        }
    }

    /** Мерцание света (затемнение экрана). */
    public static final class LightFlicker extends HorrorEvent {
        public LightFlicker() { super("light_flicker", EventCategory.PSYCHOLOGICAL, 4, 8, 6, 400, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.effect(p, ClientEffectPayload.FLICKER, 100);
            EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.3F, 1.2F);
        }
    }

    /** Копание под землёй. */
    public static final class UndergroundDigging extends HorrorEvent {
        public UndergroundDigging() { super("underground_digging", EventCategory.PSYCHOLOGICAL, 4, 7, 14, 500, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {}
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age % 25 == 0) {
                Vec3 pos = p.position().add((p.getRandom().nextDouble() - 0.5) * 12, -4 - p.getRandom().nextInt(3),
                        (p.getRandom().nextDouble() - 0.5) * 12);
                EventUtil.sound(p, ModSounds.UNDERGROUND_DIGGING, pos, 0.9F, 0.7F + p.getRandom().nextFloat() * 0.3F);
            }
        }
    }

    /** Что-то рядом: короткая тряска камеры и глухой звук. */
    public static final class CameraShake extends HorrorEvent {
        public CameraShake() { super("camera_shake", EventCategory.PSYCHOLOGICAL, 5, 6, 4, 600, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.effect(p, ClientEffectPayload.SHAKE, 60);
            EventUtil.sound(p, ModSounds.STALKER_AMBIENT, EventUtil.behind(p, 3), 1.0F, 0.7F);
        }
    }

    /** Крипер за спиной: звук без крипера. */
    public static final class FakeCreeper extends HorrorEvent {
        public FakeCreeper() { super("fake_creeper", EventCategory.PSYCHOLOGICAL, 3, 9, 3, 450, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.sound(p, ModSounds.FAKE_CREEPER, EventUtil.behind(p, 2.5), 1.0F, 1.0F);
        }
    }

    /** Сообщение «игрок покинул игру». */
    public static final class FakeLeaveMessage extends HorrorEvent {
        public FakeLeaveMessage() { super("fake_leave_message", EventCategory.PSYCHOLOGICAL, 2, 8, 3, 600, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            String name = EventUtil.randomName(p.getRandom());
            p.sendSystemMessage(Component.translatable("multiplayer.player.left", Component.literal(name))
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
