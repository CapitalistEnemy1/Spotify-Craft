/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_12249
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.neonlyrics.cosmetic;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.cosmetic.Accessories;
import dev.neonlyrics.cosmetic.DeathFx;
import dev.neonlyrics.sky.SkyTextures;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_12249;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

final class DeathCards {
    static final float T_END = 4.2f;
    static final float[] TT = new float[9001];
    private static final int N = 8;
    private static final float DASH0 = 1.15f;
    private static final float GAP = 0.1f;
    private static final float DUR = 0.14f;
    private static final float TS = 2.05f;
    private static final float FIN0 = 2.25f;
    private static final float FIN1 = 2.75f;
    private static final float DY = 2.45f;
    private static final int[] SLASH_PX;
    private static final int[][] SLICES;
    private static final float[][] PARTS;
    private static final float[] A;
    private static final float[] B;
    private static final float[] S0;
    private static final float[][] LAND;
    private static final float[] C;
    private static final float[] C2;
    private static final float CW = 0.21f;
    private static final float CH = 0.29f;
    private static final float CT = 0.006f;
    static final class_2960 CARDS;
    private static final int AW = 512;
    private static final int AH = 256;
    private static final int TW = 80;
    private static final int TH = 112;
    private static boolean cardsReady;
    private static boolean cardsFailed;

    private DeathCards() {
    }

    static Object create(DeathFx.Fx f, Random r) {
        int i;
        float sp;
        double a;
        int i2;
        State s = new State();
        s.face[0] = 1;
        s.face[1] = 2;
        for (i2 = 2; i2 < 8; ++i2) {
            s.face[i2] = 3 + r.nextInt(4);
        }
        for (int j = 0; j < SLICES.length; ++j) {
            a = r.nextDouble() * Math.PI * 2.0;
            sp = j == 0 ? 0.3f + r.nextFloat() * 0.3f : 0.9f + r.nextFloat() * 1.3f;
            float ax = r.nextFloat() - 0.5f;
            float ay = r.nextFloat() - 0.5f;
            float az = r.nextFloat() - 0.5f;
            float al = (float)Math.sqrt(ax * ax + ay * ay + az * az) + 1.0E-4f;
            s.pieces[j] = new float[]{(float)Math.cos(a) * sp, j == 0 ? 0.4f : 1.4f + (float)j * 0.45f + r.nextFloat() * 0.8f, (float)Math.sin(a) * sp, ax / al, ay / al, az / al, j == 0 ? 2.5f : 5.0f + r.nextFloat() * 5.0f};
        }
        for (i2 = 0; i2 < s.hits.length; ++i2) {
            s.hits[i2] = new float[]{(float)r.nextGaussian() * 0.5f, 0.4f + r.nextFloat() * 1.2f, (float)r.nextGaussian() * 0.5f, 1.8f + r.nextFloat() * 2.4f, 0.04f + r.nextFloat() * 0.06f, r.nextFloat() < 0.4f ? 1.0f : 0.0f};
        }
        for (i2 = 0; i2 < s.burst.length; ++i2) {
            a = r.nextDouble() * Math.PI * 2.0;
            sp = 1.5f + r.nextFloat() * 3.0f;
            s.burst[i2] = new float[]{(float)Math.cos(a) * sp, 1.0f + r.nextFloat() * 3.5f, (float)Math.sin(a) * sp, 0.04f + r.nextFloat() * 0.08f, r.nextFloat() * 14.0f, r.nextFloat() < 0.35f ? 1.0f : 0.0f};
        }
        int[] chipCols = new int[]{13111342, 0x151515, 0xF2F2F2, 1986504, 2067002};
        for (i = 0; i < s.chips.length; ++i) {
            double a2 = r.nextDouble() * Math.PI * 2.0;
            float sp2 = 1.6f + r.nextFloat() * 2.4f;
            s.chips[i] = new float[]{(float)Math.cos(a2) * sp2, 2.5f + r.nextFloat() * 3.0f, (float)Math.sin(a2) * sp2, 6.0f + r.nextFloat() * 14.0f, Float.intBitsToFloat(chipCols[r.nextInt(chipCols.length)])};
        }
        for (i = 0; i < s.sparks.length; ++i) {
            s.sparks[i] = new float[]{i < 12 ? 0.1f + r.nextFloat() * 0.15f : 2.75f + r.nextFloat() * 0.1f, r.nextFloat() * 6.283f, 0.6f + r.nextFloat() * 1.8f, 0.3f + r.nextFloat() * 1.5f, 0.05f + r.nextFloat() * 0.08f};
        }
        for (i = 0; i < s.embers.length; ++i) {
            s.embers[i] = new float[]{2.95f + r.nextFloat() * 0.5f, (r.nextFloat() - 0.5f) * 0.4f, (r.nextFloat() - 0.5f) * 0.4f, 0.6f + r.nextFloat() * 1.2f};
        }
        return s;
    }

    private static float omega(float t) {
        float k = Math.max(0.0f, t - 0.6f);
        return 1.3f * t + 6.0f * k * k;
    }

    private static void orbit(int i, float t, boolean flipped, float[] o) {
        double ang = (double)i * Math.PI * 2.0 / 8.0 + (double)DeathCards.omega(t) + (flipped ? Math.PI : 0.0);
        float r = 1.6f - 0.15f * DeathFx.sm(0.9f, 1.2f, t);
        o[0] = (float)Math.cos(ang) * r;
        o[1] = 1.05f + 0.2f * (float)Math.sin(ang * 2.0 + (double)(t * 3.0f));
        o[2] = (float)Math.sin(ang) * r;
    }

    private static float appear(int i) {
        return 0.2f + (float)i * 0.04f;
    }

    private static int flyPos(int i, float t, float[] o) {
        float ts = 1.15f + (float)i * 0.1f;
        if (t < DeathCards.appear(i)) {
            o[0] = 0.0f;
            o[1] = 2.45f + (float)i * 0.012f;
            o[2] = 0.0f;
            return 0;
        }
        if (t < ts) {
            DeathCards.orbit(i, t, false, o);
            float k = DeathFx.eo((t - DeathCards.appear(i)) / 0.45f);
            o[0] = DeathFx.lerp(0.0f, o[0], k);
            o[1] = DeathFx.lerp(2.45f + (float)i * 0.012f, o[1], k);
            o[2] = DeathFx.lerp(0.0f, o[2], k);
            return 1;
        }
        DeathCards.orbit(i, ts, false, A);
        float cy = (float)SLASH_PX[i] * 0.05859375f;
        if (t < ts + 0.14f) {
            float k = (t - ts) / 0.14f * 2.0f;
            o[0] = A[0] + (0.0f - A[0]) * k;
            o[1] = A[1] + (cy - A[1]) * k;
            o[2] = A[2] + (0.0f - A[2]) * k;
            return 2;
        }
        float px = -A[0];
        float py = 2.0f * cy - A[1];
        float pz = -A[2];
        DeathCards.orbit(i, t, true, B);
        float k = DeathFx.eo((t - ts - 0.14f) / 0.3f);
        o[0] = DeathFx.lerp(px, B[0], k);
        o[1] = DeathFx.lerp(py, B[1], k);
        o[2] = DeathFx.lerp(pz, B[2], k);
        return 1;
    }

    private static boolean card(int i, float t, float[] o) {
        if (t < 2.25f) {
            int st = DeathCards.flyPos(i, t, o);
            o[3] = st == 0 ? 0.3f * (float)i : t * (st == 2 ? 45.0f : 28.0f) + (float)i * 1.3f;
            o[4] = st == 0 ? 0.0f : 0.25f * (float)Math.sin((float)i * 1.7f + t * 2.0f);
            o[5] = st == 0 ? DeathFx.sm(0.08f, 0.22f, t) : 1.0f;
            return true;
        }
        DeathCards.flyPos(i, 2.249f, S0);
        float a = t - 2.25f;
        if (i < 2) {
            float[] L = LAND[i];
            float k = DeathFx.cl(a / 0.5f);
            float kx = DeathFx.sm(0.0f, 1.0f, k);
            o[0] = DeathFx.lerp(S0[0], L[0], kx);
            o[2] = DeathFx.lerp(S0[2], L[2], kx);
            o[1] = DeathFx.lerp(S0[1], L[1], k * k) + (float)Math.sin(Math.PI * (double)k) * 0.7f * (1.0f - k * 0.3f);
            o[3] = L[3] + (1.0f - k) * (1.0f - k) * 14.0f;
            o[4] = 0.0f;
            o[5] = 1.0f - DeathFx.sm(3.7f, 4.1f, t);
            return o[5] > 0.01f;
        }
        float l = (float)Math.sqrt(S0[0] * S0[0] + S0[2] * S0[2]) + 1.0E-4f;
        float vx = S0[0] / l * 3.2f;
        float vz = S0[2] / l * 3.2f;
        float vy = 2.2f + (float)i * 0.15f;
        float land = (vy + (float)Math.sqrt(vy * vy + 18.0f * Math.max(0.0f, S0[1] - 0.012f))) / 9.0f;
        float aa = Math.min(a, land);
        o[0] = S0[0] + vx * aa;
        o[2] = S0[2] + vz * aa;
        o[1] = Math.max(0.012f + (float)i * 0.002f, S0[1] + vy * aa - 4.5f * aa * aa);
        o[3] = 63.0f + (float)i * 1.3f + aa * 20.0f;
        o[4] = a < land ? (float)Math.sin(a * 9.0f + (float)i) * 0.6f : 0.0f;
        o[5] = 1.0f - DeathFx.sm(3.2f, 3.7f, t);
        return o[5] > 0.01f;
    }

    static void draw(class_310 mc, class_4597 buffers, Matrix4f base, class_243 cam, DeathFx.Fx f, float t) {
        float as;
        float ar;
        float a;
        float dz;
        float dx;
        int i;
        State s;
        block38: {
            block37: {
                Object object = f.extra;
                if (!(object instanceof State)) break block37;
                s = (State)object;
                if (Accessories.whiteTex() && DeathCards.cardsTex()) break block38;
            }
            return;
        }
        Matrix4f m = new Matrix4f((Matrix4fc)base).translate((float)(f.ox - cam.field_1352), (float)(f.oy - cam.field_1351), (float)(f.oz - cam.field_1350)).rotateY(-f.yaw);
        int light = DeathFx.light(mc, f.ox, f.oy + 1.0, f.oz);
        boolean firstPerson = f.self && mc.field_1690.method_31044().method_31034();
        class_4588 g = buffers.method_73477(class_12249.method_76023());
        for (i = 0; i < 8; ++i) {
            if (t < DeathCards.appear(i) + 0.05f || t > 2.75f) continue;
            boolean dash = t >= 1.15f + (float)i * 0.1f && t < 1.15f + (float)i * 0.1f + 0.14f;
            int col = dash ? 16734780 : 16765802;
            DeathCards.card(i, t, C);
            for (int k = 1; k <= 6; ++k) {
                DeathCards.card(i, t - (float)k * 0.014f, C2);
                dx = C2[0] - C[0];
                dz = C2[2] - C[2];
                float speed = (float)Math.sqrt(dx * dx + dz * dz) / 0.014f;
                a = (dash ? 0.75f : 0.4f) * (1.0f - (float)k / 7.0f) * DeathFx.sm(2.0f, 7.0f, speed);
                float w0 = 0.2f * (1.0f - (float)(k - 1) / 7.0f);
                float w1 = 0.2f * (1.0f - (float)k / 7.0f);
                DeathCards.ribbon(g, m, C[0], C[1], C[2], C2[0], C2[1], C2[2], w0, w1, col, a, a * 0.6f);
                System.arraycopy(C2, 0, C, 0, 3);
            }
        }
        if (t < 2.2f && !firstPerson) {
            for (i = 0; i < 8; ++i) {
                float tc = 1.15f + (float)i * 0.1f + 0.07f;
                float age = t - tc;
                if (age < 0.0f || age > 0.6f) continue;
                DeathCards.orbit(i, 1.15f + (float)i * 0.1f, false, A);
                float l = (float)Math.sqrt(A[0] * A[0] + A[2] * A[2]) + 1.0E-4f;
                dx = -A[0] / l;
                dz = -A[2] / l;
                float cy = (float)SLASH_PX[i] * 0.05859375f;
                a = (1.0f - age / 0.6f) * (t > 2.05f ? 1.0f - DeathFx.sm(2.05f, 2.2f, t) : 1.0f);
                float len = 0.42f;
                float w = 0.045f * (1.0f - age);
                DeathCards.cross(g, m, -dx * len, cy, -dz * len, dx * len, cy, dz * len, w * 3.5f, 16719936, a * 0.35f);
                DeathCards.cross(g, m, -dx * len, cy, -dz * len, dx * len, cy, dz * len, w, 16773320, a);
            }
        }
        if ((ar = (t - 2.75f) / 0.6f) > 0.0f && ar < 1.0f) {
            float rr = 0.2f + DeathFx.eo(ar) * 1.6f;
            DeathCards.ringFlat(g, m, 0.0f, 0.03f, 0.0f, rr * 0.85f, rr, 16765802, 0.9f * (1.0f - ar));
        }
        if (t > 2.7f) {
            float a2 = DeathFx.sm(2.7f, 2.85f, t) * (1.0f - DeathFx.sm(3.7f, 4.1f, t)) * (0.32f + 0.06f * (float)Math.sin(t * 6.0f));
            DeathFx.disc(g, m, 0.0f, 0.015f, 0.0f, 0.0f, 0.75f, 16762960, a2, 16762960, 0.0f, 28);
        }
        if ((as = (t - 2.05f) / 0.35f) > 0.0f && as < 1.0f) {
            DeathCards.ringFlat(g, m, 0.0f, 1.1f, 0.0f, 0.2f + DeathFx.eo(as) * 1.6f, 0.3f + DeathFx.eo(as) * 1.75f, 16724048, 0.85f * (1.0f - as));
        }
        class_4588 w = buffers.method_73477(class_12249.method_75994((class_2960)Accessories.WHITE));
        if (t >= 2.05f && !firstPerson) {
            for (int j = 0; j < SLICES.length; ++j) {
                Matrix4f pm = DeathCards.pieceMatrix(m, s, j, t);
                if (pm == null) continue;
                float glow = 0.75f + 0.25f * (float)Math.sin(t * 14.0f + (float)j);
                int capCol = DeathFx.mix(7997206, 16723274, glow);
                for (float[] p : PARTS) {
                    DeathCards.caps(w, pm, p, SLICES[j][0], SLICES[j][1], capCol);
                }
            }
        }
        for (int i2 = 0; i2 < 8; ++i2) {
            float tc = 1.15f + (float)i2 * 0.1f + 0.07f;
            float a0 = t - tc;
            if (a0 <= 0.0f || a0 > 0.8f || firstPerson) continue;
            DeathCards.orbit(i2, 1.15f + (float)i2 * 0.1f, false, A);
            float l = (float)Math.sqrt(A[0] * A[0] + A[2] * A[2]) + 1.0E-4f;
            float dx2 = -A[0] / l;
            float dz2 = -A[2] / l;
            float cy = (float)SLASH_PX[i2] * 0.05859375f;
            for (int k = 0; k < 7; ++k) {
                float[] h = s.hits[i2 * 7 + k];
                float vx = dx2 * h[3] + h[0];
                float vy = h[1];
                float vz = dz2 * h[3] + h[2];
                float x = vx * a0;
                float y = cy + vy * a0 - 6.0f * a0 * a0;
                float z = vz * a0;
                if (y < 0.0f) continue;
                float sz = h[4] * (1.0f - a0 / 0.8f * 0.5f);
                boolean hot = h[5] > 0.5f;
                DeathFx.cube(w, new Matrix4f((Matrix4fc)m).translate(x, y, z).rotateXYZ(a0 * 9.0f, a0 * 7.0f, 0.0f), -sz / 2.0f, -sz / 2.0f, -sz / 2.0f, sz / 2.0f, sz / 2.0f, sz / 2.0f, hot ? -54715 : -12974576, hot ? 0xF000F0 : light);
            }
        }
        float ab = t - 2.05f;
        if (ab > 0.0f && ab < 1.6f) {
            for (float[] b : s.burst) {
                float x = b[0] * ab;
                float y = 1.1f + b[1] * ab - 6.0f * ab * ab;
                float z = b[2] * ab;
                if (y < 0.0f) continue;
                float sz = b[3] * (1.0f - ab / 1.6f * 0.5f);
                boolean hot = b[5] > 0.5f;
                DeathFx.cube(w, new Matrix4f((Matrix4fc)m).translate(x, y, z).rotateXYZ(b[4] * ab, b[4] * ab * 0.6f, 0.0f), -sz / 2.0f, -sz / 2.0f, -sz / 2.0f, sz / 2.0f, sz / 2.0f, sz / 2.0f, hot ? -54715 : -12974576, hot ? 0xF000F0 : light);
            }
        }
        if (ab > 0.0f && t < 3.8f) {
            for (float[] c : s.chips) {
                float vy = c[1];
                float land = (vy + (float)Math.sqrt(vy * vy + 19.44f)) / 9.0f;
                float aa = Math.min(ab, land);
                float x = c[0] * aa;
                float z = c[2] * aa;
                float y = Math.max(0.02f, 1.1f + vy * aa - 4.5f * aa * aa);
                float sc = 1.0f - DeathFx.sm(3.3f, 3.8f, t);
                Matrix4f cm = new Matrix4f((Matrix4fc)m).translate(x, y, z).rotateXYZ(ab < land ? c[3] * aa : 0.0f, c[3] * aa * 0.5f, ab < land ? c[3] * aa * 0.3f : 0.0f).scale(sc);
                DeathCards.chip(w, cm, Float.floatToRawIntBits(c[4]) | 0xFF000000, light);
            }
        }
        class_4588 cv = buffers.method_73477(class_12249.method_75994((class_2960)CARDS));
        for (int i3 = 0; i3 < 8; ++i3) {
            if (!DeathCards.card(i3, t, C) || C[5] <= 0.01f) continue;
            Matrix4f cm = new Matrix4f((Matrix4fc)m).translate(C[0], C[1], C[2]).rotateY(C[3]).rotateX(C[4]).scale(C[5]);
            DeathCards.cardQuad(cv, cm, s.face[i3]);
        }
        if (!firstPerson && f.skin != null) {
            class_4588 sk = buffers.method_73477(class_12249.method_75994((class_2960)f.skin));
            int bl = DeathFx.light(mc, f.ox, f.oy + 1.2, f.oz);
            if (t < 2.05f) {
                float e = (float)(0.8 * Math.exp(-t * 10.0f));
                float ry = 0.0f;
                float rx = -0.12f * (float)Math.sin((double)DeathFx.cl(t / 0.35f) * Math.PI);
                for (int i4 = 0; i4 < 8; ++i4) {
                    float tc = 1.15f + (float)i4 * 0.1f + 0.07f;
                    if (t < tc) continue;
                    float j = (float)Math.exp(-(t - tc) * 12.0f);
                    e = Math.max(e, j);
                    ry += ((i4 & 1) == 0 ? 0.22f : -0.22f) * j;
                    rx += 0.07f * j;
                }
                int tint = DeathCards.tint(Math.min(1.0f, e));
                float cy = 1.0546875f;
                Matrix4f bm = new Matrix4f((Matrix4fc)m).translate(0.0f, cy, 0.0f).rotateY(ry).rotateX(rx).translate(0.0f, -cy, 0.0f);
                for (int j = 0; j < SLICES.length; ++j) {
                    for (float[] p : PARTS) {
                        DeathCards.slice(sk, bm, p, SLICES[j][0], SLICES[j][1], tint, bl);
                    }
                }
            } else {
                for (int j = 0; j < SLICES.length; ++j) {
                    Matrix4f pm = DeathCards.pieceMatrix(m, s, j, t);
                    if (pm == null) continue;
                    float burn = DeathFx.sm(2.95f, 3.45f, t);
                    int tint = DeathCards.tint(Math.min(1.0f, 0.35f * (float)Math.exp(-(t - 2.05f) * 5.0f) + burn));
                    for (float[] p : PARTS) {
                        DeathCards.slice(sk, pm, p, SLICES[j][0], SLICES[j][1], tint, bl);
                    }
                }
            }
        }
        if (SkyTextures.spritesReady()) {
            int i5;
            class_4588 sp = buffers.method_73477(class_12249.method_76022((class_2960)SkyTextures.SPRITES));
            float rx = DeathFx.AX[0];
            float ry = DeathFx.AX[1];
            float rz = DeathFx.AX[2];
            float ux = DeathFx.AX[3];
            float uy = DeathFx.AX[4];
            float uz = DeathFx.AX[5];
            float ad = (t - 0.05f) / 0.5f;
            if (ad > 0.0f && ad < 1.0f) {
                DeathFx.sprite(sp, base, f, cam, 0.0f, 2.45f, 0.0f, 1.6f * DeathFx.eo(ad), 2, 16765802, (1.0f - ad) * 0.9f, rx, ry, rz, ux, uy, uz);
            }
            for (i5 = 0; i5 < 8; ++i5) {
                if (!DeathCards.card(i5, t, C) || C[5] <= 0.01f) continue;
                DeathFx.sprite(sp, base, f, cam, C[0], C[1], C[2], 0.75f * C[5], 2, i5 < 2 && t > 2.75f ? 16769162 : 16762960, 0.3f * C[5], rx, ry, rz, ux, uy, uz);
            }
            for (i5 = 0; i5 < 8; ++i5) {
                float age = t - (1.15f + (float)i5 * 0.1f + 0.07f);
                if (age < 0.0f || age > 0.25f || firstPerson) continue;
                DeathFx.sprite(sp, base, f, cam, 0.0f, (float)SLASH_PX[i5] * 0.05859375f, 0.0f, 0.6f + age * 4.0f, 2, 16773320, 1.0f - age / 0.25f, rx, ry, rz, ux, uy, uz);
            }
            if (t >= 2.05f && t < 2.45f) {
                float a3 = (t - 2.05f) / 0.4f;
                DeathFx.sprite(sp, base, f, cam, 0.0f, 1.1f, 0.0f, 1.2f + a3 * 3.0f, 2, 16726613, 0.9f * (1.0f - a3), rx, ry, rz, ux, uy, uz);
            }
            for (float[] sk : s.sparks) {
                float a4 = (t - sk[0]) / 0.9f;
                if (a4 <= 0.0f || a4 >= 1.0f) continue;
                boolean deck = sk[0] < 1.0f;
                float cx = 0.0f;
                float cy = deck ? 2.45f : 0.05f;
                float cz = 0.0f;
                float rad = sk[2] * DeathFx.eo(a4) * (deck ? 0.8f : 1.0f);
                DeathFx.sprite(sp, base, f, cam, cx + (float)Math.cos(sk[1]) * rad, cy + sk[3] * a4 * (deck ? 0.6f : 1.0f), cz + (float)Math.sin(sk[1]) * rad, sk[4] * 2.4f, 2, 16765802, (float)Math.sin((double)a4 * Math.PI), rx, ry, rz, ux, uy, uz);
            }
            if (!firstPerson) {
                for (int k = 0; k < s.embers.length; ++k) {
                    float[] e = s.embers[k];
                    float a5 = (t - e[0]) / 0.9f;
                    if (a5 <= 0.0f || a5 >= 1.0f) continue;
                    DeathCards.piecePos(s, k % SLICES.length, Math.min(t, 3.45f), A);
                    DeathFx.sprite(sp, base, f, cam, A[0] + e[1], A[1] + e[3] * a5, A[2] + e[2], 0.14f * (1.0f - a5 * 0.5f), 2, (k & 1) == 0 ? 16720960 : 16756800, (float)Math.sin((double)a5 * Math.PI), rx, ry, rz, ux, uy, uz);
                }
            }
        }
    }

    private static int tint(float e) {
        int gb = Math.round(255.0f * (1.0f - 0.65f * e));
        return 0xFFFF0000 | gb << 8 | gb;
    }

    private static float restY(int j) {
        return (float)(SLICES[j][0] + SLICES[j][1]) / 2.0f * 0.05859375f;
    }

    private static void piecePos(State s, int j, float t, float[] o) {
        float[] p = s.pieces[j];
        float a = Math.max(0.0f, t - 2.05f);
        float y0 = DeathCards.restY(j);
        float yl = 0.14f;
        float land = (p[1] + (float)Math.sqrt(p[1] * p[1] + 18.0f * Math.max(0.0f, y0 - yl))) / 9.0f;
        float aa = Math.min(a, land);
        float slide = a > land ? (1.0f - (float)Math.exp(-(a - land) * 4.0f)) / 4.0f * 0.35f : 0.0f;
        o[0] = p[0] * (aa + slide);
        o[2] = p[2] * (aa + slide);
        o[1] = a < land ? y0 + p[1] * aa - 4.5f * aa * aa : yl;
    }

    private static Matrix4f pieceMatrix(Matrix4f m, State s, int j, float t) {
        float sc = 1.0f - DeathFx.sm(3.0f, 3.5f, t);
        if (sc <= 0.01f) {
            return null;
        }
        float[] p = s.pieces[j];
        DeathCards.piecePos(s, j, t, B);
        float a = t - 2.05f;
        float y0 = DeathCards.restY(j);
        float land = (p[1] + (float)Math.sqrt(p[1] * p[1] + 18.0f * Math.max(0.0f, y0 - 0.14f))) / 9.0f;
        float ang = p[6] * Math.min(a, land) + p[6] * 0.15f * (a > land ? 1.0f - (float)Math.exp(-(a - land) * 5.0f) : 0.0f);
        return new Matrix4f((Matrix4fc)m).translate(B[0], B[1], B[2]).rotate(ang, p[3], p[4], p[5]).scale(sc).translate(0.0f, -y0, 0.0f);
    }

    private static void slice(class_4588 vc, Matrix4f m, float[] p, int lo, int hi, int col, int light) {
        float b;
        float p0 = p[1];
        float p1 = p[1] + p[3];
        float a = Math.max((float)lo, p0);
        if (a >= (b = Math.min((float)hi, p1))) {
            return;
        }
        Matrix4f pm = new Matrix4f((Matrix4fc)m).translate(p[0] * 0.05859375f, (p0 + p[3] / 2.0f) * 0.05859375f, 0.0f);
        DeathCards.box(vc, pm, p[2], p[3], p[4], 0.0f, (int)p[5], (int)p[6], a - p0, b - p0, col, light);
        DeathCards.box(vc, pm, p[2], p[3], p[4], p[9], (int)p[7], (int)p[8], a - p0, b - p0, col, light);
    }

    private static void caps(class_4588 w, Matrix4f m, float[] p, int lo, int hi, int col) {
        float y;
        float b;
        float p0 = p[1];
        float p1 = p[1] + p[3];
        float a = Math.max((float)lo, p0);
        if (a >= (b = Math.min((float)hi, p1))) {
            return;
        }
        float hx = p[2] / 2.0f * 0.05859375f * 0.97f;
        float hz = p[4] / 2.0f * 0.05859375f * 0.97f;
        float x = p[0] * 0.05859375f;
        if (b < p1) {
            y = b * 0.05859375f;
            DeathFx.face(w, m, x - hx, y, -hz, x + hx, y, -hz, x + hx, y, hz, x - hx, y, hz, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, col, 0xF000F0);
        }
        if (a > p0) {
            y = a * 0.05859375f;
            DeathFx.face(w, m, x - hx, y, hz, x + hx, y, hz, x + hx, y, -hz, x - hx, y, -hz, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, -1.0f, 0.0f, col, 0xF000F0);
        }
    }

    private static void box(class_4588 vc, Matrix4f m, float w, float h, float d, float infl, int u, int v, float y0, float y1, int col, int light) {
        float hx = (w / 2.0f + infl) * 0.05859375f;
        float hz = (d / 2.0f + infl) * 0.05859375f;
        float yb = (y0 - h / 2.0f) * 0.05859375f - (y0 <= 0.0f ? infl * 0.05859375f : 0.0f);
        float yt = (y1 - h / 2.0f) * 0.05859375f + (y1 >= h ? infl * 0.05859375f : 0.0f);
        float k = 0.015625f;
        float vt = ((float)v + d + (h - y1)) * k;
        float vb = ((float)v + d + (h - y0)) * k;
        DeathFx.face(vc, m, -hx, yt, hz, hx, yt, hz, hx, yb, hz, -hx, yb, hz, ((float)u + d) * k, vt, ((float)u + d + w) * k, vb, 0.0f, 0.0f, 1.0f, col, light);
        DeathFx.face(vc, m, hx, yt, -hz, -hx, yt, -hz, -hx, yb, -hz, hx, yb, -hz, ((float)u + 2.0f * d + w) * k, vt, ((float)u + 2.0f * d + 2.0f * w) * k, vb, 0.0f, 0.0f, -1.0f, col, light);
        DeathFx.face(vc, m, -hx, yt, -hz, -hx, yt, hz, -hx, yb, hz, -hx, yb, -hz, (float)u * k, vt, ((float)u + d) * k, vb, -1.0f, 0.0f, 0.0f, col, light);
        DeathFx.face(vc, m, hx, yt, hz, hx, yt, -hz, hx, yb, -hz, hx, yb, hz, ((float)u + d + w) * k, vt, ((float)u + 2.0f * d + w) * k, vb, 1.0f, 0.0f, 0.0f, col, light);
        if (y1 >= h) {
            DeathFx.face(vc, m, -hx, yt, -hz, hx, yt, -hz, hx, yt, hz, -hx, yt, hz, ((float)u + d) * k, (float)v * k, ((float)u + d + w) * k, ((float)v + d) * k, 0.0f, 1.0f, 0.0f, col, light);
        }
        if (y0 <= 0.0f) {
            DeathFx.face(vc, m, -hx, yb, hz, hx, yb, hz, hx, yb, -hz, -hx, yb, -hz, ((float)u + d + w) * k, (float)v * k, ((float)u + d + 2.0f * w) * k, ((float)v + d) * k, 0.0f, -1.0f, 0.0f, col, light);
        }
    }

    private static void cardQuad(class_4588 vc, Matrix4f m, int tile) {
        float[] f = DeathCards.uv(tile);
        float[] b = DeathCards.uv(0);
        DeathFx.face(vc, m, -0.21f, 0.006f, -0.29f, 0.21f, 0.006f, -0.29f, 0.21f, 0.006f, 0.29f, -0.21f, 0.006f, 0.29f, f[0], f[1], f[2], f[3], 0.0f, 1.0f, 0.0f, -1, 0xF000F0);
        DeathFx.face(vc, m, 0.21f, -0.006f, -0.29f, -0.21f, -0.006f, -0.29f, -0.21f, -0.006f, 0.29f, 0.21f, -0.006f, 0.29f, b[0], b[1], b[2], b[3], 0.0f, -1.0f, 0.0f, -1, 0xF000F0);
    }

    private static void chip(class_4588 w, Matrix4f m, int col, int light) {
        float r = 0.075f;
        float h = 0.014f;
        int rim = DeathFx.mix(col & 0xFFFFFF, 0xFFFFFF, 0.55f);
        for (int i = 0; i < 8; ++i) {
            double a0 = (double)i * Math.PI / 4.0;
            double a1 = (double)(i + 1) * Math.PI / 4.0;
            float x0 = (float)Math.cos(a0) * r;
            float z0 = (float)Math.sin(a0) * r;
            float x1 = (float)Math.cos(a1) * r;
            float z1 = (float)Math.sin(a1) * r;
            int sc = (i & 1) == 0 ? rim : col;
            float nx = (float)Math.cos((a0 + a1) / 2.0);
            float nz = (float)Math.sin((a0 + a1) / 2.0);
            DeathFx.face(w, m, x0, h, z0, x1, h, z1, x1, -h, z1, x0, -h, z0, 0.5f, 0.5f, 0.5f, 0.5f, nx, 0.0f, nz, sc, light);
            DeathFx.tri(w, m, 0.0f, h, 0.0f, x1, h, z1, x0, h, z0, i % 2 == 0 ? col : DeathFx.mix(col & 0xFFFFFF, 0xFFFFFF, 0.15f), light);
            DeathFx.tri(w, m, 0.0f, -h, 0.0f, x0, -h, z0, x1, -h, z1, col, light);
        }
    }

    private static void ribbon(class_4588 g, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float wa, float wb, int rgb, float aa, float ab) {
        float dx = bx - ax;
        float dz = bz - az;
        float l = (float)Math.sqrt(dx * dx + dz * dz);
        if (l < 1.0E-5f || aa <= 0.003f && ab <= 0.003f) {
            return;
        }
        float nx = -dz / l;
        float nz = dx / l;
        int ca = DeathFx.argb(rgb, aa);
        int cb = DeathFx.argb(rgb, ab);
        DeathFx.GP[0] = ax + nx * wa / 2.0f;
        DeathFx.GP[1] = ay;
        DeathFx.GP[2] = az + nz * wa / 2.0f;
        DeathFx.GP[3] = bx + nx * wb / 2.0f;
        DeathFx.GP[4] = by;
        DeathFx.GP[5] = bz + nz * wb / 2.0f;
        DeathFx.GP[6] = bx - nx * wb / 2.0f;
        DeathFx.GP[7] = by;
        DeathFx.GP[8] = bz - nz * wb / 2.0f;
        DeathFx.GP[9] = ax - nx * wa / 2.0f;
        DeathFx.GP[10] = ay;
        DeathFx.GP[11] = az - nz * wa / 2.0f;
        DeathFx.gquad(g, m, DeathFx.GP, ca, cb, cb, ca);
        DeathFx.GP[0] = ax;
        DeathFx.GP[1] = ay + wa * 0.25f;
        DeathFx.GP[2] = az;
        DeathFx.GP[3] = bx;
        DeathFx.GP[4] = by + wb * 0.25f;
        DeathFx.GP[5] = bz;
        DeathFx.GP[6] = bx;
        DeathFx.GP[7] = by - wb * 0.25f;
        DeathFx.GP[8] = bz;
        DeathFx.GP[9] = ax;
        DeathFx.GP[10] = ay - wa * 0.25f;
        DeathFx.GP[11] = az;
        DeathFx.gquad(g, m, DeathFx.GP, ca, cb, cb, ca);
    }

    private static void cross(class_4588 g, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float w, int rgb, float a) {
        DeathCards.ribbon(g, m, ax, ay, az, bx, by, bz, w, w, rgb, a, a);
        float dx = bx - ax;
        float dz = bz - az;
        float l = (float)Math.sqrt(dx * dx + dz * dz);
        if (l < 1.0E-5f) {
            return;
        }
        int c = DeathFx.argb(rgb, a);
        DeathFx.GP[0] = ax;
        DeathFx.GP[1] = ay + w / 2.0f;
        DeathFx.GP[2] = az;
        DeathFx.GP[3] = bx;
        DeathFx.GP[4] = by + w / 2.0f;
        DeathFx.GP[5] = bz;
        DeathFx.GP[6] = bx;
        DeathFx.GP[7] = by - w / 2.0f;
        DeathFx.GP[8] = bz;
        DeathFx.GP[9] = ax;
        DeathFx.GP[10] = ay - w / 2.0f;
        DeathFx.GP[11] = az;
        DeathFx.gquad(g, m, DeathFx.GP, c, c, c, c);
    }

    private static void ringFlat(class_4588 g, Matrix4f m, float cx, float y, float cz, float r0, float r1, int rgb, float a) {
        DeathFx.disc(g, m, cx, y, cz, r0, r1, rgb, 0.0f, rgb, a, 40);
        DeathFx.disc(g, m, cx, y, cz, r1, r1 * 1.05f, rgb, a, rgb, 0.0f, 40);
    }

    private static float[] uv(int tile) {
        int col = tile % 6;
        int row = tile / 6;
        float x = 2 + col * 84;
        float y = 4 + row * 124;
        return new float[]{x / 512.0f, y / 256.0f, (x + 80.0f) / 512.0f, (y + 112.0f) / 256.0f};
    }

    private static boolean cardsTex() {
        if (cardsReady) {
            return true;
        }
        if (cardsFailed) {
            return false;
        }
        try {
            BufferedImage img = new BufferedImage(512, 256, 2);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String[] rank = new String[]{"", "A", "K", "Q", "J", "10", "7"};
            int[] suit = new int[]{0, 0, 1, 2, 3, 1, 0};
            for (int tile = 0; tile < 7; ++tile) {
                int col = tile % 6;
                int row = tile / 6;
                int x = 2 + col * 84;
                int y = 4 + row * 124;
                if (tile == 0) {
                    DeathCards.back(g, x, y);
                    continue;
                }
                DeathCards.front(g, x, y, rank[tile], suit[tile]);
            }
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)img, "png", out);
            class_310.method_1551().method_1531().method_4616(CARDS, (class_1044)new class_1043(() -> "neonlyrics_cards", class_1011.method_49277((byte[])out.toByteArray())));
            cardsReady = true;
        }
        catch (Throwable t) {
            NeonLyrics.LOG.warn("Karten-Bild konnte nicht gemalt werden", t);
            cardsFailed = true;
        }
        return cardsReady;
    }

    private static void back(Graphics2D g, int x, int y) {
        RoundRectangle2D.Float card = new RoundRectangle2D.Float(x, y, 80.0f, 112.0f, 12.0f, 12.0f);
        g.setColor(new Color(9309986));
        g.fill(card);
        RoundRectangle2D.Float inner = new RoundRectangle2D.Float(x + 6, y + 6, 68.0f, 100.0f, 8.0f, 8.0f);
        Shape old = g.getClip();
        g.setClip(inner);
        g.setColor(new Color(217, 180, 74, 70));
        g.setStroke(new BasicStroke(1.4f));
        for (int k = -112; k < 192; k += 9) {
            g.drawLine(x + k, y, x + k + 112, y + 112);
            g.drawLine(x + k, y + 112, x + k + 112, y);
        }
        g.setClip(old);
        g.setColor(new Color(14267466));
        g.setStroke(new BasicStroke(2.6f));
        g.draw(inner);
        float cx = (float)x + 40.0f;
        float cy = (float)y + 56.0f;
        g.fill(new Ellipse2D.Float(cx - 17.0f, cy - 17.0f, 34.0f, 34.0f));
        g.setColor(new Color(1380376));
        g.fill(DeathCards.suitShape(0, cx, cy, 11.0f));
    }

    private static void front(Graphics2D g, int x, int y, String rank, int suit) {
        RoundRectangle2D.Float card = new RoundRectangle2D.Float(x, y, 80.0f, 112.0f, 12.0f, 12.0f);
        g.setColor(new Color(16183783));
        g.fill(card);
        g.setColor(new Color(13616820));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new RoundRectangle2D.Float(x + 1, y + 1, 78.0f, 110.0f, 11.0f, 11.0f));
        Color c = suit == 1 || suit == 2 ? new Color(13111342) : new Color(0x16161C);
        g.setColor(c);
        g.setFont(new Font("SansSerif", 1, rank.length() > 1 ? 17 : 20));
        g.drawString(rank, x + 7, y + 22);
        g.fill(DeathCards.suitShape(suit, x + 14, y + 34, 6.0f));
        AffineTransform at = g.getTransform();
        g.rotate(Math.PI, (double)x + 40.0, (double)y + 56.0);
        g.drawString(rank, x + 7, y + 22);
        g.fill(DeathCards.suitShape(suit, x + 14, y + 34, 6.0f));
        g.setTransform(at);
        g.fill(DeathCards.suitShape(suit, (float)x + 40.0f, (float)y + 56.0f, suit == 0 && rank.equals("A") ? 22.0f : 17.0f));
    }

    private static Shape suitShape(int suit, float cx, float cy, float s) {
        Path2D.Float p = new Path2D.Float();
        switch (suit) {
            case 1: {
                p.moveTo(cx, cy + s * 0.9f);
                p.curveTo(cx - s * 1.25f, cy + s * 0.05f, cx - s * 0.65f, cy - s * 1.0f, cx, cy - s * 0.35f);
                p.curveTo(cx + s * 0.65f, cy - s * 1.0f, cx + s * 1.25f, cy + s * 0.05f, cx, cy + s * 0.9f);
                break;
            }
            case 2: {
                p.moveTo(cx, cy - s);
                p.lineTo(cx + s * 0.72f, cy);
                p.lineTo(cx, cy + s);
                p.lineTo(cx - s * 0.72f, cy);
                break;
            }
            case 3: {
                p.append(new Ellipse2D.Float(cx - s * 0.42f, cy - s * 0.95f, s * 0.84f, s * 0.84f), false);
                p.append(new Ellipse2D.Float(cx - s * 0.92f, cy - s * 0.3f, s * 0.84f, s * 0.84f), false);
                p.append(new Ellipse2D.Float(cx + s * 0.08f, cy - s * 0.3f, s * 0.84f, s * 0.84f), false);
                p.moveTo(cx, cy);
                p.lineTo(cx - s * 0.32f, cy + s * 0.95f);
                p.lineTo(cx + s * 0.32f, cy + s * 0.95f);
                break;
            }
            default: {
                p.moveTo(cx, cy - s * 0.95f);
                p.curveTo(cx + s * 1.25f, cy - s * 0.1f, cx + s * 0.65f, cy + s * 0.8f, cx, cy + s * 0.25f);
                p.curveTo(cx - s * 0.65f, cy + s * 0.8f, cx - s * 1.25f, cy - s * 0.1f, cx, cy - s * 0.95f);
                p.moveTo(cx, cy + s * 0.1f);
                p.lineTo(cx - s * 0.36f, cy + s * 0.95f);
                p.lineTo(cx + s * 0.36f, cy + s * 0.95f);
            }
        }
        p.closePath();
        return p;
    }

    static {
        float t = 0.0f;
        for (int i = 0; i < TT.length; ++i) {
            DeathCards.TT[i] = t;
            float r = (float)i / 1000.0f;
            t += (1.0f - 0.72f * DeathFx.sm(1.97f, 2.07f, r) * (1.0f - DeathFx.sm(2.55f, 2.8f, r))) / 1000.0f;
        }
        SLASH_PX = new int[]{24, 12, 18, 6, 18, 24, 6, 12};
        SLICES = new int[][]{{0, 6}, {6, 12}, {12, 18}, {18, 24}, {24, 32}};
        PARTS = new float[][]{{-2.0f, 0.0f, 4.0f, 12.0f, 4.0f, 0.0f, 16.0f, 0.0f, 32.0f, 0.25f}, {2.0f, 0.0f, 4.0f, 12.0f, 4.0f, 16.0f, 48.0f, 0.0f, 48.0f, 0.25f}, {0.0f, 12.0f, 8.0f, 12.0f, 4.0f, 16.0f, 16.0f, 16.0f, 32.0f, 0.25f}, {-6.0f, 12.0f, 4.0f, 12.0f, 4.0f, 40.0f, 16.0f, 40.0f, 32.0f, 0.25f}, {6.0f, 12.0f, 4.0f, 12.0f, 4.0f, 32.0f, 48.0f, 48.0f, 48.0f, 0.25f}, {0.0f, 24.0f, 8.0f, 8.0f, 8.0f, 0.0f, 0.0f, 32.0f, 0.0f, 0.5f}};
        A = new float[3];
        B = new float[3];
        S0 = new float[3];
        LAND = new float[][]{{-0.17f, 0.02f, 0.05f, 0.35f}, {0.16f, 0.028f, -0.05f, -0.28f}};
        C = new float[6];
        C2 = new float[6];
        CARDS = class_2960.method_60655((String)"neonlyrics", (String)"cosmetic/cards");
    }

    private static final class State {
        int[] face = new int[8];
        float[][] pieces = new float[SLICES.length][];
        float[][] hits = new float[56][];
        float[][] burst = new float[30][];
        float[][] chips = new float[16][];
        float[][] sparks = new float[40][];
        float[][] embers = new float[SLICES.length * 8][];

        private State() {
        }
    }
}

