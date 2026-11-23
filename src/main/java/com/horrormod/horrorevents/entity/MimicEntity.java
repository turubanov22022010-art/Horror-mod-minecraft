package com.horrormod.horrorevents.entity;

import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** Подражатель. COPY — повторяет движения игрока; APPROACH — медленно подходит и бьёт. Анимации: idle, copy, walk. */
public class MimicEntity extends HorrorMob {
    public enum Mode { COPY, APPROACH }

    private static final EntityDataAccessor<Boolean> COPYING =
            SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation COPY = RawAnimation.begin().thenLoop("copy");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private Mode mode = Mode.COPY;
    private Vec3 lastPlayerPos;
    private int stareTicks;

    public MimicEntity(EntityType<? extends MimicEntity> type, Level level) {
        super(type, level);
        this.maxLife = 1200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COPYING, true);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.entityData.set(COPYING, mode == Mode.COPY);
    }

    @Override
    protected void tickBehaviour(ServerPlayer p) {
        if (mode == Mode.COPY) {
            Vec3 now = p.position();
            if (lastPlayerPos != null) {
                Vec3 d = now.subtract(lastPlayerPos);
                if (d.x * d.x + d.z * d.z > 1.0E-5D) move(MoverType.SELF, new Vec3(d.x, 0, d.z));
                if (p.getDeltaMovement().y > 0.3D && onGround()) jumpFromGround();
            }
            lastPlayerPos = now;
            setShiftKeyDown(p.isShiftKeyDown());
            setYRot(p.getYRot());
            setYHeadRot(p.getYHeadRot());
            setYBodyRot(p.getYRot());
            setXRot(p.getXRot());
            if (distanceTo(p) < 4.0F) {
                vanish(true);
                return;
            }
            if (isSeenBy(p) && ++stareTicks > 200) vanish(true);
        } else {
            getNavigation().moveTo(p, 1.0D);
            facePlayer(p);
            if (distanceTo(p) < 2.0F) {
                p.hurt(damageSources().mobAttack(this), 4.0F);
                vanish(true);
            }
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() { return ModSounds.FAKE_BREATHING.get(); }

    @Override
    protected PlayState predicate(AnimationState<HorrorMob> state) {
        if (state.isMoving()) return state.setAndContinue(WALK);
        return state.setAndContinue(this.entityData.get(COPYING) ? COPY : IDLE);
    }
}
