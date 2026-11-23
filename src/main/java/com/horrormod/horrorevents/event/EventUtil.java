package com.horrormod.horrorevents.event;

import com.horrormod.horrorevents.entity.HorrorMob;
import com.horrormod.horrorevents.network.ClientEffectPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

/** Общие вспомогательные функции для событий. */
public final class EventUtil {
    public static final String[] NAMES = {"Null", "Observer", "Guest_0", "Wanderer", "Entity_404", "Alex_", "Steve_2"};

    private EventUtil() {}

    public static String randomName(RandomSource r) {
        return NAMES[r.nextInt(NAMES.length)];
    }

    public static void effect(ServerPlayer p, int effect, int duration) {
        PacketDistributor.sendToPlayer(p, new ClientEffectPayload(effect, duration));
    }

    /** Звук слышит только этот игрок. */
    public static void sound(ServerPlayer p, Holder<SoundEvent> s, Vec3 pos, float volume, float pitch) {
        p.connection.send(new ClientboundSoundPacket(s, SoundSource.AMBIENT, pos.x, pos.y, pos.z,
                volume, pitch, p.getRandom().nextLong()));
    }

    /** Точка позади игрока на расстоянии d (на уровне пояса). */
    public static Vec3 behind(ServerPlayer p, double d) {
        Vec3 look = p.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0E-4) flat = new Vec3(0, 0, 1);
        flat = flat.normalize();
        return p.position().add(flat.scale(-d)).add(0, 0.8, 0);
    }

    public static boolean isFree(Level l, BlockPos pos) {
        BlockState s = l.getBlockState(pos);
        return s.getCollisionShape(l, pos).isEmpty() && s.getFluidState().isEmpty();
    }

    public static boolean isSolid(Level l, BlockPos pos) {
        BlockState s = l.getBlockState(pos);
        return s.blocksMotion() && s.getFluidState().isEmpty();
    }

    /** Ближайшая к высоте cy позиция, где можно стоять (воздух над твёрдым блоком). */
    public static BlockPos findGround(ServerLevel l, int x, int cy, int z) {
        if (!l.hasChunkAt(new BlockPos(x, cy, z))) return null;
        for (int y = cy + 4; y >= cy - 6; y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (isFree(l, p) && isFree(l, p.above()) && isSolid(l, p.below())) return p;
        }
        return null;
    }

    public static boolean canSee(ServerPlayer p, Vec3 target) {
        return p.level().clip(new ClipContext(p.getEyePosition(), target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p)).getType() == HitResult.Type.MISS;
    }

    /**
     * Подбирает позицию для появления существа.
     * centerDeg: 0 — спереди, 180 — сзади; spread — разброс угла; hidden — позиция не должна быть видна игроку.
     */
    public static BlockPos findSpawn(ServerPlayer p, double min, double max, double centerDeg,
                                     double spreadDeg, boolean hidden) {
        ServerLevel l = p.serverLevel();
        RandomSource r = p.getRandom();
        for (int i = 0; i < 40; i++) {
            double ang = Math.toRadians(p.getYRot() + centerDeg + (r.nextDouble() * 2 - 1) * spreadDeg);
            double dist = min + r.nextDouble() * (max - min);
            int x = Mth.floor(p.getX() - Math.sin(ang) * dist);
            int z = Mth.floor(p.getZ() + Math.cos(ang) * dist);
            BlockPos g = findGround(l, x, p.getBlockY(), z);
            if (g == null) continue;
            if (hidden && canSee(p, new Vec3(g.getX() + 0.5, g.getY() + 1.6, g.getZ() + 0.5))) continue;
            return g;
        }
        return null;
    }

    public static <T extends HorrorMob> T spawn(ServerPlayer p, ActiveEvent a, EntityType<T> type, BlockPos pos) {
        if (pos == null) return null;
        ServerLevel l = p.serverLevel();
        T e = type.create(l);
        if (e == null) return null;
        e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, l.random.nextFloat() * 360F, 0F);
        e.setTargetPlayer(p);
        l.addFreshEntity(e);
        a.track(e);
        return e;
    }

    /** Блоки, которые обычно ставит игрок (постройки). */
    public static boolean isManMade(BlockState s) {
        return s.is(BlockTags.PLANKS) || s.is(BlockTags.WOOL) || s.is(Tags.Blocks.GLASS_BLOCKS)
                || s.is(BlockTags.STONE_BRICKS) || s.is(BlockTags.LOGS)
                || s.is(Blocks.COBBLESTONE) || s.is(Blocks.BRICKS);
    }
}
