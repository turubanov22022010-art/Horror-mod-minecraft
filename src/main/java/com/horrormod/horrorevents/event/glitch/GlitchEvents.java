package com.horrormod.horrorevents.event.glitch;

import com.horrormod.horrorevents.event.ActiveEvent;
import com.horrormod.horrorevents.event.EventCategory;
import com.horrormod.horrorevents.event.EventUtil;
import com.horrormod.horrorevents.event.HorrorEvent;
import com.horrormod.horrorevents.network.ClientEffectPayload;
import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** 11 событий, ломающих игровую логику. */
public final class GlitchEvents {
    private static final Block[] CORRUPT = {Blocks.SCULK, Blocks.COAL_BLOCK, Blocks.BLACKSTONE, Blocks.CRYING_OBSIDIAN};

    private GlitchEvents() {}

    public static void registerAll(Consumer<HorrorEvent> r) {
        r.accept(new InventoryTheft());
        r.accept(new ShortTeleport());
        r.accept(new HiddenTrap());
        r.accept(new BlockReplacement());
        r.accept(new FakeCrash());
        r.accept(new HouseDestruction());
        r.accept(new TickDistortion());
        r.accept(new ItemTextGlitch());
        r.accept(new FloorDisappearance());
        r.accept(new WarningSign());
        r.accept(new WorldCorruption());
    }

    private static boolean plainSolid(ServerLevel l, BlockPos pos) {
        BlockState s = l.getBlockState(pos);
        BlockEntity be = l.getBlockEntity(pos);
        float h = s.getDestroySpeed(l, pos);
        return be == null && s.isSolidRender(l, pos) && h >= 0.0F && h <= 10.0F;
    }

    /** Из инвентаря пропадает один предмет. */
    public static final class InventoryTheft extends HorrorEvent {
        public InventoryTheft() { super("inventory_theft", EventCategory.GLITCH, 13, 7, 2, 900, true); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < 36; i++) if (!p.getInventory().items.get(i).isEmpty()) slots.add(i);
            if (slots.isEmpty()) { a.finish(); return; }
            ItemStack s = p.getInventory().items.get(slots.get(p.getRandom().nextInt(slots.size())));
            s.shrink(1);
            p.getInventory().setChanged();
            p.containerMenu.broadcastChanges();
            EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.4F, 1.4F);
        }
    }

    /** Сдвиг пространства: короткая телепортация. */
    public static final class ShortTeleport extends HorrorEvent {
        public ShortTeleport() { super("short_teleport", EventCategory.GLITCH, 10, 6, 2, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            BlockPos g = EventUtil.findSpawn(p, 5, 9, p.getRandom().nextInt(360), 0, false);
            if (g == null) { a.finish(); return; }
            p.teleportTo(g.getX() + 0.5, g.getY(), g.getZ() + 0.5);
            EventUtil.effect(p, ClientEffectPayload.FLICKER, 12);
            EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.6F, 1.0F);
        }
    }

    /** Скрытая ловушка: яма под нажимной плитой на пути игрока. */
    public static final class HiddenTrap extends HorrorEvent {
        public HiddenTrap() { super("hidden_trap", EventCategory.GLITCH, 14, 5, 2, 1200, true); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            Vec3 look = p.getLookAngle();
            int x = Mth.floor(p.getX() + look.x * 6.0);
            int z = Mth.floor(p.getZ() + look.z * 6.0);
            BlockPos g = EventUtil.findGround(l, x, p.getBlockY(), z);
            if (g == null) { a.finish(); return; }
            l.setBlock(g, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), 2);
            for (int i = 1; i <= 5; i++) l.setBlock(g.below(i), Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /** Подмена блоков постройки на «неправильные». */
    public static final class BlockReplacement extends HorrorEvent {
        public BlockReplacement() { super("block_replacement", EventCategory.GLITCH, 12, 7, 12, 900, true); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            BlockPos c = p.blockPosition();
            List<BlockPos> list = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-9, -4, -9), c.offset(9, 5, 9))) {
                if (EventUtil.isManMade(l.getBlockState(pos)) && l.getBlockEntity(pos) == null) list.add(pos.immutable());
            }
            if (list.isEmpty()) { a.finish(); return; }
            Collections.shuffle(list);
            a.vars.put("list", list);
            a.vars.put("n", 0);
        }
        @SuppressWarnings("unchecked")
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            List<BlockPos> list = (List<BlockPos>) a.vars.get("list");
            Integer n = (Integer) a.vars.get("n");
            if (list == null || n == null || a.age % 40 != 10) return;
            if (n >= 5 || n >= list.size()) return;
            a.level.setBlock(list.get(n), CORRUPT[p.getRandom().nextInt(CORRUPT.length)].defaultBlockState(), 3);
            a.vars.put("n", n + 1);
        }
    }

    /** Критическая ошибка: поддельный экран краша (игра не падает). */
    public static final class FakeCrash extends HorrorEvent {
        public FakeCrash() { super("fake_crash", EventCategory.GLITCH, 8, 4, 6, 1500, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.effect(p, ClientEffectPayload.FAKE_CRASH, 80);
        }
    }

    /** Исчезновение дома: блоки построек вокруг кровати/игрока пропадают. */
    public static final class HouseDestruction extends HorrorEvent {
        public HouseDestruction() { super("house_destruction", EventCategory.GLITCH, 18, 3, 20, 2400, true); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            BlockPos c = p.blockPosition();
            BlockPos bed = p.getRespawnPosition();
            if (bed != null && p.getRespawnDimension() == Level.OVERWORLD && bed.distSqr(c) < 64 * 64) c = bed;
            List<BlockPos> list = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-8, -3, -8), c.offset(8, 6, 8))) {
                if (EventUtil.isManMade(l.getBlockState(pos))) list.add(pos.immutable());
            }
            if (list.isEmpty()) { a.finish(); return; }
            Collections.shuffle(list);
            a.vars.put("list", list.size() > 160 ? new ArrayList<>(list.subList(0, 160)) : list);
            a.vars.put("n", 0);
            EventUtil.effect(p, ClientEffectPayload.FLICKER, 60);
        }
        @SuppressWarnings("unchecked")
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            List<BlockPos> list = (List<BlockPos>) a.vars.get("list");
            Integer n = (Integer) a.vars.get("n");
            if (list == null || n == null) return;
            if (a.age % 20 == 0) EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.5F, 0.7F);
            if (a.age % 5 != 0) return;
            int end = Math.min(list.size(), n + 8);
            for (int i = n; i < end; i++) a.level.setBlock(list.get(i), Blocks.AIR.defaultBlockState(), 3);
            a.vars.put("n", end);
            if (end >= list.size()) a.finish();
        }
    }

    /** Сбой реальности: игрок то замедляется, то ускоряется. */
    public static final class TickDistortion extends HorrorEvent {
        public TickDistortion() { super("tick_distortion", EventCategory.GLITCH, 11, 6, 10, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.7F, 0.8F);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age % 40 == 0) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3, false, false, false));
                EventUtil.effect(p, ClientEffectPayload.FLICKER, 10);
            } else if (a.age % 40 == 20) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 3, false, false, false));
            }
        }
    }

    /** Искажённые надписи предметов. */
    public static final class ItemTextGlitch extends HorrorEvent {
        public ItemTextGlitch() { super("item_text_glitch", EventCategory.GLITCH, 6, 8, 30, 600, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.effect(p, ClientEffectPayload.ITEM_GLITCH, 600);
        }
    }

    /** Исчезающий пол: блоки под игроком пропадают и затем возвращаются. */
    public static final class FloorDisappearance extends HorrorEvent {
        private record Removed(BlockPos pos, BlockState state) {}

        public FloorDisappearance() { super("floor_disappearance", EventCategory.GLITCH, 13, 6, 8, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            a.vars.put("removed", new ArrayList<Removed>());
            EventUtil.sound(p, ModSounds.UNDERGROUND_DIGGING, p.position().add(0, -2, 0), 1.0F, 0.6F);
        }
        @SuppressWarnings("unchecked")
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age != 20) return;
            List<Removed> removed = (List<Removed>) a.vars.get("removed");
            BlockPos base = p.blockPosition().below();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = base.offset(dx, 0, dz);
                    if (plainSolid(a.level, pos)) {
                        removed.add(new Removed(pos, a.level.getBlockState(pos)));
                        a.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        @SuppressWarnings("unchecked")
        @Override public void onEnd(ServerPlayer p, ActiveEvent a) {
            List<Removed> removed = (List<Removed>) a.vars.get("removed");
            if (removed == null) return;
            for (Removed r : removed) {
                if (!a.level.getBlockState(r.pos()).isAir()) continue;
                if (p != null && p.getBoundingBox().intersects(new AABB(r.pos()))) continue;
                a.level.setBlock(r.pos(), r.state(), 3);
            }
        }
    }

    /** Послание: табличка с загадочным текстом. */
    public static final class WarningSign extends HorrorEvent {
        private static final String[][] TEXTS = {
                {"", "ОН СМОТРИТ", "НА ТЕБЯ", ""},
                {"НЕ", "ОБОРАЧИВАЙСЯ", "", ""},
                {"ТЫ", "НЕ ОДИН", "", ""},
                {"Я ЗА", "ТОБОЙ", "", ""},
                {"УХОДИ", "ПОКА", "МОЖЕШЬ", ""},
                {"ОНО", "НЕ СПИТ", "", ""}
        };

        public WarningSign() { super("warning_sign", EventCategory.GLITCH, 9, 7, 3, 900, false); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            ServerLevel l = p.serverLevel();
            Vec3 look = p.getLookAngle();
            int x = Mth.floor(p.getX() + look.x * 4.0);
            int z = Mth.floor(p.getZ() + look.z * 4.0);
            BlockPos g = EventUtil.findGround(l, x, p.getBlockY(), z);
            if (g == null) { a.finish(); return; }
            int rot = Mth.floor((p.getYRot() + 180.0F) * 16.0F / 360.0F + 0.5F) & 15;
            l.setBlock(g, Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rot), 3);
            if (l.getBlockEntity(g) instanceof SignBlockEntity sign) {
                String[] lines = TEXTS[p.getRandom().nextInt(TEXTS.length)];
                sign.updateText(t -> {
                    var out = t;
                    for (int i = 0; i < 4; i++) out = out.setMessage(i, Component.literal(lines[i]));
                    return out;
                }, true);
                sign.setChanged();
                l.sendBlockUpdated(g, l.getBlockState(g), l.getBlockState(g), 3);
            }
        }
    }

    /** Повреждение реальности: блоки вокруг игрока «портятся», экран мерцает. */
    public static final class WorldCorruption extends HorrorEvent {
        public WorldCorruption() { super("world_corruption", EventCategory.GLITCH, 21, 4, 60, 1800, true); }
        @Override public void onStart(ServerPlayer p, ActiveEvent a) {
            EventUtil.effect(p, ClientEffectPayload.FLICKER, 40);
            EventUtil.sound(p, ModSounds.DISTORTION, p.position(), 0.9F, 0.6F);
        }
        @Override public void onTick(ServerPlayer p, ActiveEvent a) {
            if (a.age % 100 == 0) EventUtil.effect(p, ClientEffectPayload.FLICKER, 30);
            if (a.age % 15 != 0) return;
            for (int i = 0; i < 3; i++) {
                BlockPos pos = p.blockPosition().offset(p.getRandom().nextInt(21) - 10, p.getRandom().nextInt(9) - 4,
                        p.getRandom().nextInt(21) - 10);
                if (plainSolid(a.level, pos) && !a.level.getBlockState(pos).is(Blocks.BEDROCK)) {
                    a.level.setBlock(pos, CORRUPT[p.getRandom().nextInt(CORRUPT.length)].defaultBlockState(), 3);
                }
            }
        }
    }
}
