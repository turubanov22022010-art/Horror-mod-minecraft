package com.horrormod.horrorevents.network;

import com.horrormod.horrorevents.HorrorEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Пакет сервер -> клиент: запустить визуальный эффект. */
public record ClientEffectPayload(int effect, int duration) implements CustomPacketPayload {
    public static final int FLICKER = 0;
    public static final int SHAKE = 1;
    public static final int FAKE_CRASH = 2;
    public static final int ITEM_GLITCH = 3;
    public static final int FOG = 4;

    public static final CustomPacketPayload.Type<ClientEffectPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HorrorEvents.MOD_ID, "client_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientEffectPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientEffectPayload::effect,
            ByteBufCodecs.VAR_INT, ClientEffectPayload::duration,
            ClientEffectPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
