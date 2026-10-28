/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  org.joml.Matrix4f
 */
package dev.neonlyrics.sky;

import dev.neonlyrics.config.Config;
import dev.neonlyrics.sky.SkyPreset;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;

public final class LiveSkyShader {
    private LiveSkyShader() {
    }

    public static boolean ready() {
        return false;
    }

    public static void disable(Throwable t) {
    }

    public static RenderType type() {
        return null;
    }

    public static void params(SkyPreset p, int a1, int a2, double t, Config c, Matrix4f pose) {
    }
}

