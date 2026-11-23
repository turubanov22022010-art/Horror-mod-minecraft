package com.horrormod.horrorevents.registry;

import com.horrormod.horrorevents.HorrorEvents;
import com.horrormod.horrorevents.entity.FollowerEntity;
import com.horrormod.horrorevents.entity.MimicEntity;
import com.horrormod.horrorevents.entity.StalkerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HorrorEvents.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<StalkerEntity>> STALKER = ENTITIES.register("stalker",
            () -> EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.8F).clientTrackingRange(12).build("stalker"));
    public static final DeferredHolder<EntityType<?>, EntityType<FollowerEntity>> FOLLOWER = ENTITIES.register("follower",
            () -> EntityType.Builder.<FollowerEntity>of(FollowerEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.0F).clientTrackingRange(12).build("follower"));
    public static final DeferredHolder<EntityType<?>, EntityType<MimicEntity>> MIMIC = ENTITIES.register("mimic",
            () -> EntityType.Builder.<MimicEntity>of(MimicEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(12).build("mimic"));

    private ModEntities() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(STALKER.get(), StalkerEntity.createAttributes().build());
        event.put(FOLLOWER.get(), FollowerEntity.createAttributes().build());
        event.put(MIMIC.get(), MimicEntity.createAttributes().build());
    }
}
