package com.horrormod.horrorevents.entity;

import com.horrormod.horrorevents.event.EventUtil;
import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/** Базовый класс существ мода: привязка к игроку, ограниченное время жизни, GeckoLib. */
public abstract class HorrorMob extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID targetId;
    protected int life;
    protected int maxLife = 2400;
    protected int seenTicks;
    protected int seenLimit = 12;
    protected boolean vanishWhenSeen;

    protected HorrorMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public void setTargetPlayer(ServerPlayer p) { this.targetId = p.getUUID(); }
    public void setMaxLife(int ticks) { this.maxLife = ticks; }
    public void setVanishWhenSeen(boolean v, int limit) { this.vanishWhenSeen = v; this.seenLimit = limit; }

    protected ServerPlayer getTargetPlayer() {
        if (targetId == null || level().getServer() == null) return null;
        return level().getServer().getPlayerList().getPlayer(targetId);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        life++;
        ServerPlayer p = getTargetPlayer();
        if (p == null || !p.isAlive() || life > maxLife) {
            vanish(life > maxLife);
            return;
        }
        if (vanishWhenSeen && isSeenBy(p)) {
            if (++seenTicks >= seenLimit) {
                vanish(true);
                return;
            }
        } else if (seenTicks > 0) {
            seenTicks--;
        }
        tickBehaviour(p);
    }

    protected void tickBehaviour(ServerPlayer player) {}

    protected boolean isSeenBy(ServerPlayer p) {
        Vec3 view = p.getViewVector(1.0F).normalize();
        Vec3 to = new Vec3(getX() - p.getX(), getEyeY() - p.getEyeY(), getZ() - p.getZ()).normalize();
        return view.dot(to) > 0.93D && p.hasLineOfSight(this);
    }

    protected void facePlayer(ServerPlayer p) {
        double dx = p.getX() - getX();
        double dz = p.getZ() - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        setYRot(yaw);
        setYHeadRot(yaw);
        setYBodyRot(yaw);
    }

    public void vanish(boolean withSound) {
        if (level() instanceof ServerLevel sl) {
            ServerPlayer p = getTargetPlayer();
            if (withSound && p != null && !isSilent()) {
                EventUtil.sound(p, ModSounds.STALKER_DISAPPEAR, position(), 0.7F, 1.0F);
            }
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() / 2, getZ(),
                    20, 0.3D, 0.6D, 0.3D, 0.02D);
        }
        discard();
    }

    /** Существа не сохраняются в мир: после перезапуска не остаются «бродить». */
    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    protected abstract PlayState predicate(AnimationState<HorrorMob> state);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
