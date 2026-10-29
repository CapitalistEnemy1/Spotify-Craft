/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_12249
 *  net.minecraft.class_1944
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_746
 *  net.minecraft.class_765
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.neonlyrics.cosmetic;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.lyrics.NowPlaying;
import dev.neonlyrics.lyrics.SpotifyClient;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_12249;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_746;
import net.minecraft.class_765;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class Accessories {
    public static final String[] HATS = new String[]{"none", "headphones", "cone", "crown", "halo", "pumpkin", "flame_crown", "saturn", "storm_cloud", "unicorn", "disco_ball", "rubber_duck", "octopus", "cat_ears", "devil_horns", "top_hat", "cap", "party_hat", "viking", "bunny_ears", "note_ring", "flower_crown", "beanie", "visor", "witch_hat", "antennae", "mushroom"};
    public static final class_2960 WHITE = class_2960.method_60655((String)"neonlyrics", (String)"cosmetic/white");
    private static boolean whiteReady;
    private static boolean whiteFailed;
    private static final float[][] MESH;
    private static final boolean[] TRIED;
    private static final int STRIDE = 14;

    private Accessories() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static float[] mesh(int i) {
        if (TRIED[i]) {
            return MESH[i];
        }
        Accessories.TRIED[i] = true;
        try (InputStream in = Accessories.class.getResourceAsStream("/assets/neonlyrics/models/" + HATS[i] + ".bin");){
            if (in == null) {
                float[] fArray = null;
                return fArray;
            }
            ByteBuffer b = ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
            if (b.getInt() != 827149390) {
                float[] fArray = null;
                return fArray;
            }
            int n = b.getInt();
            float[] m = new float[n * 14];
            for (int t = 0; t < n; ++t) {
                for (int k = 0; k < 12; ++k) {
                    m[t * 14 + k] = b.getFloat();
                }
                m[t * 14 + 12] = Float.intBitsToFloat(b.getInt());
                m[t * 14 + 13] = b.getInt();
            }
            Accessories.MESH[i] = m;
            return MESH[i];
        }
        catch (Throwable t) {
            NeonLyrics.LOG.warn("Hut-Modell fehlt: " + HATS[i], t);
        }
        return MESH[i];
    }

    public static boolean whiteTex() {
        return Accessories.white();
    }

    private static boolean white() {
        if (whiteReady) {
            return true;
        }
        if (whiteFailed) {
            return false;
        }
        try {
            BufferedImage img = new BufferedImage(4, 4, 2);
            for (int y = 0; y < 4; ++y) {
                for (int x = 0; x < 4; ++x) {
                    img.setRGB(x, y, -1);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)img, "png", out);
            class_310.method_1551().method_1531().method_4616(WHITE, (class_1044)new class_1043(() -> "neonlyrics_white", class_1011.method_49277((byte[])out.toByteArray())));
            whiteReady = true;
        }
        catch (Throwable t) {
            whiteFailed = true;
        }
        return whiteReady;
    }

    public static void render(class_310 mc, class_4597 buffers, Matrix4f base, class_243 cam, float pt) {
        int light;
        Config c = NeonLyrics.cfg();
        int idx = c.hat;
        if (idx <= 0 || idx >= HATS.length) {
            return;
        }
        class_746 pl = mc.field_1724;
        if (pl == null || pl.method_5767() || pl.method_6113() || pl.method_5681() || pl.method_6128()) {
            return;
        }
        if (mc.field_1690.method_31044().method_31034()) {
            return;
        }
        float[] m = Accessories.mesh(idx);
        if (m == null || !Accessories.white()) {
            return;
        }
        class_243 pos = pl.method_30950(pt);
        double neckY = pos.field_1351 + (double)pl.method_5751() - 0.22;
        float yaw = (float)Math.toRadians(class_3532.method_17821((float)pt, (float)pl.field_6259, (float)pl.field_6241));
        float pitch = (float)Math.toRadians(pl.method_5695(pt));
        float cy = (float)Math.cos(-yaw);
        float sy = (float)Math.sin(-yaw);
        float cp = (float)Math.cos(pitch);
        float sp = (float)Math.sin(pitch);
        float s = 0.060937498f;
        double ox = pos.field_1352 - cam.field_1352;
        double oy = neckY - cam.field_1351;
        double oz = pos.field_1350 - cam.field_1350;
        try {
            class_2338 bp = class_2338.method_49637((double)pos.field_1352, (double)(neckY + 0.3), (double)pos.field_1350);
            light = class_765.method_23687((int)mc.field_1687.method_8314(class_1944.field_9282, bp), (int)mc.field_1687.method_8314(class_1944.field_9284, bp));
        }
        catch (Throwable t) {
            light = 0xF000F0;
        }
        NowPlaying np = SpotifyClient.now();
        float beat = np.playing ? np.beatPulse(np.progressNow(c.lyricsOffsetMs)) : 0.0f;
        float glow = c.hatGlow ? 0.75f + 0.25f * beat : 0.55f;
        class_4588 vc = buffers.method_73477(class_12249.method_75994((class_2960)WHITE));
        int n = m.length / 14;
        float[] v = new float[9];
        for (int t = 0; t < n; ++t) {
            int col;
            int o = t * 14;
            float nx = m[o + 9];
            float ny = m[o + 10];
            float nz = m[o + 11];
            float ny1 = ny * cp - nz * sp;
            float nz1 = ny * sp + nz * cp;
            float nx2 = nx * cy + nz1 * sy;
            float nz2 = -nx * sy + nz1 * cy;
            for (int k = 0; k < 3; ++k) {
                float x = m[o + k * 3] * s;
                float y = (m[o + k * 3 + 1] + 4.0f) * s;
                float z = m[o + k * 3 + 2] * s;
                float y1 = y * cp - z * sp;
                float z1 = y * sp + z * cp;
                v[k * 3] = x * cy + z1 * sy;
                v[k * 3 + 1] = y1;
                v[k * 3 + 2] = -x * sy + z1 * cy;
            }
            int rgb = Float.floatToRawIntBits(m[o + 12]);
            boolean em = m[o + 13] > 0.5f;
            int li = light;
            if (em) {
                float k = glow;
                int r = Math.min(255, Math.round((float)(rgb >> 16 & 0xFF) * (0.6f + 0.6f * k)));
                int g = Math.min(255, Math.round((float)(rgb >> 8 & 0xFF) * (0.6f + 0.6f * k)));
                int bl = Math.min(255, Math.round((float)(rgb & 0xFF) * (0.6f + 0.6f * k)));
                col = 0xFF000000 | r << 16 | g << 8 | bl;
                li = 0xF000F0;
            } else {
                col = 0xFF000000 | rgb;
            }
            for (int k = 0; k < 4; ++k) {
                int kk = Math.min(k, 2);
                vc.method_22918((Matrix4fc)base, (float)(ox + (double)v[kk * 3]), (float)(oy + (double)v[kk * 3 + 1]), (float)(oz + (double)v[kk * 3 + 2])).method_39415(col).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(li).method_22914(nx2, ny1, nz2);
            }
        }
    }

    static {
        MESH = new float[HATS.length][];
        TRIED = new boolean[HATS.length];
    }
}

