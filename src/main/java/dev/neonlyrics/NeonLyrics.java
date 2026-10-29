/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.fabricmc.fabric.api.event.player.AttackEntityCallback
 *  net.minecraft.class_1269
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package dev.neonlyrics;

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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.class_1269;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NeonLyrics
implements ClientModInitializer {
    public static final String MOD_ID = "neonlyrics";
    public static final Logger LOG = LoggerFactory.getLogger((String)"NeonLyrics");
    private static Config config = new Config();
    public static class_304 keyMenu;
    public static class_304 keySkip;
    public static class_304 keyPrev;
    public static class_304 keyPause;
    public static class_304 keyLyrics;
    public static class_304 keySync;
    public static class_304 keyFav;
    public static class_304 keyClip;
    public static class_304 keyProfile;
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
        class_304.class_11900 cat = class_304.class_11900.method_74698((class_2960)class_2960.method_60655((String)MOD_ID, (String)"main"));
        keyMenu = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.openmenu", class_3675.class_307.field_1668, 74, cat));
        keySkip = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.skip", class_3675.class_307.field_1668, 298, cat));
        keyPrev = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.prev", class_3675.class_307.field_1668, -1, cat));
        keyPause = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.pause", class_3675.class_307.field_1668, -1, cat));
        keyFav = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.fav", class_3675.class_307.field_1668, 78, cat));
        keyClip = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.clip", class_3675.class_307.field_1668, 299, cat));
        keyProfile = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.profile", class_3675.class_307.field_1668, 297, cat));
        keySync = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.sync", class_3675.class_307.field_1668, -1, cat));
        keyLyrics = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.neonlyrics.lyrics", class_3675.class_307.field_1668, -1, cat));
        HudElementRegistry.addFirst((class_2960)class_2960.method_60655((String)MOD_ID, (String)"sky"), SkyRenderer::render);
        HudElementRegistry.addLast((class_2960)class_2960.method_60655((String)MOD_ID, (String)"island"), Island::render);
        HudElementRegistry.replaceElement((class_2960)VanillaHudElements.HOTBAR, GlassHotbar::wrap);
        HudElementRegistry.replaceElement((class_2960)VanillaHudElements.CROSSHAIR, Crosshair::wrap);
        HudElementRegistry.addLast((class_2960)class_2960.method_60655((String)MOD_ID, (String)"beatglow"), BeatGlow::render);
        WorldRenderEvents.AFTER_ENTITIES.register(WorldSky::render);
        SpotifyClient.init();
        StartupIntro.register();
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            DeathFx.onAttack(entity);
            return class_1269.field_5811;
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ClientTickEvents.START_CLIENT_TICK.register(PhoneMenu::startTick);
        LOG.info("SpotifyCraft geladen");
    }

    private void tick(class_310 mc) {
        if (!License.ok()) {
            while (keyMenu.method_1436()) {
                if (mc.field_1755 != null) continue;
                mc.method_1507((class_437)new LicenseScreen());
            }
            for (class_304 k : new class_304[]{keySkip, keyPrev, keyPause, keySync, keyFav, keyClip, keyProfile, keyLyrics}) {
                while (k.method_1436()) {
                }
            }
            if (!licenseHint && mc.field_1724 != null) {
                licenseHint = true;
                mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43471((String)"neonlyrics.lic.chat"));
            }
            return;
        }
        AutoPerf.tick(mc);
        PhoneMenu.tick(mc);
        DeathFx.tick(mc);
        SkyRenderer.tick(mc);
        while (keyMenu.method_1436()) {
            if (mc.field_1755 != null) continue;
            PhoneMenu.open(mc);
        }
        while (keySkip.method_1436()) {
            SpotifyClient.control(SpotifyClient.Cmd.NEXT);
        }
        while (keyPrev.method_1436()) {
            SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
        }
        while (keyPause.method_1436()) {
            SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
        }
        while (keySync.method_1436()) {
            SpotifyClient.syncHere();
        }
        while (keyFav.method_1436()) {
            Favorites.saveCurrent();
        }
        while (keyClip.method_1436()) {
            mc.field_1690.field_1842 = !mc.field_1690.field_1842;
        }
        while (keyProfile.method_1436()) {
            Profiles.toggle();
        }
        while (keyLyrics.method_1436()) {
            NeonLyrics.config.lyricsSky = !NeonLyrics.config.lyricsSky;
            NeonLyrics.saveConfig();
            Island.notify(Fx.t(NeonLyrics.config.lyricsSky ? "neonlyrics.media.lyricson" : "neonlyrics.media.lyricsoff", new Object[0]), NeonLyrics.config.lyricsSky);
        }
    }
}

