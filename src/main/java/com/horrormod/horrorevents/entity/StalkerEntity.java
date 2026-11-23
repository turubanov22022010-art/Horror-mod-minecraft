package com.horrormod.horrorevents.entity;

import com.horrormod.horrorevents.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** Наблюдатель: стоит неподвижно, смотрит на игрока и исчезает, когда на него смотрят. Анимации: appear, idle, vanish. */
public class StalkerEntity extends HorrorMob {
    private static final RawAnimation APPEAR_IDLE = RawAnimation.begin().thenPlay("appear").thenLoop("idle");

    public StalkerEntity(EntityType<? extends StalkerEntity> type, Level level) {
        super(type, level);
        this.vanishWhenSeen = true;
        this.seenLimit = 12;
        this.maxLife = 1200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void tickBehaviour(ServerPlayer player) {
        facePlayer(player);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) vanish(true);
        return false;
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected SoundEvent getAmbientSound() { return ModSounds.STALKER_AMBIENT.get(); }

    @Override
    public int getAmbientSoundInterval() { return 240; }

    @Override
    protected PlayState predicate(AnimationState<HorrorMob> state) {
        return state.setAndContinue(APPEAR_IDLE);
    }
}
