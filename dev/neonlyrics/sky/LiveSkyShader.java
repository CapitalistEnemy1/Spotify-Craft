/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_10789
 *  net.minecraft.class_12247
 *  net.minecraft.class_1921
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.neonlyrics.sky;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.config.Storage;
import dev.neonlyrics.sky.LiveSky;
import dev.neonlyrics.sky.SkyPreset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.time.LocalDateTime;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class LiveSkyShader {
    private static final class_2960 TEX = class_2960.method_60655((String)"neonlyrics", (String)"live_sky_params");
    private static final RenderPipeline PIPE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"neonlyrics", (String)"pipeline/live_sky")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexShader(class_2960.method_60655((String)"neonlyrics", (String)"core/live_sky")).withFragmentShader(class_2960.method_60655((String)"neonlyrics", (String)"core/live_sky")).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false).withCull(false).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).build();
    private static class_1921 type;
    private static class_1043 tex;
    private static int state;
    private static final int[] last;
    private static final int[] next;

    private LiveSkyShader() {
    }

    public static boolean ready() {
        if (state != 0) {
            return state > 0;
        }
        try {
            if (!RenderSystem.getDevice().precompilePipeline(PIPE).isValid()) {
                NeonLyrics.LOG.warn("Himmel-Shader laesst sich nicht kompilieren - nehme den alten Himmel");
                state = -1;
                return false;
            }
            class_1011 img = new class_1011(16, 1, false);
            tex = new class_1043(() -> "neonlyrics_live_sky", img);
            class_310.method_1551().method_1531().method_4616(TEX, (class_1044)tex);
            type = class_1921.method_75940((String)"neonlyrics_live_sky", (class_12247)class_12247.method_75927((RenderPipeline)PIPE).method_75934("Sampler0", TEX).method_75938());
            state = 1;
            NeonLyrics.LOG.info("Himmel-Shader aktiv");
        }
        catch (Throwable t) {
            NeonLyrics.LOG.warn("Himmel-Shader nicht verfuegbar - nehme den alten Himmel", t);
            state = -1;
        }
        LiveSkyShader.status(state > 0 ? "aktiv" : "aus (alter Himmel)");
        return state > 0;
    }

    private static void status(String s) {
        try {
            Object gpu = "";
            try {
                gpu = RenderSystem.getDevice().getRenderer() + " / " + RenderSystem.getDevice().getVersion();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            Files.writeString(Storage.dir().resolve("sky-status.txt"), (CharSequence)(String.valueOf(LocalDateTime.now()) + "\nHimmel-Shader: " + s + "\nGrafik: " + (String)gpu + "\n"), new OpenOption[0]);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void disable(Throwable t) {
        NeonLyrics.LOG.warn("Himmel-Shader abgeschaltet", t);
        state = -1;
        LiveSkyShader.status("Fehler beim Zeichnen: " + String.valueOf(t));
    }

    public static class_1921 type() {
        return type;
    }

    public static void params(SkyPreset p, int a1, int a2, double t, Config c, Matrix4f pose) {
        float pattern = 1.0f;
        float lively = 0.6f;
        float breath = 1.0f;
        float aurora = 1.0f;
        if (c != null) {
            pattern = Math.max(0.2f, Math.min(1.5f, c.skyPattern));
            lively = Math.max(0.0f, Math.min(1.0f, c.skyLively));
            breath = c.skyBreath <= 0.01f ? 1.0f : 0.85f + 0.15f * (float)Math.sin(t * 0.45 * (double)Math.max(0.2f, c.skyBreath));
            aurora = c.fxAurora ? 1.0f : 0.0f;
        }
        LiveSkyShader.next[0] = LiveSkyShader.rgba(p.top, 255);
        LiveSkyShader.next[1] = LiveSkyShader.rgba(p.horizon, 255);
        LiveSkyShader.next[2] = LiveSkyShader.rgba(a1, 255);
        LiveSkyShader.next[3] = LiveSkyShader.rgba(a2, 255);
        LiveSky.Body b = LiveSky.body(p);
        LiveSkyShader.next[4] = b == null ? 0 : LiveSkyShader.rgba(b.color(), b.kind() + 1);
        LiveSkyShader.next[5] = LiveSkyShader.bytes(LiveSkyShader.b8(pattern / 1.5f), LiveSkyShader.b8(lively), LiveSkyShader.b8(breath), LiveSkyShader.b8(aurora));
        LiveSkyShader.next[6] = b == null ? 0 : LiveSkyShader.pair(b.az() * 100.0, (b.el() + 90.0) * 100.0);
        LiveSkyShader.next[7] = (b == null ? 0 : LiveSkyShader.pair(b.radius() * 1000.0, 0.0) & 0xFFFF0000) | p.ordinal() << 8;
        Matrix3f inv = new Matrix3f().set((Matrix4fc)pose).invert();
        float[] m = new float[]{inv.m00, inv.m01, inv.m02, inv.m10, inv.m11, inv.m12, inv.m20, inv.m21, inv.m22, 0.0f};
        for (int i = 0; i < 5; ++i) {
            LiveSkyShader.next[8 + i] = LiveSkyShader.pair(LiveSkyShader.enc(m[i * 2]), LiveSkyShader.enc(m[i * 2 + 1]));
        }
        boolean changed = false;
        for (int i = 0; i < 16; ++i) {
            if (next[i] == last[i]) continue;
            LiveSkyShader.last[i] = next[i];
            changed = true;
            int v = next[i];
            tex.method_4525().method_61941(i, 0, (v & 0xFF) << 24 | v >>> 8);
        }
        if (changed) {
            tex.method_4524();
        }
    }

    private static int pair(double v1, double v2) {
        int a = (int)Math.max(0L, Math.min(65535L, Math.round(v1)));
        int b = (int)Math.max(0L, Math.min(65535L, Math.round(v2)));
        return a << 16 | b;
    }

    private static double enc(float v) {
        return ((double)Math.max(-2.0f, Math.min(2.0f, v)) + 2.0) / 4.0 * 65535.0;
    }

    private static int b8(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255.0f)));
    }

    private static int rgba(int rgb, int a) {
        return (rgb & 0xFFFFFF) << 8 | a & 0xFF;
    }

    private static int bytes(int r, int g, int b, int a) {
        return r << 24 | g << 16 | b << 8 | a;
    }

    static {
        state = 0;
        last = new int[16];
        next = new int[16];
    }
}

