package com.horrormod.horrorevents.client;

import com.horrormod.horrorevents.HorrorEvents;
import com.horrormod.horrorevents.client.render.HorrorRenderer;
import com.horrormod.horrorevents.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = HorrorEvents.MOD_ID, dist = Dist.CLIENT)
public final class HorrorClient {
    public HorrorClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(HorrorClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(ClientEffects::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEffects::onRenderGui);
        NeoForge.EVENT_BUS.addListener(ClientEffects::onCameraAngles);
        NeoForge.EVENT_BUS.addListener(ClientEffects::onTooltip);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.STALKER.get(), ctx -> new HorrorRenderer<>(ctx, "stalker"));
        e.registerEntityRenderer(ModEntities.FOLLOWER.get(), ctx -> new HorrorRenderer<>(ctx, "follower"));
        e.registerEntityRenderer(ModEntities.MIMIC.get(), ctx -> new HorrorRenderer<>(ctx, "mimic"));
    }
}
