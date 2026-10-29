/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.lyrics;

import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Storage;
import dev.neonlyrics.hud.Island;
import dev.neonlyrics.lyrics.NowPlaying;
import dev.neonlyrics.lyrics.SpotifyClient;
import dev.neonlyrics.util.Fx;
import java.util.ArrayList;
import java.util.List;

public final class Favorites {
    private static Data data;

    private Favorites() {
    }

    private static Data data() {
        if (data == null) {
            data = Storage.load("favorites.json", Data.class, Data::new);
            if (Favorites.data.list == null) {
                Favorites.data.list = new ArrayList<Entry>();
            }
        }
        return data;
    }

    public static List<Entry> all() {
        return Favorites.data().list;
    }

    public static void saveCurrent() {
        NowPlaying np = SpotifyClient.now();
        if (!np.hasTrack() || np.lines.isEmpty()) {
            Island.notify(Fx.t("neonlyrics.media.nolyrics", new Object[0]), false);
            return;
        }
        long p = np.progressNow(NeonLyrics.cfg().lyricsOffsetMs);
        int li = np.lineIndex(p);
        if (li < 0 || np.lines.get(li).text().isBlank()) {
            Island.notify(Fx.t("neonlyrics.fav.noline", new Object[0]), false);
            return;
        }
        String text = np.lines.get(li).text();
        for (Entry e : Favorites.data().list) {
            if (!e.line.equals(text) || !e.title.equals(np.title)) continue;
            Island.notify(Fx.t("neonlyrics.fav.already", new Object[0]), true);
            return;
        }
        Entry e = new Entry();
        e.line = text;
        e.title = np.title;
        e.artist = np.artist;
        e.time = System.currentTimeMillis();
        Favorites.data().list.add(0, e);
        while (Favorites.data().list.size() > 200) {
            Favorites.data().list.remove(Favorites.data().list.size() - 1);
        }
        Storage.save("favorites.json", Favorites.data());
        Island.notify(Fx.t("neonlyrics.fav.saved", new Object[0]), true);
    }

    public static void remove(Entry e) {
        Favorites.data().list.remove(e);
        Storage.save("favorites.json", Favorites.data());
    }

    private static final class Data {
        List<Entry> list = new ArrayList<Entry>();

        private Data() {
        }
    }

    public static final class Entry {
        public String line = "";
        public String title = "";
        public String artist = "";
        public long time;
    }
}

