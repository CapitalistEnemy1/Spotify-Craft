/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_1060
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.hud.Anim;
import dev.neonlyrics.hud.Gfx;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class Cover {
    private static final class_2960 ID = class_2960.method_60655((String)"neonlyrics", (String)"cover");
    private static final class_2960 ID_SQ = class_2960.method_60655((String)"neonlyrics", (String)"cover_sq");
    private static final int SIZE = 64;
    private static final int SQ = 256;
    private static volatile Pending pending;
    private static volatile boolean clearPending;
    private static volatile long loadedAt;
    private static class_1043 tex;
    private static class_1043 texSq;
    private static boolean ready;
    private static long shownAt;
    private static int col1;
    private static int col2;

    private Cover() {
    }

    public static void load(byte[] imageBytes) {
        try {
            int argb;
            float a;
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (src == null) {
                return;
            }
            BufferedImage round = Cover.scale(src, 64);
            float r = 32.0f;
            for (int y = 0; y < 64; ++y) {
                for (int x = 0; x < 64; ++x) {
                    float dx = (float)x + 0.5f - r;
                    float dy = (float)y + 0.5f - r;
                    a = Math.max(0.0f, Math.min(1.0f, r - (float)Math.sqrt(dx * dx + dy * dy)));
                    argb = round.getRGB(x, y);
                    round.setRGB(x, y, Math.round((float)(argb >>> 24 & 0xFF) * a) << 24 | argb & 0xFFFFFF);
                }
            }
            BufferedImage sq = Cover.scale(src, 256);
            float cr = 11.52f;
            for (int y = 0; y < 256; ++y) {
                for (int x = 0; x < 256; ++x) {
                    a = Cover.cornerA(x, y, 256, cr);
                    if (a >= 1.0f) continue;
                    argb = sq.getRGB(x, y);
                    sq.setRGB(x, y, Math.round((float)(argb >>> 24 & 0xFF) * a) << 24 | argb & 0xFFFFFF);
                }
            }
            int c1 = Cover.avg(sq, 0, 128);
            int c2 = Cover.avg(sq, 128, 256);
            pending = new Pending(Cover.png(round), Cover.png(sq), c1, c2);
            loadedAt = System.currentTimeMillis();
        }
        catch (Throwable t) {
            NeonLyrics.LOG.debug("Cover konnte nicht geladen werden", t);
        }
    }

    private static BufferedImage scale(BufferedImage src, int size) {
        BufferedImage img = new BufferedImage(size, size, 2);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int s = Math.min(src.getWidth(), src.getHeight());
        int ox = (src.getWidth() - s) / 2;
        int oy = (src.getHeight() - s) / 2;
        g2.drawImage(src, 0, 0, size, size, ox, oy, ox + s, oy + s, null);
        g2.dispose();
        return img;
    }

    private static float cornerA(int x, int y, int n, float r) {
        float cx = (float)x + 0.5f;
        float cy = (float)y + 0.5f;
        float qx = Math.max(0.0f, Math.max(r - cx, cx - ((float)n - r)));
        float qy = Math.max(0.0f, Math.max(r - cy, cy - ((float)n - r)));
        if (qx == 0.0f || qy == 0.0f) {
            return 1.0f;
        }
        float d = (float)Math.sqrt(qx * qx + qy * qy);
        return Math.max(0.0f, Math.min(1.0f, r - d + 0.5f));
    }

    private static int avg(BufferedImage img, int y0, int y1) {
        long r = 0L;
        long g = 0L;
        long b = 0L;
        long n = 0L;
        for (int y = y0; y < y1; y += 4) {
            for (int x = 0; x < img.getWidth(); x += 4) {
                int c = img.getRGB(x, y);
                r += (long)(c >> 16 & 0xFF);
                g += (long)(c >> 8 & 0xFF);
                b += (long)(c & 0xFF);
                ++n;
            }
        }
        if (n == 0L) {
            return 0x202024;
        }
        return (int)(r / n) << 16 | (int)(g / n) << 8 | (int)(b / n);
    }

    private static byte[] png(BufferedImage img) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage)img, "png", out);
        return out.toByteArray();
    }

    public static void clear() {
        clearPending = true;
    }

    public static long loadedAt() {
        return loadedAt;
    }

    private static void sync() {
        Pending p;
        if (clearPending) {
            clearPending = false;
            ready = false;
            col2 = -1;
            col1 = -1;
        }
        if ((p = pending) == null) {
            return;
        }
        pending = null;
        try {
            class_1011 a = class_1011.method_49277((byte[])p.round());
            class_1011 b = class_1011.method_49277((byte[])p.square());
            class_1060 tm = class_310.method_1551().method_1531();
            if (tex == null) {
                tex = new class_1043(() -> "neonlyrics_cover", a);
                tm.method_4616(ID, (class_1044)tex);
            } else {
                tex.method_4526(a);
                tex.method_4524();
            }
            if (texSq == null) {
                texSq = new class_1043(() -> "neonlyrics_cover_sq", b);
                tm.method_4616(ID_SQ, (class_1044)texSq);
            } else {
                texSq.method_4526(b);
                texSq.method_4524();
            }
            col1 = p.c1();
            col2 = p.c2();
            ready = true;
            shownAt = System.currentTimeMillis();
        }
        catch (Throwable t) {
            ready = false;
        }
    }

    public static boolean draw(class_332 g, int x, int y, int size, float alpha) {
        if (!NeonLyrics.cfg().albumCover) {
            return false;
        }
        try {
            Cover.sync();
            if (!ready) {
                return false;
            }
            float in = Anim.outCubic(Anim.since(shownAt, 350L));
            g.method_25291(class_10799.field_56883, ID, x, y, 0.0f, 0.0f, size, size, size, size, Gfx.argb(0xFFFFFF, alpha * in));
            return in > 0.5f;
        }
        catch (Throwable t) {
            ready = false;
            return false;
        }
    }

    public static boolean drawSquare(class_332 g, int x, int y, int size, float alpha) {
        try {
            Cover.sync();
            if (!ready) {
                return false;
            }
            float in = Anim.outCubic(Anim.since(shownAt, 450L));
            g.method_25291(class_10799.field_56883, ID_SQ, x, y, 0.0f, 0.0f, size, size, size, size, Gfx.argb(0xFFFFFF, alpha * in));
            return true;
        }
        catch (Throwable t) {
            ready = false;
            return false;
        }
    }

    public static int[] colors() {
        int[] nArray;
        Cover.sync();
        if (ready && col1 >= 0) {
            int[] nArray2 = new int[2];
            nArray2[0] = col1;
            nArray = nArray2;
            nArray2[1] = col2;
        } else {
            nArray = null;
        }
        return nArray;
    }

    public static float fade() {
        return ready ? Anim.outCubic(Anim.since(shownAt, 600L)) : 0.0f;
    }

    static {
        loadedAt = 0L;
        col1 = -1;
        col2 = -1;
    }

    private record Pending(byte[] round, byte[] square, int c1, int c2) {
    }
}

