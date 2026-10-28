/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.fabricmc.fabric.api.event.player.AttackEntityCallback
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.InteractionResult
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package dev.neonlyrics;

import com.mojang.blaze3d.platform.InputConstants;
import dev.neonlyrics.License;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.config.Storage;
import dev.neonlyrics.cosmetic.DeathFx;
import dev.neonlyrics.cosmetic.PhoneMenu;
import dev.neonlyrics.gui.LicenseScreen;
import dev.neonlyrics.hud.BeatGlow;
import dev.neonlyrics.hud.Crosshair;
import dev.neonlyrics.hud.GlassHotbar;
import dev.neonlyrics.hud.Island;
import dev.neonlyrics.hud.StartupIntro;
import dev.neonlyrics.lyrics.Favorites;
import dev.neonlyrics.lyrics.SpotifyClient;
import dev.neonlyrics.sky.SkyRenderer;
import dev.neonlyrics.sky.WorldSky;
import dev.neonlyrics.util.AutoPerf;
import dev.neonlyrics.util.Fx;
import dev.neonlyrics.util.Profiles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NeonLyrics
implements ClientModInitializer {
    public static final String MOD_ID = "neonlyrics";
    public static final Logger LOG = LoggerFactory.getLogger((String)"NeonLyrics");
    private static Config config = new Config();
    public static KeyMapping keyMenu;
    public static KeyMapping keySkip;
    public static KeyMapping keyPrev;
    public static KeyMapping keyPause;
    public static KeyMapping keyLyrics;
    public static KeyMapping keySync;
    public static KeyMapping keyFav;
    public static KeyMapping keyClip;
    public static KeyMapping keyProfile;
    private static boolean licenseHint;

    public static Config cfg() {
        return config;
    }

    public static void saveConfig() {
        config.clamp();
        Storage.save("config.json", config);
    }

    public void onInitializeClient() {
        Storage.migrateFromAuction();
        config = Storage.load("config.json", Config.class, Config::new);
        config.clamp();
        NeonLyrics.saveConfig();
        KeyMapping.Category cat = KeyMapping.Category.register((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"main"));
        keyMenu = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.openmenu", InputConstants.Type.KEYSYM, 74, cat));
        keySkip = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.skip", InputConstants.Type.KEYSYM, 298, cat));
        keyPrev = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.prev", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), cat));
        keyPause = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.pause", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), cat));
        keyFav = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.fav", InputConstants.Type.KEYSYM, 78, cat));
        keyClip = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.clip", InputConstants.Type.KEYSYM, 299, cat));
        keyProfile = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.profile", InputConstants.Type.KEYSYM, 297, cat));
        keySync = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.sync", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), cat));
        keyLyrics = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.neonlyrics.lyrics", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), cat));
        HudElementRegistry.addFirst((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"sky"), SkyRenderer::render);
        HudElementRegistry.addLast((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"island"), Island::render);
        HudElementRegistry.replaceElement((Identifier)VanillaHudElements.HOTBAR, GlassHotbar::wrap);
        HudElementRegistry.replaceElement((Identifier)VanillaHudElements.CROSSHAIR, Crosshair::wrap);
        HudElementRegistry.addLast((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"beatglow"), BeatGlow::render);
        LevelRenderEvents.COLLECT_SUBMITS.register(WorldSky::render);
        SpotifyClient.init();
        StartupIntro.register();
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            DeathFx.onAttack(entity);
            return InteractionResult.PASS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ClientTickEvents.START_CLIENT_TICK.register(PhoneMenu::startTick);
        LOG.info("Lyrics Cord geladen");
    }

    private void tick(Minecraft mc) {
        if (!License.ok()) {
            while (keyMenu.consumeClick()) {
                if (mc.screen != null) continue;
                mc.setScreen((Screen)new LicenseScreen());
            }
            for (KeyMapping k : new KeyMapping[]{keySkip, keyPrev, keyPause, keySync, keyFav, keyClip, keyProfile, keyLyrics}) {
                while (k.consumeClick()) {
                }
            }
            if (!licenseHint && mc.player != null) {
                licenseHint = true;
                mc.gui.getChat().addClientSystemMessage((Component)Component.translatable((String)"neonlyrics.lic.chat"));
            }
            return;
        }
        AutoPerf.tick(mc);
        PhoneMenu.tick(mc);
        DeathFx.tick(mc);
        SkyRenderer.tick(mc);
        while (keyMenu.consumeClick()) {
            if (mc.screen != null) continue;
            PhoneMenu.open(mc);
        }
        while (keySkip.consumeClick()) {
            SpotifyClient.control(SpotifyClient.Cmd.NEXT);
        }
        while (keyPrev.consumeClick()) {
            SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
        }
        while (keyPause.consumeClick()) {
            SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
        }
        while (keySync.consumeClick()) {
            SpotifyClient.syncHere();
        }
        while (keyFav.consumeClick()) {
            Favorites.saveCurrent();
        }
        while (keyClip.consumeClick()) {
            mc.options.hideGui = !mc.options.hideGui;
        }
        while (keyProfile.consumeClick()) {
            Profiles.toggle();
        }
        while (keyLyrics.consumeClick()) {
            NeonLyrics.config.lyricsSky = !NeonLyrics.config.lyricsSky;
            NeonLyrics.saveConfig();
            Island.notify(Fx.t(NeonLyrics.config.lyricsSky ? "neonlyrics.media.lyricson" : "neonlyrics.media.lyricsoff", new Object[0]), NeonLyrics.config.lyricsSky);
        }
    }
}

