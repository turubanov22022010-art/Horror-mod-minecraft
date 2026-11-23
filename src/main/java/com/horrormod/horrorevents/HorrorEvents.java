package com.horrormod.horrorevents;

import com.horrormod.horrorevents.command.HorrorCommand;
import com.horrormod.horrorevents.config.HorrorConfig;
import com.horrormod.horrorevents.event.EventManager;
import com.horrormod.horrorevents.network.HorrorNetwork;
import com.horrormod.horrorevents.registry.ModEntities;
import com.horrormod.horrorevents.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Главный класс мода Horror Events. */
@Mod(HorrorEvents.MOD_ID)
public final class HorrorEvents {
    public static final String MOD_ID = "horror_events";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public HorrorEvents(IEventBus modEventBus, ModContainer modContainer) {
        ModSounds.SOUNDS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, HorrorConfig.SPEC);

        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(HorrorNetwork::register);

        NeoForge.EVENT_BUS.addListener(EventManager::onServerTick);
        NeoForge.EVENT_BUS.addListener(EventManager::onServerStopping);
        NeoForge.EVENT_BUS.addListener(EventManager::onWakeUp);
        NeoForge.EVENT_BUS.addListener(HorrorCommand::onRegister);
        LOGGER.info("Horror Events загружен.");
    }
}
