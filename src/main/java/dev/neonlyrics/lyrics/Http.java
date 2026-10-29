/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.lyrics;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class Http {
    static final String UA = "NeonLyricsHUD/1.1 (Minecraft Fabric mod)";

    private Http() {
    }

    static Response get(String url, Map<String, String> headers) throws Exception {
        return Http.send("GET", url, headers, null, null);
    }

    static Response postForm(String url, Map<String, String> headers, String form) throws Exception {
        return Http.send("POST", url, headers, form, "application/x-www-form-urlencoded");
    }

    static byte[] getBytes(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection)URI.create(url).toURL().openConnection();
        c.setConnectTimeout(6000);
        c.setReadTimeout(8000);
        c.setRequestProperty("User-Agent", UA);
        try {
            byte[] byArray;
            block9: {
                InputStream in = c.getInputStream();
                try {
                    byArray = in.readAllBytes();
                    if (in == null) break block9;
                }
                catch (Throwable throwable) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                in.close();
            }
            return byArray;
        }
        finally {
            c.disconnect();
        }
    }

    static Response empty(String method, String url, Map<String, String> headers) throws Exception {
        return Http.send(method, url, headers, "", "application/json");
    }

    private static Response send(String method, String url, Map<String, String> headers, String body, String contentType) throws Exception {
        int code;
        HttpURLConnection c = (HttpURLConnection)URI.create(url).toURL().openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(6000);
        c.setReadTimeout(8000);
        c.setRequestProperty("User-Agent", UA);
        c.setRequestProperty("Accept", "application/json");
        for (Map.Entry<String, String> h : headers.entrySet()) {
            c.setRequestProperty(h.getKey(), h.getValue());
        }
        if (body != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", contentType);
            try (OutputStream os = c.getOutputStream();){
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        InputStream in = (code = c.getResponseCode()) >= 400 ? c.getErrorStream() : c.getInputStream();
        String text = "";
        if (in != null) {
            try (InputStream inputStream = in;){
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                in.transferTo(bo);
                text = bo.toString(StandardCharsets.UTF_8);
            }
        }
        c.disconnect();
        return new Response(code, text);
    }

    record Response(int code, String body) {
    }
}

