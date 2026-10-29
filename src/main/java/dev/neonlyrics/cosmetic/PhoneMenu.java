/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 */
package dev.neonlyrics.cosmetic;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.cosmetic.WorldPanel;
import dev.neonlyrics.gui.LyricsScreen;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_4597;
import org.joml.Matrix4f;

public final class PhoneMenu {
    private static WorldPanel panel;

    private PhoneMenu() {
    }

    public static boolean interactive() {
        return NeonLyrics.cfg().menuMode == 1;
    }

    public static void open(class_310 mc) {
        if (panel != null) {
            if (!panel.closing()) {
                panel.close();
            }
            return;
        }
        if (!PhoneMenu.interactive() || mc.field_1687 == null || mc.field_1724 == null) {
            mc.method_1507((class_437)new LyricsScreen());
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

    public static void tick(class_310 mc) {
        if (panel == null) {
            return;
        }
        if (mc.field_1687 == null || mc.field_1724 == null || panel.finished() || mc.field_1724.method_73189().method_1022(panel.pos()) > 32.0) {
            panel = null;
            return;
        }
        panel.tick(mc);
    }

    public static void startTick(class_310 mc) {
        if (panel == null || mc.field_1755 != null || !panel.hovered()) {
            return;
        }
        while (mc.field_1690.field_1886.method_1436()) {
            panel.click();
        }
        while (mc.field_1690.field_1904.method_1436()) {
            panel.click();
        }
    }

    public static void render(class_310 mc, class_4597 buffers, Matrix4f base, float pt) {
        if (panel != null && mc.field_1724 != null) {
            panel.render(mc, buffers, base);
        }
    }
}

