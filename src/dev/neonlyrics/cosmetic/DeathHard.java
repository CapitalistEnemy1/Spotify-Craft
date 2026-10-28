/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package dev.neonlyrics.cosmetic;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neonlyrics.compat.MultiBufferSource;
import dev.neonlyrics.cosmetic.Accessories;
import dev.neonlyrics.cosmetic.DeathFx;
import dev.neonlyrics.sky.SkyTextures;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

final class DeathHard {
    static final int FIRST = 3;
    static final float[] END = new float[]{6.4f};
    private static final float[][][] SLOW = new float[][][]{new float[][]{{1.6f, 2.0f, 0.22f}, {3.45f, 3.7f, 0.45f}}};
    static final float[][] TT = new float[END.length][];
    static final int[] BLOOD;
    static final int[] DIRT;
    static final int[][] UV;
    static final int[][] DIM;
    static final int[] BOTTOM;
    static final float[] INFL;

    private DeathHard() {
    }

    private static float[] table(float[][] slow, float end) {
        float k;
        ArrayList<Float> out = new ArrayList<Float>();
        for (float t = 0.0f; t < end + 0.05f && out.size() < 30000; t += k / 1000.0f) {
            out.add(Float.valueOf(t));
            k = 1.0f;
            for (float[] w : slow) {
                float e = DeathFx.sm(w[0] - 0.08f, w[0], t) * (1.0f - DeathFx.sm(w[1], w[1] + 0.12f, t));
                k = Math.min(k, DeathFx.lerp(1.0f, w[2], e));
            }
        }
        float[] a = new float[out.size()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = ((Float)out.get(i)).floatValue();
        }
        return a;
    }

    static Object create(DeathFx.Fx f, Random r) {
        return new Tentacles(r);
    }

    static void draw(Minecraft mc, MultiBufferSource buffers, Matrix4f base, Vec3 cam, DeathFx.Fx f, float t) {
        Anim s;
        block10: {
            block9: {
                Object object = f.extra;
                if (!(object instanceof Anim)) break block9;
                s = (Anim)object;
                if (Accessories.whiteTex()) break block10;
            }
            return;
        }
        Ctx c = new Ctx();
        c.mc = mc;
        c.base = base;
        c.cam = cam;
        c.f = f;
        c.m = new Matrix4f((Matrix4fc)base).translate((float)(f.ox - cam.x), (float)(f.oy - cam.y), (float)(f.oz - cam.z)).rotateY(-f.yaw);
        c.light = DeathFx.light(mc, f.ox, f.oy + 1.0, f.oz);
        c.firstPerson = f.self && mc.options.getCameraType().isFirstPerson();
        float dt = t - s.lastT;
        if (dt < 0.0f || dt > 0.5f) {
            dt = 0.0f;
        }
        s.lastT = t;
        while (dt > 1.0E-5f) {
            float st = Math.min(dt, 0.016666668f);
            s.tick(s.simT + st, st);
            s.simT += st;
            dt -= st;
        }
        s.simT = t;
        s.pose(t);
        VertexConsumer g = buffers.getBuffer(RenderTypes.debugQuads());
        s.glow(c, g, t);
        s.drawPools(g, c.m, t);
        VertexConsumer w = buffers.getBuffer(RenderTypes.entityCutout((Identifier)Accessories.WHITE));
        s.solid(c, w, t);
        if (s.blood != null) {
            s.blood.draw(w, c.m, s.fade(t), c.light);
        }
        if (s.dirt != null) {
            s.dirt.draw(w, c.m, s.fade(t), c.light);
        }
        if (f.skin != null && !c.firstPerson) {
            VertexConsumer sk = buffers.getBuffer(RenderTypes.entityCutout((Identifier)f.skin));
            s.skin(c, sk, t);
        }
        if (SkyTextures.spritesReady()) {
            VertexConsumer sp = buffers.getBuffer(RenderTypes.text((Identifier)SkyTextures.SPRITES));
            s.sprites(c, sp, t);
        }
    }

    static void poolDisc(VertexConsumer g, Matrix4f m, float cx, float cz, float rad, float k, float a, int seed) {
        if (k <= 0.01f || a <= 0.01f) {
            return;
        }
        int seg = 18;
        float[] p = new float[12];
        for (int i = 0; i < seg; ++i) {
            float r0 = rad * k * (0.7f + 0.5f * DeathHard.hash(seed + i));
            float r1 = rad * k * (0.7f + 0.5f * DeathHard.hash(seed + (i + 1) % seg));
            double a0 = (double)i * Math.PI * 2.0 / (double)seg;
            double a1 = (double)(i + 1) * Math.PI * 2.0 / (double)seg;
            p[0] = cx;
            p[1] = 0.016f;
            p[2] = cz;
            p[3] = cx;
            p[4] = 0.016f;
            p[5] = cz;
            p[6] = cx + (float)Math.cos(a1) * r1;
            p[7] = 0.016f;
            p[8] = cz + (float)Math.sin(a1) * r1;
            p[9] = cx + (float)Math.cos(a0) * r0;
            p[10] = 0.016f;
            p[11] = cz + (float)Math.sin(a0) * r0;
            DeathFx.gquad(g, m, p, DeathFx.argb(7999004, a * 0.95f), DeathFx.argb(7999004, a * 0.95f), DeathFx.argb(4851730, a * 0.92f), DeathFx.argb(4851730, a * 0.92f));
        }
    }

    static float hash(int i) {
        int h = i * 374761393 + 668265263;
        h = (h ^ h >>> 13) * 1274126177;
        return (float)((h ^ h >>> 16) & 0xFFFF) / 65535.0f;
    }

    static Matrix4f part(Rig g, int i, Matrix4f out) {
        out.identity().translate(g.px, g.py, g.pz).rotateY(g.ry).rotateX(g.rx).rotateZ(g.rz).translate(0.0f, -1.0f, 0.0f);
        switch (i) {
            case 2: {
                out.translate(0.0f, 1.0546875f, 0.0f).scale(1.0f, 1.0f, g.belly);
                break;
            }
            case 5: {
                out.translate(0.0f, 1.40625f, 0.0f).rotateX(g.head[0]).rotateY(g.head[1]).rotateZ(g.head[2]).translate(0.0f, 0.234375f, 0.0f);
                break;
            }
            case 3: {
                out.translate(-0.3515625f, 1.2890625f, 0.0f).rotateX(g.armR[0]).rotateY(g.armR[1]).rotateZ(g.armR[2]).translate(0.0f, -0.234375f, 0.0f);
                break;
            }
            case 4: {
                out.translate(0.3515625f, 1.2890625f, 0.0f).rotateX(g.armL[0]).rotateY(g.armL[1]).rotateZ(g.armL[2]).translate(0.0f, -0.234375f, 0.0f);
                break;
            }
            case 0: {
                out.translate(-0.1171875f, 0.703125f, 0.0f).rotateX(g.legR[0]).rotateY(g.legR[1]).rotateZ(g.legR[2]).translate(0.0f, -0.3515625f, 0.0f);
                break;
            }
            default: {
                out.translate(0.1171875f, 0.703125f, 0.0f).rotateX(g.legL[0]).rotateY(g.legL[1]).rotateZ(g.legL[2]).translate(0.0f, -0.3515625f, 0.0f);
            }
        }
        return out;
    }

    static void rig(VertexConsumer sk, Matrix4f m, Rig g, int tint, int light, int lo, int hi) {
        Matrix4f tmp = new Matrix4f();
        for (int i = 0; i < 6; ++i) {
            float y1;
            int b = BOTTOM[i];
            int h = DIM[i][1];
            float y0 = Math.max(0, lo - b);
            if (y0 >= (y1 = (float)Math.min(h, hi - b))) continue;
            Matrix4f pm = new Matrix4f((Matrix4fc)m).mul((Matrix4fc)DeathHard.part(g, i, tmp));
            DeathHard.sbox(sk, pm, DIM[i][0], h, DIM[i][2], 0.0f, UV[i][0], UV[i][1], y0, y1, tint, light);
            DeathHard.sbox(sk, pm, DIM[i][0], h, DIM[i][2], INFL[i], UV[i][2], UV[i][3], y0, y1, tint, light);
        }
    }

    static void caps(VertexConsumer w, Matrix4f m, Rig g, int cut, int col) {
        Matrix4f tmp = new Matrix4f();
        for (int i = 0; i < 6; ++i) {
            int b = BOTTOM[i];
            int h = DIM[i][1];
            if (cut <= b || cut >= b + h) continue;
            Matrix4f pm = new Matrix4f((Matrix4fc)m).mul((Matrix4fc)DeathHard.part(g, i, tmp));
            float y = ((float)(cut - b) - (float)h / 2.0f) * 0.05859375f;
            float hx = (float)DIM[i][0] / 2.0f * 0.05859375f * 0.98f;
            float hz = (float)DIM[i][2] / 2.0f * 0.05859375f * 0.98f;
            DeathFx.face(w, pm, -hx, y, -hz, hx, y, -hz, hx, y, hz, -hx, y, hz, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, col, 0xF000F0);
            DeathFx.face(w, pm, -hx, y, hz, hx, y, hz, hx, y, -hz, -hx, y, -hz, 0.5f, 0.5f, 0.5f, 0.5f, 0.0f, -1.0f, 0.0f, col, 0xF000F0);
        }
    }

    static void sbox(VertexConsumer vc, Matrix4f m, float w, float h, float d, float infl, int u, int v, float y0, float y1, int col, int light) {
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

    static void voxel(VertexConsumer vc, Matrix4f m, int part, float cx, float cy, float cz, float s, int tint, int light) {
        int w = DIM[part][0];
        int h = DIM[part][1];
        int d = DIM[part][2];
        int u = UV[part][0];
        int v = UV[part][1];
        float k = 0.015625f;
        float hs = s / 2.0f * 0.05859375f;
        float fu = (float)(u + d) + (cx - s / 2.0f + (float)w / 2.0f);
        float fv = (float)(v + d) + ((float)h / 2.0f - (cy + s / 2.0f));
        DeathFx.face(vc, m, -hs, hs, hs, hs, hs, hs, hs, -hs, hs, -hs, -hs, hs, fu * k, fv * k, (fu + s) * k, (fv + s) * k, 0.0f, 0.0f, 1.0f, tint, light);
        float bu = (float)(u + 2 * d + w) + ((float)w / 2.0f - (cx + s / 2.0f));
        DeathFx.face(vc, m, hs, hs, -hs, -hs, hs, -hs, -hs, -hs, -hs, hs, -hs, -hs, bu * k, fv * k, (bu + s) * k, (fv + s) * k, 0.0f, 0.0f, -1.0f, tint, light);
        float ru = (float)u + (cz - s / 2.0f + (float)d / 2.0f);
        DeathFx.face(vc, m, -hs, hs, -hs, -hs, hs, hs, -hs, -hs, hs, -hs, -hs, -hs, ru * k, fv * k, (ru + s) * k, (fv + s) * k, -1.0f, 0.0f, 0.0f, tint, light);
        float lu = (float)(u + d + w) + ((float)d / 2.0f - (cz + s / 2.0f));
        DeathFx.face(vc, m, hs, hs, hs, hs, hs, -hs, hs, -hs, -hs, hs, -hs, hs, lu * k, fv * k, (lu + s) * k, (fv + s) * k, 1.0f, 0.0f, 0.0f, tint, light);
        float tu = (float)(u + d) + (cx - s / 2.0f + (float)w / 2.0f);
        float tv = (float)v + (cz - s / 2.0f + (float)d / 2.0f);
        DeathFx.face(vc, m, -hs, hs, -hs, hs, hs, -hs, hs, hs, hs, -hs, hs, hs, tu * k, tv * k, (tu + s) * k, (tv + s) * k, 0.0f, 1.0f, 0.0f, tint, light);
        float ou = (float)(u + d + w) + (cx - s / 2.0f + (float)w / 2.0f);
        float ov = (float)v + ((float)d / 2.0f - (cz + s / 2.0f));
        DeathFx.face(vc, m, -hs, -hs, hs, hs, -hs, hs, hs, -hs, -hs, -hs, -hs, -hs, ou * k, ov * k, (ou + s) * k, (ov + s) * k, 0.0f, -1.0f, 0.0f, tint, light);
    }

    static List<Vox> voxelize(Rig g, int step) {
        ArrayList<Vox> out = new ArrayList<Vox>();
        Matrix4f tmp = new Matrix4f();
        for (int i = 0; i < 6; ++i) {
            DeathHard.part(g, i, tmp);
            Quaternionf q = tmp.getNormalizedRotation(new Quaternionf());
            for (int x = 0; x < DIM[i][0]; x += step) {
                for (int y = 0; y < DIM[i][1]; y += step) {
                    for (int z = 0; z < DIM[i][2]; z += step) {
                        Vox v = new Vox();
                        v.part = i;
                        v.cx = (float)x + (float)step / 2.0f - (float)DIM[i][0] / 2.0f;
                        v.cy = (float)y + (float)step / 2.0f - (float)DIM[i][1] / 2.0f;
                        v.cz = (float)z + (float)step / 2.0f - (float)DIM[i][2] / 2.0f;
                        tmp.transformPosition(v.cx * 0.05859375f, v.cy * 0.05859375f, v.cz * 0.05859375f, v.p);
                        v.q.set((Quaternionfc)q);
                        out.add(v);
                    }
                }
            }
        }
        return out;
    }

    static void drawVox(VertexConsumer sk, Matrix4f m, Vox v, float s, float scale, int light) {
        if (scale <= 0.01f) {
            return;
        }
        Matrix4f vm = new Matrix4f((Matrix4fc)m).translate((Vector3fc)v.p).rotate((Quaternionfc)v.q).rotateXYZ(v.r.x, v.r.y, v.r.z).scale(scale);
        DeathHard.voxel(sk, vm, v.part, v.cx, v.cy, v.cz, s, v.tint, light);
    }

    static boolean fly(Vector3f p, Vector3f v, Vector3f r, Vector3f w, float dt, float floor, float g) {
        v.y -= g * dt;
        p.add(v.x * dt, v.y * dt, v.z * dt);
        r.add(w.x * dt, w.y * dt, w.z * dt);
        if (p.y < floor) {
            p.y = floor;
            if (v.y < 0.0f) {
                v.y *= -0.3f;
            }
            v.x *= 0.55f;
            v.z *= 0.55f;
            w.mul(0.55f);
            return true;
        }
        return false;
    }

    static void gline(VertexConsumer g, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float w, int rgb, float a) {
        if (a <= 0.003f) {
            return;
        }
        float dx = bx - ax;
        float dy = by - ay;
        float dz = bz - az;
        float l = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 1.0E-5f) {
            return;
        }
        dx /= l;
        dy /= l;
        dz /= l;
        float ux = 0.0f;
        float uy = 1.0f;
        float uz = 0.0f;
        if (Math.abs(dy) > 0.9f) {
            ux = 1.0f;
            uy = 0.0f;
        }
        float n1x = dy * uz - dz * uy;
        float n1y = dz * ux - dx * uz;
        float n1z = dx * uy - dy * ux;
        float nl = (float)Math.sqrt(n1x * n1x + n1y * n1y + n1z * n1z);
        float n2x = dy * (n1z /= nl) - dz * (n1y /= nl);
        float n2y = dz * (n1x /= nl) - dx * n1z;
        float n2z = dx * n1y - dy * n1x;
        int c = DeathFx.argb(rgb, a);
        float h = w / 2.0f;
        float[] p = new float[12];
        for (int k = 0; k < 2; ++k) {
            float ex = k == 0 ? n1x : n2x;
            float ey = k == 0 ? n1y : n2y;
            float ez = k == 0 ? n1z : n2z;
            p[0] = ax + ex * h;
            p[1] = ay + ey * h;
            p[2] = az + ez * h;
            p[3] = bx + ex * h;
            p[4] = by + ey * h;
            p[5] = bz + ez * h;
            p[6] = bx - ex * h;
            p[7] = by - ey * h;
            p[8] = bz - ez * h;
            p[9] = ax - ex * h;
            p[10] = ay - ey * h;
            p[11] = az - ez * h;
            DeathFx.gquad(g, m, p, c, c, c, c);
        }
    }

    static void gbox(VertexConsumer g, Matrix4f m, float hx, float hy, float hz, int argbCol) {
        float[][] f;
        for (float[] q : f = new float[][]{{-hx, hy, hz, hx, hy, hz, hx, -hy, hz, -hx, -hy, hz}, {hx, hy, -hz, -hx, hy, -hz, -hx, -hy, -hz, hx, -hy, -hz}, {-hx, hy, -hz, -hx, hy, hz, -hx, -hy, hz, -hx, -hy, -hz}, {hx, hy, hz, hx, hy, -hz, hx, -hy, -hz, hx, -hy, hz}, {-hx, hy, -hz, hx, hy, -hz, hx, hy, hz, -hx, hy, hz}, {-hx, -hy, hz, hx, -hy, hz, hx, -hy, -hz, -hx, -hy, -hz}}) {
            DeathFx.gquad(g, m, q, argbCol, argbCol, argbCol, argbCol);
        }
    }

    static void block(VertexConsumer w, Matrix4f m, float cx, float cy, float cz, float sx, float sy, float sz, int rgb, int light) {
        DeathFx.cube(w, m, cx - sx / 2.0f, cy - sy / 2.0f, cz - sz / 2.0f, cx + sx / 2.0f, cy + sy / 2.0f, cz + sz / 2.0f, 0xFF000000 | rgb, light);
    }

    static float rnd(Random r) {
        return r.nextFloat() - 0.5f;
    }

    static {
        for (int i = 0; i < END.length; ++i) {
            DeathHard.TT[i] = DeathHard.table(SLOW[i], END[i]);
        }
        BLOOD = new int[]{3802640, 6949400, 11540526, 16722501};
        DIRT = new int[]{5214008, 6965804, 4160046, 8017460};
        UV = new int[][]{{0, 16, 0, 32}, {16, 48, 0, 48}, {16, 16, 16, 32}, {40, 16, 40, 32}, {32, 48, 48, 48}, {0, 0, 32, 0}};
        DIM = new int[][]{{4, 12, 4}, {4, 12, 4}, {8, 12, 4}, {4, 12, 4}, {4, 12, 4}, {8, 8, 8}};
        BOTTOM = new int[]{0, 0, 12, 12, 12, 24};
        INFL = new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.5f};
    }

    static final class Tentacles
    extends Anim {
        final Rig g = new Rig();
        static final int N = 5;
        static final int SEG = 30;
        final float[] ph = new float[5];
        final float[] sp = new float[5];
        final float[] gx = new float[5];
        final float[] gz = new float[5];
        final boolean[] hit = new boolean[5];
        final Vector3f belly = new Vector3f();
        final Vector3f nrm = new Vector3f();
        boolean burst;
        float inner;
        float throb;
        float tug;
        float pullK;
        float arch;

        Tentacles(Random r0) {
            super(r0);
            this.blood = new Blood(520, this.r, BLOOD);
            this.dirt = new Blood(200, this.r, DIRT);
            for (int i = 0; i < 5; ++i) {
                double a = (double)i / 5.0 * Math.PI * 2.0 + 0.4;
                this.gx[i] = (float)Math.cos(a) * 1.35f;
                this.gz[i] = (float)Math.sin(a) * 1.35f;
                this.ph[i] = this.r.nextFloat() * 6.0f;
                this.sp[i] = 0.8f + this.r.nextFloat() * 0.5f;
            }
        }

        @Override
        float end() {
            return 6.4f;
        }

        @Override
        void pose(float t) {
            this.arch = DeathFx.eo(DeathFx.sm(0.8f, 1.55f, t));
            this.pullK = DeathFx.ei(DeathFx.sm(3.45f, 4.9f, t));
            float twitch = DeathFx.sm(0.1f, 0.4f, t) * (1.0f - this.arch) * (float)(this.r.nextFloat() < 0.25f ? 1 : 0);
            this.g.rx = -1.2f * this.arch + DeathHard.rnd(this.r) * 0.15f * twitch;
            this.g.rz = DeathHard.rnd(this.r) * 0.12f * twitch;
            this.tug = t > 3.45f ? 0.12f * Math.max(0.0f, (float)Math.sin((t - 3.45f) * 11.0f)) : 0.0f;
            this.g.py = 1.0f + 0.55f * this.arch - this.pullK * 3.4f - this.tug;
            this.g.head[0] = -0.8f * this.arch + DeathHard.rnd(this.r) * 0.4f * twitch;
            float reach = DeathFx.sm(4.1f, 4.5f, t);
            this.g.armR[0] = 0.7f * this.arch * (1.0f - reach);
            this.g.armR[2] = DeathFx.lerp(-1.35f * this.arch, -2.9f, reach) + (float)Math.sin(t * 13.0f) * 0.15f * reach;
            this.g.armL[0] = 0.7f * this.arch * (1.0f - reach);
            this.g.armL[2] = DeathFx.lerp(1.35f * this.arch, 2.9f, reach) - (float)Math.sin(t * 15.0f) * 0.15f * reach;
            this.g.legR[0] = 0.35f * this.arch + DeathHard.rnd(this.r) * 0.3f * twitch;
            this.g.legL[0] = 0.2f * this.arch;
            this.throb = 0.5f + 0.5f * (float)Math.sin(t * (6.0f + 10.0f * DeathFx.sm(0.8f, 1.6f, t)));
            this.inner = DeathFx.sm(0.6f, 1.5f, t) * (1.0f - DeathFx.sm(1.6f, 1.8f, t));
            this.g.belly = t < 1.6f ? 1.0f + 0.25f * this.inner * this.throb : 1.0f;
            Matrix4f bm = DeathHard.part(this.g, 2, new Matrix4f());
            bm.transformPosition(0.0f, 0.02f, (float)DIM[2][2] / 2.0f * 0.05859375f, this.belly);
            bm.transformDirection(0.0f, 0.0f, 1.0f, this.nrm).normalize();
        }

        @Override
        void tick(float t, float dt) {
            this.pose(t);
            if (t >= 1.6f && !this.burst) {
                this.burst = true;
                this.blood.spurt(this.belly.x, this.belly.y, this.belly.z, 110, this.nrm.x * 4.0f, this.nrm.y * 4.0f, this.nrm.z * 4.0f, 3.5f, 0.075f);
            }
            if (t > 1.6f && t < 3.4f && this.r.nextFloat() < 0.7f) {
                this.blood.spurt(this.belly.x, this.belly.y, this.belly.z, 2, this.nrm.x * 1.6f, this.nrm.y * 1.6f, this.nrm.z * 1.6f, 1.6f, 0.05f);
            }
            if (t > 3.4f && this.pullK < 0.85f && this.r.nextFloat() < 0.5f) {
                this.blood.spurt(this.belly.x, Math.max(0.05f, this.belly.y), this.belly.z, 2, 0.0f, 1.2f, 0.0f, 2.0f, 0.05f);
            }
            float dive = DeathFx.sm(2.35f, 3.25f, t);
            for (int i = 0; i < 5; ++i) {
                if (!(dive > 0.85f) || this.hit[i]) continue;
                this.hit[i] = true;
                this.dirt.spurt(this.gx[i], 0.05f, this.gz[i], 22, 0.0f, 3.0f, 0.0f, 2.6f, 0.07f);
            }
            if (t > 4.9f && this.pools.isEmpty()) {
                this.pool(0.0f, 0.0f, 1.5f, t, 1.0f);
            }
            super.tick(t, dt);
        }

        @Override
        void glow(Ctx c, VertexConsumer gv, float t) {
            float fade = this.fade(t);
            DeathFx.disc(gv, c.m, 0.0f, 0.012f, 0.0f, 0.0f, 1.6f, 9054936, 0.35f * DeathFx.sm(0.0f, 0.9f, t) * (0.7f + 0.3f * this.throb) * fade, 9054936, 0.0f, 32);
            for (int i = 0; i < 5; ++i) {
                if (!this.hit[i]) continue;
                float s = 1.0f + 0.6f * DeathFx.sm(3.0f, 3.6f, t);
                float a = (0.7f + 0.3f * (float)Math.sin(t * 12.0f + (float)i)) * (1.0f - DeathFx.sm(5.2f, 6.0f, t));
                DeathFx.disc(gv, c.m, this.gx[i], 0.015f, this.gz[i], 0.12f * s, 0.32f * s, 11553535, a, 11553535, 0.0f, 16);
            }
            float hole = DeathFx.sm(3.3f, 3.7f, t) * (1.0f - DeathFx.sm(5.1f, 5.9f, t));
            if (hole > 0.01f) {
                DeathFx.disc(gv, c.m, 0.0f, 0.014f, 0.0f, 0.0f, 0.75f * hole, 524812, 1.0f, 524812, 1.0f, 28);
                DeathFx.disc(gv, c.m, 0.0f, 0.016f, 0.0f, 0.75f * hole, 0.92f * hole, 11553535, 0.8f * hole, 11553535, 0.0f, 28);
            }
        }

        @Override
        void solid(Ctx c, VertexConsumer w, float t) {
            float grow = DeathFx.eo(DeathFx.sm(1.6f, 2.4f, t));
            float dive = DeathFx.sm(2.35f, 3.25f, t);
            if (grow > 0.01f) {
                for (int i = 0; i < 5; ++i) {
                    float dx = (float)Math.cos((double)i / 5.0 * Math.PI * 2.0 + 0.4);
                    float dz = (float)Math.sin((double)i / 5.0 * Math.PI * 2.0 + 0.4);
                    float sx = -dz;
                    float sz = dx;
                    float L = 2.3f * grow;
                    float p0x = this.belly.x;
                    float p0y = this.belly.y;
                    float p0z = this.belly.z;
                    float p1x = this.belly.x + this.nrm.x * 1.2f + dx * 0.6f;
                    float p1y = this.belly.y + this.nrm.y * 1.2f;
                    float p1z = this.belly.z + this.nrm.z * 1.2f + dz * 0.6f;
                    float p2x = this.gx[i];
                    float p2y = 1.3f - 0.9f * this.pullK;
                    float p2z = this.gz[i];
                    float p3x = this.gx[i];
                    float p3y = -0.9f;
                    float p3z = this.gz[i];
                    for (int k = 0; k < 30; ++k) {
                        float s = (float)k / 29.0f;
                        float wig = (float)Math.sin(s * 7.0f - t * 6.0f * this.sp[i] + this.ph[i]) * 0.35f * s * grow;
                        float fx = this.belly.x + this.nrm.x * s * L * 0.85f + dx * s * L * 0.55f + sx * wig;
                        float fy = this.belly.y + this.nrm.y * s * L * 0.85f + (float)Math.cos(s * 5.0f - t * 5.0f + this.ph[i]) * 0.2f * s * grow;
                        float fz = this.belly.z + this.nrm.z * s * L * 0.85f + dz * s * L * 0.55f + sz * wig;
                        float u = 1.0f - s;
                        float b0 = u * u * u;
                        float b1 = 3.0f * u * u * s;
                        float b2 = 3.0f * u * s * s;
                        float b3 = s * s * s;
                        float w2 = (float)Math.sin(s * 9.0f - t * 9.0f + this.ph[i]) * 0.06f * (1.0f - this.pullK);
                        float bx = b0 * p0x + b1 * p1x + b2 * p2x + b3 * p3x + sx * w2;
                        float by = b0 * p0y + b1 * p1y + b2 * p2y + b3 * p3y;
                        float bz = b0 * p0z + b1 * p1z + b2 * p2z + b3 * p3z + sz * w2;
                        float e = DeathFx.ei(dive);
                        float x = DeathFx.lerp(fx, bx, e);
                        float y = DeathFx.lerp(fy, by, e);
                        float z = DeathFx.lerp(fz, bz, e);
                        if (y < -0.4f) continue;
                        float rad = (0.13f * (float)Math.pow(1.0f - s, 0.7) + 0.025f) * Math.min(1.0f, grow * 1.5f);
                        int col = DeathFx.mix(2756154, 10506456, s);
                        if (k % 3 == 1) {
                            col = DeathFx.mix(col & 0xFFFFFF, 14715592, 0.35f);
                        }
                        Matrix4f sm4 = new Matrix4f((Matrix4fc)c.m).translate(x, y, z).rotateY(s * 3.0f + (float)i);
                        DeathFx.cube(w, sm4, -rad, -rad, -rad, rad, rad, rad, col, c.light);
                    }
                }
            }
            if (this.burst && !c.firstPerson) {
                Matrix4f bm = new Matrix4f((Matrix4fc)c.m).mul((Matrix4fc)DeathHard.part(this.g, 2, new Matrix4f()));
                float z0 = (float)DIM[2][2] / 2.0f * 0.05859375f;
                DeathFx.cube(w, bm, -0.15f, -0.16f, z0, 0.15f, 0.2f, z0 + 0.02f, -5236690, 0xF000F0);
                DeathFx.cube(w, bm, -0.09f, -0.09f, z0 + 0.005f, 0.09f, 0.13f, z0 + 0.025f, -12974576, c.light);
            }
        }

        @Override
        void skin(Ctx c, VertexConsumer sk, float t) {
            int tint = DeathFx.mix(0xFFFFFF, 13144319, 0.5f * this.inner * this.throb);
            DeathHard.rig(sk, c.m, this.g, tint, c.light, 0, 32);
        }

        @Override
        void sprites(Ctx c, VertexConsumer sp, float t) {
            float on = DeathFx.sm(0.3f, 1.0f, t) * (1.0f - DeathFx.sm(5.4f, 6.2f, t));
            for (int e = 0; e < 60; ++e) {
                double ae = (double)e * 2.39996;
                float rr = 0.3f + (float)(e % 9) * 0.15f;
                float life = (t * 0.6f + (float)(e % 13) / 13.0f) % 1.0f;
                this.spr(c, sp, (float)Math.cos(ae) * rr, life * 2.2f, (float)Math.sin(ae) * rr, 0.12f, 2, 13144319, on * (float)Math.sin((double)life * Math.PI) * 0.8f);
            }
            if (this.inner > 0.05f) {
                this.spr(c, sp, this.belly.x + this.nrm.x * 0.1f, this.belly.y + this.nrm.y * 0.1f, this.belly.z + this.nrm.z * 0.1f, 0.9f, 2, 11553535, this.inner * this.throb * 0.8f);
            }
            if (t > 1.6f && t < 2.2f) {
                this.spr(c, sp, this.belly.x, this.belly.y, this.belly.z, 1.6f, 2, 16724048, 1.0f - (t - 1.6f) / 0.6f);
            }
        }
    }

    static abstract class Anim {
        final Random r;
        float lastT = 0.0f;
        float simT = 0.0f;
        Blood blood;
        Blood dirt;
        final List<float[]> pools = new ArrayList<float[]>();

        Anim(Random r) {
            this.r = new Random(r.nextLong());
        }

        void tick(float t, float dt) {
            if (this.blood != null) {
                this.blood.step(dt);
            }
            if (this.dirt != null) {
                this.dirt.step(dt);
            }
        }

        abstract void pose(float var1);

        void glow(Ctx c, VertexConsumer g, float t) {
        }

        void solid(Ctx c, VertexConsumer w, float t) {
        }

        void skin(Ctx c, VertexConsumer sk, float t) {
        }

        void sprites(Ctx c, VertexConsumer sp, float t) {
        }

        float end() {
            return 5.4f;
        }

        float fade(float t) {
            return 1.0f - DeathFx.sm(this.end() - 0.45f, this.end(), t);
        }

        void pool(float x, float z, float rad, float start, float dur) {
            this.pools.add(new float[]{x, z, rad, start, dur, this.r.nextInt(100000)});
        }

        void drawPools(VertexConsumer g, Matrix4f m, float t) {
            float a = this.fade(t);
            for (float[] p : this.pools) {
                DeathHard.poolDisc(g, m, p[0], p[1], p[2], DeathFx.eo((t - p[3]) / p[4]), a, (int)p[5]);
            }
        }

        void spr(Ctx c, VertexConsumer sp, float x, float y, float z, float size, int k, int rgb, float a) {
            DeathFx.sprite(sp, c.base, c.f, c.cam, x, y, z, size, k, rgb, a, DeathFx.AX[0], DeathFx.AX[1], DeathFx.AX[2], DeathFx.AX[3], DeathFx.AX[4], DeathFx.AX[5]);
        }
    }

    static final class Ctx {
        Minecraft mc;
        Matrix4f base;
        Matrix4f m;
        Vec3 cam;
        DeathFx.Fx f;
        int light;
        boolean firstPerson;

        Ctx() {
        }
    }

    static final class Blood {
        final int max;
        final int[] cols;
        int n = 0;
        final float[] x;
        final float[] y;
        final float[] z;
        final float[] vx;
        final float[] vy;
        final float[] vz;
        final float[] s;
        final float[] splat;
        final float[] rot;
        final int[] col;
        final boolean[] down;
        final Random r;

        Blood(int max, Random r, int[] cols) {
            this.max = max;
            this.r = r;
            this.cols = cols;
            this.x = new float[max];
            this.y = new float[max];
            this.z = new float[max];
            this.vx = new float[max];
            this.vy = new float[max];
            this.vz = new float[max];
            this.s = new float[max];
            this.splat = new float[max];
            this.rot = new float[max];
            this.col = new int[max];
            this.down = new boolean[max];
        }

        void spurt(float px, float py, float pz, int count, float dx, float dy, float dz, float spread, float size) {
            int i = 0;
            while (i < count && this.n < this.max) {
                this.x[this.n] = px + (this.r.nextFloat() - 0.5f) * 0.15f;
                this.y[this.n] = py + (this.r.nextFloat() - 0.5f) * 0.15f;
                this.z[this.n] = pz + (this.r.nextFloat() - 0.5f) * 0.15f;
                this.vx[this.n] = dx + (this.r.nextFloat() - 0.5f) * spread;
                this.vy[this.n] = dy + (this.r.nextFloat() - 0.5f) * spread * 0.7f + spread * 0.25f;
                this.vz[this.n] = dz + (this.r.nextFloat() - 0.5f) * spread;
                this.s[this.n] = size * (0.5f + this.r.nextFloat());
                this.splat[this.n] = 1.0f + this.r.nextFloat() * 2.2f;
                this.rot[this.n] = this.r.nextFloat() * 6.28f;
                this.col[this.n] = this.cols[this.r.nextInt(this.cols.length)];
                this.down[this.n] = false;
                ++i;
                ++this.n;
            }
        }

        void step(float dt) {
            for (int i = 0; i < this.n; ++i) {
                if (this.down[i]) continue;
                int n = i;
                this.vy[n] = this.vy[n] - 14.0f * dt;
                int n2 = i;
                this.x[n2] = this.x[n2] + this.vx[i] * dt;
                int n3 = i;
                this.y[n3] = this.y[n3] + this.vy[i] * dt;
                int n4 = i;
                this.z[n4] = this.z[n4] + this.vz[i] * dt;
                if (!(this.y[i] <= 0.015f)) continue;
                this.y[i] = 0.015f;
                this.down[i] = true;
                this.col[i] = DeathFx.mix(this.col[i], 4851730, 0.45f) & 0xFFFFFF;
            }
        }

        void draw(VertexConsumer w, Matrix4f m, float fade, int light) {
            if (fade <= 0.01f) {
                return;
            }
            for (int i = 0; i < this.n; ++i) {
                int c = 0xFF000000 | this.col[i];
                boolean hot = this.col[i] == 16722501;
                Matrix4f dm = new Matrix4f((Matrix4fc)m).translate(this.x[i], this.y[i], this.z[i]).rotateY(this.rot[i]);
                if (!this.down[i]) {
                    float h = this.s[i] / 2.0f * fade;
                    dm.rotateX(this.rot[i] * this.y[i]);
                    DeathFx.cube(w, dm, -h, -h, -h, h, h, h, c, hot ? 0xF000F0 : light);
                    continue;
                }
                float hx = this.s[i] * this.splat[i] / 2.0f * fade;
                float hz = this.s[i] * this.splat[i] * 0.4f * fade;
                DeathFx.cube(w, dm, -hx, -0.004f, -hz, hx, 0.004f, hz, c, light);
            }
        }
    }

    static final class Rig {
        float px;
        float py = 1.0f;
        float pz;
        float rx;
        float ry;
        float rz;
        float belly = 1.0f;
        final float[] head = new float[3];
        final float[] armR = new float[3];
        final float[] armL = new float[3];
        final float[] legR = new float[3];
        final float[] legL = new float[3];

        Rig() {
        }

        void copy(Rig o) {
            this.px = o.px;
            this.py = o.py;
            this.pz = o.pz;
            this.rx = o.rx;
            this.ry = o.ry;
            this.rz = o.rz;
            this.belly = o.belly;
            System.arraycopy(o.head, 0, this.head, 0, 3);
            System.arraycopy(o.armR, 0, this.armR, 0, 3);
            System.arraycopy(o.armL, 0, this.armL, 0, 3);
            System.arraycopy(o.legR, 0, this.legR, 0, 3);
            System.arraycopy(o.legL, 0, this.legL, 0, 3);
        }

        void zero() {
            this.px = 0.0f;
            this.py = 1.0f;
            this.pz = 0.0f;
            this.rz = 0.0f;
            this.ry = 0.0f;
            this.rx = 0.0f;
            this.belly = 1.0f;
            for (float[] a : new float[][]{this.head, this.armR, this.armL, this.legR, this.legL}) {
                Arrays.fill(a, 0.0f);
            }
        }
    }

    static final class Vox {
        int part;
        float cx;
        float cy;
        float cz;
        final Vector3f p = new Vector3f();
        final Vector3f v = new Vector3f();
        final Vector3f w = new Vector3f();
        final Vector3f r = new Vector3f();
        final Quaternionf q = new Quaternionf();
        int tint = -1;
        float floor;
        float delay;

        Vox() {
        }
    }
}

