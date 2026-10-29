/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.lyrics;

import dev.neonlyrics.lyrics.NowPlaying;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class LyricTiming {
    public final long[][] wordStart;
    public final double beatMs;
    public final double beatPhase;
    public final float confidence;
    public final float sylPerSec;
    private static final Map<List<NowPlaying.Line>, LyricTiming> CACHE = Collections.synchronizedMap(new IdentityHashMap());

    private LyricTiming(long[][] ws, double beat, double phase, float conf, float sps) {
        this.wordStart = ws;
        this.beatMs = beat;
        this.beatPhase = phase;
        this.confidence = conf;
        this.sylPerSec = sps;
    }

    public static LyricTiming of(List<NowPlaying.Line> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        LyricTiming t = CACHE.get(lines);
        if (t == null) {
            t = LyricTiming.build(lines);
            if (CACHE.size() > 20) {
                CACHE.clear();
            }
            CACHE.put(lines, t);
        }
        return t;
    }

    static float weight(String w) {
        float s = NowPlaying.syllables(w);
        char last = w.charAt(w.length() - 1);
        if (last == ',' || last == '.' || last == '!' || last == '?' || last == ')') {
            s += 0.6f;
        }
        return s;
    }

    private static long nextStart(List<NowPlaying.Line> lines, int i) {
        return i + 1 < lines.size() ? lines.get(i + 1).timeMs() : lines.get(i).timeMs() + 5000L;
    }

    private static LyricTiming build(List<NowPlaying.Line> lines) {
        int n = lines.size();
        ArrayList<Float> rates = new ArrayList<Float>();
        for (int i = 0; i < n; ++i) {
            long gap;
            NowPlaying.Line l = lines.get(i);
            if (l.words().length < 3 || (gap = LyricTiming.nextStart(lines, i) - l.timeMs()) < 900L || gap > 9000L) continue;
            float syl = 0.0f;
            for (String w : l.words()) {
                syl += LyricTiming.weight(w);
            }
            rates.add(Float.valueOf(syl / ((float)gap * 0.85f / 1000.0f)));
        }
        float sps = 4.0f;
        if (!rates.isEmpty()) {
            Collections.sort(rates);
            sps = ((Float)rates.get(rates.size() / 2)).floatValue();
        }
        sps = Math.max(2.2f, Math.min(8.0f, sps));
        ArrayList<Long> starts = new ArrayList<Long>();
        for (NowPlaying.Line l : lines) {
            if (l.text().isBlank()) continue;
            starts.add(l.timeMs());
        }
        double bestBeat = 0.0;
        double bestPhase = 0.0;
        float bestScore = 0.0f;
        if (starts.size() >= 6) {
            for (double bpm = 60.0; bpm <= 180.0; bpm += 0.25) {
                float pref;
                double p = 60000.0 / bpm;
                double sx = 0.0;
                double sy = 0.0;
                Iterator iterator = starts.iterator();
                while (iterator.hasNext()) {
                    long t = (Long)iterator.next();
                    double a = Math.PI * 2 * ((double)t % p) / p;
                    sx += Math.cos(a);
                    sy += Math.sin(a);
                }
                float r = (float)(Math.sqrt(sx * sx + sy * sy) / (double)starts.size());
                float f = pref = bpm >= 80.0 && bpm <= 150.0 ? 1.0f : 0.93f;
                if (!(r * pref > bestScore)) continue;
                bestScore = r * pref;
                bestBeat = p;
                double ang = Math.atan2(sy, sx);
                if (ang < 0.0) {
                    ang += Math.PI * 2;
                }
                bestPhase = ang / (Math.PI * 2) * p;
            }
        }
        boolean useGrid = bestScore >= 0.45f && bestBeat > 0.0;
        long[][] ws = new long[n][];
        for (int i = 0; i < n; ++i) {
            NowPlaying.Line l = lines.get(i);
            String[] words = l.words();
            ws[i] = new long[words.length];
            if (words.length == 0) continue;
            if (l.wordTimes() != null && l.wordTimes().length == words.length) {
                ws[i] = (long[])l.wordTimes().clone();
                continue;
            }
            long start = l.timeMs();
            long gap = Math.min(LyricTiming.nextStart(lines, i) - start, 7000L);
            float total = 0.0f;
            for (String w : words) {
                total += LyricTiming.weight(w);
            }
            double dur = Math.min((double)gap * 0.92, Math.max(300.0, (double)(total / sps) * 1000.0));
            double acc = 0.0;
            double grid = 0.0;
            if (useGrid) {
                double perWord = dur / (double)words.length;
                grid = perWord < bestBeat / 2.0 * 0.75 ? bestBeat / 4.0 : bestBeat / 2.0;
            }
            long prev = start - 1L;
            for (int k = 0; k < words.length; ++k) {
                double rel;
                double snapped;
                double t = (double)start + acc / (double)total * dur;
                if (k == 0) {
                    t = start;
                } else if (grid > 0.0 && Math.abs((snapped = bestPhase + (double)Math.round((rel = t - bestPhase) / grid) * grid) - t) < grid * 0.6) {
                    t = snapped;
                }
                long ti = Math.max(prev + 40L, Math.round(t));
                ws[i][k] = ti = Math.min(ti, start + (long)((double)gap * 0.97));
                prev = ti;
                acc += (double)LyricTiming.weight(words[k]);
            }
        }
        return new LyricTiming(ws, useGrid ? bestBeat : 0.0, bestPhase, useGrid ? Math.min(1.0f, bestScore) : 0.0f, sps);
    }

    public float wordPosition(int line, long progress, long lineEnd) {
        if (line < 0 || line >= this.wordStart.length) {
            return -1.0f;
        }
        long[] s = this.wordStart[line];
        if (s.length == 0) {
            return -1.0f;
        }
        int k = Arrays.binarySearch(s, progress);
        if (k < 0) {
            k = -k - 2;
        }
        if (k < 0) {
            return 0.0f;
        }
        if (k >= s.length - 1) {
            long end = Math.max(s[s.length - 1] + 200L, Math.min(lineEnd, s[s.length - 1] + 900L));
            return (float)(s.length - 1) + Math.min(0.999f, (float)(progress - s[s.length - 1]) / (float)(end - s[s.length - 1]));
        }
        return (float)k + (float)(progress - s[k]) / (float)Math.max(1L, s[k + 1] - s[k]);
    }

    public float beatPulse(long progress) {
        if (this.beatMs <= 0.0) {
            return 0.0f;
        }
        double ph = (((double)progress - this.beatPhase) % this.beatMs + this.beatMs) % this.beatMs;
        return (float)Math.exp(-ph / (this.beatMs * 0.18)) * this.confidence;
    }
}

