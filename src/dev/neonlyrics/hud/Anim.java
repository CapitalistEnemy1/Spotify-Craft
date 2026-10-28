/*
 * Decompiled with CFR 0.152.
 */
package dev.neonlyrics.hud;

import java.util.HashMap;
import java.util.Map;

public final class Anim {
    private double value;
    private double target;
    private final double speed;
    private long last = System.nanoTime();

    public Anim(double start, double speed) {
        this.value = start;
        this.target = start;
        this.speed = speed;
    }

    public Anim target(double t) {
        this.target = t;
        return this;
    }

    public float get() {
        long now = System.nanoTime();
        double dt = Math.min(0.1, (double)(now - this.last) / 1.0E9);
        this.last = now;
        this.value += (this.target - this.value) * (1.0 - Math.exp(-this.speed * dt));
        if (Math.abs(this.target - this.value) < 1.0E-4) {
            this.value = this.target;
        }
        return (float)this.value;
    }

    public void snap(double v) {
        this.value = v;
        this.target = v;
    }

    public double target() {
        return this.target;
    }

    public static float outCubic(float t) {
        t = Anim.clamp(t);
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    public static float outBack(float t) {
        t = Anim.clamp(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float)Math.pow(t - 1.0f, 3.0) + c1 * (float)Math.pow(t - 1.0f, 2.0);
    }

    public static float outElastic(float t) {
        if ((t = Anim.clamp(t)) == 0.0f || t == 1.0f) {
            return t;
        }
        return (float)(Math.pow(2.0, -10.0f * t) * Math.sin(((double)(t * 10.0f) - 0.75) * 2.0943951023931953) + 1.0);
    }

    public static float clamp(float t) {
        return Math.max(0.0f, Math.min(1.0f, t));
    }

    public static float since(long startMs, long durationMs) {
        return Anim.clamp((float)(System.currentTimeMillis() - startMs) / (float)durationMs);
    }

    public static float pulse(double periodMs) {
        return (float)(0.5 + 0.5 * Math.sin((double)System.currentTimeMillis() / periodMs * Math.PI * 2.0));
    }

    public static final class Pool {
        private final Map<String, Anim> map = new HashMap<String, Anim>();
        private final double speed;

        public Pool(double speed) {
            this.speed = speed;
        }

        public float to(String id, double target) {
            return this.map.computeIfAbsent(id, k -> new Anim(target, this.speed)).target(target).get();
        }

        public float toFrom(String id, double start, double target) {
            return this.map.computeIfAbsent(id, k -> new Anim(start, this.speed)).target(target).get();
        }

        public void reset(String prefix) {
            this.map.keySet().removeIf(k -> k.startsWith(prefix));
        }
    }
}

