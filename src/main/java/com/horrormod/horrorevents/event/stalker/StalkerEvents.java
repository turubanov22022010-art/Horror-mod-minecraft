package com.horrormod.horrorevents.event.stalker;

import com.horrormod.horrorevents.entity.FollowerEntity;
import com.horrormod.horrorevents.entity.MimicEntity;
import com.horrormod.horrorevents.entity.StalkerEntity;
import com.horrormod.horrorevents.event.ActiveEvent;
import com.horrormod.horrorevents.event.EventCategory;
import com.horrormod.horrorevents.event.EventUtil;
import com.horrormod.horrorevents.event.HorrorEvent;
import com.horrormod.horrorevents.network.ClientEffectPayload;
import com.horrormod.horrorevents.registry.ModEntities;
import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 11 событий с существами-сталкерами. */
public final class StalkerEvents {
    private StalkerEvents() {}

    public static void registerAll(Consumer<HorrorEvent> r) {
        r.accept(new DistantWatcher());
        r.accept(new FollowingStalker());
        r.accept(new WindowSilhouette());
        r.accept(new BedsideVisitor());
        r.accept(new AcceleratingHunter());
        r.accept(new PlayerMimic());
        r.accept(new FogCreature());
        r.accept(new PostSleepStalker());
        r.accept(new CornerWatcher());
        r.accept(new SilentShadow());
        r.accept(new FalseFriend());
    }

    private static StalkerEntity spawnWatcher(ServerPlayer p, ActiveEvent a, BlockPos pos, int maxLife) {
        StalkerEntity e = EventUtil.spawn(p, a, ModEntities.STALKER.get(), pos);
        if (e == null) {
            a.finish();
            return null;
        }
        e.setMaxLife(maxLife);
        EventUtil.sound(p, ModSounds.STALKER_APPEAR, Vec3.atCenterOf(pos), 0.5F, 1.0F);
        return e;
    }

    /** Наблюдатель далеко впереди; исчезает, если смотреть на него. */
    public static final class DistantWatcher extends HorrorEvent {
        public DistantWatcher() { super("distant_watcher", EventCategory.STALKER, 7, 10, 60, 600, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            spawnWatcher(p, a, EventUtil.findSpawn(p, 24, 36, 0, 50, false), 1200);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Преследователь: идёт к игроку и замирает, когда на него смотрят. */
    public static final class FollowingStalker extends HorrorEvent {
        public FollowingStalker() { super("following_stalker", EventCategory.STALKER, 9, 8, 90, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            FollowerEntity e = EventUtil.spawn(p, a, ModEntities.FOLLOWER.get(), EventUtil.findSpawn(p, 26, 34, 180, 80, false));
            if (e == null) { a.finish(); return; }
            e.setMode(FollowerEntity.Mode.FREEZE);
            e.setMaxLife(1800);
            EventUtil.sound(p, ModSounds.STALKER_APPEAR, e.position(), 0.5F, 0.8F);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Силуэт в окне: существо стоит возле стекла. */
    public static final class WindowSilhouette extends HorrorEvent {
        public WindowSilhouette() { super("window_silhouette", EventCategory.STALKER, 8, 7, 40, 700, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            BlockPos c = p.blockPosition();
            List<BlockPos> spots = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-22, -4, -22), c.offset(22, 4, 22))) {
                BlockState s = l.getBlockState(pos);
                if (!(s.is(Tags.Blocks.GLASS_BLOCKS) || s.is(Tags.Blocks.GLASS_PANES))) continue;
                if (pos.distSqr(c) < 36) continue;
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    BlockPos out = pos.relative(d);
                    if (EventUtil.isFree(l, out) && EventUtil.isFree(l, out.above()) && EventUtil.isSolid(l, out.below())) {
                        spots.add(out.immutable());
                        break;
                    }
                }
            }
            if (spots.isEmpty()) { a.finish(); return; }
            spawnWatcher(p, a, spots.get(p.getRandom().nextInt(spots.size())), 800);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Гость у кровати: существо рядом с точкой возрождения. */
    public static final class BedsideVisitor extends HorrorEvent {
        public BedsideVisitor() { super("bedside_visitor", EventCategory.STALKER, 10, 5, 40, 900, false); }
        @Override public boolean canStart(ServerPlayer p) {
            BlockPos bed = p.getRespawnPosition();
            return bed != null && p.getRespawnDimension() == Level.OVERWORLD && bed.distSqr(p.blockPosition()) < 60 * 60;
        }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            BlockPos bed = p.getRespawnPosition();
            if (bed == null) { a.finish(); return; }
            ServerLevel l = p.serverLevel();
            for (int i = 0; i < 20; i++) {
                int dx = p.getRandom().nextInt(7) - 3;
                int dz = p.getRandom().nextInt(7) - 3;
                if (Math.abs(dx) + Math.abs(dz) < 2) continue;
                BlockPos g = EventUtil.findGround(l, bed.getX() + dx, bed.getY(), bed.getZ() + dz);
                if (g != null) { spawnWatcher(p, a, g, 900); return; }
            }
            a.finish();
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Быстрый охотник: постоянно ускоряется. */
    public static final class AcceleratingHunter extends HorrorEvent {
        public AcceleratingHunter() { super("accelerating_hunter", EventCategory.STALKER, 14, 5, 90, 1200, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            FollowerEntity e = EventUtil.spawn(p, a, ModEntities.FOLLOWER.get(), EventUtil.findSpawn(p, 30, 40, 180, 90, false));
            if (e == null) { a.finish(); return; }
            e.setMode(FollowerEntity.Mode.ACCELERATE);
            e.setMaxLife(1800);
            EventUtil.sound(p, ModSounds.STALKER_APPEAR, e.position(), 0.7F, 0.7F);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Подражатель: повторяет движения игрока. */
    public static final class PlayerMimic extends HorrorEvent {
        public PlayerMimic() { super("player_mimic", EventCategory.STALKER, 11, 6, 60, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            MimicEntity e = EventUtil.spawn(p, a, ModEntities.MIMIC.get(), EventUtil.findSpawn(p, 12, 16, 0, 30, false));
            if (e == null) { a.finish(); return; }
            e.setMode(MimicEntity.Mode.COPY);
            e.setMaxLife(1200);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Существо в тумане: серая дымка и наблюдатель. */
    public static final class FogCreature extends HorrorEvent {
        public FogCreature() { super("fog_creature", EventCategory.STALKER, 12, 6, 50, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.effect(p, ClientEffectPayload.FOG, 1000);
            spawnWatcher(p, a, EventUtil.findSpawn(p, 18, 26, 0, 60, false), 1000);
        }
        @Override public void onEnd(ServerPlayer p, ActiveEvent a) {
            if (p != null) EventUtil.effect(p, ClientEffectPayload.FOG, 0);
        }
    }

    /** После пробуждения: существо стоит за спиной. Запускается при пробуждении. */
    public static final class PostSleepStalker extends HorrorEvent {
        public PostSleepStalker() { super("post_sleep_stalker", EventCategory.STALKER, 8, 8, 20, 600, false); }
        @Override public boolean randomTrigger() { return false; }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            Vec3 b = EventUtil.behind(p, 3.0);
            BlockPos g = EventUtil.findGround(p.serverLevel(), (int) Math.floor(b.x), p.getBlockY(), (int) Math.floor(b.z));
            StalkerEntity e = spawnWatcher(p, a, g, 400);
            if (e != null) e.setVanishWhenSeen(true, 5);
        }
    }

    /** За углом: существо появляется там, где игрок его пока не видит. */
    public static final class CornerWatcher extends HorrorEvent {
        public CornerWatcher() { super("corner_watcher", EventCategory.STALKER, 9, 7, 40, 700, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            spawnWatcher(p, a, EventUtil.findSpawn(p, 7, 14, 0, 180, true), 800);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }

    /** Безмолвная тень: бесшумно появляется позади и исчезает, когда на неё смотрят. */
    public static final class SilentShadow extends HorrorEvent {
        public SilentShadow() { super("silent_shadow", EventCategory.STALKER, 13, 6, 80, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {}
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age % 60 != 0 || a.hasLiveEntities()) return;
            StalkerEntity e = EventUtil.spawn(p, a, ModEntities.STALKER.get(), EventUtil.findSpawn(p, 14, 20, 180, 70, false));
            if (e != null) {
                e.setSilent(true);
                e.setMaxLife(600);
                e.setVanishWhenSeen(true, 3);
            }
        }
    }

    /** Незнакомец: «игрок» с чужим ником подходит и бьёт. */
    public static final class FalseFriend extends HorrorEvent {
        public FalseFriend() { super("false_friend", EventCategory.STALKER, 15, 4, 60, 1200, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            MimicEntity e = EventUtil.spawn(p, a, ModEntities.MIMIC.get(), EventUtil.findSpawn(p, 20, 28, 0, 60, false));
            if (e == null) { a.finish(); return; }
            String name = EventUtil.randomName(p.getRandom());
            e.setMode(MimicEntity.Mode.APPROACH);
            e.setCustomName(Component.literal(name));
            e.setCustomNameVisible(true);
            e.setMaxLife(1200);
            p.sendSystemMessage(Component.translatable("chat.type.text", Component.literal(name), Component.literal("привет")));
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age > 20 && !a.hasLiveEntities()) a.finish();
        }
    }
}
