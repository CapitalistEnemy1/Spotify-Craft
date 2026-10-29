/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package dev.neonlyrics.hud;

import dev.neonlyrics.License;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.hud.Anim;
import dev.neonlyrics.hud.Gfx;
import dev.neonlyrics.hud.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class GlassHotbar {
    private static final Anim sel = new Anim(0.0, 22.0);

    private GlassHotbar() {
    }

    public static HudElement wrap(HudElement original) {
        return (g, delta) -> {
            class_310 mc = class_310.method_1551();
            if (!NeonLyrics.cfg().glassHotbar || !License.ok() || mc.field_1724 == null || mc.field_1724.method_7325()) {
                original.render(g, delta);
                return;
            }
            GlassHotbar.render(g, mc);
        };
    }

    private static void render(class_332 g, class_310 mc) {
        float old = Gfx.alpha;
        Gfx.alpha = 1.0f;
        int acc = Theme.accent();
        int w = 182;
        int h = 22;
        int x = g.method_51421() / 2 - w / 2;
        int y = g.method_51443() - h - 1;
        class_1661 inv = mc.field_1724.method_31548();
        Gfx.round(g, x, y, w, h, 6, 1183260, 0.72f);
        Gfx.round(g, x, y, w, 1, 1, 0xFFFFFF, 0.07f);
        int selected = inv.method_67532();
        float sx = sel.target(selected).get();
        int slotX = x + 1 + Math.round(sx * 20.0f);
        Gfx.glow(g, slotX, y + 1, 20, 20, 5, acc, 0.9f, 4);
        Gfx.round(g, slotX, y + 1, 20, 20, 5, acc, 0.22f);
        GlassHotbar.outline(g, slotX, y + 1, 20, 20, acc);
        for (int i = 0; i < 9; ++i) {
            class_1799 st = inv.method_5438(i);
            if (st.method_7960()) continue;
            int ix = x + 3 + i * 20;
            int iy = y + 3;
            g.method_51427(st, ix, iy);
            g.method_51431(mc.field_1772, st, ix, iy);
        }
        class_1799 off = mc.field_1724.method_6079();
        if (!off.method_7960()) {
            int ox = x - 26;
            Gfx.round(g, ox, y, 22, 22, 6, 1183260, 0.72f);
            g.method_51427(off, ox + 3, y + 3);
            g.method_51431(mc.field_1772, off, ox + 3, y + 3);
        }
        Gfx.alpha = old;
    }

    private static void outline(class_332 g, int x, int y, int w, int h, int col) {
        Gfx.rect(g, x + 3, y, w - 6, 1, col, 0.95f);
        Gfx.rect(g, x + 3, y + h - 1, w - 6, 1, col, 0.95f);
        Gfx.rect(g, x, y + 3, 1, h - 6, col, 0.95f);
        Gfx.rect(g, x + w - 1, y + 3, 1, h - 6, col, 0.95f);
        Gfx.rect(g, x + 1, y + 1, 2, 1, col, 0.95f);
        Gfx.rect(g, x + 1, y + 2, 1, 1, col, 0.95f);
        Gfx.rect(g, x + w - 3, y + 1, 2, 1, col, 0.95f);
        Gfx.rect(g, x + w - 2, y + 2, 1, 1, col, 0.95f);
        Gfx.rect(g, x + 1, y + h - 2, 2, 1, col, 0.95f);
        Gfx.rect(g, x + 1, y + h - 3, 1, 1, col, 0.95f);
        Gfx.rect(g, x + w - 3, y + h - 2, 2, 1, col, 0.95f);
        Gfx.rect(g, x + w - 2, y + h - 3, 1, 1, col, 0.95f);
    }
}

