/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.rendertype.RenderType
 */
package dev.neonlyrics.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neonlyrics.compat.MultiBufferSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class Buffers
implements MultiBufferSource.BufferSource {
    public static final Buffers INSTANCE = new Buffers();
    private final List<Seg> segs = new ArrayList<Seg>();
    private final Map<RenderType, Rec> open = new HashMap<RenderType, Rec>();
    private static final int POS = 1;
    private static final int COL = 2;
    private static final int UV = 4;
    private static final int UV1 = 8;
    private static final int UV2 = 16;
    private static final int NRM = 32;
    private static final int LW = 64;
    private static final int UV3 = 128;

    private Buffers() {
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        Rec r = this.open.get(type);
        if (r == null) {
            r = new Rec();
            this.open.put(type, r);
            this.segs.add(new Seg(type, r));
        }
        return r;
    }

    @Override
    public void endBatch(RenderType type) {
        this.open.remove(type);
    }

    public void flush(SubmitNodeCollector collector) {
        PoseStack identity = new PoseStack();
        for (Seg s : this.segs) {
            if (s.rec().n == 0) continue;
            Rec copy = s.rec().snapshot();
            collector.submitCustomGeometry(identity, s.type(), (pose, vc) -> copy.replay(vc));
        }
        this.segs.clear();
        this.open.clear();
    }

    static void setUv3On(VertexConsumer vc, float u, float v) {
    }

    static final class Rec
    implements VertexConsumer {
        int n = 0;
        int used = 0;
        float[] f = new float[704];
        int[] i = new int[192];

        Rec() {
        }

        private int fo() {
            return (this.n - 1) * 11;
        }

        private int io() {
            return (this.n - 1) * 3;
        }

        Rec snapshot() {
            Rec r = new Rec();
            r.n = this.n;
            r.used = this.used;
            r.f = Arrays.copyOf(this.f, this.n * 11);
            r.i = Arrays.copyOf(this.i, this.n * 3);
            return r;
        }

        void replay(VertexConsumer vc) {
            for (int k = 0; k < this.n; ++k) {
                int a = k * 11;
                int b = k * 3;
                vc.addVertex(this.f[a], this.f[a + 1], this.f[a + 2]);
                if ((this.used & 2) != 0) {
                    vc.setColor(this.i[b]);
                }
                if ((this.used & 4) != 0) {
                    vc.setUv(this.f[a + 3], this.f[a + 4]);
                }
                if ((this.used & 8) != 0) {
                    vc.setUv1(this.i[b + 1] & 0xFFFF, this.i[b + 1] >>> 16);
                }
                if ((this.used & 0x10) != 0) {
                    vc.setUv2(this.i[b + 2] & 0xFFFF, this.i[b + 2] >>> 16);
                }
                if ((this.used & 0x20) != 0) {
                    vc.setNormal(this.f[a + 5], this.f[a + 6], this.f[a + 7]);
                }
                if ((this.used & 0x40) != 0) {
                    vc.setLineWidth(this.f[a + 8]);
                }
                if ((this.used & 0x80) == 0) continue;
                Buffers.setUv3On(vc, this.f[a + 9], this.f[a + 10]);
            }
        }

        public VertexConsumer addVertex(float x, float y, float z) {
            ++this.n;
            if (this.n * 11 > this.f.length) {
                this.f = Arrays.copyOf(this.f, this.f.length * 2);
            }
            if (this.n * 3 > this.i.length) {
                this.i = Arrays.copyOf(this.i, this.i.length * 2);
            }
            int a = this.fo();
            this.f[a] = x;
            this.f[a + 1] = y;
            this.f[a + 2] = z;
            this.used |= 1;
            return this;
        }

        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this.setColor((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF);
        }

        public VertexConsumer setColor(int argb) {
            this.i[this.io()] = argb;
            this.used |= 2;
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            int a = this.fo();
            this.f[a + 3] = u;
            this.f[a + 4] = v;
            this.used |= 4;
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            this.i[this.io() + 1] = u & 0xFFFF | v << 16;
            this.used |= 8;
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            this.i[this.io() + 2] = u & 0xFFFF | v << 16;
            this.used |= 0x10;
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            int a = this.fo();
            this.f[a + 5] = x;
            this.f[a + 6] = y;
            this.f[a + 7] = z;
            this.used |= 0x20;
            return this;
        }

        public VertexConsumer setLineWidth(float w) {
            this.f[this.fo() + 8] = w;
            this.used |= 0x40;
            return this;
        }

        public VertexConsumer setUv3(float u, float v) {
            int a = this.fo();
            this.f[a + 9] = u;
            this.f[a + 10] = v;
            this.used |= 0x80;
            return this;
        }
    }

    private record Seg(RenderType type, Rec rec) {
    }
}

