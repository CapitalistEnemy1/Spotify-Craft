/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package dev.neonlyrics.hud;

import com.mojang.blaze3d.platform.NativeImage;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Storage;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.sky.LiveSky;
import dev.neonlyrics.sky.SkyPainter;
import dev.neonlyrics.sky.SkyPreset;
import dev.neonlyrics.sky.SkyTextures;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class SkyThumbs {
    public static final int TW = 288;
    public static final int TH = 162;
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NeonLyrics-Thumbs");
        t.setDaemon(true);
        t.setPriority(1);
        return t;
    });
    private static final Map<String, String> SHOWN = new HashMap<String, String>();
    private static final Set<String> QUEUED = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Done> DONE = new ConcurrentLinkedQueue();
    private static final int CACHE_VER = 12;
    private static long lastUpload = 0L;

    private SkyThumbs() {
    }

    private static float[] look(String kind) {
        float[] fArray;
        switch (kind) {
            case "SYNTHWAVE": {
                float[] fArray2 = new float[2];
                fArray2[0] = 180.0f;
                fArray = fArray2;
                fArray2[1] = 11.0f;
                break;
            }
            case "BLACK_HOLE": {
                float[] fArray3 = new float[2];
                fArray3[0] = 165.0f;
                fArray = fArray3;
                fArray3[1] = 22.0f;
                break;
            }
            case "MIDNIGHT": 
            case "BLOOD_MOON": {
                float[] fArray4 = new float[2];
                fArray4[0] = 200.0f;
                fArray = fArray4;
                fArray4[1] = 22.0f;
                break;
            }
            case "GOLDEN_HOUR": {
                float[] fArray5 = new float[2];
                fArray5[0] = 200.0f;
                fArray = fArray5;
                fArray5[1] = 12.0f;
                break;
            }
            case "COSMOS": {
                float[] fArray6 = new float[2];
                fArray6[0] = 60.0f;
                fArray = fArray6;
                fArray6[1] = 28.0f;
                break;
            }
            case "DEEP_SEA": {
                float[] fArray7 = new float[2];
                fArray7[0] = 40.0f;
                fArray = fArray7;
                fArray7[1] = 32.0f;
                break;
            }
            case "SAKURA": {
                float[] fArray8 = new float[2];
                fArray8[0] = 60.0f;
                fArray = fArray8;
                fArray8[1] = 12.0f;
                break;
            }
            case "COTTON_CANDY": {
                float[] fArray9 = new float[2];
                fArray9[0] = 120.0f;
                fArray = fArray9;
                fArray9[1] = 26.0f;
                break;
            }
            case "NEBULA": {
                float[] fArray10 = new float[2];
                fArray10[0] = 40.0f;
                fArray = fArray10;
                fArray10[1] = 26.0f;
                break;
            }
            case "NEON_CITY": {
                float[] fArray11 = new float[2];
                fArray11[0] = 60.0f;
                fArray = fArray11;
                fArray11[1] = 16.0f;
                break;
            }
            case "FROST": {
                float[] fArray12 = new float[2];
                fArray12[0] = 210.0f;
                fArray = fArray12;
                fArray12[1] = 24.0f;
                break;
            }
            case "PULSAR": {
                float[] fArray13 = new float[2];
                fArray13[0] = 30.0f;
                fArray = fArray13;
                fArray13[1] = 34.0f;
                break;
            }
            case "PRISM": 
            case "MATRIX": 
            case "INFERNO": 
            case "AQUARELL": {
                float[] fArray14 = new float[2];
                fArray14[0] = 60.0f;
                fArray = fArray14;
                fArray14[1] = 24.0f;
                break;
            }
            default: {
                float[] fArray15 = new float[2];
                fArray15[0] = 150.0f;
                fArray = fArray15;
                fArray15[1] = 20.0f;
            }
        }
        return fArray;
    }

    public static boolean draw(GuiGraphicsExtractor g, SkyPreset p, int x, int y, int w, int h, float alpha) {
        if (p.painted()) {
            Identifier img = Identifier.fromNamespaceAndPath((String)"neonlyrics", (String)("textures/gui/skythumbs/" + p.name().toLowerCase(Locale.ROOT) + ".png"));
            g.blit(RenderPipelines.GUI_TEXTURED, img, x, y, 0.0f, 0.0f, w, h, w, h, Gfx.argb(0xFFFFFF, alpha));
            return true;
        }
        SkyThumbs.upload();
        SkyPainter.Spec s = SkyTextures.spec(p);
        String key = SkyTextures.key(s);
        String shown = SHOWN.get(p.name());
        if (!key.equals(shown)) {
            SkyThumbs.request(p, s, key);
        }
        if (shown == null) {
            return false;
        }
        Identifier id = SkyThumbs.id(p);
        g.blit(RenderPipelines.GUI_TEXTURED, id, x, y, 0.0f, 0.0f, w, h, w, h, Gfx.argb(0xFFFFFF, alpha));
        return true;
    }

    public static Identifier texture(SkyPreset p) {
        SkyThumbs.upload();
        SkyPainter.Spec s = SkyTextures.spec(p);
        String key = SkyTextures.key(s);
        String shown = SHOWN.get(p.name());
        if (!key.equals(shown)) {
            SkyThumbs.request(p, s, key);
        }
        return shown == null ? null : SkyThumbs.id(p);
    }

    private static Path cacheFile(String key) {
        try {
            return Storage.dir().resolve("thumbs").resolve("v12_" + Integer.toHexString(key.hashCode()) + "_288.png");
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static Identifier id(SkyPreset p) {
        return Identifier.fromNamespaceAndPath((String)"neonlyrics", (String)("thumb/" + p.name().toLowerCase(Locale.ROOT)));
    }

    private static void request(SkyPreset p, SkyPainter.Spec s, String key) {
        if (!QUEUED.add(key)) {
            return;
        }
        EXEC.execute(() -> {
            try {
                Path file = SkyThumbs.cacheFile(key);
                if (file != null && Files.isRegularFile(file, new LinkOption[0])) {
                    DONE.add(new Done(p.name(), key, NativeImage.read((byte[])Files.readAllBytes(file))));
                    return;
                }
                float[] lk = SkyThumbs.look(s.kind());
                int[] px = new LiveSky().view(p, s.a1(), s.a2(), 12.0, 288, 162, lk[0], -lk[1], 78.0f);
                BufferedImage img = new BufferedImage(288, 162, 2);
                img.setRGB(0, 0, 288, 162, px, 0, 288);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage)img, "png", out);
                byte[] png = out.toByteArray();
                if (file != null) {
                    try {
                        Files.createDirectories(file.getParent(), new FileAttribute[0]);
                        Files.write(file, png, new OpenOption[0]);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                DONE.add(new Done(p.name(), key, NativeImage.read((byte[])png)));
            }
            catch (Throwable t) {
                NeonLyrics.LOG.debug("Himmel-Vorschau fehlgeschlagen", t);
            }
        });
    }

    private static void upload() {
        long frame = System.nanoTime() / 5000000L;
        if (frame == lastUpload) {
            return;
        }
        Done d = DONE.poll();
        if (d == null) {
            return;
        }
        lastUpload = frame;
        try {
            SkyPreset p = SkyPreset.valueOf(d.name());
            Minecraft.getInstance().getTextureManager().register(SkyThumbs.id(p), (AbstractTexture)new DynamicTexture(() -> "neonlyrics_thumb_" + d.name(), d.img()));
            SHOWN.put(d.name(), d.key());
        }
        catch (Throwable t) {
            d.img().close();
        }
        QUEUED.remove(d.key());
    }

    private record Done(String name, String key, NativeImage img) {
    }
}

