/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_757
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.neonlyrics.mixin;

import dev.neonlyrics.cosmetic.PhoneMenu;
import net.minecraft.class_757;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_757.class})
public abstract class HandMixin {
    @Inject(method={"method_3172(FZLorg/joml/Matrix4f;)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void neonlyrics$hideHand(float partialTick, boolean sleeping, Matrix4f projection, CallbackInfo ci) {
        if (PhoneMenu.hidesHand()) {
            ci.cancel();
        }
    }
}

