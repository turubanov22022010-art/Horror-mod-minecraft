package com.horrormod.horrorevents.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Поддельный экран краша. Это только визуальный эффект: игра не падает, экран закрывается сам или по Esc. */
public class FakeCrashScreen extends Screen {
    private static final String[] LINES = {
            "Minecraft has crashed!",
            "",
            "java.lang.IllegalStateException: Entity not found in world",
            "    at net.minecraft.world.level.Level.getEntity(Level.java:???)",
            "    at ???.???(Unknown Source)",
            "",
            "A detailed walkthrough of the error has been saved. Exit code: -1"
    };
    private final int total;
    private int ticks;

    public FakeCrashScreen(int total) {
        super(Component.literal("Minecraft"));
        this.total = total;
    }

    @Override
    public void tick() {
        if (++ticks >= total) onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xFF141414);
        int y = height / 2 - LINES.length * 6;
        for (int i = 0; i < LINES.length; i++) {
            int color = i == 0 ? 0xFFFF5555 : 0xFFBBBBBB;
            g.drawCenteredString(font, Component.literal(LINES[i]), width / 2, y + i * 12, color);
        }
    }
}
