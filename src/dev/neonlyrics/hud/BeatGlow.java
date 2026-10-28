/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.License;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.hud.Theme;
import dev.neonlyrics.lyrics.NowPlaying;
import dev.neonlyrics.lyrics.SpotifyClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class BeatGlow {
    private static float level = 0.0f;
    private static long last = System.nanoTime();

    private BeatGlow() {
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (!License.ok()) {
            return;
        }
        Config c = NeonLyrics.cfg();
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (float)(now - last) / 1.0E9f);
        last = now;
        NowPlaying np = SpotifyClient.now();
        float beat = c.beatGlow && np.playing && !mc.options.hideGui ? np.beatPulse(np.progressNow(c.lyricsOffsetMs)) : 0.0f;
        float f = level = beat > level ? beat : level + (beat - level) * (1.0f - (float)Math.exp(-5.0f * dt));
        if (level < 0.02f) {
            return;
        }
        float old = Gfx.alpha;
        Gfx.alpha = 1.0f;
        int w = g.guiWidth();
        int h = g.guiHeight();
        int acc = Theme.accent();
        int size = Math.max(24, Math.min(w, h) / 7);
        float a = 0.28f * level;
        Gfx.vGradient(g, 0, 0, w, size, acc, acc, a, 0.0f);
        Gfx.vGradient(g, 0, h - size, w, size, acc, acc, 0.0f, a);
        int steps = 12;
        for (int i = 0; i < steps; ++i) {
            float k = 1.0f - (float)i / (float)steps;
            int x0 = Math.round((float)(size * i) / (float)steps);
            int x1 = Math.round((float)(size * (i + 1)) / (float)steps);
            Gfx.rect(g, x0, 0, x1 - x0, h, acc, a * k * k / 2.2f);
            Gfx.rect(g, w - x1, 0, x1 - x0, h, acc, a * k * k / 2.2f);
        }
        Gfx.alpha = old;
    }
}

