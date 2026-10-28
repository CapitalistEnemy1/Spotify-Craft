/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.neonlyrics.sky;

import dev.neonlyrics.compat.MultiBufferSource;
import dev.neonlyrics.compat.TextDraw;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.lyrics.NowPlaying;
import dev.neonlyrics.lyrics.SpotifyClient;
import dev.neonlyrics.sky.WorldSky;
import dev.neonlyrics.util.AutoPerf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class FloatWords {
    private static final float[][] SLOTS = new float[][]{{-22.0f, 7.0f}, {20.0f, -4.0f}, {-6.0f, -12.0f}, {16.0f, 12.0f}, {-26.0f, -8.0f}, {8.0f, 4.0f}, {-14.0f, 14.0f}, {26.0f, 6.0f}, {2.0f, -16.0f}, {-18.0f, 0.0f}, {12.0f, -10.0f}, {-8.0f, 9.0f}};
    private static final float BLOCKS_PER_PX = 0.062f;
    private static final List<Word> WORDS = new ArrayList<Word>();
    private static final Random RNG = new Random();
    private static String lastTrack = "";
    private static int lastLine = -1;
    private static int lastChunk = -1;
    private static int lastSlot = -1;
    private static int prevSlot = -1;
    private static String[] chunks = new String[0];
    private static int[] chunkOf = new int[0];
    private static final Map<String, Component> COMP = new HashMap<String, Component>();
    private static final Matrix4f M0 = new Matrix4f();
    private static final Matrix4f TMP = new Matrix4f();
    private static final Matrix4f WM = new Matrix4f();
    private static final Matrix4f SCR = new Matrix4f();

    private FloatWords() {
    }

    public static void clear() {
        WORDS.clear();
        lastLine = -1;
        lastChunk = -1;
    }

    public static void renderWorld(Minecraft mc, Config c, MultiBufferSource buffers, Matrix4f base, Vec3 cam, float pt, boolean through) {
        if (mc == null || c == null || base == null || cam == null) {
            return;
        }
        if (!c.lyricsSky || c.lyricsStyle != 1 || mc.options.hideGui || mc.player == null || mc.level == null) {
            FloatWords.clear();
            return;
        }
        long now = System.currentTimeMillis();
        boolean live = false;
        NowPlaying np = SpotifyClient.now();
        if (np != null && np.hasTrack() && np.playing && !np.lines.isEmpty()) {
            long progress;
            int li;
            String tid = String.valueOf(np.trackId);
            if (!tid.equals(lastTrack)) {
                lastTrack = tid;
                WORDS.clear();
                lastLine = -1;
                lastChunk = -1;
            }
            if ((li = np.lineIndex(progress = np.progressNow(c.lyricsOffsetMs + 150))) >= 0 && li < np.lines.size() && progress <= np.lineEnd(li) + 450L) {
                live = FloatWords.update(mc, c, np, li, progress, cam, pt, now);
            }
        }
        if (!live) {
            for (Word w2 : WORDS) {
                if (w2.gone != 0L) continue;
                w2.gone = now;
            }
        }
        float fadeMs = 2200.0f;
        WORDS.removeIf(w -> w.gone != 0L && (float)(now - w.gone) > 2200.0f);
        FloatWords.draw(mc, c, buffers, base, cam, pt, now, 2200.0f, through, np);
    }

    private static boolean update(Minecraft mc, Config c, NowPlaying np, int li, long progress, Vec3 cam, float pt, long now) {
        int aw;
        NowPlaying.Line line = np.lines.get(li);
        String text = line.text();
        if (text == null || text.isBlank()) {
            return false;
        }
        String[] words = line.words();
        if (words == null || words.length == 0) {
            words = text.trim().split("\\s+");
        }
        if (li != lastLine) {
            lastLine = li;
            lastChunk = -1;
            FloatWords.buildChunks(words);
        }
        if (chunks.length == 0) {
            return false;
        }
        float wp = np.wordPosition(li, progress);
        if (wp >= 0.0f) {
            aw = Math.min(words.length - 1, (int)Math.floor(wp));
        } else {
            long start = line.timeMs();
            long end = Math.max(start + 1L, np.lineEnd(li));
            aw = (int)Math.floor((float)(progress - start) / (float)(end - start) * (float)words.length);
            aw = Math.max(0, Math.min(words.length - 1, aw));
        }
        int ac = Math.max(0, Math.min(chunks.length - 1, chunkOf[Math.min(aw, chunkOf.length - 1)]));
        if (ac > lastChunk) {
            if (ac - lastChunk > 2) {
                lastChunk = ac - 1;
            }
            while (lastChunk < ac) {
                FloatWords.spawn(chunks[++lastChunk], mc, c, cam, pt, now);
            }
        }
        return true;
    }

    private static float sz(Config c) {
        return c.lyricsSize / 1.7f;
    }

    private static void spawn(String text, Minecraft mc, Config c, Vec3 cam, float pt, long now) {
        double nz;
        double nx;
        if (text == null || text.isEmpty()) {
            return;
        }
        float yaw = mc.player.getViewYRot(pt);
        float el0 = -mc.player.getViewXRot(pt) + 12.0f;
        String txt = text.toUpperCase(Locale.ROOT);
        int widthPx = Gfx.w(txt, Gfx.LYRICS);
        double bestScore = Double.MAX_VALUE;
        double bAz = 0.0;
        double bEl = 0.0;
        double bDist = 8.0;
        double bSize = 1.0;
        double bHw = 1.0;
        double bHh = 1.0;
        int bestSlot = 0;
        for (int attempt = 0; attempt < 28; ++attempt) {
            double hh;
            int slot;
            int tries = 0;
            while (((slot = RNG.nextInt(SLOTS.length)) == lastSlot || slot == prevSlot) && ++tries < 12) {
            }
            double az = Math.toRadians(yaw + (SLOTS[slot][0] + (RNG.nextFloat() - 0.5f) * 7.0f));
            double el = Math.toRadians(el0 + (SLOTS[slot][1] + (RNG.nextFloat() - 0.5f) * 6.0f));
            double dist = 6.5 + RNG.nextDouble() * 8.5;
            float size = 0.82f + RNG.nextFloat() * 0.5f;
            double hw = (double)((float)widthPx * 0.062f * FloatWords.sz(c) * size) * 0.5 * 0.95;
            double score = FloatWords.overlapScore(az, el, dist, hw, hh = (double)(0.682f * FloatWords.sz(c) * size) * 0.5, cam);
            if (score < bestScore) {
                bestScore = score;
                bAz = az;
                bEl = el;
                bDist = dist;
                bSize = size;
                bHw = hw;
                bHh = hh;
                bestSlot = slot;
            }
            if (score <= 0.0) break;
        }
        prevSlot = lastSlot;
        lastSlot = bestSlot;
        double az = bAz;
        double el = bEl;
        double dist = bDist;
        float size = (float)bSize;
        double dx = -Math.sin(az) * Math.cos(el);
        double dy = Math.sin(el);
        double dz = Math.cos(az) * Math.cos(el);
        double x = cam.x + dx * dist;
        double y = cam.y + dy * dist;
        double z = cam.z + dz * dist;
        double h = Math.sqrt(dx * dx + dz * dz);
        if (h < 1.0E-4) {
            double yr = Math.toRadians(yaw);
            nx = Math.sin(yr);
            nz = -Math.cos(yr);
        } else {
            nx = -dx / h;
            nz = -dz / h;
        }
        float tilt = (float)Math.toRadians((float)(RNG.nextBoolean() ? 1 : -1) * (10.0f + RNG.nextFloat() * 22.0f));
        float pitch = (float)Math.toRadians((RNG.nextFloat() - 0.5f) * 14.0f);
        float roll = (float)Math.toRadians((RNG.nextFloat() - 0.5f) * 8.0f);
        for (Word o : WORDS) {
            if (o.gone != 0L) continue;
            o.gone = now;
        }
        WORDS.add(new Word(text, x, y, z, nx, nz, tilt, pitch, roll, size, (float)bHw, (float)bHh, now));
        int max = 7;
        while (WORDS.size() > max) {
            WORDS.remove(0);
        }
    }

    private static double overlapScore(double az, double el, double dist, double hw, double hh, Vec3 cam) {
        double score = 0.0;
        double angW = hw / dist;
        double angH = hh / dist;
        for (Word o : WORDS) {
            double ex = o.x - cam.x;
            double ey = o.y - cam.y;
            double ez = o.z - cam.z;
            double d = Math.sqrt(ex * ex + ey * ey + ez * ez);
            if (d < 0.5) continue;
            double oAz = Math.atan2(-ex, ez);
            double oEl = Math.asin(Math.max(-1.0, Math.min(1.0, ey / d)));
            double dAz = Math.abs(((az - oAz) % (Math.PI * 2) + Math.PI * 3) % (Math.PI * 2) - Math.PI) * Math.cos(el);
            double dEl = Math.abs(el - oEl);
            double needW = angW + (double)o.halfW / d + 0.035;
            double needH = angH + (double)o.halfH / d + 0.03;
            if (!(dAz < needW) || !(dEl < needH)) continue;
            score += 1.0 - dAz / needW + (1.0 - dEl / needH) + 0.5;
        }
        return score;
    }

    private static void buildChunks(String[] words) {
        ArrayList<String> list = new ArrayList<String>();
        int[] map = new int[words.length];
        int i = 0;
        while (i < words.length) {
            String b;
            String a = FloatWords.clean(words[i]);
            if (a.isEmpty()) {
                map[i] = list.isEmpty() ? 0 : list.size() - 1;
                ++i;
                continue;
            }
            int idx = list.size();
            Object chunk = a;
            int used = 1;
            if (a.length() <= 3 && i + 1 < words.length && !(b = FloatWords.clean(words[i + 1])).isEmpty()) {
                chunk = a + " " + b;
                used = 2;
                map[i + 1] = idx;
            }
            map[i] = idx;
            list.add((String)chunk);
            i += used;
        }
        chunks = list.toArray(new String[0]);
        chunkOf = map;
    }

    private static String clean(String s) {
        int a;
        if (s == null) {
            return "";
        }
        int b = s.length();
        for (a = 0; a < b && !FloatWords.keep(s.charAt(a)); ++a) {
        }
        while (b > a && !FloatWords.keep(s.charAt(b - 1))) {
            --b;
        }
        return s.substring(a, b);
    }

    private static boolean keep(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '*' || ch == '\'';
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : Math.min(v, 1.0f);
    }

    private static float easeOut(float t) {
        float u = 1.0f - t;
        return 1.0f - u * u * u;
    }

    private static Component comp(String s, int fontIdx) {
        if (COMP.size() > 600) {
            COMP.clear();
        }
        return COMP.computeIfAbsent(fontIdx + "|" + s, k -> Gfx.comp(s, Gfx.LYRICS));
    }

    private static void draw(Minecraft mc, Config c, MultiBufferSource buffers, Matrix4f base, Vec3 cam, float pt, long now, float fadeMs, boolean through, NowPlaying np) {
        if (WORDS.isEmpty()) {
            return;
        }
        Font font = mc.font;
        int fontIdx = c.lyricsFont;
        int baseCol = c.lyricsColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsColor, WorldSky.LYRIC_COLORS.length)] : 0xFFFFFF;
        boolean glow = c.lyricsGlow && AutoPerf.allow(3);
        int q = AutoPerf.quality();
        int glowN = q <= 0 ? 0 : (q == 1 ? 2 : 3);
        Font.DisplayMode mode = through ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
        double yr = Math.toRadians(mc.player.getViewYRot(pt));
        double pr = Math.toRadians(mc.player.getViewXRot(pt));
        double vfx = -Math.sin(yr) * Math.cos(pr);
        double vfy = -Math.sin(pr);
        double vfz = Math.cos(yr) * Math.cos(pr);
        float beat = c.fxBeatPulse && np != null && np.hasTrack() ? np.beatPulse(np.progressNow(c.lyricsOffsetMs)) : 0.0f;
        for (Word w : WORDS) {
            double nz;
            double rz;
            double drift;
            double ry;
            double rx;
            boolean current;
            long age = now - w.born;
            float a = FloatWords.clamp01((float)age / 140.0f) * c.lyricsOpacity;
            float bright = 1.0f;
            boolean bl = current = w.gone == 0L;
            if (!current) {
                float k = (float)(now - w.gone) / fadeMs;
                if (k >= 1.0f) continue;
                a *= 1.0f - k * k;
                bright = 1.0f - 0.5f * FloatWords.easeOut(FloatWords.clamp01((float)(now - w.gone) / 240.0f));
            }
            if (a < 0.03f || (rx = w.x - cam.x) * vfx + (ry = w.y + (drift = Math.min((double)age / 1000.0 * 0.25, 1.0)) - cam.y) * vfy + (rz = w.z - cam.z) * vfz < 0.3) continue;
            double ct = Math.cos(w.tilt);
            double st = Math.sin(w.tilt);
            double nx = w.nx * ct - w.nz * st;
            double Rx = nz = w.nx * st + w.nz * ct;
            double Ry = 0.0;
            double Rz = -nx;
            double Ux = 0.0;
            double Uy = 1.0;
            double Uz = 0.0;
            double Nx = nx;
            double Ny = 0.0;
            double Nz = nz;
            double cp = Math.cos(w.pitch);
            double sp = Math.sin(w.pitch);
            double U2x = Ux * cp + Nx * sp;
            double U2y = Uy * cp + Ny * sp;
            double U2z = Uz * cp + Nz * sp;
            double N2x = Nx * cp - Ux * sp;
            double N2y = Ny * cp - Uy * sp;
            double N2z = Nz * cp - Uz * sp;
            double cr = Math.cos(w.roll);
            double sr = Math.sin(w.roll);
            double R3x = Rx * cr + U2x * sr;
            double R3y = Ry * cr + U2y * sr;
            double R3z = Rz * cr + U2z * sr;
            double U3x = U2x * cr - Rx * sr;
            double U3y = U2y * cr - Ry * sr;
            double U3z = U2z * cr - Rz * sr;
            if (rx * N2x + ry * N2y + rz * N2z > 0.0) {
                R3x = -R3x;
                R3y = -R3y;
                R3z = -R3z;
                N2x = -N2x;
                N2y = -N2y;
                N2z = -N2z;
            }
            float pop = 1.0f + 0.22f * (1.0f - FloatWords.easeOut(FloatWords.clamp01((float)age / 240.0f)));
            float kk = 0.062f * FloatWords.sz(c) * w.size * pop;
            Matrix4f m0 = M0.set((Matrix4fc)base).mul((Matrix4fc)TMP.set((float)R3x * kk, (float)R3y * kk, (float)R3z * kk, 0.0f, (float)(-U3x) * kk, (float)(-U3y) * kk, (float)(-U3z) * kk, 0.0f, (float)N2x * kk, (float)N2y * kk, (float)N2z * kk, 0.0f, (float)rx, (float)ry, (float)rz, 1.0f));
            String txt = w.text.toUpperCase(Locale.ROOT);
            int width = Gfx.w(txt, Gfx.LYRICS);
            int shownLen = txt.length();
            shownLen = Math.max(1, (int)Math.ceil((float)txt.length() * FloatWords.clamp01((float)age / 230.0f)));
            Component shown = FloatWords.comp(txt.substring(0, shownLen), fontIdx);
            Matrix4f m = WM.set((Matrix4fc)m0);
            if (current && beat > 0.01f) {
                float bs = 1.0f + 0.06f * beat;
                m.scale(bs, bs, 1.0f);
            }
            m.translate((float)(-width) / 2.0f, -4.5f, 0.0f);
            int col = Gfx.lerpColor(9211040, baseCol, bright);
            int glowBase = c.lyricsGlowColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsGlowColor, WorldSky.LYRIC_COLORS.length)] : baseCol;
            int gcol = Gfx.lerpColor(9211040, glowBase, bright);
            FloatWords.put(font, buffers, m, shown, 0.7f, 0.9f, -0.5f, 328460, a * 0.55f, mode);
            if (glow) {
                float ga = a * bright * 0.2f * c.lyricsGlowStrength * (current ? 1.3f : 0.8f);
                if (glowN > 0) {
                    for (int i = 0; i < glowN; ++i) {
                        double ang = (double)i * Math.PI * 2.0 / (double)glowN + 0.39269908169872414;
                        FloatWords.put(font, buffers, m, shown, (float)Math.cos(ang) * 1.1f, (float)Math.sin(ang) * 1.1f, 0.1f, gcol, ga * 6.0f / (float)glowN, mode);
                    }
                } else {
                    FloatWords.put(font, buffers, m, shown, 0.0f, 0.6f, 0.1f, gcol, ga * 1.6f, mode);
                }
            }
            FloatWords.put(font, buffers, m, shown, 0.0f, 0.0f, 0.3f, col, a, mode);
        }
    }

    private static void put(Font font, MultiBufferSource buffers, Matrix4f m, Component text, float x, float y, float z, int rgb, float a, Font.DisplayMode mode) {
        if (a <= 0.03f) {
            return;
        }
        Matrix4f mm = SCR.set((Matrix4fc)m).translate(x, y, z);
        TextDraw.drawInBatch(font, text, 0.0f, 0.0f, Gfx.argbRaw(rgb, a), false, mm, buffers, mode, 0, 0xF000F0);
    }

    private static final class Word {
        final String text;
        final double x;
        final double y;
        final double z;
        final double nx;
        final double nz;
        final float tilt;
        final float pitch;
        final float roll;
        final float size;
        final float halfW;
        final float halfH;
        final long born;
        long gone = 0L;

        Word(String text, double x, double y, double z, double nx, double nz, float tilt, float pitch, float roll, float size, float halfW, float halfH, long born) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.nz = nz;
            this.tilt = tilt;
            this.pitch = pitch;
            this.roll = roll;
            this.size = size;
            this.halfW = halfW;
            this.halfH = halfH;
            this.born = born;
        }
    }
}

