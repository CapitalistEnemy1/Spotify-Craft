/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.hud.Gfx;

public final class Theme {
    public static final int BG = 986135;
    public static final int PANEL = 1446431;
    public static final int CARD = 1972265;
    public static final int CARD_HI = 2761272;
    public static final int LINE = 3813456;
    public static final int TEXT = 0xF3EFFF;
    public static final int MUTED = 10326968;
    public static final int GREEN = 6022299;
    public static final int RED = 16735354;
    public static final int GOLD = 16762460;
    public static final int[] ACCENTS = new int[]{10976255, 16740036, 8162559, 6022336, 16762460, 16747084};
    public static final String[] ACCENT_NAMES = new String[]{"Lila", "Pink", "Indigo", "Mint", "Gold", "Orange"};

    private Theme() {
    }

    public static int accent() {
        if (NeonLyrics.cfg().rgbAccent) {
            return Gfx.hsv((float)(System.currentTimeMillis() % 6000L) / 6000.0f, 0.75f, 1.0f);
        }
        return ACCENTS[Math.floorMod(NeonLyrics.cfg().accent, ACCENTS.length)];
    }

    public static int accent2() {
        if (NeonLyrics.cfg().rgbAccent) {
            return Gfx.hsv((float)((System.currentTimeMillis() + 2000L) % 6000L) / 6000.0f, 0.75f, 1.0f);
        }
        return ACCENTS[Math.floorMod(NeonLyrics.cfg().accent + 1, ACCENTS.length)];
    }
}

