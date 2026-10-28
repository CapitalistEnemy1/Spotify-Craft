/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CloudStatus
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.level.Level
 */
package dev.neonlyrics.sky;

import dev.neonlyrics.License;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.hud.Anim;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.hud.Island;
import dev.neonlyrics.sky.SkyPreset;
import dev.neonlyrics.util.Fx;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;

public final class SkyRenderer {
    private static float sw;
    private static float sh;
    private static float focal;
    private static double rx;
    private static double ry;
    private static double rz;
    private static double ux;
    private static double uy;
    private static double uz;
    private static double fx;
    private static double fy;
    private static double fz;
    private static float camYaw;
    private static float camPitch;
    private static final float[] P;
    private static final Anim visible;
    private static final Random RND;
    private static float[][] stars;
    private static final List<float[]> shooting;
    private static long nextShooting;
    private static double[][] parts;
    private static SkyPreset.Weather partType;
    private static long lastNanos;
    private static long flashAt;
    private static long nextFlash;
    private static boolean forcedRain;
    private static int lastSky;

    private SkyRenderer() {
    }

    private static void setupCamera(GuiGraphicsExtractor g, Minecraft mc, float pt) {
        sw = g.guiWidth();
        sh = g.guiHeight();
        double fov = Math.toRadians(((Integer)mc.options.fov().get()).intValue());
        focal = (float)((double)sh / 2.0 / Math.tan(fov / 2.0));
        camYaw = mc.player.getViewYRot(pt);
        camPitch = mc.player.getViewXRot(pt);
        double y = Math.toRadians(camYaw);
        double p = Math.toRadians(camPitch);
        fx = -Math.sin(y) * Math.cos(p);
        fy = -Math.sin(p);
        fz = Math.cos(y) * Math.cos(p);
        rx = -Math.cos(y);
        ry = 0.0;
        rz = -Math.sin(y);
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;
    }

    private static boolean project(double dx, double dy, double dz) {
        double zc = dx * fx + dy * fy + dz * fz;
        if (zc < 0.02) {
            return false;
        }
        double xc = dx * rx + dy * ry + dz * rz;
        double yc = dx * ux + dy * uy + dz * uz;
        SkyRenderer.P[0] = (float)((double)(sw / 2.0f) + (double)focal * xc / zc);
        SkyRenderer.P[1] = (float)((double)(sh / 2.0f) - (double)focal * yc / zc);
        SkyRenderer.P[2] = (float)zc;
        return true;
    }

    private static boolean projectDir(double azDeg, double elDeg) {
        double a = Math.toRadians(azDeg);
        double e = Math.toRadians(elDeg);
        return SkyRenderer.project(-Math.sin(a) * Math.cos(e), Math.sin(e), Math.cos(a) * Math.cos(e));
    }

    private static void initStars() {
        Random r = new Random(1337L);
        stars = new float[520][5];
        for (int i = 0; i < stars.length; ++i) {
            float[] s = stars[i];
            s[0] = r.nextFloat() * 360.0f;
            s[1] = (float)Math.toDegrees(Math.asin(0.05 + (double)r.nextFloat() * 0.95));
            s[2] = r.nextFloat() < 0.12f ? 2.0f : 1.0f;
            s[3] = 0.35f + r.nextFloat() * 0.65f;
            s[4] = r.nextFloat() * 6.28f;
        }
    }

    public static void tick(Minecraft mc) {
        boolean wantRain;
        SkyPreset p;
        Config c = NeonLyrics.cfg();
        if (c.sky != lastSky) {
            if (lastSky != -99 && mc.player != null) {
                p = SkyPreset.get(c.sky);
                Island.notify(p == null ? Fx.t("neonlyrics.sky.off", new Object[0]) : Fx.t("neonlyrics.sky.enabled", Fx.t(p.key(), new Object[0])), p != null);
            }
            lastSky = c.sky;
        }
        if (mc.level == null) {
            forcedRain = false;
            return;
        }
        SkyRenderer.updateClouds(mc, c);
        p = SkyPreset.get(c.sky);
        boolean bl = wantRain = p != null && c.realWeather && (p.weather == SkyPreset.Weather.RAIN || p.weather == SkyPreset.Weather.THUNDER) && mc.level.dimension() == Level.OVERWORLD;
        if (wantRain) {
            mc.level.setRainLevel(1.0f);
            mc.level.setThunderLevel(p.weather == SkyPreset.Weather.THUNDER ? 1.0f : 0.0f);
            forcedRain = true;
        } else if (forcedRain) {
            mc.level.setRainLevel(0.0f);
            mc.level.setThunderLevel(0.0f);
            forcedRain = false;
        }
    }

    private static void updateClouds(Minecraft mc, Config c) {
        boolean hide = c.hideClouds && c.sky >= 0;
        OptionInstance opt = mc.options.cloudStatus();
        if (hide && opt.get() != CloudStatus.OFF) {
            c.cloudsBefore = ((CloudStatus)opt.get()).name();
            opt.set((Object)CloudStatus.OFF);
            NeonLyrics.saveConfig();
        } else if (!hide && !c.cloudsBefore.isEmpty()) {
            try {
                opt.set((Object)CloudStatus.valueOf((String)c.cloudsBefore));
            }
            catch (Exception exception) {
                // empty catch block
            }
            c.cloudsBefore = "";
            NeonLyrics.saveConfig();
        }
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (!License.ok()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) {
            return;
        }
        Config c = NeonLyrics.cfg();
        long nowNs = System.nanoTime();
        float dt = Math.min(0.1f, (float)(nowNs - lastNanos) / 1.0E9f);
        lastNanos = nowNs;
        float pt = delta.getGameTimeDeltaPartialTick(false);
        SkyRenderer.setupCamera(g, mc, pt);
        SkyPreset p = SkyPreset.get(c.sky);
        float old = Gfx.alpha;
        Gfx.alpha = 1.0f;
        if (p != null && p.weather == SkyPreset.Weather.THUNDER && mc.level.dimension() == Level.OVERWORLD) {
            SkyRenderer.drawLightning(g);
        }
        Gfx.alpha = old;
    }

    private static void drawLightning(GuiGraphicsExtractor g) {
        long age;
        long now = System.currentTimeMillis();
        if (now > nextFlash) {
            flashAt = now;
            nextFlash = now + 5000L + (long)RND.nextInt(9000);
        }
        if ((age = now - flashAt) < 600L) {
            float a = age < 90L ? 0.32f : (age < 180L ? 0.05f : (age < 260L ? 0.22f : 0.22f * (1.0f - (float)(age - 260L) / 340.0f)));
            Gfx.rect(g, 0, 0, (int)sw, (int)sh, 15266047, Math.max(0.0f, a));
        }
    }

    static {
        P = new float[3];
        visible = new Anim(0.0, 4.0);
        RND = new Random();
        shooting = new ArrayList<float[]>();
        nextShooting = 0L;
        parts = new double[0][];
        partType = null;
        lastNanos = System.nanoTime();
        flashAt = 0L;
        nextFlash = 0L;
        forcedRain = false;
        lastSky = -99;
    }
}

