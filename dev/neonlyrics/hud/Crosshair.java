/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.License;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.hud.Theme;
import dev.neonlyrics.lyrics.NowPlaying;
import dev.neonlyrics.lyrics.SpotifyClient;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class Crosshair {
    private Crosshair() {
    }

    public static HudElement wrap(HudElement original) {
        return (g, delta) -> {
            Config c = NeonLyrics.cfg();
            class_310 mc = class_310.method_1551();
            if (c.crosshair <= 0 || !License.ok() || mc.field_1724 == null || !mc.field_1690.method_31044().method_31034() || mc.field_1690.field_1842) {
                original.render(g, delta);
                return;
            }
            Crosshair.draw(g, c);
        };
    }

    private static void draw(class_332 g, Config c) {
        float old = Gfx.alpha;
        Gfx.alpha = 1.0f;
        float cx = (float)g.method_51421() / 2.0f;
        float cy = (float)g.method_51443() / 2.0f;
        int col = 0xFFFFFF;
        int sh = 0;
        switch (c.crosshair) {
            case 1: {
                Gfx.aaCircle(g, cx, cy, 2.2f, sh, 0.45f);
                Gfx.aaCircle(g, cx, cy, 1.4f, col, 1.0f);
                break;
            }
            case 2: {
                Crosshair.cross(g, cx, cy, 2.5f, 4.5f, col, sh);
                break;
            }
            case 3: {
                Crosshair.ring(g, cx, cy, 4.5f, sh, 0.45f, 1.6f);
                Crosshair.ring(g, cx, cy, 4.5f, col, 1.0f, 0.9f);
                Gfx.aaCircle(g, cx, cy, 0.9f, col, 1.0f);
                break;
            }
            case 4: {
                NowPlaying np = SpotifyClient.now();
                float beat = np.playing ? np.beatPulse(np.progressNow(c.lyricsOffsetMs)) : 0.0f;
                Crosshair.cross(g, cx, cy, 2.0f + 2.5f * beat, 4.0f, Gfx.lerpColor(col, Theme.accent(), beat), sh);
                Gfx.aaCircle(g, cx, cy, 0.9f, col, 1.0f);
                break;
            }
        }
        Gfx.alpha = old;
    }

    private static void cross(class_332 g, float cx, float cy, float gap, float len, int col, int sh) {
        float w = 1.2f;
        for (int pass = 0; pass < 2; ++pass) {
            float e = pass == 0 ? 0.6f : 0.0f;
            int cc = pass == 0 ? sh : col;
            float a = pass == 0 ? 0.45f : 1.0f;
            Gfx.aaRound(g, cx - w / 2.0f - e, cy - gap - len - e, w + 2.0f * e, len + 2.0f * e, 0.6f, cc, a);
            Gfx.aaRound(g, cx - w / 2.0f - e, cy + gap - e, w + 2.0f * e, len + 2.0f * e, 0.6f, cc, a);
            Gfx.aaRound(g, cx - gap - len - e, cy - w / 2.0f - e, len + 2.0f * e, w + 2.0f * e, 0.6f, cc, a);
            Gfx.aaRound(g, cx + gap - e, cy - w / 2.0f - e, len + 2.0f * e, w + 2.0f * e, 0.6f, cc, a);
        }
    }

    private static void ring(class_332 g, float cx, float cy, float r, int col, float a, float dot) {
        int n = 28;
        for (int i = 0; i < n; ++i) {
            double an = (double)i * Math.PI * 2.0 / (double)n;
            Gfx.aaCircle(g, (float)((double)cx + Math.cos(an) * (double)r), (float)((double)cy + Math.sin(an) * (double)r), dot / 2.0f + 0.3f, col, a);
        }
    }
}

