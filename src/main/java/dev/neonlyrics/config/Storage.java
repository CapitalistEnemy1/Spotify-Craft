/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package dev.neonlyrics.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neonlyrics.NeonLyrics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public final class Storage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Storage() {
    }

    public static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("neonlyrics");
    }

    public static void migrateFromAuction() {
        try {
            Path oldCfg;
            Path old = FabricLoader.getInstance().getConfigDir().resolve("neonauction");
            if (!Files.isDirectory(old, new LinkOption[0]) || Files.exists(Storage.dir().resolve("config.json"), new LinkOption[0])) {
                return;
            }
            Files.createDirectories(Storage.dir(), new FileAttribute[0]);
            Path path = oldCfg = Files.exists(old.resolve("config-v13-backup.json"), new LinkOption[0]) ? old.resolve("config-v13-backup.json") : old.resolve("config.json");
            if (Files.exists(oldCfg, new LinkOption[0])) {
                Files.copy(oldCfg, Storage.dir().resolve("config.json"), new CopyOption[0]);
            }
            for (String f : new String[]{"spotify.json", "favorites.json"}) {
                Path src = old.resolve(f);
                if (!Files.exists(src, new LinkOption[0]) || Files.exists(Storage.dir().resolve(f), new LinkOption[0])) continue;
                Files.copy(src, Storage.dir().resolve(f), new CopyOption[0]);
            }
            NeonLyrics.LOG.info("Einstellungen aus config/neonauction uebernommen");
        }
        catch (Exception e) {
            NeonLyrics.LOG.warn("Konnte alte Einstellungen nicht uebernehmen", (Throwable)e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T load(String file, Type type, Supplier<T> fallback) {
        Path p = Storage.dir().resolve(file);
        if (!Files.exists(p, new LinkOption[0])) {
            return fallback.get();
        }
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8);){
            Object t = GSON.fromJson((Reader)r, type);
            Object object = t != null ? t : fallback.get();
            return (T)object;
        }
        catch (Exception e) {
            NeonLyrics.LOG.warn("Konnte {} nicht laden, nutze Standardwerte", (Object)file, (Object)e);
            return fallback.get();
        }
    }

    public static void save(String file, Object data) {
        try {
            Files.createDirectories(Storage.dir(), new FileAttribute[0]);
            Path p = Storage.dir().resolve(file);
            Path tmp = Storage.dir().resolve(file + ".tmp");
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(data, (Appendable)w);
            }
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            NeonLyrics.LOG.warn("Konnte {} nicht speichern", (Object)file, (Object)e);
        }
    }
}

