package com.horrormod.horrorevents.entity;

import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** Преследователь. FREEZE — замирает, когда игрок смотрит; ACCELERATE — постепенно разгоняется. Анимации: idle, walk, run. */
public class FollowerEntity extends HorrorMob {
    public enum Mode { FREEZE, ACCELERATE }

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");

    private Mode mode = Mode.FREEZE;
    private boolean frozen;

    public FollowerEntity(EntityType<? extends FollowerEntity> type, Level level) {
        super(type, level);
        this.maxLife = 2400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    public void setMode(Mode mode) { this.mode = mode; }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
    }

    @Override
    protected void tickBehaviour(ServerPlayer player) {
        setTarget(player);
        if (mode == Mode.FREEZE) {
            frozen = isSeenBy(player);
            if (frozen) getNavigation().stop();
        } else {
            frozen = false;
            double t = Math.min(1.0D, life / 1200.0D);
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2D + 0.25D * t);
        }
    }

    @Override
    protected boolean isImmobile() { return super.isImmobile() || frozen; }

    @Override
    public boolean doHurtTarget(Entity target) {
        return !frozen && super.doHurtTarget(target);
    }

    @Override
    protected SoundEvent getAmbientSound() { return ModSounds.STALKER_AMBIENT.get(); }

    @Override
    public int getAmbientSoundInterval() { return 200; }

    @Override
    protected PlayState predicate(AnimationState<HorrorMob> state) {
        double s = getDeltaMovement().horizontalDistanceSqr();
        if (s > 0.05D) return state.setAndContinue(RUN);
        if (s > 0.0005D) return state.setAndContinue(WALK);
        return state.setAndContinue(IDLE);
    }
}
