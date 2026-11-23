package com.horrormod.horrorevents.registry;

import com.horrormod.horrorevents.HorrorEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Реестр звуков мода. OGG-файлы добавляются в assets/horror_events/sounds/. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, HorrorEvents.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FAKE_STEPS = register("fake_steps");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAKE_BREATHING = register("fake_breathing");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOOR_CREAK = register("door_creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> UNDERGROUND_DIGGING = register("underground_digging");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAKE_CREEPER = register("fake_creeper");
    public static final DeferredHolder<SoundEvent, SoundEvent> STALKER_APPEAR = register("stalker_appear");
    public static final DeferredHolder<SoundEvent, SoundEvent> STALKER_DISAPPEAR = register("stalker_disappear");
    public static final DeferredHolder<SoundEvent, SoundEvent> STALKER_AMBIENT = register("stalker_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTORTION = register("distortion");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HorrorEvents.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
