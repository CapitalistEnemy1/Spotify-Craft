/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.Font$GlyphVisitor
 *  net.minecraft.client.gui.Font$PreparedText
 *  net.minecraft.client.gui.font.TextRenderable
 *  net.minecraft.client.gui.font.TextRenderable$Styled
 *  net.minecraft.network.chat.Component
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.neonlyrics.compat;

import dev.neonlyrics.compat.MultiBufferSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class TextDraw {
    private TextDraw() {
    }

    public static void drawInBatch(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int bg, int light) {
        TextDraw.draw(font.prepareText(text.getVisualOrderText(), x, y, color, shadow, false, bg), pose, buffers, mode, light);
    }

    public static void drawInBatch(Font font, String text, float x, float y, int color, boolean shadow, Matrix4f pose, MultiBufferSource buffers, Font.DisplayMode mode, int bg, int light) {
        TextDraw.draw(font.prepareText(text, x, y, color, shadow, bg), pose, buffers, mode, light);
    }

    private static void draw(Font.PreparedText text, final Matrix4f pose, final MultiBufferSource buffers, final Font.DisplayMode mode, final int light) {
        text.visit(new Font.GlyphVisitor(){

            public void acceptGlyph(TextRenderable.Styled glyph) {
                this.put((TextRenderable)glyph);
            }

            public void acceptEffect(TextRenderable effect) {
                this.put(effect);
            }

            public void acceptRenderable(TextRenderable r) {
                this.put(r);
            }

            private void put(TextRenderable r) {
                r.render((Matrix4fc)pose, buffers.getBuffer(r.renderType(mode)), light, false);
            }
        });
    }
}

