/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package dev.neonlyrics.hud;

import com.mojang.blaze3d.platform.NativeImage;
import dev.neonlyrics.NeonLyrics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class Shapes {
    public static final int MAX_R = 512;
    private static final Map<Integer, Identifier> CIRCLES = new HashMap<Integer, Identifier>();
    private static boolean failed = false;

    private Shapes() {
    }

    public static boolean usable() {
        return !failed;
    }

    public static void fail() {
        failed = true;
    }

    public static Identifier circle(int r) {
        Identifier id;
        if (failed || r < 1) {
            return null;
        }
        if (r > 96) {
            r = Math.min(512, (r + 4) / 8 * 8);
        }
        if ((id = CIRCLES.get(r)) != null) {
            return id;
        }
        try {
            int n = r * 2;
            BufferedImage img = new BufferedImage(n, n, 2);
            for (int y = 0; y < n; ++y) {
                for (int x = 0; x < n; ++x) {
                    int hit = 0;
                    for (int sy = 0; sy < 4; ++sy) {
                        for (int sx = 0; sx < 4; ++sx) {
                            float dx = (float)x + ((float)sx + 0.5f) / 4.0f - (float)r;
                            float dy = (float)y + ((float)sy + 0.5f) / 4.0f - (float)r;
                            if (!(dx * dx + dy * dy <= (float)r * (float)r)) continue;
                            ++hit;
                        }
                    }
                    int a = Math.round((float)hit / 16.0f * 255.0f);
                    img.setRGB(x, y, a << 24 | 0xFFFFFF);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)img, "png", out);
            NativeImage ni = NativeImage.read((byte[])out.toByteArray());
            id = Identifier.fromNamespaceAndPath((String)"neonlyrics", (String)("shape/c" + r));
            DynamicTexture tex = new DynamicTexture(() -> "neonlyrics_circle", ni);
            Minecraft.getInstance().getTextureManager().register(id, (AbstractTexture)tex);
            CIRCLES.put(r, id);
            return id;
        }
        catch (Throwable t) {
            NeonLyrics.LOG.warn("Formen als Textur gehen nicht, nutze einfache Variante", t);
            failed = true;
            return null;
        }
    }
}

