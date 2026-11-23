package com.horrormod.horrorevents.client;

import com.horrormod.horrorevents.network.ClientEffectPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Состояние и отрисовка клиентских эффектов: мерцание, тряска, туман, искажённые подписи, фейковый краш. */
public final class ClientEffects {
    private static int flicker;
    private static int shake;
    private static int itemGlitch;
    private static int fog;

    private ClientEffects() {}

    public static void handle(ClientEffectPayload p) {
        switch (p.effect()) {
            case ClientEffectPayload.FLICKER -> flicker = p.duration();
            case ClientEffectPayload.SHAKE -> shake = p.duration();
            case ClientEffectPayload.ITEM_GLITCH -> itemGlitch = p.duration();
            case ClientEffectPayload.FOG -> fog = p.duration();
            case ClientEffectPayload.FAKE_CRASH -> Minecraft.getInstance().setScreen(new FakeCrashScreen(p.duration()));
            default -> { }
        }
    }

    public static void onClientTick(ClientTickEvent.Post e) {
        if (flicker > 0) flicker--;
        if (shake > 0) shake--;
        if (itemGlitch > 0) itemGlitch--;
        if (fog > 0) fog--;
    }

    public static void onRenderGui(RenderGuiEvent.Post e) {
        GuiGraphics g = e.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (fog > 0) {
            int a = (int) (95 * Math.min(1.0F, fog / 40.0F));
            g.fill(0, 0, w, h, (a << 24) | 0x7C848C);
        }
        if (flicker > 0 && r.nextFloat() < 0.55F) {
            int a = 120 + r.nextInt(120);
            g.fill(0, 0, w, h, a << 24);
        }
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles e) {
        if (shake <= 0) return;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        float amp = Math.min(1.0F, shake / 20.0F) * 1.6F;
        e.setYaw(e.getYaw() + (r.nextFloat() - 0.5F) * amp);
        e.setPitch(e.getPitch() + (r.nextFloat() - 0.5F) * amp);
        e.setRoll(e.getRoll() + (r.nextFloat() - 0.5F) * amp * 2.0F);
    }

    public static void onTooltip(ItemTooltipEvent e) {
        if (itemGlitch <= 0) return;
        List<Component> tip = e.getToolTip();
        if (!tip.isEmpty()) tip.set(0, tip.get(0).copy().withStyle(ChatFormatting.OBFUSCATED));
    }
}
