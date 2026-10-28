/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.language.I18n
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 */
package dev.neonlyrics.util;

import dev.neonlyrics.NeonLyrics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class Fx {
    private Fx() {
    }

    public static String t(String key, Object ... args) {
        return I18n.get((String)key, (Object[])args);
    }

    public static void sound(Sfx s) {
        if (!NeonLyrics.cfg().sounds) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) {
            return;
        }
        switch (s.ordinal()) {
            case 0: {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value()), (float)1.4f, (float)0.35f));
                break;
            }
            case 1: {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.PLAYER_LEVELUP, (float)1.0f, (float)0.6f));
                break;
            }
            case 2: {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value()), (float)0.6f, (float)0.7f));
                break;
            }
            case 3: {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.NOTE_BLOCK_HAT.value()), (float)1.8f, (float)0.4f));
            }
        }
    }

    public static void info(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) {
            return;
        }
        mc.gui.getChat().addClientSystemMessage((Component)Component.literal((String)"[Lyrics Cord] ").withStyle(ChatFormatting.AQUA).append((Component)Component.literal((String)text).withStyle(ChatFormatting.WHITE)));
    }

    public static void error(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) {
            return;
        }
        mc.gui.getChat().addClientSystemMessage((Component)Component.literal((String)"[Lyrics Cord] ").withStyle(ChatFormatting.LIGHT_PURPLE).append((Component)Component.literal((String)text).withStyle(ChatFormatting.RED)));
        Fx.sound(Sfx.ERROR);
    }

    public static void clipboard(String s) {
        Minecraft.getInstance().keyboardHandler.setClipboard(s);
    }

    public static String myName() {
        return Minecraft.getInstance().getUser().getName();
    }

    public static enum Sfx {
        CLICK,
        WIN,
        ERROR,
        TICK;

    }
}

