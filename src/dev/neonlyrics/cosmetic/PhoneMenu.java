/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  org.joml.Matrix4f
 */
package dev.neonlyrics.cosmetic;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.compat.MultiBufferSource;
import dev.neonlyrics.cosmetic.WorldPanel;
import dev.neonlyrics.gui.LyricsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix4f;

public final class PhoneMenu {
    private static WorldPanel panel;

    private PhoneMenu() {
    }

    public static boolean interactive() {
        return NeonLyrics.cfg().menuMode == 1;
    }

    public static void open(Minecraft mc) {
        if (panel != null) {
            if (!panel.closing()) {
                panel.close();
            }
            return;
        }
        if (!PhoneMenu.interactive() || mc.level == null || mc.player == null) {
            mc.setScreen((Screen)new LyricsScreen());
            return;
        }
        panel = new WorldPanel(mc);
    }

    public static void closeNow() {
        panel = null;
    }

    public static void menuClosed() {
    }

    public static boolean hidesHand() {
        return false;
    }

    public static void tick(Minecraft mc) {
        if (panel == null) {
            return;
        }
        if (mc.level == null || mc.player == null || panel.finished() || mc.player.position().distanceTo(panel.pos()) > 32.0) {
            panel = null;
            return;
        }
        panel.tick(mc);
    }

    public static void startTick(Minecraft mc) {
        if (panel == null || mc.screen != null || !panel.hovered()) {
            return;
        }
        while (mc.options.keyAttack.consumeClick()) {
            panel.click();
        }
        while (mc.options.keyUse.consumeClick()) {
            panel.click();
        }
    }

    public static void render(Minecraft mc, MultiBufferSource buffers, Matrix4f base, float pt) {
        if (panel != null && mc.player != null) {
            panel.render(mc, buffers, base);
        }
    }
}

