/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package dev.neonlyrics.lyrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.lyrics.Http;
import dev.neonlyrics.lyrics.LyricsProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Translator {
    private static final Map<String, String> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, String>(256, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> e) {
            return this.size() > 400;
        }
    });
    private static final Set<String> PENDING = Collections.synchronizedSet(new HashSet());
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NeonLyrics-Translate");
        t.setDaemon(true);
        return t;
    });
    private static volatile long blockedUntil = 0L;

    private Translator() {
    }

    public static String get(String line) {
        if (!NeonLyrics.cfg().translate || line == null || line.isBlank()) {
            return null;
        }
        String key = NeonLyrics.cfg().translateTo + "|" + line;
        String v = CACHE.get(key);
        if (v != null) {
            return v.isEmpty() ? null : v;
        }
        Translator.request(line);
        return null;
    }

    public static void prefetch(String line) {
        if (NeonLyrics.cfg().translate && line != null && !line.isBlank()) {
            Translator.get(line);
        }
    }

    private static void request(String line) {
        String to = NeonLyrics.cfg().translateTo;
        String key = to + "|" + line;
        if (System.currentTimeMillis() < blockedUntil || !PENDING.add(key)) {
            return;
        }
        EXEC.execute(() -> {
            try {
                JsonObject d;
                JsonObject o;
                String url = "https://api.mymemory.translated.net/get?q=" + LyricsProvider.enc(line) + "&langpair=en%7C" + LyricsProvider.enc(to);
                Http.Response r = Http.get(url, Map.of());
                if (r.code() == 429) {
                    blockedUntil = System.currentTimeMillis() + 60000L;
                    return;
                }
                String out = "";
                if (r.code() == 200 && (o = JsonParser.parseString((String)r.body()).getAsJsonObject()).has("responseData") && o.get("responseData").isJsonObject() && (d = o.getAsJsonObject("responseData")).has("translatedText") && !d.get("translatedText").isJsonNull()) {
                    out = d.get("translatedText").getAsString().trim();
                }
                if (out.equalsIgnoreCase(line.trim()) || out.toUpperCase().contains("MYMEMORY WARNING")) {
                    out = "";
                }
                CACHE.put(key, out);
                Thread.sleep(350L);
            }
            catch (Exception e) {
                blockedUntil = System.currentTimeMillis() + 15000L;
            }
            finally {
                PENDING.remove(key);
            }
        });
    }
}

