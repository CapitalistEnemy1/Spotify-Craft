/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.compat;

import dev.neonlyrics.NeonLyrics;
import java.util.Locale;

public final class Compat {
    private Compat() {
    }

    public static void openUri(String url) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            ProcessBuilder pb = os.contains("win") ? new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url) : (os.contains("mac") ? new ProcessBuilder("open", url) : new ProcessBuilder("xdg-open", url));
            pb.start();
        }
        catch (Exception e) {
            NeonLyrics.LOG.warn("Link konnte nicht geoeffnet werden: {}", (Object)url, (Object)e);
        }
    }
}

