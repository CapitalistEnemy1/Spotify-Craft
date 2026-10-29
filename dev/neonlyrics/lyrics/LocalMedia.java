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
import dev.neonlyrics.config.Storage;
import dev.neonlyrics.hud.Cover;
import dev.neonlyrics.lyrics.SpotifyClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.util.Locale;

public final class LocalMedia {
    private static volatile Status status = Status.STARTING;
    private static volatile String app = "";
    private static volatile String lastError = "";
    private static volatile Process proc;
    private static volatile long restartAt;
    private static int failures;
    private static Path log;
    private static String ownId;
    private static long ownStart;
    private static long pausedSince;
    private static int logLines;
    private static long lastCover;
    public static final boolean WINDOWS;
    public static volatile boolean exactTime;
    private static final String SCRIPT = "$ErrorActionPreference = 'Stop'\n[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false\nfunction Out-Line($l) { try { [Console]::Out.WriteLine($l); [Console]::Out.Flush() } catch { exit } }\n$mgr = $null\ntry {\n  Add-Type -AssemblyName System.Runtime.WindowsRuntime\n  $asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\n  function Await($op, $type) { $t = $asTask.MakeGenericMethod($type).Invoke($null, @($op)); $null = $t.Wait(-1); $t.Result }\n  $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\n  $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties,Windows.Media.Control,ContentType=WindowsRuntime]\n  $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\n  [Console]::Error.WriteLine('mediensteuerung ok')\n} catch { [Console]::Error.WriteLine('mediensteuerung fehlt: ' + $_) }\nfunction FromSession($s) {\n  $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n  if ($null -eq $p -or [string]::IsNullOrEmpty($p.Title)) { return $null }\n  $tl = $s.GetTimelineProperties()\n  $playing = ($s.GetPlaybackInfo().PlaybackStatus.ToString() -eq 'Playing')\n  $pos = $tl.Position.TotalMilliseconds\n  $dur = $tl.EndTime.TotalMilliseconds\n  if ($dur -le 0) { $pos = -1 }\n  elseif ($playing) {\n    $d = ([DateTimeOffset]::Now - $tl.LastUpdatedTime).TotalMilliseconds\n    if ($d -gt 0 -and $d -lt $dur) { $pos += $d }\n  }\n  # Albumcover einmal pro Song als Datei ablegen\n  $key = [string]$p.Title + '|' + [string]$p.Artist\n  if ($key -ne $script:coverKey) {\n    $script:coverKey = $key\n    try {\n      if ($null -ne $p.Thumbnail) {\n        $rs = Await ($p.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\n        $ns = [System.IO.WindowsRuntimeStreamExtensions]::AsStreamForRead($rs)\n        $fs = [System.IO.File]::Create($coverFile)\n        $ns.CopyTo($fs); $fs.Close(); $ns.Close()\n        $script:coverId++\n      } else { $script:coverId = -[Math]::Abs($script:coverId) - 1 }\n    } catch { $script:coverId = -[Math]::Abs($script:coverId) - 1 }\n  }\n  return [ordered]@{ app = [string]$s.SourceAppUserModelId; title = [string]$p.Title; artist = [string]$p.Artist; album = [string]$p.AlbumTitle; pos = [long]$pos; dur = [long]$dur; playing = $playing; cover = $script:coverId }\n}\nfunction FromWindow {\n  # Ersatz: Fenstertitel der Spotify-App (\"Kuenstler - Titel\"). Pausiert steht dort nur \"Spotify\".\n  $p = Get-Process -Name Spotify -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '* - *' } | Select-Object -First 1\n  if ($null -eq $p) { return $null }\n  $t = $p.MainWindowTitle; $i = $t.IndexOf(' - ')\n  return [ordered]@{ app = 'Spotify.exe'; title = $t.Substring($i + 3); artist = $t.Substring(0, $i); album = ''; pos = -1; dur = 0; playing = $true }\n}\n$cmdFile = Join-Path $PSScriptRoot 'spotify-cmd.txt'\n$coverFile = Join-Path $PSScriptRoot 'cover.img'\n$script:coverKey = ''\n$script:coverId = 0\ntry { $null = [Windows.Storage.Streams.IRandomAccessStreamWithContentType,Windows.Storage.Streams,ContentType=WindowsRuntime] } catch {}\nfunction Do-Command($cmd) {\n  $done = $false\n  try {\n    if ($null -ne $mgr) {\n      $s = $null\n      foreach ($x in $mgr.GetSessions()) { if ($x.SourceAppUserModelId -like '*Spotify*') { $s = $x; break } }\n      if ($null -eq $s) { $s = $mgr.GetCurrentSession() }\n      if ($null -ne $s) {\n        if ($cmd -eq 'next') { $done = Await ($s.TrySkipNextAsync()) ([bool]) }\n        elseif ($cmd -eq 'previous') { $done = Await ($s.TrySkipPreviousAsync()) ([bool]) }\n        elseif ($cmd -eq 'playpause') { $done = Await ($s.TryTogglePlayPauseAsync()) ([bool]) }\n      }\n    }\n  } catch { [Console]::Error.WriteLine('befehl fehlgeschlagen: ' + $_) }\n  if (-not $done) {\n    # Ersatz: Medientaste druecken (176 = naechster, 177 = vorheriger, 179 = Play/Pause)\n    $key = 0\n    if ($cmd -eq 'next') { $key = 176 } elseif ($cmd -eq 'previous') { $key = 177 } elseif ($cmd -eq 'playpause') { $key = 179 }\n    if ($key -ne 0) { try { (New-Object -ComObject WScript.Shell).SendKeys([string][char]$key) } catch {} }\n  }\n}\nwhile ($true) {\n  $o = $null\n  try {\n    if ($null -ne $mgr) {\n      foreach ($x in $mgr.GetSessions()) { if ($x.SourceAppUserModelId -like '*Spotify*') { $o = FromSession $x; break } }\n    }\n    if ($null -eq $o) { $o = FromWindow }\n    if ($null -eq $o -and $null -ne $mgr) { $c = $mgr.GetCurrentSession(); if ($null -ne $c) { $o = FromSession $c } }\n  } catch { [Console]::Error.WriteLine('fehler: ' + $_) }\n  if ($null -eq $o) { Out-Line '{}' } else { Out-Line ($o | ConvertTo-Json -Compress) }\n  for ($w = 0; $w -lt 5; $w++) {\n    if (Test-Path -LiteralPath $cmdFile) {\n      $cmd = ''\n      try { $cmd = (Get-Content -LiteralPath $cmdFile -Raw).Trim(); Remove-Item -LiteralPath $cmdFile -Force } catch {}\n      Do-Command $cmd\n      break\n    }\n    Start-Sleep -Milliseconds 100\n  }\n}\n";

    private LocalMedia() {
    }

    private static synchronized void logLine(String l) {
        Path f = log;
        if (f == null || logLines++ > 200) {
            return;
        }
        try {
            Files.writeString(f, (CharSequence)(l + "\n"), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static Status status() {
        return WINDOWS ? status : Status.UNSUPPORTED;
    }

    public static String app() {
        return app;
    }

    public static String lastError() {
        return lastError;
    }

    static void ensureRunning() {
        if (!WINDOWS) {
            return;
        }
        Process p = proc;
        if (p != null && p.isAlive()) {
            return;
        }
        if (System.currentTimeMillis() < restartAt) {
            return;
        }
        LocalMedia.start();
    }

    public static void command(String cmd) {
        try {
            Path f = Storage.dir().resolve("spotify-cmd.txt");
            Files.writeString(f, (CharSequence)cmd, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception e) {
            lastError = String.valueOf(e.getMessage());
        }
    }

    public static void stop() {
        Process p = proc;
        proc = null;
        if (p != null) {
            p.destroy();
        }
        status = Status.STARTING;
    }

    public static void restart() {
        failures = 0;
        restartAt = 0L;
        LocalMedia.stop();
    }

    private static void start() {
        try {
            Path script = Storage.dir().resolve("spotify-helper.ps1");
            Files.createDirectories(script.getParent(), new FileAttribute[0]);
            Files.writeString(script, (CharSequence)"\ufeff$ErrorActionPreference = 'Stop'\n[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false\nfunction Out-Line($l) { try { [Console]::Out.WriteLine($l); [Console]::Out.Flush() } catch { exit } }\n$mgr = $null\ntry {\n  Add-Type -AssemblyName System.Runtime.WindowsRuntime\n  $asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\n  function Await($op, $type) { $t = $asTask.MakeGenericMethod($type).Invoke($null, @($op)); $null = $t.Wait(-1); $t.Result }\n  $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\n  $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties,Windows.Media.Control,ContentType=WindowsRuntime]\n  $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\n  [Console]::Error.WriteLine('mediensteuerung ok')\n} catch { [Console]::Error.WriteLine('mediensteuerung fehlt: ' + $_) }\nfunction FromSession($s) {\n  $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n  if ($null -eq $p -or [string]::IsNullOrEmpty($p.Title)) { return $null }\n  $tl = $s.GetTimelineProperties()\n  $playing = ($s.GetPlaybackInfo().PlaybackStatus.ToString() -eq 'Playing')\n  $pos = $tl.Position.TotalMilliseconds\n  $dur = $tl.EndTime.TotalMilliseconds\n  if ($dur -le 0) { $pos = -1 }\n  elseif ($playing) {\n    $d = ([DateTimeOffset]::Now - $tl.LastUpdatedTime).TotalMilliseconds\n    if ($d -gt 0 -and $d -lt $dur) { $pos += $d }\n  }\n  # Albumcover einmal pro Song als Datei ablegen\n  $key = [string]$p.Title + '|' + [string]$p.Artist\n  if ($key -ne $script:coverKey) {\n    $script:coverKey = $key\n    try {\n      if ($null -ne $p.Thumbnail) {\n        $rs = Await ($p.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\n        $ns = [System.IO.WindowsRuntimeStreamExtensions]::AsStreamForRead($rs)\n        $fs = [System.IO.File]::Create($coverFile)\n        $ns.CopyTo($fs); $fs.Close(); $ns.Close()\n        $script:coverId++\n      } else { $script:coverId = -[Math]::Abs($script:coverId) - 1 }\n    } catch { $script:coverId = -[Math]::Abs($script:coverId) - 1 }\n  }\n  return [ordered]@{ app = [string]$s.SourceAppUserModelId; title = [string]$p.Title; artist = [string]$p.Artist; album = [string]$p.AlbumTitle; pos = [long]$pos; dur = [long]$dur; playing = $playing; cover = $script:coverId }\n}\nfunction FromWindow {\n  # Ersatz: Fenstertitel der Spotify-App (\"Kuenstler - Titel\"). Pausiert steht dort nur \"Spotify\".\n  $p = Get-Process -Name Spotify -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '* - *' } | Select-Object -First 1\n  if ($null -eq $p) { return $null }\n  $t = $p.MainWindowTitle; $i = $t.IndexOf(' - ')\n  return [ordered]@{ app = 'Spotify.exe'; title = $t.Substring($i + 3); artist = $t.Substring(0, $i); album = ''; pos = -1; dur = 0; playing = $true }\n}\n$cmdFile = Join-Path $PSScriptRoot 'spotify-cmd.txt'\n$coverFile = Join-Path $PSScriptRoot 'cover.img'\n$script:coverKey = ''\n$script:coverId = 0\ntry { $null = [Windows.Storage.Streams.IRandomAccessStreamWithContentType,Windows.Storage.Streams,ContentType=WindowsRuntime] } catch {}\nfunction Do-Command($cmd) {\n  $done = $false\n  try {\n    if ($null -ne $mgr) {\n      $s = $null\n      foreach ($x in $mgr.GetSessions()) { if ($x.SourceAppUserModelId -like '*Spotify*') { $s = $x; break } }\n      if ($null -eq $s) { $s = $mgr.GetCurrentSession() }\n      if ($null -ne $s) {\n        if ($cmd -eq 'next') { $done = Await ($s.TrySkipNextAsync()) ([bool]) }\n        elseif ($cmd -eq 'previous') { $done = Await ($s.TrySkipPreviousAsync()) ([bool]) }\n        elseif ($cmd -eq 'playpause') { $done = Await ($s.TryTogglePlayPauseAsync()) ([bool]) }\n      }\n    }\n  } catch { [Console]::Error.WriteLine('befehl fehlgeschlagen: ' + $_) }\n  if (-not $done) {\n    # Ersatz: Medientaste druecken (176 = naechster, 177 = vorheriger, 179 = Play/Pause)\n    $key = 0\n    if ($cmd -eq 'next') { $key = 176 } elseif ($cmd -eq 'previous') { $key = 177 } elseif ($cmd -eq 'playpause') { $key = 179 }\n    if ($key -ne 0) { try { (New-Object -ComObject WScript.Shell).SendKeys([string][char]$key) } catch {} }\n  }\n}\nwhile ($true) {\n  $o = $null\n  try {\n    if ($null -ne $mgr) {\n      foreach ($x in $mgr.GetSessions()) { if ($x.SourceAppUserModelId -like '*Spotify*') { $o = FromSession $x; break } }\n    }\n    if ($null -eq $o) { $o = FromWindow }\n    if ($null -eq $o -and $null -ne $mgr) { $c = $mgr.GetCurrentSession(); if ($null -ne $c) { $o = FromSession $c } }\n  } catch { [Console]::Error.WriteLine('fehler: ' + $_) }\n  if ($null -eq $o) { Out-Line '{}' } else { Out-Line ($o | ConvertTo-Json -Compress) }\n  for ($w = 0; $w -lt 5; $w++) {\n    if (Test-Path -LiteralPath $cmdFile) {\n      $cmd = ''\n      try { $cmd = (Get-Content -LiteralPath $cmdFile -Raw).Trim(); Remove-Item -LiteralPath $cmdFile -Force } catch {}\n      Do-Command $cmd\n      break\n    }\n    Start-Sleep -Milliseconds 100\n  }\n}\n", StandardCharsets.UTF_8, new OpenOption[0]);
            Files.deleteIfExists(Storage.dir().resolve("spotify-cmd.txt"));
            log = Storage.dir().resolve("spotify-helper.log");
            logLines = 0;
            Files.writeString(log, (CharSequence)("Start " + String.valueOf(LocalDateTime.now()) + "\n"), StandardCharsets.UTF_8, new OpenOption[0]);
            String root = System.getenv("SystemRoot");
            File ps = new File(root == null ? "C:\\Windows" : root, "System32\\WindowsPowerShell\\v1.0\\powershell.exe");
            ProcessBuilder pb = new ProcessBuilder(ps.isFile() ? ps.getPath() : "powershell.exe", "-NoProfile", "-NonInteractive", "-NoLogo", "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-File", script.toString());
            Process p = pb.start();
            p.getOutputStream().close();
            proc = p;
            status = Status.STARTING;
            long started = System.currentTimeMillis();
            Thread err = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));){
                    String l;
                    while ((l = r.readLine()) != null) {
                        if (l.isBlank()) continue;
                        lastError = l.trim();
                        LocalMedia.logLine(l.trim());
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }, "NeonLyrics-MediaErr");
            err.setDaemon(true);
            err.start();
            Thread out = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));){
                    String l;
                    while ((l = r.readLine()) != null) {
                        if (proc != p) {
                            break;
                        }
                        LocalMedia.handle(l.trim());
                    }
                }
                catch (Exception e) {
                    lastError = String.valueOf(e.getMessage());
                }
                if (proc == p) {
                    proc = null;
                    failures = System.currentTimeMillis() - started < 10000L ? ++failures : 0;
                    status = failures >= 3 ? Status.FAILED : Status.STARTING;
                    restartAt = System.currentTimeMillis() + Math.min(60000L, 3000L * (1L << Math.min(4, failures)));
                    LocalMedia.logLine("Helfer beendet (Code " + (p.isAlive() ? "?" : String.valueOf(p.exitValue())) + ")");
                    if (failures >= 3) {
                        NeonLyrics.LOG.warn("Windows-Mediensteuerung nicht verfuegbar: {}", (Object)lastError);
                    }
                }
            }, "NeonLyrics-Media");
            out.setDaemon(true);
            out.start();
        }
        catch (Exception e) {
            lastError = String.valueOf(e.getMessage());
            LocalMedia.logLine("Start fehlgeschlagen: " + lastError);
            status = Status.FAILED;
            restartAt = System.currentTimeMillis() + 60000L;
        }
    }

    private static void handle(String line) {
        long cov;
        JsonObject o;
        if (!line.startsWith("{")) {
            return;
        }
        try {
            o = JsonParser.parseString((String)line).getAsJsonObject();
        }
        catch (Exception e) {
            return;
        }
        if (!o.has("title")) {
            if (pausedSince == 0L) {
                pausedSince = System.currentTimeMillis();
            }
            status = Status.NO_PLAYER;
            app = "";
            SpotifyClient.publishNothing();
            return;
        }
        String title = LocalMedia.str(o, "title");
        String artist = LocalMedia.str(o, "artist");
        if (title.isBlank()) {
            status = Status.NO_PLAYER;
            SpotifyClient.publishNothing();
            return;
        }
        failures = 0;
        app = LocalMedia.prettyApp(LocalMedia.str(o, "app"));
        status = Status.PLAYING;
        long pos = o.has("pos") ? o.get("pos").getAsLong() : 0L;
        long dur = o.has("dur") ? o.get("dur").getAsLong() : 0L;
        boolean playing = o.has("playing") && o.get("playing").getAsBoolean();
        String id = title + "|" + artist;
        long nowMs = System.currentTimeMillis();
        boolean bl = exactTime = pos >= 0L;
        if (pos < 0L) {
            if (!id.equals(ownId)) {
                ownId = id;
                ownStart = nowMs;
            } else if (pausedSince > 0L) {
                ownStart += nowMs - pausedSince;
            }
            pausedSince = 0L;
            pos = nowMs - ownStart;
        }
        if (dur > 0L) {
            pos = Math.max(0L, Math.min(pos, dur));
        }
        long l = cov = o.has("cover") ? o.get("cover").getAsLong() : 0L;
        if (cov != lastCover) {
            lastCover = cov;
            if (cov > 0L) {
                Thread t = new Thread(() -> {
                    try {
                        Cover.load(Files.readAllBytes(Storage.dir().resolve("cover.img")));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }, "NeonLyrics-Cover");
                t.setDaemon(true);
                t.start();
            } else {
                Cover.clear();
            }
        }
        SpotifyClient.publish("local:" + title + "|" + artist, title, artist, artist, LocalMedia.str(o, "album"), dur, pos, playing);
    }

    private static String prettyApp(String id) {
        String l = id.toLowerCase(Locale.ROOT);
        if (l.contains("spotify")) {
            return "Spotify";
        }
        if (l.contains("chrome")) {
            return "Chrome";
        }
        if (l.contains("msedge") || l.contains("edge")) {
            return "Edge";
        }
        if (l.contains("firefox")) {
            return "Firefox";
        }
        if (l.contains("opera")) {
            return "Opera";
        }
        if (l.contains("zunemusic") || l.contains("media")) {
            return "Media Player";
        }
        int bang = id.indexOf(33);
        String s = bang > 0 ? id.substring(0, bang) : id;
        return s.replace(".exe", "");
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    static {
        restartAt = 0L;
        failures = 0;
        ownId = "";
        ownStart = 0L;
        pausedSince = 0L;
        logLines = 0;
        lastCover = 0L;
        WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        exactTime = true;
    }

    public static enum Status {
        UNSUPPORTED,
        STARTING,
        NO_PLAYER,
        PLAYING,
        FAILED;

    }
}

