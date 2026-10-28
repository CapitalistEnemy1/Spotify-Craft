/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.LightCoordsUtil
 *  net.minecraft.util.Mth
 *  net.minecraft.world.level.LightLayer
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.neonlyrics.cosmetic;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.compat.MultiBufferSource;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class Accessories {
    public static final String[] HATS = new String[]{"none", "headphones", "cone", "crown", "halo", "pumpkin", "flame_crown", "saturn", "storm_cloud", "unicorn", "disco_ball", "rubber_duck", "octopus", "cat_ears", "devil_horns", "top_hat", "cap", "party_hat", "viking", "bunny_ears", "note_ring", "flower_crown", "beanie", "visor", "witch_hat", "antennae", "mushroom"};
    public static final Identifier WHITE = Identifier.fromNamespaceAndPath((String)"neonlyrics", (String)"cosmetic/white");
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
            Minecraft.getInstance().getTextureManager().register(WHITE, (AbstractTexture)new DynamicTexture(() -> "neonlyrics_white", NativeImage.read((byte[])out.toByteArray())));
            whiteReady = true;
        }
        catch (Throwable t) {
            whiteFailed = true;
        }
        return whiteReady;
    }

    public static void render(Minecraft mc, MultiBufferSource buffers, Matrix4f base, Vec3 cam, float pt) {
        int light;
        Config c = NeonLyrics.cfg();
        int idx = c.hat;
        if (idx <= 0 || idx >= HATS.length) {
            return;
        }
        LocalPlayer pl = mc.player;
        if (pl == null || pl.isInvisible() || pl.isSleeping() || pl.isSwimming() || pl.isFallFlying()) {
            return;
        }
        if (mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        float[] m = Accessories.mesh(idx);
        if (m == null || !Accessories.white()) {
            return;
        }
        Vec3 pos = pl.getPosition(pt);
        double neckY = pos.y + (double)pl.getEyeHeight() - 0.22;
        float yaw = (float)Math.toRadians(Mth.rotLerp((float)pt, (float)pl.yHeadRotO, (float)pl.yHeadRot));
        float pitch = (float)Math.toRadians(pl.getViewXRot(pt));
        float cy = (float)Math.cos(-yaw);
        float sy = (float)Math.sin(-yaw);
        float cp = (float)Math.cos(pitch);
        float sp = (float)Math.sin(pitch);
        float s = 0.060937498f;
        double ox = pos.x - cam.x;
        double oy = neckY - cam.y;
        double oz = pos.z - cam.z;
        try {
            BlockPos bp = BlockPos.containing((double)pos.x, (double)(neckY + 0.3), (double)pos.z);
            light = LightCoordsUtil.pack((int)mc.level.getBrightness(LightLayer.BLOCK, bp), (int)mc.level.getBrightness(LightLayer.SKY, bp));
        }
        catch (Throwable t) {
            light = 0xF000F0;
        }
        NowPlaying np = SpotifyClient.now();
        float beat = np.playing ? np.beatPulse(np.progressNow(c.lyricsOffsetMs)) : 0.0f;
        float glow = c.hatGlow ? 0.75f + 0.25f * beat : 0.55f;
        VertexConsumer vc = buffers.getBuffer(RenderTypes.entityCutout((Identifier)WHITE));
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
                vc.addVertex((Matrix4fc)base, (float)(ox + (double)v[kk * 3]), (float)(oy + (double)v[kk * 3 + 1]), (float)(oz + (double)v[kk * 3 + 2])).setColor(col).setUv(0.5f, 0.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(li).setNormal(nx2, ny1, nz2);
            }
        }
    }

    static {
        MESH = new float[HATS.length][];
        TRIED = new boolean[HATS.length];
    }
}

