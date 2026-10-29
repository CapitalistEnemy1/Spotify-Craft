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
package dev.neonlyrics.hud;

import dev.neonlyrics.NeonLyrics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class Shapes {
    public static final int MAX_R = 512;
    private static final Map<Integer, class_2960> CIRCLES = new HashMap<Integer, class_2960>();
    private static boolean failed = false;

    private Shapes() {
    }

    public static boolean usable() {
        return !failed;
    }

    public static void fail() {
        failed = true;
    }

    public static class_2960 circle(int r) {
        class_2960 id;
        if (failed || r < 1) {
            return null;
        }
        if (r > 96) {
            r = Math.min(512, (r + 4) / 8 * 8);
        }
        if ((id = CIRCLES.get(r)) != null) {
            return id;
        }
        try {
            int n = r * 2;
            BufferedImage img = new BufferedImage(n, n, 2);
            for (int y = 0; y < n; ++y) {
                for (int x = 0; x < n; ++x) {
                    int hit = 0;
                    for (int sy = 0; sy < 4; ++sy) {
                        for (int sx = 0; sx < 4; ++sx) {
                            float dx = (float)x + ((float)sx + 0.5f) / 4.0f - (float)r;
                            float dy = (float)y + ((float)sy + 0.5f) / 4.0f - (float)r;
                            if (!(dx * dx + dy * dy <= (float)r * (float)r)) continue;
                            ++hit;
                        }
                    }
                    int a = Math.round((float)hit / 16.0f * 255.0f);
                    img.setRGB(x, y, a << 24 | 0xFFFFFF);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)img, "png", out);
            class_1011 ni = class_1011.method_49277((byte[])out.toByteArray());
            id = class_2960.method_60655((String)"neonlyrics", (String)("shape/c" + r));
            class_1043 tex = new class_1043(() -> "neonlyrics_circle", ni);
            class_310.method_1551().method_1531().method_4616(id, (class_1044)tex);
            CIRCLES.put(r, id);
            return id;
        }
        catch (Throwable t) {
            NeonLyrics.LOG.warn("Formen als Textur gehen nicht, nutze einfache Variante", t);
            failed = true;
            return null;
        }
    }
}

