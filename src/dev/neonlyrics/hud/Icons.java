/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.hud.Gfx;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Icons {
    private static final int U = 4;
    private static GuiGraphicsExtractor G;
    private static int C;
    private static float A;

    private Icons() {
    }

    public static void draw(GuiGraphicsExtractor g, Icon icon, float cx, float cy, float size, int rgb, float a) {
        if (a * Gfx.alpha <= 0.02f) {
            return;
        }
        G = g;
        C = rgb;
        A = a;
        g.pose().pushMatrix();
        g.pose().translate(cx - size / 2.0f, cy - size / 2.0f);
        float s = size / 16.0f / 4.0f;
        g.pose().scale(s, s);
        switch (icon.ordinal()) {
            case 0: {
                Icons.line(2.0f, 8.0f, 8.0f, 2.5f);
                Icons.line(8.0f, 2.5f, 14.0f, 8.0f);
                Icons.line(3.8f, 7.0f, 3.8f, 13.5f);
                Icons.line(12.2f, 7.0f, 12.2f, 13.5f);
                Icons.line(3.8f, 13.5f, 12.2f, 13.5f);
                break;
            }
            case 1: {
                Icons.line(6.0f, 12.0f, 6.0f, 3.2f);
                Icons.line(13.0f, 10.5f, 13.0f, 2.0f);
                Icons.line(6.0f, 3.2f, 13.0f, 2.0f, 2.2f);
                Icons.dot(4.3f, 12.3f, 2.0f);
                Icons.dot(11.3f, 10.8f, 2.0f);
                break;
            }
            case 2: {
                Icons.line(2.5f, 4.0f, 13.5f, 4.0f, 1.7f);
                Icons.line(2.5f, 8.0f, 11.0f, 8.0f, 1.7f);
                Icons.line(2.5f, 12.0f, 8.5f, 12.0f, 1.7f);
                break;
            }
            case 3: {
                Icons.ring(7.0f, 8.5f, 4.6f);
                Icons.dot(12.5f, 3.5f, 1.1f);
                Icons.dot(14.0f, 7.0f, 0.8f);
                Icons.dot(10.5f, 1.8f, 0.7f);
                break;
            }
            case 4: {
                Icons.arc(8.0f, 13.0f, 9.0f, 1.5f);
                Icons.arc(8.0f, 13.0f, 6.0f, 1.5f);
                Icons.arc(8.0f, 13.0f, 3.0f, 1.6f);
                break;
            }
            case 5: {
                Icons.box(2.0f, 3.0f, 14.0f, 11.5f);
                Icons.line(6.0f, 14.0f, 10.0f, 14.0f);
                Icons.line(8.0f, 11.5f, 8.0f, 14.0f);
                break;
            }
            case 6: {
                Icons.line(9.5f, 1.8f, 4.5f, 9.0f);
                Icons.line(4.5f, 9.0f, 11.5f, 7.0f);
                Icons.line(11.5f, 7.0f, 6.5f, 14.2f);
                break;
            }
            case 7: {
                Icons.line(4.0f, 2.0f, 12.0f, 2.0f);
                Icons.line(4.0f, 2.0f, 4.0f, 14.0f);
                Icons.line(12.0f, 2.0f, 12.0f, 14.0f);
                Icons.line(4.0f, 14.0f, 8.0f, 10.5f);
                Icons.line(12.0f, 14.0f, 8.0f, 10.5f);
                break;
            }
            case 8: {
                Icons.line(2.0f, 4.5f, 14.0f, 4.5f);
                Icons.line(2.0f, 8.0f, 14.0f, 8.0f);
                Icons.line(2.0f, 11.5f, 14.0f, 11.5f);
                Icons.dot(5.0f, 4.5f, 1.6f);
                Icons.dot(11.0f, 8.0f, 1.6f);
                Icons.dot(7.0f, 11.5f, 1.6f);
                break;
            }
            case 9: {
                Icons.tri(4.5f, 2.5f, 13.0f, 8.0f, 4.5f, 13.5f);
                break;
            }
            case 10: {
                Icons.fill(4.0f, 3.0f, 6.6f, 13.0f);
                Icons.fill(9.4f, 3.0f, 12.0f, 13.0f);
                break;
            }
            case 11: {
                Icons.tri(3.0f, 3.0f, 10.5f, 8.0f, 3.0f, 13.0f);
                Icons.fill(11.0f, 3.0f, 13.0f, 13.0f);
                break;
            }
            case 12: {
                Icons.tri(13.0f, 3.0f, 5.5f, 8.0f, 13.0f, 13.0f);
                Icons.fill(3.0f, 3.0f, 5.0f, 13.0f);
                break;
            }
            case 13: {
                Icons.line(4.0f, 4.0f, 12.0f, 12.0f, 1.7f);
                Icons.line(12.0f, 4.0f, 4.0f, 12.0f, 1.7f);
                break;
            }
            case 14: {
                Icons.line(10.0f, 3.0f, 5.0f, 8.0f, 1.8f);
                Icons.line(5.0f, 8.0f, 10.0f, 13.0f, 1.8f);
                break;
            }
            case 15: {
                Icons.line(6.0f, 3.0f, 11.0f, 8.0f, 1.8f);
                Icons.line(11.0f, 8.0f, 6.0f, 13.0f, 1.8f);
                break;
            }
            case 16: {
                Icons.line(3.0f, 8.5f, 6.5f, 12.0f, 2.0f);
                Icons.line(6.5f, 12.0f, 13.0f, 4.5f, 2.0f);
                break;
            }
            case 18: {
                Icons.line(1.5f, 11.5f, 14.5f, 11.5f, 1.8f);
                Icons.line(4.5f, 11.5f, 5.2f, 4.5f);
                Icons.line(11.5f, 11.5f, 10.8f, 4.5f);
                Icons.line(5.2f, 4.5f, 10.8f, 4.5f);
                Icons.line(4.9f, 9.0f, 11.1f, 9.0f, 1.2f);
                break;
            }
            case 17: {
                Icons.arc(8.0f, 8.0f, 5.5f, 1.6f);
                Icons.line(13.5f, 8.0f, 11.6f, 6.0f, 1.6f);
                Icons.line(13.5f, 8.0f, 15.2f, 6.0f, 1.6f);
            }
        }
        g.pose().popMatrix();
    }

    private static void line(float x0, float y0, float x1, float y1) {
        Icons.line(x0, y0, x1, y1, 1.5f);
    }

    private static void line(float x0, float y0, float x1, float y1, float w) {
        float dx = (x1 - x0) * 4.0f;
        float dy = (y1 - y0) * 4.0f;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        int hw = Math.max(1, Math.round(w * 4.0f / 2.0f));
        G.pose().pushMatrix();
        G.pose().translate(x0 * 4.0f, y0 * 4.0f);
        G.pose().rotate((float)Math.atan2(dy, dx));
        G.fill(-hw + 1, -hw, Math.round(len) + hw - 1, hw, Gfx.argb(C, A));
        G.pose().popMatrix();
    }

    private static void box(float x0, float y0, float x1, float y1) {
        Icons.line(x0, y0, x1, y0);
        Icons.line(x1, y0, x1, y1);
        Icons.line(x1, y1, x0, y1);
        Icons.line(x0, y1, x0, y0);
    }

    private static void fill(float x0, float y0, float x1, float y1) {
        Gfx.round(G, Math.round(x0 * 4.0f), Math.round(y0 * 4.0f), Math.round((x1 - x0) * 4.0f), Math.round((y1 - y0) * 4.0f), 2, C, A);
    }

    private static void tri(float ax, float ay, float bx, float by, float cx, float cy) {
        float minY = Math.min(ay, Math.min(by, cy));
        float maxY = Math.max(ay, Math.max(by, cy));
        int col = Gfx.argb(C, A);
        for (int yy = Math.round(minY * 4.0f); yy < Math.round(maxY * 4.0f); ++yy) {
            float[][] e;
            float y = ((float)yy + 0.5f) / 4.0f;
            float lo = Float.MAX_VALUE;
            float hi = -3.4028235E38f;
            for (float[] s : e = new float[][]{{ax, ay, bx, by}, {bx, by, cx, cy}, {cx, cy, ax, ay}}) {
                if (!(y >= s[1] && y <= s[3]) && (!(y >= s[3]) || !(y <= s[1]))) continue;
                float t = s[3] == s[1] ? 0.0f : (y - s[1]) / (s[3] - s[1]);
                float x = s[0] + (s[2] - s[0]) * t;
                lo = Math.min(lo, x);
                hi = Math.max(hi, x);
            }
            if (!(hi > lo)) continue;
            G.fill(Math.round(lo * 4.0f), yy, Math.round(hi * 4.0f), yy + 1, col);
        }
    }

    private static void dot(float x, float y, float r) {
        Gfx.circle(G, x * 4.0f, y * 4.0f, r * 4.0f, C, A);
    }

    private static void ring(float x, float y, float r) {
        int n = 14;
        for (int i = 0; i < n; ++i) {
            double a0 = (double)i * Math.PI * 2.0 / (double)n;
            double a1 = (double)(i + 1) * Math.PI * 2.0 / (double)n;
            Icons.line(x + (float)Math.cos(a0) * r, y + (float)Math.sin(a0) * r, x + (float)Math.cos(a1) * r, y + (float)Math.sin(a1) * r);
        }
    }

    private static void arc(float x, float y, float r, float w) {
        int n = 8;
        for (int i = 0; i < n; ++i) {
            double a0 = 3.6128315516282616 + (double)i * Math.PI * 0.7 / (double)n;
            double a1 = 3.6128315516282616 + (double)(i + 1) * Math.PI * 0.7 / (double)n;
            Icons.line(x + (float)Math.cos(a0) * r, y + (float)Math.sin(a0) * r, x + (float)Math.cos(a1) * r, y + (float)Math.sin(a1) * r, w);
        }
    }

    public static enum Icon {
        HOME,
        LYRICS,
        TEXT,
        SKY,
        SPOTIFY,
        DISPLAY,
        PERF,
        FAV,
        SETTINGS,
        PLAY,
        PAUSE,
        NEXT,
        PREV,
        CLOSE,
        BACK,
        CHEVRON,
        CHECK,
        SYNC,
        HAT;

    }
}

