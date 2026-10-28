/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.rendertype.RenderType
 */
package dev.neonlyrics.compat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface MultiBufferSource {
    public VertexConsumer getBuffer(RenderType var1);

    public static interface BufferSource
    extends MultiBufferSource {
        public void endBatch(RenderType var1);
    }
}

