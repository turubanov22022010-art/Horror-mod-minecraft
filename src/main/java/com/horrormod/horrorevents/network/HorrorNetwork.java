package com.horrormod.horrorevents.network;

import com.horrormod.horrorevents.client.ClientEffects;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class HorrorNetwork {
    private HorrorNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ClientEffectPayload.TYPE, ClientEffectPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientEffects.handle(payload)));
    }
}
