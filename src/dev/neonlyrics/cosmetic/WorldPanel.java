/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package dev.neonlyrics.cosmetic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.compat.Buffers;
import dev.neonlyrics.compat.MultiBufferSource;
import dev.neonlyrics.compat.TextDraw;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.cosmetic.Accessories;
import dev.neonlyrics.cosmetic.PhoneMenu;
import dev.neonlyrics.gui.LyricsScreen;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.hud.SkyThumbs;
import dev.neonlyrics.lyrics.NowPlaying;
import dev.neonlyrics.lyrics.SpotifyClient;
import dev.neonlyrics.sky.SkyPreset;
import dev.neonlyrics.util.Fx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

final class WorldPanel {
    static final int W = 380;
    static final int H = 222;
    static final float K = 0.0105f;
    private static final float RCX = 98.0f;
    private static final float RCY = 104.0f;
    private static final float RR = 78.0f;
    private static final float BX = 62.0f;
    private static final float CX0 = 206.0f;
    private static final float CX1 = 366.0f;
    private static final int ACCENT = 10321407;
    private static final int ACCENT2 = 0xDCCFFF;
    private static final int DEEP = 5914576;
    private static final int TOP = 1709620;
    private static final int BOTTOM = 723479;
    private static final int LIGHT = 0xF000F0;
    private static RenderType flat;
    private final Vec3 pos;
    private final float rx;
    private final float rz;
    private final float nx;
    private final float nz;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<String, Float> hov = new HashMap<String, Float>();
    private int page;
    private int skyPage;
    private int hatPage;
    private final long born = System.currentTimeMillis();
    private long closing = -1L;
    private long pageTime = 0L;
    private long lastNs = System.nanoTime();
    private float dt;
    private float spin;
    private boolean hovered;
    private boolean sndLand;
    private boolean sndClose;
    private float hx;
    private float hy;
    private static final String[] TABS;
    private Matrix4f m;
    private float ox;
    private float oy;
    private VertexConsumer q;
    private final List<Runnable> late = new ArrayList<Runnable>();

    private static RenderType flat() {
        if (flat == null) {
            flat = RenderType.create((String)"neonlyrics_panel", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.DEBUG_QUADS).createRenderSetup());
        }
        return flat;
    }

    private static String tr(String key) {
        return Component.translatable((String)("neonlyrics.wp." + key)).getString();
    }

    WorldPanel(Minecraft mc) {
        Camera cam = mc.gameRenderer.getMainCamera();
        Vector3fc f = cam.forwardVector();
        double fl = Math.sqrt(f.x() * f.x() + f.z() * f.z());
        float fx = fl < 1.0E-4 ? 0.0f : (float)((double)f.x() / fl);
        float fz = fl < 1.0E-4 ? 1.0f : (float)((double)f.z() / fl);
        Vec3 c = cam.position();
        this.pos = new Vec3(c.x + (double)fx * 3.2, c.y - 0.05, c.z + (double)fz * 3.2);
        this.nx = -fx;
        this.nz = -fz;
        this.rx = -fz;
        this.rz = fx;
        WorldPanel.play(mc, SoundEvents.AMETHYST_BLOCK_CHIME, 1.3f, 0.8f);
    }

    Vec3 pos() {
        return this.pos;
    }

    boolean hovered() {
        return this.hovered;
    }

    boolean closing() {
        return this.closing >= 0L;
    }

    void close() {
        if (this.closing < 0L) {
            this.closing = System.currentTimeMillis();
        }
    }

    boolean finished() {
        return this.closing >= 0L && System.currentTimeMillis() - this.closing > 900L;
    }

    private float age() {
        return System.currentTimeMillis() - this.born;
    }

    private float closeAge() {
        return this.closing < 0L ? -1.0f : (float)(System.currentTimeMillis() - this.closing);
    }

    void tick(Minecraft mc) {
        if (!this.sndLand && this.age() > 860.0f) {
            this.sndLand = true;
            WorldPanel.play(mc, SoundEvents.BEACON_POWER_SELECT, 1.8f, 0.3f);
        }
        if (!this.sndClose && this.closing >= 0L && this.closeAge() > 360.0f) {
            this.sndClose = true;
            WorldPanel.play(mc, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, 0.7f);
        }
    }

    private static void play(Minecraft mc, SoundEvent ev, float pitch, float vol) {
        try {
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)ev, (float)pitch, (float)vol));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void aim(Camera cam) {
        this.hovered = false;
        if (this.age() < 1200.0f || this.closing >= 0L) {
            return;
        }
        Vec3 o = cam.position();
        Vector3fc d = cam.forwardVector();
        double denom = d.x() * this.nx + d.z() * this.nz;
        if (denom > -1.0E-4) {
            return;
        }
        double t = ((this.pos.x - o.x) * (double)this.nx + (this.pos.z - o.z) * (double)this.nz) / denom;
        if (t <= 0.0 || t > 14.0) {
            return;
        }
        double px = o.x + (double)d.x() * t - this.pos.x;
        double py = o.y + (double)d.y() * t - this.pos.y;
        double pz = o.z + (double)d.z() * t - this.pos.z;
        float u = (float)((px * (double)this.rx + pz * (double)this.rz) / (double)0.0105f) + 190.0f;
        float v = 111.0f - (float)(py / (double)0.0105f);
        if (u < 14.0f || v < 0.0f || u > 380.0f || v > 222.0f) {
            return;
        }
        this.hovered = true;
        this.hx = u;
        this.hy = v;
    }

    private boolean over(float x, float y, float w, float h) {
        return this.hovered && this.hx >= x && this.hy >= y && this.hx < x + w && this.hy < y + h;
    }

    private float hv(String id, boolean on) {
        float v = this.hov.getOrDefault(id, Float.valueOf(0.0f)).floatValue();
        v += ((on ? 1.0f : 0.0f) - v) * Math.min(1.0f, this.dt * 14.0f);
        this.hov.put(id, Float.valueOf(v));
        return v;
    }

    void click() {
        if (!this.hovered || this.closing >= 0L) {
            return;
        }
        for (int i = this.hits.size() - 1; i >= 0; --i) {
            Hit h = this.hits.get(i);
            if (!(this.hx >= h.x()) || !(this.hy >= h.y()) || !(this.hx < h.x() + h.w()) || !(this.hy < h.y() + h.h())) continue;
            Fx.sound(Fx.Sfx.CLICK);
            h.run().run();
            NeonLyrics.saveConfig();
            return;
        }
    }

    private void go(int p) {
        if (p == this.page) {
            return;
        }
        this.page = p;
        this.pageTime = System.currentTimeMillis();
    }

    void render(Minecraft mc, MultiBufferSource buffers, Matrix4f base) {
        float arm;
        float content;
        float slide;
        float fly;
        Camera cam = mc.gameRenderer.getMainCamera();
        this.aim(cam);
        long ns = System.nanoTime();
        this.dt = Math.min(0.1f, (float)(ns - this.lastNs) / 1.0E9f);
        this.lastNs = ns;
        float t = this.age();
        float c = this.closeAge();
        if (c < 0.0f) {
            fly = WorldPanel.inOut(WorldPanel.clamp(t / 900.0f));
            slide = WorldPanel.outCubic(WorldPanel.clamp((t - 820.0f) / 460.0f));
            content = WorldPanel.outCubic(WorldPanel.clamp((t - 1100.0f) / 300.0f));
            arm = WorldPanel.inOut(WorldPanel.clamp((t - 1150.0f) / 400.0f));
        } else {
            arm = 1.0f - WorldPanel.inOut(WorldPanel.clamp(c / 220.0f));
            content = 1.0f - WorldPanel.clamp(c / 160.0f);
            slide = 1.0f - WorldPanel.inOut(WorldPanel.clamp((c - 120.0f) / 300.0f));
            fly = 1.0f - WorldPanel.inOut(WorldPanel.clamp((c - 380.0f) / 480.0f));
        }
        float w = 3.5f + 16.5f * (float)Math.exp(-t / 420.0f) + (c >= 0.0f ? 10.0f * WorldPanel.clamp(c / 600.0f) : 0.0f);
        this.spin += w * this.dt;
        Vec3 cp = cam.position();
        float bob = (float)Math.sin((double)System.currentTimeMillis() / 1200.0) * 0.012f * slide;
        Matrix4f rot = new Matrix4f(this.rx, 0.0f, this.rz, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, this.nx, 0.0f, this.nz, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        Matrix4f panel = new Matrix4f((Matrix4fc)base).translate((float)(this.pos.x - cp.x), (float)(this.pos.y - cp.y) + bob, (float)(this.pos.z - cp.z)).mul((Matrix4fc)rot);
        this.q = buffers.getBuffer(WorldPanel.flat());
        this.hits.clear();
        this.late.clear();
        Config cf = NeonLyrics.cfg();
        NowPlaying np = SpotifyClient.now();
        if (slide > 0.01f) {
            float anchor = -0.96599996f;
            this.m = new Matrix4f((Matrix4fc)panel).translate(anchor, 0.0f, 0.0f).scale(slide, 1.0f, 1.0f).translate(-anchor, 0.0f, 0.0f);
            this.ox = 190.0f;
            this.oy = 111.0f;
            float a = Math.min(1.0f, slide * 1.5f);
            for (int i = 4; i >= 1; --i) {
                this.round(62.0f - (float)i * 2.5f, (float)(-i) * 2.5f, 318.0f + (float)i * 5.0f, 222.0f + (float)i * 5.0f, 14.0f + (float)i * 2.5f, 10321407, 10321407, 0.05f * a, 0.0f);
            }
            this.round(61.2f, -0.8f, 319.6f, 223.6f, 14.8f, 12167664, 7231688, 0.35f * a, 0.0f);
            this.round(62.0f, 0.0f, 318.0f, 222.0f, 14.0f, 1709620, 723479, 0.96f * a, 0.0f);
            this.round(63.0f, 0.6f, 316.0f, 1.2f, 1.0f, 0xFFFFFF, 0xFFFFFF, 0.1f * a, 0.0f);
            if (content > 0.01f) {
                this.body(mc, cf, np, content);
            }
        }
        float recX = -0.96599996f;
        float recY = 0.0735f;
        Vector3f end = new Vector3f(this.rx * recX, recY + bob, this.rz * recX).add((float)(this.pos.x - cp.x), (float)(this.pos.y - cp.y), (float)(this.pos.z - cp.z));
        Vector3fc f = cam.forwardVector();
        Vector3fc u = cam.upVector();
        Vector3f start = new Vector3f(f).mul(0.9f).add((Vector3fc)new Vector3f(u).mul(-0.3f));
        Vector3f p = new Vector3f((Vector3fc)start).lerp((Vector3fc)end, fly).add(0.0f, (float)Math.sin(Math.PI * (double)fly) * 0.45f, 0.0f);
        float scale = 0.12f + 0.88f * WorldPanel.outCubic(fly);
        float tilt = (1.0f - fly) * 70.0f;
        float ra = Math.min(1.0f, (c < 0.0f ? t : 900.0f - c) / 150.0f);
        if (ra > 0.01f) {
            this.m = new Matrix4f((Matrix4fc)base).translate((Vector3fc)p).mul((Matrix4fc)rot).rotateX((float)Math.toRadians(-tilt)).scale(scale).translate(0.0f, 0.0f, 0.006f);
            this.ox = 0.0f;
            this.oy = 0.0f;
            this.record(np, ra, arm);
        }
        if (this.hovered) {
            this.m = new Matrix4f((Matrix4fc)panel);
            this.ox = 190.0f;
            this.oy = 111.0f;
            this.ring(this.hx, this.hy, 0.0f, 2.2f, 0.0f, 360.0f, 0xFFFFFF, 0.95f, 0xFFFFFF, 0.95f, 0.02f, 12);
            this.ring(this.hx, this.hy, 2.2f, 5.5f, 0.0f, 360.0f, 10321407, 0.35f, 10321407, 0.0f, 0.02f, 12);
        }
        if (buffers instanceof MultiBufferSource.BufferSource) {
            MultiBufferSource.BufferSource bs = (MultiBufferSource.BufferSource)buffers;
            bs.endBatch(WorldPanel.flat());
        }
        for (Runnable r : this.late) {
            r.run();
        }
    }

    private void record(NowPlaying np, float a, float arm) {
        float R = 78.0f;
        this.ring(0.0f, 0.0f, R, R + 14.0f, 0.0f, 360.0f, 10321407, 0.32f * a, 10321407, 0.0f, 0.0f, 48);
        int bands = 10;
        for (int i = 0; i < bands; ++i) {
            float r0 = R * (0.38f + 0.62f * (float)i / (float)bands);
            float r1 = R * (0.38f + 0.62f * (float)(i + 1) / (float)bands);
            int col = i % 2 == 0 ? 986900 : 0x17171E;
            this.ring(0.0f, 0.0f, r0, r1, 0.0f, 360.0f, col, a, col, a, 0.001f, 48);
        }
        this.ring(0.0f, 0.0f, R - 1.4f, R, 0.0f, 360.0f, 0x34343F, a, 0x34343F, a, 0.002f, 48);
        float deg = (float)Math.toDegrees(this.spin);
        for (int k = 0; k < 4; ++k) {
            float r0 = R * (0.48f + 0.12f * (float)k);
            float s0 = deg + (float)(k * 97);
            this.ring(0.0f, 0.0f, r0, r0 + 1.1f, s0, s0 + 38.0f, 0x45455A, a, 0x45455A, a, 0.002f, 8);
        }
        this.ring(0.0f, 0.0f, R * 0.4f, R * 0.98f, 300.0f, 330.0f, 0xFFFFFF, 0.02f * a, 0xFFFFFF, 0.11f * a, 0.003f, 6);
        this.ring(0.0f, 0.0f, R * 0.4f, R * 0.98f, 120.0f, 150.0f, 0xFFFFFF, 0.02f * a, 0xFFFFFF, 0.08f * a, 0.003f, 6);
        this.ring(0.0f, 0.0f, 0.0f, R * 0.36f, 0.0f, 360.0f, 0xDCCFFF, a, 5914576, a, 0.004f, 40);
        this.ring(0.0f, 0.0f, R * 0.33f, R * 0.36f, 0.0f, 360.0f, 5914576, a, 5914576, a, 0.005f, 40);
        this.ring(0.0f, 0.0f, 0.0f, R * 0.04f, 0.0f, 360.0f, 526348, a, 526348, a, 0.006f, 16);
        Matrix4f lm = new Matrix4f((Matrix4fc)this.m).rotateZ(-this.spin);
        this.textAt(lm, "LYRICS CORD", 0.0f, -R * 0.2f, 0.5f, 0xFFFFFF, a, true);
        String sub = np.hasTrack() ? np.artist : WorldPanel.tr("nomusic");
        this.textAt(lm, WorldPanel.ellip(sub, 70.0f, 0.42f), 0.0f, R * 0.1f, 0.42f, 0xFFFFFF, 0.85f * a, true);
        float px = R * 0.98f;
        float py = -R * 0.92f;
        float th = (float)Math.toRadians(88.0f + 15.0f * arm);
        float L = R * 1.05f;
        float dx = (float)Math.cos(th);
        float dy = (float)Math.sin(th);
        float nx2 = -dy * 1.7f;
        float ny2 = dx * 1.7f;
        float ex = px + dx * L;
        float ey = py + dy * L;
        this.poly(px + nx2, py + ny2, ex + nx2, ey + ny2, ex - nx2, ey - ny2, px - nx2, py - ny2, 14211298, a, 0.008f);
        this.poly(ex - 4.0f * dx + 3.5f * -dy, ey - 4.0f * dy + 3.5f * dx, ex + 5.0f * dx + 3.5f * -dy, ey + 5.0f * dy + 3.5f * dx, ex + 5.0f * dx - 3.5f * -dy, ey + 5.0f * dy - 3.5f * dx, ex - 4.0f * dx - 3.5f * -dy, ey - 4.0f * dy - 3.5f * dx, 11053238, a, 0.009f);
        this.ring(px, py, 0.0f, 8.0f, 0.0f, 360.0f, 5921384, a, 2894902, a, 0.009f, 20);
        this.ring(px, py, 0.0f, 3.0f, 0.0f, 360.0f, 0xE0E0EA, a, 0xE0E0EA, a, 0.01f, 12);
    }

    private void body(Minecraft mc, Config cf, NowPlaying np, float a) {
        float by = 197.0f;
        this.roundBtn("prev", 72.0f, by, 9.0f, "\u23ee", a, () -> SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS));
        this.roundBtn("play", 98.0f, by, 10.5f, np.playing ? "\u23f8" : "\u25b6", a, () -> SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE));
        this.roundBtn("next", 124.0f, by, 9.0f, "\u23ed", a, () -> SpotifyClient.control(SpotifyClient.Cmd.NEXT));
        this.hits.add(new Hit(43.4f, 49.4f, 109.2f, 109.2f, () -> SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE)));
        Font font = Minecraft.getInstance().font;
        this.text("Lyrics Cord", 206.0f, 10.0f, 0.85f, 0xFFFFFF, a);
        float xh = this.hv("x", this.over(351.0f, 6.0f, 15.0f, 15.0f));
        this.ring(358.5f, 13.5f, 0.0f, 7.5f, 0.0f, 360.0f, Gfx.lerpColor(0xFFFFFF, 16734842, xh), (0.08f + 0.55f * xh) * a, Gfx.lerpColor(0xFFFFFF, 16734842, xh), (0.08f + 0.55f * xh) * a, 0.002f, 16);
        this.text("\u2715", 355.5f, 10.5f, 0.7f, 0xFFFFFF, a);
        this.hits.add(new Hit(351.0f, 6.0f, 15.0f, 15.0f, this::close));
        String nl = WorldPanel.tr("normal");
        float nw = (float)font.width(nl) * 0.62f + 12.0f;
        float nhv = this.hv("nrm", this.over(346.0f - nw, 6.0f, nw, 15.0f));
        this.round(346.0f - nw, 6.0f, nw, 15.0f, 7.5f, 0xFFFFFF, 0xFFFFFF, (0.07f + 0.12f * nhv) * a, 0.002f);
        this.text(nl, 352.0f - nw, 10.5f, 0.62f, 0xFFFFFF, (0.8f + 0.2f * nhv) * a);
        this.hits.add(new Hit(346.0f - nw, 6.0f, nw, 15.0f, () -> {
            cf.menuMode = 0;
            PhoneMenu.closeNow();
            mc.setScreen((Screen)new LyricsScreen());
        }));
        float tabW = 37.0f;
        for (int i = 0; i < TABS.length; ++i) {
            float x = 206.0f + (float)i * (tabW + 4.0f);
            boolean on = this.page == i;
            float h = this.hv("tab" + i, this.over(x, 26.0f, tabW, 14.0f));
            int k = i;
            if (on) {
                this.round(x, 26.0f, tabW, 14.0f, 7.0f, 10321407, 8018664, 0.9f * a, 0.002f);
            } else {
                this.round(x, 26.0f, tabW, 14.0f, 7.0f, 0xFFFFFF, 0xFFFFFF, (0.05f + 0.08f * h) * a, 0.002f);
            }
            String lbl = WorldPanel.ellip(WorldPanel.tr(TABS[i]), tabW - 6.0f, 0.62f);
            this.text(lbl, x + (tabW - (float)font.width(lbl) * 0.62f) / 2.0f, 30.0f, 0.62f, 0xFFFFFF, (on ? 1.0f : 0.7f + 0.3f * h) * a);
            this.hits.add(new Hit(x, 26.0f, tabW, 14.0f, () -> this.go(k)));
        }
        String song = np.hasTrack() ? "\u266a  " + np.title + "  \u00b7  " + np.artist : "\u266a  " + WorldPanel.tr("nomusic");
        this.text(WorldPanel.ellip(song, 160.0f, 0.62f), 206.0f, 208.0f, 0.62f, 0xDCCFFF, 0.75f * a);
        float pa = a * WorldPanel.outCubic(WorldPanel.clamp((float)(System.currentTimeMillis() - this.pageTime) / 220.0f));
        switch (this.page) {
            case 0: {
                this.skies(cf, pa);
                break;
            }
            case 1: {
                this.lyrics(cf, pa);
                break;
            }
            case 2: {
                this.style(cf, pa);
                break;
            }
            default: {
                this.more(mc, cf, pa);
            }
        }
    }

    private void skies(Config cf, float a) {
        int idx;
        SkyPreset[] all = LyricsScreen.skyOrder();
        int per = 6;
        int pages = (all.length + per - 1) / per;
        this.skyPage = Math.floorMod(this.skyPage, pages);
        float tw = 48.0f;
        float th = 27.0f;
        for (int i = 0; i < per && (idx = this.skyPage * per + i) < all.length; ++i) {
            SkyPreset p = all[idx];
            float x = 206.0f + (float)(i % 3) * (tw + 8.0f);
            float y = 47.0f + (float)(i / 3) * (th + 14.0f);
            boolean on = cf.sky == p.ordinal();
            float h = this.hv("sky" + idx, this.over(x - 1.0f, y - 1.0f, tw + 2.0f, th + 12.0f));
            float lift = h * 1.2f;
            if (on) {
                this.round(x - 2.0f, y - 2.0f - lift, tw + 4.0f, th + 4.0f, 6.0f, 10321407, 0xDCCFFF, a, 0.002f);
            } else if (h > 0.01f) {
                this.round(x - 1.5f, y - 1.5f - lift, tw + 3.0f, th + 3.0f, 5.5f, 0xFFFFFF, 0xFFFFFF, 0.5f * h * a, 0.002f);
            }
            Identifier img = SkyThumbs.texture(p);
            if (img != null) {
                this.image(img, x, y - lift, tw, th, a);
            } else {
                this.round(x, y - lift, tw, th, 4.0f, p.top, p.horizon, a, 0.003f);
            }
            this.text(WorldPanel.ellip(Component.translatable((String)p.key()).getString(), tw, 0.56f), x + 1.0f, y + th + 3.0f, 0.56f, 0xFFFFFF, a * (on ? 1.0f : 0.75f + 0.25f * h));
            this.hits.add(new Hit(x - 1.0f, y - 1.0f, tw + 2.0f, th + 12.0f, () -> {
                cf.lastSky = cf.sky = p.ordinal();
            }));
        }
        this.pager("sk", 128.0f, this.skyPage, pages, d -> this.skyPage += d, a);
        this.slider("str", WorldPanel.tr("strength"), 148.0f, (cf.skyStrength - 0.2f) / 1.3f, v -> {
            cf.skyStrength = 0.2f + 1.3f * v.floatValue();
        }, a);
        this.slider("spd", WorldPanel.tr("speed"), 167.0f, (cf.skySpeed - 0.25f) / 1.75f, v -> {
            cf.skySpeed = 0.25f + 1.75f * v.floatValue();
        }, a);
        this.slider("pat", WorldPanel.tr("pattern"), 186.0f, (cf.skyPattern - 0.2f) / 1.3f, v -> {
            cf.skyPattern = 0.2f + 1.3f * v.floatValue();
        }, a);
    }

    private void lyrics(Config cf, float a) {
        this.toggle(0, WorldPanel.tr("lyricssky"), cf.lyricsSky, () -> {
            cf.lyricsSky = !cf.lyricsSky;
        }, a);
        this.toggle(1, WorldPanel.tr("floating"), cf.lyricsStyle == 1, () -> {
            cf.lyricsStyle = cf.lyricsStyle == 1 ? 0 : 1;
        }, a);
        this.toggle(2, WorldPanel.tr("songintro"), cf.fxSongIntro, () -> {
            cf.fxSongIntro = !cf.fxSongIntro;
        }, a);
        this.toggle(3, WorldPanel.tr("beat"), cf.fxBeatPulse, () -> {
            cf.fxBeatPulse = !cf.fxBeatPulse;
        }, a);
        this.toggle(4, WorldPanel.tr("skymusic"), cf.fxSkyMusic, () -> {
            cf.fxSkyMusic = !cf.fxSkyMusic;
        }, a);
        this.slider("lsz", WorldPanel.tr("size"), 160.0f, (cf.lyricsSize - 0.8f) / 1.8f, v -> {
            cf.lyricsSize = 0.8f + 1.8f * v.floatValue();
        }, a);
    }

    private void style(Config cf, float a) {
        int idx;
        int i;
        String[] hats = Accessories.HATS;
        int per = 6;
        int pages = (hats.length + per - 1) / per;
        this.hatPage = Math.floorMod(this.hatPage, pages);
        this.text(WorldPanel.tr("hat"), 206.0f, 45.0f, 0.62f, 0xFFFFFF, 0.55f * a);
        for (i = 0; i < per && (idx = this.hatPage * per + i) < hats.length; ++i) {
            this.option("hat" + idx, 206.0f + (float)(i % 2 * 82), 54 + i / 2 * 18, 78.0f, 15.0f, Component.translatable((String)("neonlyrics.ui.hat." + hats[idx])).getString(), cf.hat == idx, () -> {
                cf.hat = idx;
            }, a);
        }
        this.pager("ht", 108.0f, this.hatPage, pages, d -> this.hatPage += d, a);
        this.text(WorldPanel.tr("death"), 206.0f, 125.0f, 0.62f, 0xFFFFFF, 0.55f * a);
        for (i = 0; i < 4; ++i) {
            int k = i;
            this.option("de" + i, 206.0f + (float)(i % 2 * 82), 134 + i / 2 * 18, 78.0f, 15.0f, Component.translatable((String)("neonlyrics.ui.death." + i)).getString(), cf.deathFx == i, () -> {
                cf.deathFx = k;
            }, a);
        }
        this.toggle2("aur", 206.0f, 174.0f, 78.0f, WorldPanel.tr("aura"), cf.aura, () -> {
            cf.aura = !cf.aura;
        }, a);
        this.toggle2("hgl", 288.0f, 174.0f, 78.0f, WorldPanel.tr("hatglow"), cf.hatGlow, () -> {
            cf.hatGlow = !cf.hatGlow;
        }, a);
    }

    private void more(Minecraft mc, Config cf, float a) {
        this.toggle(0, WorldPanel.tr("hotbar"), cf.glassHotbar, () -> {
            cf.glassHotbar = !cf.glassHotbar;
        }, a);
        this.toggle(1, WorldPanel.tr("island"), cf.islandEnabled, () -> {
            cf.islandEnabled = !cf.islandEnabled;
        }, a);
        this.toggle(2, WorldPanel.tr("clock"), cf.showClock, () -> {
            cf.showClock = !cf.showClock;
        }, a);
        this.option("m0", 206.0f, 114.0f, 160.0f, 15.0f, "\u2197  " + WorldPanel.tr("bigmenu"), false, () -> {
            PhoneMenu.closeNow();
            mc.setScreen((Screen)new LyricsScreen());
        }, a);
        this.option("m1", 206.0f, 132.0f, 160.0f, 15.0f, "\u2604  " + WorldPanel.tr("random"), false, SkyPreset::shuffle, a);
        this.option("m2", 206.0f, 150.0f, 160.0f, 15.0f, "\u21ba  " + WorldPanel.tr("backnormal"), false, () -> {
            cf.menuMode = 0;
            this.close();
        }, a);
        this.option("m3", 206.0f, 168.0f, 160.0f, 15.0f, "\u2715  " + WorldPanel.tr("close"), false, this::close, a);
    }

    private void toggle(int row, String label, boolean on, Runnable flip, float a) {
        this.toggle2("tg" + row, 206.0f, 47 + row * 20, 160.0f, label, on, flip, a);
    }

    private void toggle2(String id, float x, float y, float w, String label, boolean on, Runnable flip, float a) {
        float h = 16.0f;
        float hh = this.hv(id, this.over(x, y, w, h));
        this.round(x, y, w, h, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.035f + 0.05f * hh) * a, 0.002f);
        this.text(WorldPanel.ellip(label, w - 34.0f, 0.68f), x + 7.0f, y + 5.0f, 0.68f, 0xFFFFFF, 0.92f * a);
        float tx = x + w - 25.0f;
        float ty = y + 4.0f;
        this.round(tx, ty, 19.0f, 8.0f, 4.0f, on ? 10321407 : 0xFFFFFF, on ? 8018664 : 0xFFFFFF, (on ? 0.95f : 0.16f) * a, 0.003f);
        this.ring(on ? tx + 15.0f : tx + 4.0f, ty + 4.0f, 0.0f, 3.2f, 0.0f, 360.0f, 0xFFFFFF, a, 15262966, a, 0.004f, 12);
        this.hits.add(new Hit(x, y, w, h, flip));
    }

    private void option(String id, float x, float y, float w, float h, String label, boolean on, Runnable run, float a) {
        float hh = this.hv(id, this.over(x, y, w, h));
        if (on) {
            this.round(x, y, w, h, 6.0f, 10321407, 8018664, 0.88f * a, 0.002f);
        } else {
            this.round(x, y, w, h, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.035f + 0.06f * hh) * a, 0.002f);
        }
        this.text(WorldPanel.ellip(label, w - 12.0f, 0.66f), x + 7.0f, y + (h - 6.0f) / 2.0f, 0.66f, 0xFFFFFF, (on ? 1.0f : 0.8f + 0.2f * hh) * a);
        this.hits.add(new Hit(x, y, w, h, run));
    }

    private void slider(String id, String label, float y, float v, Consumer<Float> set, float a) {
        v = WorldPanel.clamp(v);
        float tx = 258.0f;
        float tw = 366.0f - tx;
        float hh = this.hv(id, this.over(206.0f, y - 4.0f, 160.0f, 14.0f));
        this.text(label, 206.0f, y, 0.66f, 0xFFFFFF, (0.7f + 0.3f * hh) * a);
        this.round(tx, y + 1.5f, tw, 4.0f, 2.0f, 0xFFFFFF, 0xFFFFFF, (0.12f + 0.06f * hh) * a, 0.002f);
        this.round(tx, y + 1.5f, Math.max(4.0f, tw * v), 4.0f, 2.0f, 0xDCCFFF, 10321407, a, 0.003f);
        this.ring(tx + tw * v, y + 3.5f, 0.0f, 4.5f + hh, 0.0f, 360.0f, 0xFFFFFF, a, 15262966, a, 0.004f, 14);
        this.hits.add(new Hit(tx - 4.0f, y - 4.0f, tw + 8.0f, 14.0f, () -> set.accept(Float.valueOf(WorldPanel.clamp((this.hx - tx) / tw)))));
    }

    private void roundBtn(String id, float cx, float cy, float r, String icon, float a, Runnable run) {
        float hh = this.hv(id, this.over(cx - r, cy - r, 2.0f * r, 2.0f * r));
        this.ring(cx, cy, 0.0f, r, 0.0f, 360.0f, Gfx.lerpColor(2762309, 10321407, hh), a, Gfx.lerpColor(1841460, 8018664, hh), a, 0.002f, 20);
        float tw = (float)Minecraft.getInstance().font.width(icon) * 0.75f;
        this.text(icon, cx - tw / 2.0f, cy - 3.0f, 0.75f, 0xFFFFFF, a);
        this.hits.add(new Hit(cx - r, cy - r, 2.0f * r, 2.0f * r, run));
    }

    private void pager(String id, float y, int cur, int pages, IntConsumer move, float a) {
        if (pages <= 1) {
            return;
        }
        float h1 = this.hv(id + "l", this.over(320.0f, y, 14.0f, 12.0f));
        float h2 = this.hv(id + "r", this.over(352.0f, y, 14.0f, 12.0f));
        this.round(320.0f, y, 14.0f, 12.0f, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.08f + 0.2f * h1) * a, 0.002f);
        this.round(352.0f, y, 14.0f, 12.0f, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.08f + 0.2f * h2) * a, 0.002f);
        this.text("\u2039", 325.0f, y + 2.5f, 0.75f, 0xFFFFFF, a);
        this.text("\u203a", 357.0f, y + 2.5f, 0.75f, 0xFFFFFF, a);
        this.text(cur + 1 + "/" + pages, 336.0f, y + 3.0f, 0.6f, 0xFFFFFF, 0.7f * a);
        this.hits.add(new Hit(320.0f, y, 14.0f, 12.0f, () -> move.accept(-1)));
        this.hits.add(new Hit(352.0f, y, 14.0f, 12.0f, () -> move.accept(1)));
    }

    private float lx(float x) {
        return (x - this.ox) * 0.0105f;
    }

    private float ly(float y) {
        return (this.oy - y) * 0.0105f;
    }

    private void vert(float x, float y, float z, int col) {
        this.q.addVertex((Matrix4fc)this.m, this.lx(x), this.ly(y), z).setColor(col);
    }

    private void quad(float x, float y, float w, float h, int cTop, float aTop, int cBot, float aBot, float z) {
        int t = Gfx.argbRaw(cTop, aTop);
        int b = Gfx.argbRaw(cBot, aBot);
        this.vert(x, y, z, t);
        this.vert(x + w, y, z, t);
        this.vert(x + w, y + h, z, b);
        this.vert(x, y + h, z, b);
    }

    private void poly(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int rgb, float a, float z) {
        int col = Gfx.argbRaw(rgb, a);
        this.vert(x0, y0, z, col);
        this.vert(x1, y1, z, col);
        this.vert(x2, y2, z, col);
        this.vert(x3, y3, z, col);
    }

    private void ring(float cx, float cy, float r0, float r1, float a0, float a1, int cIn, float aIn, int cOut, float aOut, float z, int segs) {
        if (aIn <= 0.003f && aOut <= 0.003f) {
            return;
        }
        int ci = Gfx.argbRaw(cIn, aIn);
        int co = Gfx.argbRaw(cOut, aOut);
        for (int i = 0; i < segs; ++i) {
            double t0 = Math.toRadians(a0 + (a1 - a0) * (float)i / (float)segs);
            double t1 = Math.toRadians(a0 + (a1 - a0) * (float)(i + 1) / (float)segs);
            float c0 = (float)Math.cos(t0);
            float s0 = (float)Math.sin(t0);
            float c1 = (float)Math.cos(t1);
            float s1 = (float)Math.sin(t1);
            this.vert(cx + c0 * r0, cy + s0 * r0, z, ci);
            this.vert(cx + c0 * r1, cy + s0 * r1, z, co);
            this.vert(cx + c1 * r1, cy + s1 * r1, z, co);
            this.vert(cx + c1 * r0, cy + s1 * r0, z, ci);
        }
    }

    private void round(float x, float y, float w, float h, float r, int top, int bot, float a, float z) {
        if (a <= 0.003f) {
            return;
        }
        int n = (r = Math.min(r, Math.min(w, h) / 2.0f)) > 3.0f ? 5 : 2;
        float step = r / (float)n;
        for (int j = 0; j < n; ++j) {
            float yc = r - ((float)j + 0.5f) * step;
            float in = r - (float)Math.sqrt(Math.max(0.0f, r * r - yc * yc));
            this.band(x + in, y + (float)j * step, w - 2.0f * in, step, y, h, top, bot, a, z);
            this.band(x + in, y + h - (float)(j + 1) * step, w - 2.0f * in, step, y, h, top, bot, a, z);
        }
        this.band(x, y + r, w, h - 2.0f * r, y, h, top, bot, a, z);
    }

    private void band(float x, float y, float w, float h, float oy0, float oh, int top, int bot, float a, float z) {
        if (h <= 0.0f || w <= 0.0f) {
            return;
        }
        this.quad(x, y, w, h, Gfx.lerpColor(top, bot, (y - oy0) / oh), a, Gfx.lerpColor(top, bot, (y + h - oy0) / oh), a, z);
    }

    private void image(Identifier tex, float x, float y, float w, float h, float a) {
        Matrix4f mm = new Matrix4f((Matrix4fc)this.m);
        float x0 = this.lx(x);
        float x1 = this.lx(x + w);
        float y0 = this.ly(y);
        float y1 = this.ly(y + h);
        float z = 0.005f;
        int col = Gfx.argbRaw(0xFFFFFF, a);
        this.late.add(() -> {
            VertexConsumer v = Buffers.INSTANCE.getBuffer(RenderTypes.text((Identifier)tex));
            v.addVertex((Matrix4fc)mm, x0, y0, z).setColor(col).setUv(0.0f, 0.0f).setLight(0xF000F0);
            v.addVertex((Matrix4fc)mm, x0, y1, z).setColor(col).setUv(0.0f, 1.0f).setLight(0xF000F0);
            v.addVertex((Matrix4fc)mm, x1, y1, z).setColor(col).setUv(1.0f, 1.0f).setLight(0xF000F0);
            v.addVertex((Matrix4fc)mm, x1, y0, z).setColor(col).setUv(1.0f, 0.0f).setLight(0xF000F0);
        });
    }

    private void text(String s, float x, float y, float scale, int rgb, float a) {
        this.textAt(this.m, s, this.lx(x) / 0.0105f, -this.ly(y) / 0.0105f, scale, rgb, a, false);
    }

    private void textAt(Matrix4f mat, String s, float px, float py, float scale, int rgb, float a, boolean centered) {
        if (a <= 0.01f || s.isEmpty()) {
            return;
        }
        Font f = Minecraft.getInstance().font;
        float x = centered ? px - (float)f.width(s) * scale / 2.0f : px;
        Matrix4f tm = new Matrix4f((Matrix4fc)mat).translate(x * 0.0105f, -py * 0.0105f, 0.012f).scale(0.0105f * scale, -0.0105f * scale, 0.0105f);
        int col = Gfx.argbRaw(rgb, a);
        this.late.add(() -> {
            Minecraft mc = Minecraft.getInstance();
            TextDraw.drawInBatch(mc.font, (Component)Component.literal((String)s), 0.0f, 0.0f, col, false, tm, (MultiBufferSource)Buffers.INSTANCE, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        });
    }

    private static String ellip(String s, float maxPx, float scale) {
        Font f = Minecraft.getInstance().font;
        if ((float)f.width(s) * scale <= maxPx) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && (float)f.width(t + "...") * scale > maxPx) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "...";
    }

    private static float clamp(float x) {
        return Math.max(0.0f, Math.min(1.0f, x));
    }

    private static float outCubic(float t) {
        float u = 1.0f - t;
        return 1.0f - u * u * u;
    }

    private static float inOut(float t) {
        return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float)Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
    }

    static {
        TABS = new String[]{"sky", "lyrics", "style", "more"};
    }

    private record Hit(float x, float y, float w, float h, Runnable run) {
    }
}

