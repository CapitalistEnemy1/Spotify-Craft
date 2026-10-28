/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.fog.FogRenderer
 *  net.minecraft.world.level.material.FogType
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.neonlyrics.mixin;

import dev.neonlyrics.sky.WorldSky;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FogRenderer.class})
public abstract class FogRendererMixin {
    @Inject(method={"computeFogColor"}, at={@At(value="RETURN")}, require=0)
    private void neonlyrics$skyFog(Camera camera, float pt, ClientLevel level, int renderDistance, float darken, Vector4f out, CallbackInfo ci) {
        if (camera.getFluidInCamera() != FogType.NONE) {
            return;
        }
        WorldSky.tintFog(out);
    }
}

