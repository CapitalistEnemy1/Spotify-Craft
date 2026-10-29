/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.util;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.hud.Island;
import dev.neonlyrics.util.FpsBoost;
import dev.neonlyrics.util.Fx;

public final class Profiles {
    private Profiles() {
    }

    public static void toggle() {
        Profiles.set(NeonLyrics.cfg().profile == 0 ? 1 : 0);
    }

    public static void set(int profile) {
        boolean on;
        Config c = NeonLyrics.cfg();
        boolean bl = on = profile == 0;
        if (!on && c.sky >= 0) {
            c.lastSky = c.sky;
        }
        c.sky = on ? Math.max(0, c.lastSky) : -1;
        c.fxSkyColor = on;
        c.fxWeather = on;
        c.fxAurora = on;
        c.fxStars = on;
        c.fxBeams = on;
        c.lyricsGlow = on;
        c.glassHotbar = on;
        c.blur = on;
        c.fxMenuAnim = on;
        c.fxConstellations = on;
        c.fxSkyMusic = on;
        c.quality = on ? 1 : 0;
        c.profile = profile;
        if (!on && !c.fpsBoost) {
            FpsBoost.apply(true);
        }
        if (on && c.fpsBoost) {
            FpsBoost.apply(false);
        }
        NeonLyrics.saveConfig();
        Island.notify(Fx.t(on ? "neonlyrics.profile.chill" : "neonlyrics.profile.pvp", new Object[0]), true);
    }
}

