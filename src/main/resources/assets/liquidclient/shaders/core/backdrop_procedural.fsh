#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid backdrop – procedural main-menu wallpapers, evaluated per device pixel (crisp at 4K+).
//   AURORA  : drifting northern-light curtains over a deep night gradient
//   NEBULA  : layered colourful nebula clouds with stars
//   SILK    : long satin folds with a soft sheen
//   GLOW    : a soft drifting mesh gradient
//   LIQUID  : drops of liquid glass refracting a glow behind them
//   RIBBONS : layered translucent light ribbons
//   DUNES   : layered dunes at sunset
// "frost" (vertex alpha) is how the same wallpaper looks *behind glass*: high-frequency detail is
// faded out and the field is softened, i.e. an analytic blur – no extra render pass needed.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPos;
layout(location = 2) flat in vec2 params;
layout(location = 3) flat in vec2 halfSize;
layout(location = 4) flat in vec2 center;
layout(location = 5) flat in float radius;

layout(location = 0) out vec4 fragColor;

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash12(i);
    float b = hash12(i + vec2(1.0, 0.0));
    float c = hash12(i + vec2(0.0, 1.0));
    float d = hash12(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Octave weights fall off with frost: frosted glass keeps only the low frequencies.
float fbm(vec2 p, float frost) {
    float v = 0.0;
    float amp = 0.5;
    float norm = 0.0;
    for (int i = 0; i < 5; i++) {
        float w = amp * mix(1.0, i < 2 ? 1.0 : 0.0, frost);
        v += w * vnoise2(p);
        norm += w;
        p = p * 2.02 + vec2(1.7, 9.2);
        amp *= 0.5;
    }
    return v / max(norm, 1.0e-3) * 0.94;
}

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

float vnoiseFbm3(vec2 p) {
    float v = 0.0;
    float amp = 0.55;
    for (int i = 0; i < 3; i++) {
        v += amp * vnoise2(p);
        p = p * 2.03 + vec2(3.1, 7.7);
        amp *= 0.45;
    }
    return v;
}

float stars(vec2 p, float time, float frost) {
    vec2 grid = p * 90.0;
    vec2 cell = floor(grid);
    float h = hash12(cell);
    if (h > 0.08) {
        return 0.0;
    }
    vec2 pos = 0.2 + 0.6 * vec2(hash12(cell + 3.1), hash12(cell + 7.7));
    float d = length(fract(grid) - pos);
    float s = 1.0 - smoothstep(0.0, mix(0.07, 0.5, frost), d);
    float twinkle = 0.65 + 0.35 * sin(time * (1.0 + 3.0 * h * 12.0) + h * 80.0);
    return s * s * twinkle * (0.3 + 0.7 * hash12(cell + 1.3)) * (1.0 - frost * 0.85);
}

// Sky with drifting curtains: bright, sharp lower hems; long rayed glow rising and turning violet.
vec3 auroraSky(vec2 p, float time, float frost) {
    vec3 col = mix(vec3(0.004, 0.010, 0.030), vec3(0.025, 0.065, 0.110), smoothstep(0.05, 0.84, p.y));
    col += vec3(0.05, 0.30, 0.22) * smoothstep(0.38, 0.84, p.y) * 0.55;         // green airglow low down
    col += vec3(0.85, 0.9, 1.0) * stars(p, time, frost) * (1.0 - smoothstep(0.40, 0.75, p.y));
    float t = time * 0.03;
    float warp = 0.55 * (vnoiseFbm3(vec2(p.x * 0.6 + t * 0.6, t)) - 0.5);   // shared by the curtains
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float xw = p.x * 0.85 + warp * (1.0 - fi * 0.25) + fi * 0.37;
        float hem = 0.44 + fi * 0.085 + 0.07 * sin(xw * 2.1 + t * 1.7 + fi * 2.0) + 0.025 * sin(xw * 5.3 - t * 2.3);
        float d = hem - p.y;                                        // > 0 above the hem
        // Above the hem the light rises and fades slowly; below it, it dies off fast but softly,
        // so the hem reads sharp without cutting the sky into bands.
        float rise = d > 0.0 ? exp(-d * mix(4.2, 3.2, frost)) : exp(d * mix(38.0, 14.0, frost)) * 0.55;
        float edge = 1.0;
        float rays = mix(vnoiseFbm3(vec2(xw * 34.0 + fi * 9.0, t * 4.0)), 0.6, frost * 0.85);
        float pulse = 0.7 + 0.3 * sin(t * 3.0 + fi * 2.4 + xw);
        float glow = edge * rise * (0.35 + 1.25 * rays * rays) * pulse;
        vec3 low = i == 2 ? vec3(0.30, 0.80, 1.00) : vec3(0.20, 1.00, 0.62);
        vec3 high = i == 1 ? vec3(0.90, 0.32, 0.95) : vec3(0.50, 0.32, 1.00);
        col += mix(high, low, rise) * glow * 0.62;
    }
    return col;
}

vec3 aurora(vec2 p, float time, float frost) {
    float lake = 0.84;
    float ridge = lake - 0.02 - 0.06 * vnoiseFbm3(vec2(p.x * 1.7 + 3.0, 1.7)) - 0.025 * sin(p.x * 3.1 + 1.2);
    vec3 col;
    if (p.y < lake) {
        col = auroraSky(p, time, frost);
    } else {
        // The lake mirrors the sky, dimmed, with slow ripples.
        float ripple = (vnoise2(vec2(p.x * 9.0, p.y * 70.0 - time * 0.4)) - 0.5) * 0.010 * (1.0 - frost);
        col = auroraSky(vec2(p.x + ripple, 2.0 * lake - p.y), time, frost) * 0.55 + vec3(0.003, 0.007, 0.014);
    }
    // Mountains on the far shore, and their reflection.
    float aa = mix(fwidth(p.y) * 1.25, 0.02, frost);
    float mountain = smoothstep(ridge - aa, ridge + aa, p.y) * (1.0 - smoothstep(lake - aa, lake + aa, p.y));
    col = mix(col, vec3(0.008, 0.013, 0.026) + col * 0.10, mountain);
    float mirrorRidge = 2.0 * lake - ridge;
    float shore = smoothstep(lake - aa, lake + aa, p.y) * (1.0 - smoothstep(mirrorRidge - aa, mirrorRidge + aa, p.y));
    col = mix(col, vec3(0.006, 0.010, 0.020), shore * 0.8);
    return col;
}

vec3 nebula(vec2 p, float time, float frost) {
    vec2 q = p + vec2(time * 0.004, -time * 0.002);
    float n1 = fbm(q * 2.2, frost);
    float n2 = fbm(q * 4.5 + vec2(n1 * 1.4, 3.3), frost);
    float n3 = fbm(q * 1.2 - vec2(4.1, n2), frost);
    vec3 col = vec3(0.012, 0.010, 0.035);
    col = mix(col, vec3(0.30, 0.10, 0.55), smoothstep(0.35, 0.85, n1) * 0.85);
    col = mix(col, vec3(0.05, 0.35, 0.80), smoothstep(0.45, 0.90, n2) * 0.65);
    col = mix(col, vec3(0.95, 0.35, 0.60), smoothstep(0.55, 0.95, n3) * 0.45);
    float core = smoothstep(0.62, 0.95, n1 * n2 * 1.6);
    col += vec3(1.0, 0.75, 0.9) * core * 0.35;
    col += vec3(0.9, 0.92, 1.0) * stars(p, time, frost);
    return col;
}

// Screen-space gradient of a field evaluated at p (p in screen heights): exact, one evaluation.
vec2 fieldGradient(float f, vec2 p) {
    return vec2(dFdx(f), dFdy(f)) / max(vec2(abs(dFdx(p.x)), abs(dFdy(p.y))), vec2(1.0e-6));
}



// ---- SILK: long satin folds with a soft sheen --------------------------------------------------
float silkField(vec2 q, float t) {
    float w = 0.9 * sin(q.x * 0.7 + t * 0.050) + 0.6 * sin(q.y * 1.1 - t * 0.040 + q.x * 0.4);
    float a = q.x * 1.2 + q.y * 2.0 + w * 1.6;
    return sin(a * 2.2) * 0.6 + sin(a * 1.1 + 0.8 * sin(q.x * 1.3 + t * 0.06)) * 0.4
            + 0.18 * sin(a * 4.6 + 1.3 * sin(q.y * 1.7 - t * 0.05));
}

vec3 silk(vec2 p, float time, float frost) {
    vec2 q = p * 1.25;
    float h = silkField(q, time);
    vec2 g = fieldGradient(h, p) / 1.25;
    vec3 n = normalize(vec3(-g * mix(0.16, 0.05, frost), 1.0));
    vec3 light = normalize(vec3(-0.35, -0.75, 0.55));
    float diff = clamp(dot(n, light), 0.0, 1.0);
    float sheen = pow(clamp(dot(n, normalize(light + vec3(0.0, 0.0, 1.0))), 0.0, 1.0), mix(40.0, 10.0, frost));
    float hue = 0.5 + 0.5 * sin(p.x * 1.4 + p.y * 0.8 + time * 0.03);
    vec3 dark = mix(vec3(0.020, 0.025, 0.090), vec3(0.060, 0.015, 0.080), hue);
    vec3 body = mix(vec3(0.16, 0.20, 0.78), vec3(0.62, 0.16, 0.72), hue);
    vec3 col = mix(dark, body, diff * diff);
    col += vec3(1.0, 0.78, 0.95) * sheen * 0.75;
    float v = 1.0 - smoothstep(0.25, 1.35, length((p - vec2(0.89, 0.5)) * vec2(0.62, 1.0)));
    return col * mix(0.45, 1.0, v);
}

// ---- GLOW: soft drifting mesh gradient ---------------------------------------------------------
vec3 glowBlob(vec2 p, vec2 c, float r, vec3 color) {
    vec2 d = (p - c) / r;
    return color * exp(-dot(d, d));
}

vec3 glowField(vec2 p, float time, float frost, bool warp) {
    float t = time * 0.12;
    vec3 acc = vec3(0.0);
    acc += glowBlob(p, vec2(0.35 + 0.22 * sin(t * 1.1), 0.32 + 0.16 * cos(t * 0.9)), 0.46, vec3(0.08, 0.30, 1.00));
    acc += glowBlob(p, vec2(1.05 + 0.25 * cos(t * 0.8 + 1.3), 0.68 + 0.15 * sin(t * 1.2)), 0.50, vec3(0.55, 0.16, 1.00));
    acc += glowBlob(p, vec2(1.50 + 0.18 * sin(t * 0.7 + 2.1), 0.28 + 0.18 * cos(t * 1.05 + 0.4)), 0.42, vec3(1.00, 0.20, 0.50));
    acc += glowBlob(p, vec2(0.85 + 0.30 * sin(t * 0.6 + 4.0), 1.05 + 0.10 * cos(t * 0.9)), 0.46, vec3(1.00, 0.45, 0.10));
    acc += glowBlob(p, vec2(0.15 + 0.15 * cos(t * 0.95 + 2.7), 0.92 + 0.12 * sin(t * 0.7)), 0.40, vec3(0.05, 0.80, 0.85));
    if (warp) {
        float w = fbm(p * 1.2 + vec2(t * 0.6, -t * 0.4), frost) - 0.5;
        acc *= 1.0 + w * 0.4;
    }
    vec3 col = vec3(0.006, 0.006, 0.016) + acc * 0.62;
    col = 1.0 - exp(-col * 1.35);
    return pow(col, vec3(1.12));
}

vec3 glow(vec2 p, float time, float frost) {
    return glowField(p, time, frost, true);
}

// ---- LIQUID: drops of liquid glass drifting over a soft glow, refracting it -----------------
vec3 liquid(vec2 p, float time, float frost) {
    float t = time * 0.07;
    // Metaball field of five drops; analytic gradient for the glass normal.
    float field = 0.0;
    vec2 grad = vec2(0.0);
    for (int i = 0; i < 5; i++) {
        float fi = float(i);
        vec2 c = vec2(0.25 + fi * 0.33 + 0.16 * sin(t * (0.7 + fi * 0.13) + fi * 2.3),
                0.50 + 0.26 * sin(t * (0.5 + fi * 0.11) + fi * 1.7));
        float k = 0.012 + 0.006 * sin(fi * 3.1 + 1.0);
        vec2 d = p - c;
        float d2 = max(dot(d, d), 1.0e-5);
        field += k / d2;
        grad += -2.0 * k * d / (d2 * d2);
    }
    // Pixel-exact rim: signed distance estimate from the field and its gradient.
    float dist = (field - 1.0) / max(length(grad), 1.0e-4);
    float aa = mix(fwidth(p.y) * 1.2, 0.02, frost);
    // (The estimate only holds near the rim; deeper in, the field itself says "inside".)
    float inside = mix(smoothstep(-aa, aa, dist), 1.0, smoothstep(1.2, 2.0, field));
    float core = smoothstep(1.0, 2.6, field);           // 0 at the rim, 1 deep inside
    vec2 dir = grad / max(length(grad), 1.0e-5);
    vec2 bend = -dir * (1.0 - core) * 0.9;              // strong bending at the rim, flat centre
    vec3 n = normalize(vec3(bend, 1.0));
    vec3 behind = glowField(p, time, frost, false) * 0.62;   // outside the drops, dimmed
    vec3 col = behind;
    if (inside > 0.0) {
        vec3 through = glowField(p + bend * 0.07, time, frost, false) * 1.12 + 0.03; // refracted
        col = mix(behind, through, inside);
    }
    float rim = pow(1.0 - core, 3.0) * inside;
    float light = clamp(dot(-dir, normalize(vec2(-0.6, -0.8))), 0.0, 1.0);
    col += vec3(1.0) * rim * (0.20 + 0.75 * light * light) * (1.0 - frost * 0.6);
    col *= 1.0 - inside * pow(1.0 - core, 2.0) * clamp(dot(-dir, normalize(vec2(0.6, 0.8))), 0.0, 1.0) * 0.35;
    return col;
}

// ---- RIBBONS: layered translucent light ribbons ------------------------------------------------
vec3 ribbons(vec2 p, float time, float frost) {
    float t = time * 0.05;
    vec3 col = mix(vec3(0.006, 0.008, 0.025), vec3(0.025, 0.012, 0.050), p.y);
    for (int i = 0; i < 7; i++) {
        float fi = float(i);
        float y0 = 0.27 + fi * 0.075
                + 0.10 * sin(p.x * (0.9 + fi * 0.13) + t * (1.0 + fi * 0.2) + fi * 1.9)
                + 0.05 * sin(p.x * 2.1 - t * 1.4 + fi * 0.7);
        float thick = 0.045 + 0.03 * sin(p.x * 1.3 + fi * 2.3 + t);
        float d = (p.y - y0) / thick;
        float band = exp(-d * d * mix(1.0, 0.45, frost));
        float edge = exp(-(d - 0.7) * (d - 0.7) * 22.0) * (1.0 - frost * 0.8);
        float hue = 0.5 + 0.5 * sin(fi * 1.7 + p.x * 1.1 + t * 0.7);
        vec3 tint = mix(vec3(0.10, 0.38, 1.00), vec3(0.85, 0.25, 0.95), hue);
        tint = mix(tint, vec3(1.0, 0.40, 0.50), smoothstep(0.9, 1.7, p.x) * 0.5);
        col += tint * band * 0.34 + vec3(0.95, 0.9, 1.0) * edge * 0.30 * band;
    }
    return 1.0 - exp(-col * 1.5);
}

// ---- DUNES: layered dunes at sunset ------------------------------------------------------------
float duneRidge(float x, float i, float t) {
    float s = sin(x * (1.3 + i * 0.4) + i * 2.1 + t * 0.012);
    float crest = 1.0 - pow(abs(s), 0.7);           // sharp crests, wide troughs
    return crest * 0.7 + 0.3 * sin(x * (2.7 + i * 0.6) - i * 1.3 - t * 0.009);
}

vec3 dunes(vec2 p, float time, float frost) {
    float sunY = 0.40 + 0.03 * sin(time * 0.01);
    vec3 sky = mix(vec3(0.16, 0.18, 0.40), vec3(1.00, 0.64, 0.44), smoothstep(0.0, 0.55, p.y));
    vec2 sun = vec2(1.22, sunY);
    float sd = length((p - sun) * vec2(1.0, 1.25));
    sky += vec3(1.0, 0.70, 0.42) * exp(-sd * sd * 7.0) * 0.55;
    sky = mix(sky, vec3(1.0, 0.95, 0.82), 1.0 - smoothstep(0.026, 0.032, sd));
    vec3 col = sky;
    float px = mix(fwidth(p.y) * 1.25, 0.025, frost);
    for (int k = 0; k < 5; k++) {
        float i = float(k);
        float sx = p.x * (1.0 + i * 0.22);
        float baseY = 0.50 + i * 0.105;
        float amp = 0.05 + i * 0.016;
        float r = baseY - amp * duneRidge(sx, i, time);
        float inside = smoothstep(r - px, r + px, p.y);
        float depth = max(p.y - r, 0.0);
        // Lit face (towards the sun, on the right) vs shadow face; the terminator slants down
        // and away from each crest, as on real dunes.
        float sk = sx - depth * 2.2;
        float slope = (duneRidge(sk + 0.01, i, time) - duneRidge(sk - 0.01, i, time)) / 0.02;
        float lit = smoothstep(-1.2, 1.2, slope);
        float shade = i / 4.0;
        vec3 litCol = mix(vec3(0.98, 0.62, 0.40), vec3(0.78, 0.36, 0.24), shade);
        vec3 darkCol = mix(vec3(0.55, 0.30, 0.38), vec3(0.20, 0.09, 0.14), shade);
        vec3 c = mix(darkCol, litCol, lit * exp(-depth * 4.0));
        c *= 1.0 - smoothstep(0.0, 0.3, depth) * 0.35;
        c += vec3(1.0, 0.85, 0.65) * exp(-depth * 160.0) * 0.22 * lit;
        c = mix(c, sky, (1.0 - i / 4.0) * 0.38);
        col = mix(col, c, inside);
    }
    return col;
}

void main() {
    float time = params.x;
    float screenH = max(params.y, 1.0);
    float frost = vertexColor.a;
    vec2 p = (center + localPos) / screenH;

    float coverage = 1.0;
    if (radius >= 0.0) {
        float d = sdRoundedBox(localPos, halfSize, radius);
        coverage = clamp(0.5 - d / max(fwidth(d), 1.0e-4), 0.0, 1.0);
        if (coverage <= 0.0) {
            discard;
        }
    }

#if defined(NEBULA)
    vec3 col = nebula(p, time, frost);
#elif defined(SILK)
    vec3 col = silk(p, time, frost);
#elif defined(GLOW)
    vec3 col = glow(p, time, frost);
#elif defined(LIQUID)
    vec3 col = liquid(p, time, frost);
#elif defined(RIBBONS)
    vec3 col = ribbons(p, time, frost);
#elif defined(DUNES)
    vec3 col = dunes(p, time, frost);
#else
    vec3 col = aurora(p, time, frost);
#endif
    col *= vertexColor.rgb;
    // Frosted glass scatters light: lift and slightly desaturate.
    col = mix(col, col * 0.85 + vec3(dot(col, vec3(0.299, 0.587, 0.114))) * 0.25 + 0.012, frost);
    col += (hash12(localPos * 7.13 + time) - 0.5) / 255.0;
    fragColor = vec4(max(col, vec3(0.0)), coverage) * ColorModulator;
}
