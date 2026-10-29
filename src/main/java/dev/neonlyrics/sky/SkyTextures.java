/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package dev.neonlyrics.sky;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.hud.Anim;
import dev.neonlyrics.sky.SkyPainter;
import dev.neonlyrics.sky.SkyPreset;
import dev.neonlyrics.sky.WorldSky;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class SkyTextures {
    public static final class_2960 SPRITES = class_2960.method_60655((String)"neonlyrics", (String)"sky/sprites");
    public static final int W = 2048;
    public static final int H = 512;
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NeonLyrics-SkyPaint");
        t.setDaemon(true);
        t.setPriority(4);
        return t;
    });
    private static volatile String wantKey = "";
    private static volatile Pending pending;
    private static String doneKey;
    private static int[] base;
    private static int[] fx;
    private static int version;
    private static boolean spritesReady;
    private static boolean spritesFailed;
    private static volatile class_1011 spritePending;
    private static boolean spritesRequested;
    private static long shownAt;

    private SkyTextures() {
    }

    public static SkyPainter.Spec spec(SkyPreset p) {
        if (p == SkyPreset.CUSTOM) {
            SkyPreset.applyCustom();
        }
        return new SkyPainter.Spec(p.name(), p.top, p.horizon, WorldSky.aurora1(p), WorldSky.aurora2(p), p.stars, p.aurora, p.moon, p.weather.name());
    }

    public static String key(SkyPainter.Spec s) {
        return s.kind() + "_" + Integer.toHexString(s.top()) + "_" + Integer.toHexString(s.horizon()) + "_" + Integer.toHexString(s.a1()) + "_" + Integer.toHexString(s.a2()) + "_" + s.stars() + "_" + s.aurora() + "_" + Integer.toHexString(s.moon());
    }

    public static void request(SkyPreset p) {
        SkyPainter.Spec s = SkyTextures.spec(p);
        String key = SkyTextures.key(s);
        if (key.equals(wantKey)) {
            return;
        }
        wantKey = key;
        EXEC.execute(() -> {
            try {
                if (!key.equals(wantKey)) {
                    return;
                }
                int[] b = SkyPainter.opaqueBase(SkyPainter.smooth(SkyPainter.base(s, 2048, 512), 2048, 512, false), s, 2048, 512);
                if (!key.equals(wantKey)) {
                    return;
                }
                int[] f = SkyPainter.fx(s, 2048, 512);
                if (f != null) {
                    f = SkyPainter.smooth(f, 2048, 512, true);
                }
                pending = new Pending(key, b, f);
            }
            catch (Throwable t) {
                NeonLyrics.LOG.warn("Himmel konnte nicht gemalt werden", t);
            }
        });
    }

    public static void update() {
        Pending pd;
        class_1011 sp;
        if (!spritesRequested) {
            spritesRequested = true;
            EXEC.execute(() -> {
                try {
                    spritePending = SkyTextures.image(SkyPainter.sprites(), 256, 64);
                }
                catch (Throwable t) {
                    NeonLyrics.LOG.warn("Partikel-Bilder fehlen", t);
                    spritesFailed = true;
                }
            });
        }
        if ((sp = spritePending) != null) {
            spritePending = null;
            try {
                class_310.method_1551().method_1531().method_4616(SPRITES, (class_1044)new class_1043(() -> "neonlyrics_sprites", sp));
                spritesReady = true;
            }
            catch (Throwable t) {
                spritesFailed = true;
            }
        }
        if ((pd = pending) == null) {
            return;
        }
        pending = null;
        if (!pd.key().equals(wantKey)) {
            return;
        }
        base = pd.base();
        fx = pd.fx();
        doneKey = pd.key();
        ++version;
        shownAt = System.currentTimeMillis();
    }

    public static boolean ready() {
        return base != null && doneKey.equals(wantKey);
    }

    public static boolean hasFx() {
        return fx != null;
    }

    public static int[] base() {
        return base;
    }

    public static int[] fx() {
        return fx;
    }

    public static int version() {
        return version;
    }

    public static boolean spritesReady() {
        return spritesReady && !spritesFailed;
    }

    public static float fadeIn() {
        return Anim.outCubic(Anim.since(shownAt, 1400L));
    }

    private static class_1011 image(int[] argb, int w, int h) throws Exception {
        BufferedImage img = new BufferedImage(w, h, 2);
        img.setRGB(0, 0, w, h, argb, 0, w);
        ByteArrayOutputStream out = new ByteArrayOutputStream(w * h);
        ImageIO.write((RenderedImage)img, "png", out);
        return class_1011.method_49277((byte[])out.toByteArray());
    }

    static {
        doneKey = "";
        version = 0;
        shownAt = 0L;
    }

    private record Pending(String key, int[] base, int[] fx) {
    }
}

