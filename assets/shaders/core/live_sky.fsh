#version 330

// Lebendiger Himmel auf der Grafikkarte: dieselbe Rechnung wie LiveSky.color(), aber fuer jeden Bildpunkt.
// Werte kommen aus einer kleinen Textur (Sampler0, 16x1), die Zeit aus UV0.

uniform sampler2D Sampler0;

in vec3 skyDir;
in vec3 skyInfo;

out vec4 fragColor;

// ---------------------------------------------------------------- Werte
vec3 TOP, HOR, A1, A2, BCOL;
int BKIND, PRESET;
float PATTERN, LIVELY, BREATH, AURORA_ON, T, BAZ, BEL, BRAD;
vec3 SUN;
vec3 C;

const int SUN_K = 0, MOON_K = 1, ECLIPSE_K = 2, SYNTH_K = 3;

vec4 px(int i) {
    return texelFetch(Sampler0, ivec2(i, 0), 0);
}

float u16(vec2 b) {
    return floor(b.x * 255.0 + 0.5) * 256.0 + floor(b.y * 255.0 + 0.5);
}

void add(vec3 c, float k) {
    C += c * k;
}

void over(vec3 c, float k) {
    C += (c - C) * clamp(k, 0.0, 1.0);
}

float sm(float a, float b, float x) {
    float t = clamp((x - a) / (b - a), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

float wrap180(float a) {
    return mod(a + 180.0, 360.0) - 180.0;
}

// ---------------------------------------------------------------- Rauschen (bitgleich mit LiveSky.hash3)
float hash3(int x, int y, int z) {
    uint h = uint(x) * 374761393u + uint(y) * 668265263u + uint(z) * 1274126177u;
    h = (h ^ (h >> 13u)) * 1274126177u;
    h ^= h >> 16u;
    return float(h & 0xFFFFFFu) / 16777215.0;
}

float noise(float x, float y, float z) {
    float flx = floor(x), fly = floor(y), flz = floor(z);
    int ix = int(flx), iy = int(fly), iz = int(flz);
    float fx = x - flx, fy = y - fly, fz = z - flz;
    float ux = fx * fx * (3.0 - 2.0 * fx), uy = fy * fy * (3.0 - 2.0 * fy), uz = fz * fz * (3.0 - 2.0 * fz);
    float a = hash3(ix, iy, iz), b = hash3(ix + 1, iy, iz), c = hash3(ix, iy + 1, iz), d = hash3(ix + 1, iy + 1, iz);
    float e = hash3(ix, iy, iz + 1), f = hash3(ix + 1, iy, iz + 1), g = hash3(ix, iy + 1, iz + 1), h = hash3(ix + 1, iy + 1, iz + 1);
    float x00 = a + (b - a) * ux, x10 = c + (d - c) * ux, x01 = e + (f - e) * ux, x11 = g + (h - g) * ux;
    float y0 = x00 + (x10 - x00) * uy, y1 = x01 + (x11 - x01) * uy;
    return y0 + (y1 - y0) * uz;
}

float fbm(float x, float y, float z, int oct) {
    float sum = 0.0, amp = 0.5, norm = 0.0;
    for (int i = 0; i < oct; i++) {
        sum += noise(x, y, z) * amp;
        norm += amp;
        x = x * 2.03 + 17.1;
        y = y * 2.03 - 9.7;
        z = z * 2.03 + 3.3;
        amp *= 0.5;
    }
    return sum / norm;
}

vec3 hsv(float h, float s, float v) {
    float hh = mod(h, 1.0) * 6.0, f = hh - floor(hh);
    float p = v * (1.0 - s), q = v * (1.0 - s * f), u = v * (1.0 - s * (1.0 - f));
    int i = int(floor(hh));
    if (i == 0) return vec3(v, u, p);
    if (i == 1) return vec3(q, v, p);
    if (i == 2) return vec3(p, v, u);
    if (i == 3) return vec3(p, q, v);
    if (i == 4) return vec3(u, p, v);
    return vec3(v, p, q);
}

// ---------------------------------------------------------------- Ebenen
void synth(float el) {
    float glow = exp(-max(0.0, el) / 9.0);
    add(A1, 0.5 * glow);
    float l = (el - 0.7) / 0.6;
    float line = exp(-l * l);
    float pulse = 0.75 + 0.25 * sin(T * 1.6);
    add(A2, line * 0.85 * pulse);
    add(A2, 0.08 * exp(-max(0.0, el) / 3.0));
    if (el > 20.0) {
        float sl = 0.5 + 0.5 * sin(el * 1.3 - T * 0.5);
        add(A1, 0.03 * sl * sm(20.0, 40.0, el) * PATTERN);
    }
}

void curtains(float az, float el, float strength) {
    if (AURORA_ON <= 0.0 || el < -2.0) return;
    float azr = radians(az), t = T * (0.3 + LIVELY * 0.45);
    for (int k = 0; k < 2; k++) {
        float fk = float(k);
        float base = 17.0 + fk * 11.0 + 7.0 * sin(azr * 2.0 + t * 0.22 + fk * 1.7) + 3.0 * sin(azr * 5.0 - t * 0.35 + fk);
        float h = el - base;
        float vert = h < 0.0 ? exp(-h * h / 3.0) : exp(-h / (15.0 + 6.0 * fk));
        if (vert < 0.01) continue;
        float fold = 0.55 + 0.45 * sin(azr * 9.0 + 2.5 * sin(azr * 2.0 + t * 0.45) + t * 0.7 + fk * 3.0);
        float where = sm(0.25, 0.65, noise(cos(azr) * 0.6876 + fk * 5.0, sin(azr) * 0.6876 + t * 0.04, fk + 0.3));
        float inten = vert * (0.35 + 0.65 * fold) * (0.25 + 0.75 * where) * (k == 0 ? 1.0 : 0.6) * strength * PATTERN * BREATH;
        float m = clamp(h / 18.0, 0.0, 1.0);
        add(mix(A1, A2, m), inten * 0.85);
    }
}

void swirl(float x, float y, float z) {
    float t = T * 0.012 * (0.5 + LIVELY);
    float ca = cos(t), sa = sin(t);
    float rx = x * ca - z * sa, rz = x * sa + z * ca;
    float w1 = fbm(rx * 1.2 + 3.0, y * 1.2, rz * 1.2, 2), w2 = fbm(rx * 1.2, y * 1.2 + 7.0, rz * 1.2 + 1.0, 2);
    float n = fbm(rx * 1.8 + w1 * 2.2, y * 1.8 + w2 * 2.2, rz * 1.8 - w1 * 1.5 + t * 2.0, 3);
    float cloud = sm(0.36, 0.78, n), core = sm(0.58, 0.86, n);
    if (cloud < 0.01) return;
    float mm = sm(0.3, 0.7, w1);
    add(mix(A2, A1, mm), cloud * 0.6 * PATTERN * BREATH);
    add(vec3(1.0, 0.92, 1.0), core * 0.22 * PATTERN);
}

void accretion(float az, float el) {
    if (BKIND < 0) return;
    float du = wrap180(az - BAZ);
    du *= cos(radians(el));
    float dv = el - BEL;
    float R = BRAD;
    float fla = 0.2;
    float rr = sqrt(du * du + (dv / fla) * (dv / fla));
    float dist = sqrt(du * du + dv * dv);
    if (dist > R) add(A1, exp(-(dist - R) / (R * 0.9)) * 0.35 * PATTERN);
    if (rr < R * 1.1 || rr > R * 6.5) return;
    float band = sm(R * 1.1, R * 1.35, rr) * (1.0 - sm(R * 3.0, R * 6.5, rr));
    float ang = atan(dv / fla, du);
    float a2 = ang - T * 0.3;
    float streak = 0.5 + 0.5 * noise(cos(a2) * 3.0, sin(a2) * 3.0 + rr * 0.3, 4.2);
    float doppler = 1.0 + 0.75 * (-du / rr);
    float inten = band * streak * doppler * PATTERN;
    float hot = clamp(1.0 - (rr - R * 1.1) / (R * 3.0), 0.0, 1.0);
    add(vec3(A2.r + (1.0 - A2.r) * hot * 0.7, A2.g + (A1.g - A2.g) * hot + (1.0 - A1.g) * hot * 0.3, A2.b + (A1.b - A2.b) * hot), inten * 1.1);
}

void galaxyBand(float x, float y, float z) {
    float t = T * 0.004;
    float nx = 0.36 * cos(t) - 0.73 * sin(t), ny = 0.58, nz = 0.36 * sin(t) + 0.73 * cos(t);
    float l = sqrt(nx * nx + ny * ny + nz * nz);
    float d = (x * nx + y * ny + z * nz) / l;
    float band = exp(-d * d / 0.04);
    if (band < 0.01) return;
    float core = exp(-d * d / 0.005);
    float tint = fbm(x * 2.2 + 5.0, y * 2.2, z * 2.2, 2);
    add(A1, band * sm(0.45, 0.75, tint) * 0.22 * PATTERN);
    float n = fbm(x * 5.0 + 2.0, y * 5.0, z * 5.0 - 1.0, 3);
    float dust = exp(-d * d / 0.0025) * sm(0.45, 0.7, fbm(x * 11.0, y * 11.0 + 3.0, z * 11.0, 2));
    float inten = band * (0.4 + 0.8 * n) * (1.0 - 0.7 * dust) * PATTERN;
    float m = sm(0.3, 0.8, n);
    add(mix(A2, A1, m), inten * 0.45);
    add(vec3(1.0, 0.9, 0.8), core * (1.0 - 0.7 * dust) * 0.35 * PATTERN);
}

void cityGlow(float az, float el) {
    float low = exp(-max(0.0, el) / 10.0);
    add(A1, 0.42 * low);
    add(A2, 0.22 * exp(-max(0.0, el) / 3.5));
    float azc = radians(az);
    float city = sm(0.4, 0.8, noise(cos(azc) * 2.865, 0.5 + sin(azc) * 2.865, 1.3));
    add(A1, 0.22 * city * exp(-max(0.0, el) / 5.0));
    if (el < -1.0) return;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float center = 60.0 + fi * 120.0 + 28.0 * sin(T * 0.25 + fi * 2.1);
        float tilt = 18.0 * sin(T * 0.18 + fi);
        float daz = wrap180(az - center - tilt * el / 60.0);
        float w = 1.8 + el * 0.06;
        float beam = exp(-(daz * daz) / (w * w)) * exp(-max(0.0, el) / 50.0) * sm(-1.0, 3.0, el);
        if (beam < 0.005) continue;
        float br = beam * 0.5 * PATTERN;
        if (i == 1) add(A2, br);
        else if (i == 0) add(A1, br);
        else add(vec3(0.9, 0.9, 1.0), br * 0.8);
    }
}

void iceBands(float az, float el) {
    if (el < 2.0) return;
    float azi = radians(az);
    float n = noise(cos(azi) * 1.719, T * 0.05 + sin(azi) * 1.719, 2.2);
    float band = pow(0.5 + 0.5 * sin(el * 0.35 + n * 3.0 - T * 0.15), 6.0);
    add(A1, 0.16 * band * sm(5.0, 25.0, el) * PATTERN);
}

void lava(float x, float y, float z, float el) {
    float t = T * 0.03 * (0.6 + LIVELY);
    float w = fbm(x * 1.6 + 3.0, y * 1.6 - t, z * 1.6, 2);
    float n = fbm(x * 2.4 + w * 1.6 + t, y * 2.4 - t * 0.7, z * 2.4 - w * 1.2, 3);
    float vein = pow(1.0 - abs(2.0 * n - 1.0), 6.0);
    float pulse = 0.75 + 0.25 * sin(T * 1.3 + n * 12.0);
    float k = vein * pulse * PATTERN * BREATH * (0.4 + 0.6 * sm(-2.0, 20.0, el));
    add(A1, k * 1.1);
    add(A2, vein * vein * vein * 0.8 * PATTERN);
    add(vec3(0.5, 0.06, 0.02), sm(0.45, 0.8, n) * 0.18);
}

void code(float az, float el) {
    if (el < -2.0) return;
    float colW = 4.0;
    float col = floor(az / colW);
    float h1 = hash3(int(col), 7, 3), h2 = hash3(int(col), 11, 5);
    float speed = 0.25 + h1 * 0.5 * (0.5 + LIVELY);
    float head = 90.0 - fract(T * speed * 0.35 + h2) * 110.0;
    float d = head - el;
    float cell = floor(el / 3.0);
    float flick = hash3(int(col), int(cell), int(floor(T * 3.0 + h1 * 7.0))) > 0.25 ? 1.0 : 0.35;
    float edge = abs((az / colW - col) - 0.5) < 0.38 ? 1.0 : 0.2;
    if (d < 0.0) {
        float trail = exp(d / 14.0) * flick * edge;
        add(A1, trail * 0.75 * PATTERN);
    }
    if (abs(d) < 2.2) add(A2, (1.0 - abs(d) / 2.2) * edge * PATTERN);
    add(A1, 0.05 * flick * edge * (0.5 + 0.5 * sin(T + col)));
}

void prism(float az, float el) {
    float azr = radians(az), t = T * (0.15 + LIVELY * 0.2);
    float beams = pow(0.5 + 0.5 * sin(azr * 7.0 + t + el * 0.035), 14.0);
    float beams2 = pow(0.5 + 0.5 * sin(azr * 11.0 - t * 1.3 + el * 0.02 + 1.0), 18.0);
    float fall = sm(0.0, 75.0, el);
    float hue = fract(az / 360.0 + T * 0.02);
    vec3 c = hsv(hue, 0.6, 1.0), c2 = hsv(mod(hue + 0.33, 1.0), 0.6, 1.0);
    add(c, beams * fall * 0.5 * PATTERN * BREATH);
    add(c2, beams2 * fall * 0.35 * PATTERN);
    float top = exp(-(90.0 - el) / 18.0);
    add(vec3(1.0, 0.95, 1.0), top * 0.25 * PATTERN);
}

void pulsar(float x, float y, float z) {
    float a = radians(30.0), e = radians(50.0);
    float px_ = -sin(a) * cos(e), py = sin(e), pz = cos(a) * cos(e);
    float dt = x * px_ + y * py + z * pz;
    float ang = degrees(acos(clamp(dt, -1.0, 1.0)));
    add(vec3(1.0), exp(-ang / 1.2) * 0.9 + exp(-ang / 6.0) * 0.25);
    add(A1, exp(-ang / 14.0) * 0.2);
    float t = T * (0.5 + LIVELY * 0.6);
    float ux = -sin(a + 1.5707963), uz = cos(a + 1.5707963);
    float vx = py * uz, vy = pz * ux - px_ * uz, vz = -py * ux;
    float nx = cos(t) * ux + sin(t) * vx, ny = sin(t) * vy, nz = cos(t) * uz + sin(t) * vz;
    float band = x * nx + y * ny + z * nz;
    float beam = exp(-band * band / 0.0025) * exp(-ang / 55.0);
    add(A1, beam * 0.7 * PATTERN);
    float ringR = fract(T * 0.25) * 70.0;
    float rd = (ang - ringR) / 2.0;
    float ring = exp(-rd * rd) * (1.0 - ringR / 70.0);
    add(A2, ring * 0.45 * PATTERN * BREATH);
}

vec3 paintCol(int i) {
    if (i == 0) return vec3(255.0, 158.0, 201.0) / 255.0;
    if (i == 1) return vec3(159.0, 216.0, 255.0) / 255.0;
    if (i == 2) return vec3(255.0, 211.0, 154.0) / 255.0;
    if (i == 3) return vec3(185.0, 242.0, 200.0) / 255.0;
    return vec3(201.0, 166.0, 255.0) / 255.0;
}

void paint(float x, float y, float z, float el) {
    float t = T * 0.02 * (0.5 + LIVELY);
    for (int k = 0; k < 3; k++) {
        float fk = float(k);
        float n = fbm(x * 1.3 + fk * 7.0 + t, y * 1.3 - t * 0.6, z * 1.3 + fk * 3.0, 3);
        float blob = sm(0.44, 0.62, n);
        if (blob < 0.01) continue;
        float rd = (n - 0.5) / 0.05;
        float rim = exp(-rd * rd);
        vec3 c = paintCol((k * 2 + int(floor(n * 5.0))) % 5);
        over(c, blob * 0.65 * PATTERN);
        C *= 1.0 - 0.12 * rim;
    }
}

void milkyWay(float x, float y, float z, float strength) {
    float nx = 0.36, ny = 0.58, nz = 0.73;
    float d = x * nx + y * ny + z * nz;
    float band = exp(-d * d / 0.03);
    if (band < 0.01) return;
    float n = fbm(x * 4.0 + T * 0.03, y * 4.0 - T * 0.02, z * 4.0 + T * 0.015, 3);
    float dust = exp(-d * d / 0.0035) * sm(0.42, 0.7, fbm(x * 9.0, y * 9.0 + 3.0, z * 9.0, 2));
    float inten = band * (0.35 + 0.9 * n) * (1.0 - 0.75 * dust) * strength * PATTERN;
    add(vec3(0.62 + 0.38 * n, 0.7 + 0.2 * n, 1.0 - 0.18 * n), inten * 0.55);
}

void caustics(float x, float y, float z, float el) {
    float t = T * (0.45 + LIVELY * 0.5);
    float s = 3.2;
    float n1 = noise(x * s + t, y * s + t * 0.7, z * s - t * 0.4);
    float n2 = noise(x * s * 1.6 - t * 0.8, y * s * 1.6 + 4.0, z * s * 1.6 + t * 0.5);
    float w1 = 1.0 - abs(2.0 * n1 - 1.0), w2 = 1.0 - abs(2.0 * n2 - 1.0);
    float c = pow(w1, 7.0) + 0.7 * pow(w2, 9.0);
    float hi = clamp((el - 5.0) / 50.0, 0.0, 1.0);
    float high = hi * hi * (3.0 - 2.0 * hi);
    add(A1, c * 0.5 * high * PATTERN * BREATH);
    float az = atan(-x, z) + t * 0.05;
    float sh = noise(cos(az) * 3.0, 2.5 + sin(az) * 3.0, 7.0);
    add(A2, sh * sh * sh * sh * 0.35 * high * PATTERN);
}

void clouds(float x, float y, float z, float thr, float opacity, vec3 light, vec3 dark, float speed) {
    if (y < 0.01) return;
    float s = 1.0 / (y + 0.12);
    float px_ = x * s, pz = z * s, t = T * speed * (1.2 + 1.5 * LIVELY);
    float n = fbm(px_ * 0.85 + t, pz * 0.85 + t * 0.35, t * 0.6, 4);
    float cov = sm(thr, thr + 0.22, n) * sm(0.0, 0.14, y);
    if (cov < 0.01) return;
    float lit = sm(0.35, 0.95, n + 0.15 * y);
    if (BKIND == SUN_K) lit = min(1.0, lit + 0.35 * max(0.0, x * SUN.x + y * SUN.y + z * SUN.z));
    over(mix(dark, light, lit), cov * opacity);
}

void bodyGlow(float x, float y, float z, float az, float el) {
    float dt = x * SUN.x + y * SUN.y + z * SUN.z;
    float ang = degrees(acos(clamp(dt, -1.0, 1.0)));
    float rad = BRAD;
    if (BKIND == SUN_K) {
        add(BCOL, exp(-ang / 14.0) * 0.55 + exp(-ang / 4.0) * 0.45);
        if (PRESET == 6 || PRESET == 8) {
            float around = atan(el - BEL, wrap180(az - BAZ));
            float ray = pow(0.5 + 0.5 * sin(around * 11.0 + T * 0.3 + noise(cos(around) * 2.0, T * 0.25 + sin(around) * 2.0, 3.0) * 3.0), 5.0);
            add(BCOL, ray * exp(-ang / 26.0) * 0.28 * sm(rad, rad * 2.5, ang) * PATTERN);
        }
    } else if (BKIND == MOON_K) {
        add(BCOL, exp(-ang / 5.0) * 0.32 + exp(-ang / 22.0) * 0.14);
    } else if (BKIND == SYNTH_K) {
        float o = max(0.0, ang - rad);
        add(A1, exp(-o / 10.0) * 0.45 + exp(-o / 3.0) * 0.35);
    } else {
        if (ang < rad) {
            over(vec3(0.008, 0.004, 0.02), 1.0);
            return;
        }
        float around = atan(el - BEL, wrap180(az - BAZ));
        float fl = 0.6 + 0.5 * noise(cos(around) * 3.0, T * 0.9 + sin(around) * 3.0, 5.0);
        float a5 = around + T * 0.02;
        float nn = noise(cos(a5) * 5.0 + 9.0, T * 0.35 + sin(a5) * 5.0, 2.0);
        float streams = 0.5 + 1.0 * nn * nn * 2.0;
        float o = ang - rad;
        add(A1, exp(-o / 1.6) * 1.2 * fl);
        add(A2, exp(-o / 7.0) * 0.45 * streams * PATTERN);
    }
}

// ================================================================ Gemalte Himmel (Polar, Galaxy, Ocean, Ice, Purple, Sunset, Sakura, Blood Moon, Storm)
// Richtung d (Einheitsvektor, y = oben), Zeit t in Sekunden. Liefert Farbe (0..1+).

float sh21(vec2 p) { vec3 q = fract(vec3(p.xyx) * .1031); q += dot(q, q.yzx + 33.33); return fract((q.x + q.y) * q.z); }
float sh31(vec3 q) { q = fract(q * .1031); q += dot(q, q.zyx + 31.32); return fract((q.x + q.y) * q.z); }
vec3 sh33(vec3 q) { q = fract(q * vec3(.1031, .1030, .0973)); q += dot(q, q.yxz + 33.33); return fract((q.xxy + q.yxx) * q.zyx); }
float sn2(vec2 p) {
    vec2 i = floor(p), f = fract(p); f = f * f * (3. - 2. * f);
    return mix(mix(sh21(i), sh21(i + vec2(1, 0)), f.x), mix(sh21(i + vec2(0, 1)), sh21(i + vec2(1, 1)), f.x), f.y);
}
float sn3(vec3 p) {
    vec3 i = floor(p), f = fract(p); f = f * f * (3. - 2. * f);
    return mix(mix(mix(sh31(i), sh31(i + vec3(1, 0, 0)), f.x), mix(sh31(i + vec3(0, 1, 0)), sh31(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(sh31(i + vec3(0, 0, 1)), sh31(i + vec3(1, 0, 1)), f.x), mix(sh31(i + vec3(0, 1, 1)), sh31(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}
float sf2(vec2 p, int o) {
    float s = 0., a = .5;
    mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
    for (int i = 0; i < 6; i++) { if (i >= o) break; s += a * sn2(p); p = m * p; a *= .5; }
    return s / (1. - pow(.5, float(o)));
}
float sf3(vec3 p, int o) {
    float s = 0., a = .5;
    for (int i = 0; i < 5; i++) { if (i >= o) break; s += a * sn3(p); p = p * 2.03 + 17.1; a *= .5; }
    return s / (1. - pow(.5, float(o)));
}
vec3 sdir(float azDeg, float elDeg) {
    float a = radians(azDeg), e = radians(elDeg);
    return vec3(-sin(a) * cos(e), sin(e), cos(a) * cos(e));
}
// lokale Koordinaten um eine Richtung c (fuer Scheiben, Galaxie, Blitze), Einheit = Bogenmass
vec2 slocal(vec3 d, vec3 c) {
    vec3 u = normalize(cross(vec3(0, 1, 0), c)), v = cross(c, u);
    return vec2(dot(d, u), dot(d, v)) / max(dot(d, c), 1e-3);
}

// ---------------- Sterne
vec3 sStars(vec3 d, float dens, float t) {
    vec3 c = vec3(0);
    for (int l = 0; l < 2; l++) {
        float sc = l == 0 ? 140. : 330., th = 1. - dens * (l == 0 ? .03 : .06);
        vec3 p = d * sc, id = floor(p), f = fract(p);
        float h = sh31(id + float(l) * 19.);
        if (h > th) {
            vec3 o = sh33(id) * .5 + .25;
            float dd = length(f - o), br = (h - th) / (1. - th);
            float tw = .7 + .3 * sin(t * (1.3 + h * 3.) + h * 70.);
            vec3 tint = mix(vec3(1., .85, .7), vec3(.75, .85, 1.), sh33(id + 5.).x);
            c += smoothstep(l == 0 ? .2 : .26, 0., dd) * (.35 + br * 1.4) * tw * tint;
        }
    }
    return c;
}

// ---------------- Wolken: gemalt, mit leuchtenden Kanten zur Lichtquelle
// lp = Lichtrichtung, scale = Groesse, cov = Bedeckung, drift = Wind
vec4 sClouds(vec3 d, vec3 lp, float scale, float cov, float t, float drift) {
    if (d.y < -.02) return vec4(0);
    float k = 1. / (d.y + .14);
    vec2 p = d.xz * k * scale + vec2(t * drift, t * drift * .35);
    vec2 w = vec2(sf2(p * .5 + 3.1, 3), sf2(p * .5 + 7.7, 3));
    vec2 q = p + (w - .5) * .9;
    float n = sf2(q, 5) + (sn2(q * 5.3) - .5) * .07;
    float th = mix(.66, .32, cov);
    float den = smoothstep(th, th + .1, n);
    // Licht: Dichte etwas in Richtung Licht versetzt -> Kanten zur Lichtquelle leuchten
    vec2 l2 = normalize(lp.xz + 1e-4) * .22;
    float n2 = sf2(q + l2, 5) + (sn2((q + l2) * 5.3) - .5) * .07;
    float lit = clamp((n - n2) * 9. + .45, 0., 1.);
    lit = lit * lit * (3. - 2. * lit);
    float edge = smoothstep(th - .01, th + .05, n) * (1. - smoothstep(th + .05, th + .2, n)) * (.4 + .6 * lit);
    return vec4(lit, edge, den * smoothstep(-.02, .06, d.y), n);
}

// ---------------- Lichtsaeule (senkrechter Strahl) bei Azimut az
float sBeam(vec3 d, float az, float width, float t) {
    vec3 b = sdir(az, 0.);
    float h = dot(normalize(d.xz + 1e-5), normalize(b.xz));
    float ang = acos(clamp(h, -1., 1.));
    float w = width * (1. + d.y * .6);
    float flick = .85 + .15 * sin(t * 2.3 + d.y * 9.);
    float core = exp(-ang * ang / (w * w * .08)) * 1.6;
    float halo = exp(-ang * ang / (w * w)) * .7 + exp(-ang / (w * 4.)) * .18;
    return (core + halo) * flick * smoothstep(-.05, .25, d.y) * step(0., h);
}

// ---------------- Planet mit Licht, Atmosphaere und Ring (gibt die neue Farbe zurueck)
vec3 sPlanet(vec3 col, vec3 d, vec3 c, float r, vec3 pc, vec3 atm, vec3 lightDir, float ring) {
    if (dot(d, c) < .5) return col;
    vec2 q = slocal(d, c) / r;
    float rr = length(q);
    vec2 rq = vec2(q.x * cos(.35) - q.y * sin(.35), q.x * sin(.35) + q.y * cos(.35));
    float e = length(vec2(rq.x, rq.y / .22));
    float band = ring * smoothstep(1.35, 1.45, e) * (1. - smoothstep(2.1, 2.25, e)) * (.55 + .45 * sin(e * 30.));
    // Ring hinter dem Planeten
    if (rq.y > 0.) col = mix(col, pc * 1.3 + .15, band * .75);
    // Schein
    col += atm * exp(-max(rr - 1., 0.) * 8.) * .35 * step(1., rr);
    if (rr < 1.) {
        vec3 n = vec3(q, sqrt(1. - rr * rr));
        float lam = clamp(dot(n, normalize(lightDir)), 0., 1.);
        float bands = .8 + .2 * sin(q.y * 9. + sf2(q * 3., 3) * 4.);
        vec3 s = pc * bands * (.06 + 1.1 * lam) + atm * pow(1. - n.z, 3.) * .9;
        col = mix(col, s, smoothstep(1., .96, rr));
    }
    // Ring vor dem Planeten
    if (rq.y <= 0.) col = mix(col, pc * 1.3 + .15, band * .75);
    return col;
}
// ---------------- Spiralgalaxie
vec3 sGalaxy(vec3 d, vec3 c, float size, float t) {
    if (dot(d, c) < .6) return vec3(0);
    vec2 q = slocal(d, c) / size;
    float ca = cos(.5), sa = sin(.5);
    q = vec2(q.x * ca - q.y * sa, q.x * sa + q.y * ca);
    q.y /= .42;
    float r = length(q), a = atan(q.y, q.x);
    float arms = pow(.5 + .5 * sin(2. * a - log(r + .02) * 5. + t * .03), 2.5);
    float n = sf2(q * 6. + 3., 4);
    float disk = exp(-r * 2.6) * (.25 + arms * 1.2) * (.55 + .7 * n);
    float core = exp(-r * r * 60.) * 2.2 + exp(-r * 7.) * .6;
    vec3 col = mix(vec3(.55, .45, 1.), vec3(1., .75, .9), arms) * disk + vec3(1., .92, .8) * core;
    float dust = smoothstep(.55, .75, sf2(q * 9. + 11., 3)) * arms * exp(-r * 3.);
    return col * (1. - .6 * dust);
}

// ---------------- Polarlicht-Vorhaenge
vec3 sAurora(vec3 d, float t, vec3 c1, vec3 c2) {
    if (d.y < .01) return vec3(0);
    vec3 acc = vec3(0);
    for (int i = 0; i < 26; i++) {
        float k = float(i) / 26., h = 1. + k * 1.4;
        vec2 p = d.xz / d.y * h;
        float wave = sin(p.x * .5 + t * .14) * 1.1 + sin(p.x * 1.6 - t * .2 + 1.3) * .4 + (sn2(vec2(p.x * .35, t * .05)) - .5) * 2.4;
        float dd = p.y - 4.5 - wave;
        float curtain = exp(-dd * dd * 1.2);
        float rays = pow(sn2(vec2(p.x * 6. + wave * 2., t * .3)), 2.) * 1.5 + .2;
        acc += mix(c1, c2, smoothstep(.2, .9, k)) * curtain * rays * exp(-k * 2.) * (1. + 2.5 * exp(-k * 25.));
    }
    return acc * .14 * smoothstep(.01, .12, d.y);
}

// ---------------- Blitz (gezackte Linie mit Aesten) um Azimut az
float sBolt(vec3 d, float az, float seed, float t) {
    vec3 c = sdir(az, 22.);
    if (dot(d, c) < .7) return 0.;
    vec2 q = slocal(d, c);
    if (q.y > .32 || q.y < -.42) return 0.;
    float y = q.y;
    float x = (sn2(vec2(y * 9., seed)) - .5) * .09 + (sn2(vec2(y * 30., seed + 3.)) - .5) * .025;
    float main = exp(-abs(q.x - x) * 260.) + exp(-abs(q.x - x) * 45.) * .35;
    float bx = x + (y - .05) * .5 + (sn2(vec2(y * 28., seed + 9.)) - .5) * .03;
    float br = (exp(-abs(q.x - bx) * 300.) + exp(-abs(q.x - bx) * 60.) * .2) * step(y, .05) * step(-.25, y) * .7;
    return (main + br) * smoothstep(.32, .2, y);
}

float sFlash(float t, float seed) {
    float c = floor(t / 2.6 + seed), l = fract(t / 2.6 + seed) * 2.6;
    if (sh21(vec2(c, seed * 7.)) < .3) return 0.;
    return (l < .08 ? 1. : 0.) + (l > .14 && l < .2 ? .75 : 0.) + (l > .2 ? exp(-(l - .2) * 8.) * .4 : 0.);
}

// ---------------- Kirschblueten-Zweige oben im Bild
vec4 sSakura(vec3 d, float t) {
    float el = degrees(asin(clamp(d.y, -1., 1.)));
    if (el < 16.) return vec4(0);
    float az = atan(-d.x, d.z);
    vec2 p = vec2(az * 3.2, (90. - el) / 18.);
    float m = sf2(p * 1.4 + 2., 4);
    float zone = smoothstep(.35, .95, sin(az * 2. + 1.2) * .5 + .5) * smoothstep(16., 40., el);
    // Aeste: duenne dunkle Linien
    float br = abs(sin(p.x * 2.3 + sf2(p * 2., 3) * 4.) * .5 + p.y * .9 - 1.2 - .6 * sin(p.x * .7));
    float branch = smoothstep(.035, .0, br - .015) * zone;
    // Blueten: Haufen kleiner runder Flecken
    float near = smoothstep(.45, .05, br) * zone;
    float blossom = 0.;
    vec2 id = vec2(0);
    for (int k = 0; k < 2; k++) {
        vec2 g = p * (k == 0 ? 6. : 9.5) + float(k) * 3.7;
        vec2 cid = floor(g), f = fract(g) - .5;
        float h = sh21(cid + float(k) * 11.);
        float rad = .28 + .14 * sh21(cid + 5.);
        float bl = smoothstep(rad, rad - .12, length(f + (sh33(vec3(cid, 2.)).xy - .5) * .5)) * step(.3, h);
        if (bl > blossom) { blossom = bl; id = cid; }
    }
    blossom *= near * smoothstep(.3, .55, m + near * .3);
    vec3 bc = mix(vec3(1., .55, .8), vec3(1., .85, .95), sh21(id + 3.));
    float a = max(blossom, branch);
    vec3 col = mix(vec3(.18, .05, .12), bc, blossom / max(a, 1e-3));
    return vec4(col, a);
}

// ================================================================ die neun Himmel
vec3 styledSky(vec3 d, int id, float t) {
    float y = d.y;
    float el = degrees(asin(clamp(y, -1., 1.)));
    float hz = exp(-max(y, 0.) * 6.);
    vec3 col;
    if (id == 0) { // POLAR
        col = mix(vec3(.02, .09, .16), vec3(.0, .02, .06), smoothstep(0., .8, y));
        col += vec3(.0, .25, .35) * hz * .6;
        col += sStars(d, .8, t) * .9;
        col += sAurora(d, t, vec3(.1, 1., .7), vec3(.2, .5, 1.)) * 1.3;
        float b = sBeam(d, 0., .045, t);
        col += vec3(.45, .95, 1.) * b * .7;
        vec4 c = sClouds(d, sdir(0., 10.), 1.1, .35, t, .02);
        vec3 cc = mix(vec3(.01, .05, .1), vec3(.1, .45, .55), c.x) + vec3(.3, 1., .95) * c.y * .9;
        col = mix(col, cc, c.z * .85 * smoothstep(.0, .35, 1. - y));
    } else if (id == 1) { // GALAXY
        col = mix(vec3(.05, .02, .14), vec3(.01, .0, .04), smoothstep(0., .9, y));
        float neb = sf3(d * 2.2 + 4., 4);
        col += mix(vec3(.35, .1, .55), vec3(.1, .2, .6), sf3(d * 1.3, 3)) * smoothstep(.45, .85, neb) * .55;
        col += sStars(d, 1.6, t) * 1.2;
        col += sGalaxy(d, sdir(10., 26.), .32, t) * 1.3;
        col = sPlanet(col, d, sdir(330., 32.), .075, vec3(.55, .45, .9), vec3(.6, .5, 1.), vec3(.7, .3, .6), 1.);
        col = sPlanet(col, d, sdir(60., 14.), .05, vec3(.4, .5, .9), vec3(.4, .6, 1.), vec3(-.8, .3, .5), 0.);
        col += vec3(.3, .2, .7) * hz * .35;
    } else if (id == 2) { // OCEAN
        col = mix(vec3(.15, .75, .9), vec3(.02, .2, .45), smoothstep(-.05, .7, y));
        vec4 c = sClouds(d, sdir(180., 60.), 1.3, .55, t, .03);
        vec3 cc = mix(vec3(.02, .25, .45), vec3(.5, .95, 1.), c.x) + vec3(.8, 1., 1.) * c.y * .6;
        col = mix(col, cc, c.z * .9);
        float rays = pow(sn2(vec2(atan(d.x, d.z) * 9., t * .1)), 3.) * smoothstep(.1, .7, y);
        col += vec3(.5, .95, 1.) * rays * .35;
        col += vec3(.6, 1., 1.) * hz * .25;
    } else if (id == 3) { // ICE
        col = mix(vec3(.35, .55, .9), vec3(.03, .08, .3), smoothstep(-.05, .8, y));
        col += sStars(d, .5, t) * .5 * smoothstep(.3, .8, y);
        float b = sBeam(d, 0., .05, t);
        vec4 c = sClouds(d, sdir(0., 20.), 1.0, .6, t, .02);
        float near = pow(max(dot(normalize(d.xz + 1e-4), vec2(0, 1)), 0.), 6.);
        vec3 cc = mix(vec3(.05, .12, .35), vec3(.55, .75, 1.), c.x) + vec3(.9, .97, 1.) * c.y * (.5 + near * .5);
        col = mix(col, cc, c.z * .92);
        col += vec3(.75, .9, 1.) * b * .6;
    } else if (id == 4) { // PURPLE
        col = mix(vec3(.25, .08, .45), vec3(.04, .01, .12), smoothstep(-.05, .8, y));
        col += sStars(d, 1., t) * .8;
        float b = sBeam(d, 0., .04, t);
        vec4 c = sClouds(d, sdir(0., 15.), 1.15, .5, t, .02);
        vec3 cc = mix(vec3(.1, .02, .25), vec3(.6, .3, .95), c.x) + vec3(.95, .7, 1.) * c.y * .9;
        col = mix(col, cc, c.z * .9);
        col += vec3(.75, .45, 1.) * b * .9;
        col = sPlanet(col, d, sdir(320., 38.), .06, vec3(.5, .3, .8), vec3(.8, .5, 1.), vec3(.6, .2, .7), 0.);
        col = sPlanet(col, d, sdir(40., 40.), .035, vec3(.6, .4, .9), vec3(.8, .6, 1.), vec3(-.6, .2, .7), 0.);
    } else if (id == 5) { // SUNSET
        vec3 sun = sdir(30., 2.);
        float sd = max(dot(d, sun), 0.);
        col = mix(vec3(1., .45, .15), vec3(.18, .1, .35), smoothstep(-.05, .55, y));
        col += vec3(1., .5, .2) * pow(sd, 8.) * .8 + vec3(1., .85, .5) * pow(sd, 200.) * 2.;
        vec4 c = sClouds(d, sun, 1.0, .58, t, .015);
        vec3 shade = mix(vec3(.3, .08, .15), vec3(.12, .06, .2), smoothstep(0., .5, y));
        vec3 cc = mix(shade, vec3(1., .45, .2), c.x) + vec3(1., .75, .4) * c.y * (.5 + pow(sd, 4.) * 1.5);
        col = mix(col, cc, c.z * .92);
        float rays = pow(sn2(vec2(atan(dot(d, normalize(cross(sun, vec3(0, 1, 0)))), y) * 14., 1.)), 3.) * pow(sd, 3.);
        col += vec3(1., .6, .3) * rays * .25;
    } else if (id == 6) { // SAKURA
        col = mix(vec3(.55, .2, .5), vec3(.12, .04, .2), smoothstep(-.05, .7, y));
        col += sStars(d, .7, t) * .6 * smoothstep(.2, .7, y);
        vec3 mc = sdir(0., 28.);
        float ma = acos(clamp(dot(d, mc), -1., 1.));
        float disc = smoothstep(.06, .055, ma);
        col += vec3(1., .5, .85) * (exp(-ma * 9.) * .5 + exp(-ma * 30.) * .5);
        vec4 c = sClouds(d, mc, 1.1, .45, t, .015);
        vec3 cc = mix(vec3(.25, .06, .25), vec3(.95, .45, .8), c.x) + vec3(1., .75, .95) * c.y * .8;
        col = mix(col, cc, c.z * .85);
        col = mix(col, vec3(1., .82, .95) * (.85 + .15 * sf2(slocal(d, mc) * 40., 3)), disc);
        vec4 s = sSakura(d, t);
        col = mix(col, s.rgb, s.a);
    } else if (id == 7) { // BLOOD MOON
        col = mix(vec3(.25, .01, .02), vec3(.04, .0, .01), smoothstep(-.05, .7, y));
        vec3 mc = sdir(0., 26.);
        float ma = acos(clamp(dot(d, mc), -1., 1.));
        float r = .055;
        float ring = exp(-pow((ma - r) * 90., 2.)) * 2.2 + exp(-max(ma - r, 0.) * 18.) * 1.1;
        float flame = .7 + .3 * sn2(vec2(atan(slocal(d, mc).y, slocal(d, mc).x) * 6., t * .5));
        vec4 c = sClouds(d, mc, 1.1, .6, t, .02);
        vec3 cc = mix(vec3(.08, .0, .01), vec3(.75, .05, .05), c.x) + vec3(1., .2, .1) * c.y * .9;
        col = mix(col, cc, c.z * .92);
        col += vec3(1., .15, .08) * ring * flame;
        col = mix(col, vec3(.01, 0., 0.), smoothstep(r, r * .97, ma));
    } else { // STORM
        col = mix(vec3(.08, .14, .28), vec3(.01, .03, .08), smoothstep(-.05, .7, y));
        float f1 = sFlash(t, .0), f2 = sFlash(t + 1.3, .5);
        vec4 c = sClouds(d, sdir(0., 30.), 1.2, .8, t, .04);
        vec3 cc = mix(vec3(.02, .04, .1), vec3(.25, .35, .6), c.x) + vec3(.5, .65, 1.) * c.y * .5;
        cc += vec3(.6, .7, 1.) * (f1 + f2) * .35 * (.5 + c.w);
        col = mix(col, cc, c.z * .95);
        float b = sBolt(d, 10., floor(t / 2.6), t) * f1 + sBolt(d, 300., floor((t + 1.3) / 2.6) + 50., t) * f2;
        col += vec3(.7, .8, 1.) * b * 1.4;
    }
    // Boden-Uebergang unter dem Horizont
    col *= 1. - clamp(-el / 8., 0., 1.) * .5;
    return col;
}


float soft(float v) {
    if (v <= 0.8) return max(0.0, v);
    float o = v - 0.8;
    return 0.8 + 0.2 * (o / (o + 0.2));
}

vec3 rgb(float r, float g, float b) {
    return vec3(r, g, b) / 255.0;
}

void main() {
    TOP = px(0).rgb;
    HOR = px(1).rgb;
    A1 = px(2).rgb;
    A2 = px(3).rgb;
    vec4 b = px(4);
    BCOL = b.rgb;
    BKIND = int(floor(b.a * 255.0 + 0.5)) - 1;
    vec4 q = px(5);
    PATTERN = q.r * 1.5;
    LIVELY = q.g;
    BREATH = q.b;
    AURORA_ON = q.a;
    vec4 o6 = px(6), o7 = px(7);
    BAZ = u16(o6.rg) / 100.0;
    BEL = u16(o6.ba) / 100.0 - 90.0;
    BRAD = u16(o7.rg) / 1000.0;
    PRESET = int(floor(o7.b * 255.0 + 0.5));
    T = skyInfo.x;
    float ba = radians(BAZ), be = radians(BEL);
    SUN = vec3(-sin(ba) * cos(be), sin(be), cos(ba) * cos(be));

    vec3 dir = normalize(skyDir);
    float x = dir.x, y = dir.y, z = dir.z;
    float el = degrees(asin(clamp(y, -1.0, 1.0)));
    float az = degrees(atan(-x, z));
    if (az < 0.0) az += 360.0;

    if (PRESET >= 20) {
        // gemalte Himmel: komplett eigene Rechnung (Zeit in Sekunden)
        C = styledSky(dir, PRESET - 20, T / 2.2);
    } else {
    // 1) ruhiger Verlauf vom Horizont nach oben
    float k = pow(clamp(el / 72.0, 0.0, 1.0), 0.55);
    C = mix(HOR, TOP, k);
    if (el < 0.0) C *= 1.0 - clamp(-el / 6.0, 0.0, 1.0) * 0.55;
    float lowGlow = exp(-max(0.0, el) / 9.0);

    if (PRESET == 0) synth(el);
    else if (PRESET == 1 || PRESET == 14) {
        milkyWay(x, y, z, 0.16);
        curtains(az, el, 1.0);
        add(A1, 0.06 * lowGlow);
    } else if (PRESET == 2) {
        swirl(x, y, z);
        add(A1, 0.10 * lowGlow);
    } else if (PRESET == 3) {
        milkyWay(x, y, z, 0.22);
        accretion(az, el);
    } else if (PRESET == 4) {
        galaxyBand(x, y, z);
        add(A2, 0.08 * lowGlow);
    } else if (PRESET == 5) {
        cityGlow(az, el);
        clouds(x, y, z, 0.62, 0.55, rgb(106.0, 44.0, 122.0), rgb(12.0, 10.0, 30.0), 0.03);
    } else if (PRESET == 6) {
        add(vec3(1.0, 0.55, 0.28), 0.5 * lowGlow);
        clouds(x, y, z, 0.54, 0.85, rgb(255.0, 196.0, 138.0), rgb(90.0, 44.0, 94.0), 0.02);
    } else if (PRESET == 7) {
        add(vec3(1.0, 0.8, 0.92), 0.14 * lowGlow);
        clouds(x, y, z, 0.6, 0.92, vec3(1.0), rgb(242.0, 184.0, 216.0), 0.025);
    } else if (PRESET == 8) {
        add(vec3(1.0, 0.61, 0.8), 0.4 * lowGlow);
        clouds(x, y, z, 0.57, 0.6, rgb(255.0, 232.0, 242.0), rgb(200.0, 144.0, 184.0), 0.02);
    } else if (PRESET == 9) {
        iceBands(az, el);
        curtains(az, el, 0.45);
        milkyWay(x, y, z, 0.2);
    } else if (PRESET == 10) {
        milkyWay(x, y, z, 0.5);
        add(vec3(0.16, 0.23, 0.56), 0.18 * lowGlow);
    } else if (PRESET == 11) {
        caustics(x, y, z, el);
        add(A2, 0.16 * lowGlow);
    } else if (PRESET == 12) {
        add(vec3(1.0, 0.23, 0.16), 0.25 * lowGlow);
        clouds(x, y, z, 0.6, 0.75, rgb(184.0, 48.0, 58.0), rgb(26.0, 4.0, 8.0), 0.03);
    } else if (PRESET == 13) {
        clouds(x, y, z, 0.3, 0.97, rgb(90.0, 100.0, 120.0), rgb(18.0, 22.0, 31.0), 0.11);
    } else if (PRESET == 15) {
        lava(x, y, z, el);
        add(vec3(1.0, 0.35, 0.08), 0.35 * lowGlow);
    } else if (PRESET == 16) code(az, el);
    else if (PRESET == 17) prism(az, el);
    else if (PRESET == 18) {
        milkyWay(x, y, z, 0.25);
        pulsar(x, y, z);
    } else if (PRESET == 19) paint(x, y, z, el);
    if (BKIND >= 0) bodyGlow(x, y, z, az, el);
    }

    vec3 col = vec3(soft(C.r), soft(C.g), soft(C.b));
    col = clamp(col * skyInfo.y, 0.0, 1.0);
    // deckend bis weit unter den Horizont (kein heller Minecraft-Nebel dazwischen), ganz unten weich aus
    float ht = clamp((el + 30.0) / 6.0, 0.0, 1.0);
    float a = skyInfo.z * ht * ht * (3.0 - 2.0 * ht);
    if (a <= 0.002) discard;
    fragColor = vec4(col, a);
}
