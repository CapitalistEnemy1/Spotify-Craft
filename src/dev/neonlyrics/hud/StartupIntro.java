/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.hud.Anim;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.hud.Theme;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public final class StartupIntro {
    private static long start = -1L;
    private static boolean done = false;

    private StartupIntro() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (done || !(screen instanceof TitleScreen) || !NeonLyrics.cfg().fxStartup) {
                return;
            }
            if (start < 0L) {
                start = System.currentTimeMillis();
            }
            ScreenEvents.afterExtract((Screen)screen).register((scr, g, mx, my, delta) -> {
                if (done) {
                    return;
                }
                float t = (float)(System.currentTimeMillis() - start) / 1000.0f;
                if (t > 2.4f) {
                    done = true;
                    return;
                }
                int sw = g.guiWidth();
                int sh = g.guiHeight();
                float old = Gfx.alpha;
                float out = t > 1.7f ? 1.0f - (t - 1.7f) / 0.7f : 1.0f;
                Gfx.alpha = 1.0f;
                Gfx.rect(g, 0, 0, sw, sh, 460046, 0.96f * out);
                float in = Anim.outBack(Math.min(1.0f, t / 0.6f));
                float glow = (float)(0.5 + 0.5 * Math.sin(t * 4.0f));
                int acc = Theme.accent();
                g.pose().pushMatrix();
                g.pose().translate((float)sw / 2.0f, (float)sh / 2.0f - 6.0f);
                float sc = 2.6f * (0.7f + 0.3f * in);
                g.pose().scale(sc, sc);
                String logo = "NEON";
                int lw = Gfx.w(logo, Gfx.BOLD);
                Gfx.alpha = out * Math.min(1.0f, t / 0.3f);
                Gfx.aaRound(g, (float)(-lw) / 2.0f - 8.0f, -7.0f, lw + 16, 17.0f, 8.5f, acc, 0.18f + 0.12f * glow);
                Gfx.textBold(g, logo, -lw / 2, -3, 0xFFFFFF, 1.0f, false);
                g.pose().popMatrix();
                String sub = "Lyrics";
                int n = Math.max(0, Math.min(sub.length(), (int)((t - 0.5f) / 0.05f)));
                Gfx.center(g, sub.substring(0, n), sw / 2, sh / 2 + 22, 10326968, out, false);
                float pw = Math.min(1.0f, t / 1.6f);
                Gfx.aaRound(g, (float)sw / 2.0f - 40.0f, (float)sh / 2.0f + 36.0f, 80.0f, 2.0f, 1.0f, 0xFFFFFF, 0.12f * out);
                Gfx.aaRound(g, (float)sw / 2.0f - 40.0f, (float)sh / 2.0f + 36.0f, 80.0f * pw, 2.0f, 1.0f, acc, out);
                Gfx.alpha = old;
            });
        });
    }
}

