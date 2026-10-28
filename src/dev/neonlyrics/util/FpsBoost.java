/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 */
package dev.neonlyrics.util;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class FpsBoost {
    private FpsBoost() {
    }

    public static void apply(boolean on) {
        Minecraft mc = Minecraft.getInstance();
        Config c = NeonLyrics.cfg();
        try {
            if (on) {
                c.boostBefore = FpsBoost.save(mc.options.entityShadows()) + ";" + FpsBoost.save(mc.options.particles()) + ";" + FpsBoost.save(mc.options.biomeBlendRadius());
                FpsBoost.set(mc.options.entityShadows(), "false");
                FpsBoost.set(mc.options.particles(), "MINIMAL");
                FpsBoost.set(mc.options.biomeBlendRadius(), "0");
                c.quality = 0;
            } else if (c.boostBefore != null && !c.boostBefore.isEmpty()) {
                String[] v = c.boostBefore.split(";", -1);
                if (v.length >= 3) {
                    FpsBoost.set(mc.options.entityShadows(), v[0]);
                    FpsBoost.set(mc.options.particles(), v[1]);
                    FpsBoost.set(mc.options.biomeBlendRadius(), v[2]);
                }
                c.boostBefore = "";
                if (c.quality == 0) {
                    c.quality = 1;
                }
            }
            mc.options.save();
        }
        catch (Exception e) {
            NeonLyrics.LOG.warn("FPS-Boost konnte nicht alles umstellen", (Throwable)e);
        }
        c.fpsBoost = on;
        NeonLyrics.saveConfig();
    }

    private static String save(OptionInstance<?> o) {
        String string;
        Object v = o.get();
        if (v instanceof Enum) {
            Enum e = (Enum)v;
            string = e.name();
        } else {
            string = String.valueOf(v);
        }
        return string;
    }

    private static void set(OptionInstance o, String value) {
        Comparable<Boolean> nv;
        Object cur = o.get();
        if (cur instanceof Boolean) {
            nv = Boolean.parseBoolean(value);
        } else if (cur instanceof Integer) {
            nv = Integer.parseInt(value);
        } else if (cur instanceof Enum) {
            Enum e = (Enum)cur;
            nv = Enum.valueOf(e.getDeclaringClass(), value);
        } else {
            return;
        }
        o.set((Object)nv);
    }
}

