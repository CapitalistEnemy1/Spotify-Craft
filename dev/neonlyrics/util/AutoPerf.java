/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package dev.neonlyrics.util;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import net.minecraft.class_310;

public final class AutoPerf {
    public static final int MAX = 6;
    private static int level = 0;
    private static int lastFps = 0;
    private static int lowTicks = 0;
    private static int okTicks = 0;
    private static int relaxTicks = 0;
    private static long lastCheck = 0L;
    private static int badLevel = -1;
    private static long badUntil = 0L;
    private static long raisedAt = 0L;
    private static double modMs = 0.0;
    private static long frameStart = 0L;
    private static double frameAcc = 0.0;

    private AutoPerf() {
    }

    public static int level() {
        Config c = NeonLyrics.cfg();
        return c.autoPerf ? level : 0;
    }

    public static int fps() {
        return lastFps;
    }

    public static double modMs() {
        return modMs;
    }

    public static int quality() {
        int q = Math.max(0, Math.min(2, NeonLyrics.cfg().quality));
        int lv = AutoPerf.level();
        if (lv >= 2) {
            return 0;
        }
        if (lv >= 1) {
            return Math.min(q, 1);
        }
        return q;
    }

    public static boolean allow(int cut) {
        return AutoPerf.level() < cut;
    }

    public static int target() {
        Config c = NeonLyrics.cfg();
        int t = c.targetFps <= 0 ? 60 : c.targetFps;
        try {
            int lim;
            class_310 mc = class_310.method_1551();
            if (((Boolean)mc.field_1690.method_42433().method_41753()).booleanValue()) {
                t = Math.min(t, 57);
            }
            if ((lim = ((Integer)mc.field_1690.method_42524().method_41753()).intValue()) > 0 && lim < 260) {
                t = Math.min(t, lim - 3);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return Math.max(20, t);
    }

    public static void begin() {
        frameStart = System.nanoTime();
    }

    public static void end() {
        if (frameStart != 0L) {
            frameAcc += (double)(System.nanoTime() - frameStart) / 1000000.0;
        }
        frameStart = 0L;
    }

    public static void frame() {
        modMs = modMs * 0.95 + frameAcc * 0.05;
        frameAcc = 0.0;
    }

    public static void tick(class_310 mc) {
        boolean ok;
        boolean modMatters;
        long now = System.currentTimeMillis();
        if (now - lastCheck < 500L) {
            return;
        }
        lastCheck = now;
        Config c = NeonLyrics.cfg();
        lastFps = mc.method_47599();
        if (!c.autoPerf || mc.field_1687 == null || mc.field_1724 == null || mc.field_1755 != null) {
            okTicks = 0;
            lowTicks = 0;
            return;
        }
        int t = AutoPerf.target();
        if ((double)lastFps < (double)t * 0.92) {
            ++lowTicks;
            okTicks = 0;
        } else {
            ++okTicks;
            lowTicks = 0;
        }
        double frameMs = 1000.0 / (double)Math.max(1, lastFps);
        boolean bl = modMatters = modMs > frameMs * 0.1 || modMs > 2.5;
        if (!modMatters) {
            lowTicks = 0;
            if (level > 0 && ++relaxTicks >= 6 && (level - 1 > badLevel || now >= badUntil)) {
                --level;
                raisedAt = now;
                relaxTicks = 0;
            }
            return;
        }
        relaxTicks = 0;
        if (lowTicks >= 2 && level < 6 && modMatters) {
            if (now - raisedAt < 6000L) {
                badLevel = level;
                badUntil = now + 120000L;
            }
            level = Math.min(6, level + ((double)lastFps < (double)t * 0.6 ? 2 : 1));
            lowTicks = 0;
            okTicks = 0;
            return;
        }
        boolean plenty = (double)lastFps >= (double)t * 1.25;
        boolean bl2 = ok = (double)lastFps >= (double)t * 0.98;
        if (level > 0 && (plenty && okTicks >= 8 || ok && okTicks >= 40)) {
            int next = level - 1;
            if (next <= badLevel && now < badUntil) {
                return;
            }
            level = next;
            raisedAt = now;
            okTicks = 0;
        }
    }

    public static void reset() {
        level = 0;
        okTicks = 0;
        lowTicks = 0;
        badLevel = -1;
    }
}

