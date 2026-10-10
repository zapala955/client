#version 330
#extension GL_ARB_separate_shader_objects : require

// Storm Chaser – the storm in the sky (render.storm.StormSky), drawn inside the sky pass over
// whatever sky is already there (vanilla's or the Skybox's), so it overrides every sky mode.
//
//   NOISE      once per resource set: a tileable 256² noise texture – r, g: value-noise fbm (4
//              octaves each, decorrelated), b: smooth round blobs (billows, mammatus pouches),
//              a: soft single-octave value noise. Everything below reads it (one bilinear fetch per
//              octave) instead of hashing per pixel, which is what makes this affordable.
//   SKY        half resolution: the view ray against an analytic storm (no volume march):
//                · the deck's base – bisected against a smooth height profile: the core deck, the
//                  shelf cloud's low base at the front, the higher trailing deck at the back,
//                · the towers' front / rear faces and the shelf cloud's sloped face – parabolic
//                  arcs bowing towards the viewer, hit exactly (a quadratic per face),
//              each with a soft edge one or two pixels wide, so silhouettes never alias. All the
//              detail is in the shading: thickness and rolls under the deck, cauliflower billows on
//              the towers (normals from the blob field), laminar tiers on the shelf, mammatus
//              pouches hanging from the trailing deck lit by a low sun, golden light slanting in
//              under the deck's trailing edge, the anvil spreading overhead, ragged scud racing
//              below, rain curtains hanging from the base (a few samples along the ray), haze, and
//              lightning lighting the cloud from inside. Out: premultiplied colour + coverage.
//   COMPOSITE  full resolution: the half-resolution storm upsampled and blended over the sky
//              (premultiplied), dithered so the dark gradients don't band.
//
// Lengths are blocks. The storm travels along Axis.xy; s is the distance along it from the camera,
// q the distance across. The front (and with it the shelf, the towers and the rain) moves with the
// storm; the cloud texture is anchored in the world and drifts with the wind.

#ifdef COMPOSITE
uniform sampler2D InSampler;
#endif
#ifdef SKY
uniform sampler2D NoiseSampler;

// Mirrors StormSky#write (std140, mat4 + 15 x vec4).
layout(std140) uniform LiquidStormSky {
    mat4 InvViewProj;  // NDC -> camera-relative world position
    vec4 Axis;         // xy travel direction (world x, z), zw across (world x, z)
    vec4 Storm;        // x front (blocks along the axis from the camera), y back, z deck base above the camera, w presence 0..1
    vec4 World;        // x camera along, y across (wrapped to the noise period), z deck drift (wrapped), w time (s)
    vec4 SunDir;       // xyz towards the sun (or the moon by night), w daylight 0..1
    vec4 SunColor;     // rgb, w low-sun factor (golden light under the storm's edges)
    vec4 DeckDark;     // rgb shadowed cloud, w rain curtains 0..1
    vec4 DeckLight;    // rgb lit cloud, w mammatus 0..1
    vec4 Haze;         // rgb rain haze at the horizon, w visibility (blocks)
    vec4 Flash[4];     // xyz camera-relative position of a flash in the cloud, w intensity
    vec4 FlashColor;   // rgb, w whole-sky flash
    vec4 Params;       // x pixel angle at this resolution (rad), y quality 0..2, z anvil 0..1, w gloom over the clear sky 0..1
    vec4 Extra;        // x the camera's distance across from the storm's centre line, yzw unused
};
#endif

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

// =============================================================================================
#ifdef NOISE
uint hashU(uvec2 p, uint seed) {
    uint v = p.x * 1597334673u ^ p.y * 3812015801u ^ seed * 2654435761u;
    v = v * 747796405u + 2891336453u;
    v = ((v >> ((v >> 28u) + 4u)) ^ v) * 277803737u;
    return (v >> 22u) ^ v;
}

float hash(ivec2 p, int period, uint seed) {
    uvec2 q = uvec2((p % period + period) % period);
    return float(hashU(q, seed)) / 4294967296.0;
}

float valueNoise(vec2 uv, int period, uint seed) {
    vec2 x = uv * float(period);
    ivec2 i = ivec2(floor(x));
    vec2 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i, period, seed);
    float b = hash(i + ivec2(1, 0), period, seed);
    float c = hash(i + ivec2(0, 1), period, seed);
    float d = hash(i + ivec2(1, 1), period, seed);
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 uv, uint seed) {
    float sum = 0.0;
    float amp = 0.5;
    float total = 0.0;
    int period = 8;
    for (int k = 0; k < 4; k++) {
        sum += amp * valueNoise(uv, period, seed + uint(k) * 17u);
        total += amp;
        amp *= 0.5;
        period *= 2;
    }
    return sum / total;
}

// Smooth round bumps on a jittered grid; overlapping ones merge.
float blobs(vec2 uv, int period, uint seed) {
    vec2 x = uv * float(period);
    ivec2 i = ivec2(floor(x));
    float sum = 0.0;
    for (int oy = -1; oy <= 1; oy++) {
        for (int ox = -1; ox <= 1; ox++) {
            ivec2 c = i + ivec2(ox, oy);
            vec2 centre = vec2(c) + 0.2 + 0.6 * vec2(hash(c, period, seed), hash(c, period, seed + 1u));
            float radius = 0.55 + 0.25 * hash(c, period, seed + 2u);
            vec2 d = (x - centre) / radius;
            float k = clamp(1.0 - dot(d, d), 0.0, 1.0);
            sum += k * k * (0.7 + 0.3 * hash(c, period, seed + 3u));
        }
    }
    return clamp(sum, 0.0, 1.0);
}

void main() {
    vec2 uv = texCoord;
    fragColor = vec4(fbm(uv, 1u), fbm(uv, 2u), blobs(uv, 12, 3u), valueNoise(uv, 4, 4u));
}
#endif

// =============================================================================================
#ifdef SKY
const float NONE = 1.0e12;
const float ARC_R = 30000.0;     // the front bows towards the viewer
const float SHELF = 1300.0;      // the shelf cloud reaches this far ahead of the towers
const float SHELF_K = 0.72;      // its face rises this steeply back towards the storm
const float TRAIL = 6500.0;      // the trailing deck behind the core
const float DEPTH = 7000.0;      // the towers' depth behind the front
const float RAIN_LAG = 1100.0;   // the rain core starts this far behind the front
const float ANVIL_REACH = 22000.0;

// Heights scale with the deck base above the camera (Storm.z, 520 blocks for a camera near sea level).
float H;
float H_SHELF;
float H_TRAIL;
float H_TOWER;
float H_DECK_TOP;
float H_ANVIL;
float pixelAngle;

vec2 axis;
vec2 across;

float smooth01(float e0, float e1, float v) {
    return smoothstep(e0, e1, v);
}

// One bilinear fetch of the noise texture at a world position (blocks) and scale (blocks per tile).
vec4 noiseAt(vec2 p, vec2 scale) {
    return texture(NoiseSampler, p / scale);
}

// Fades a noise term towards its mean once a texel shrinks below a pixel (the texture has no mips).
float lod(float scale, float t) {
    float footprint = t * pixelAngle;
    return 1.0 - smoothstep(scale * (1.0 / 192.0), scale * (1.0 / 40.0), footprint);
}

// The front's position at q (camera-relative across): an arc bowing towards the viewer, its apex on
// the storm's centre line.
float arcAt(float c, float q) {
    float qc = q + Extra.x;
    return c - qc * qc / (2.0 * ARC_R);
}

// Its local slope term, for "seen from outside": (ds, dq) · the face's normal.
float arcFacing(float ds, float dq, float q) {
    return ds + dq * (q + Extra.x) / ARC_R;
}

// Nearest t > 1 where the ray (s = t ds, q = t dq) meets the face s = c - q² / 2R (apex at q = 0).
float faceHit(float c, float ds, float dq) {
    float a = dq * dq / (2.0 * ARC_R);
    if (a < 1.0e-10) {
        float t = abs(ds) > 1.0e-7 ? c / ds : -1.0;
        return t > 1.0 ? t : NONE;
    }
    // a t² + ds t - c = 0, in the numerically stable form
    float disc = ds * ds + 4.0 * a * c;
    if (disc < 0.0) {
        return NONE;
    }
    float root = sqrt(disc);
    float qq = -0.5 * (ds + (ds >= 0.0 ? root : -root));
    float t1 = qq / a;
    float t2 = abs(qq) > 1.0e-9 ? -c / qq : NONE;
    float t = NONE;
    if (t1 > 1.0) {
        t = t1;
    }
    if (t2 > 1.0) {
        t = min(t, t2);
    }
    return t;
}

// The same for an arc whose apex is at q = -Extra.x: s = c - (q + q0)² / 2R expands to an apex-at-zero
// arc with a shifted constant and slope.
float arcHit(float c, float ds, float dq) {
    float q0 = Extra.x;
    return faceHit(c - q0 * q0 / (2.0 * ARC_R), ds + dq * q0 / ARC_R, dq);
}

// World-anchored position (blocks, wrapped) of a point on the deck for the drifting cloud texture.
vec2 deckPos(float s, float q) {
    return vec2(s + World.x + World.z, q + World.y);
}

// The base's height above the camera at (s, q): the core deck, lowering into the shelf's base at the
// front, rising into the trailing deck at the back, with a slow ragged undulation.
float baseHeight(float s, float q) {
    float F = arcAt(Storm.x, q);
    float B = arcAt(Storm.y, q);
    float front = smoothstep(F - 2200.0, F + 300.0, s);
    float back = 1.0 - smoothstep(B + 300.0, B + TRAIL, s);
    float n = noiseAt(deckPos(s, q), vec2(6144.0)).r - 0.5;
    return H + (H_SHELF - H) * front + (H_TRAIL - H) * back + n * H * 0.3;
}

// The towers' tops along the front: one broad dome over the storm's centre, smaller domes on it.
float towerTop(float q) {
    float broad = noiseAt(vec2(q + World.y, 0.7 * 24576.0), vec2(24576.0)).r;
    float domes = noiseAt(vec2(q + World.y, 0.37 * 24576.0), vec2(24576.0)).b * 0.6
            + noiseAt(vec2(q + World.y + 0.5 * 12288.0, 0.81 * 12288.0), vec2(12288.0)).b * 0.4;
    float centre = exp(-((q + Extra.x) / 26000.0) * ((q + Extra.x) / 26000.0));
    return H + (H_TOWER - H) * (0.35 + 0.5 * centre + 0.3 * (broad - 0.5)) + domes * H * 2.9;
}

// The towers' billows on a face: smooth round lobes, larger ones carrying smaller ones.
float billow(float q, float y) {
    vec2 p = vec2(q + World.y, y);
    return noiseAt(p, vec2(16384.0, 12288.0)).b * 0.7 + noiseAt(p + vec2(0.37 * 6144.0, 0.61 * 4096.0), vec2(6144.0, 4096.0)).b * 0.3;
}

float pouch(float s, float q) {
    return noiseAt(deckPos(s, q) * vec2(1.0, 1.0), vec2(1536.0)).b;
}

// Lightning inside the cloud: light falling off around each flash, brighter where the cloud is thin.
vec3 flashLight(vec3 p, float thin) {
    vec3 light = vec3(0.0);
    for (int i = 0; i < 4; i++) {
        vec4 f = Flash[i];
        if (f.w > 0.001) {
            vec3 d = (p - f.xyz) * vec3(1.0, 1.6, 1.0);
            float radius = 520.0 + 0.03 * length(f.xyz);
            float x = dot(d, d) / (radius * radius);
            light += f.w * (exp(-sqrt(x) * 1.4) * 0.7 + 0.3 / (1.0 + x * 0.35));
        }
    }
    return FlashColor.rgb * light * thin;
}

void main() {
    pixelAngle = Params.x;
    axis = Axis.xy;
    across = Axis.zw;
    H = Storm.z;
    H_SHELF = H * 0.54;
    H_TRAIL = H * 1.83;
    H_TOWER = H * 20.0;
    H_DECK_TOP = H * 4.2;
    H_ANVIL = H * 18.8;

    vec4 world = InvViewProj * vec4(texCoord * 2.0 - 1.0, 0.5, 1.0);
    vec3 dir = normalize(world.xyz / world.w);
    float dy = dir.y;
    float ds = dot(dir.xz, axis);
    float dq = dot(dir.xz, across);
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))) + World.w * 0.618034);
    vec3 sun = normalize(SunDir.xyz);
    float sunS = dot(sun.xz, axis);
    float sunQ = dot(sun.xz, across);
    float day = SunDir.w;
    float lowSun = SunColor.w;
    vec3 golden = SunColor.rgb * vec3(1.0, 0.64, 0.42);
    int quality = int(Params.y + 0.5);

    // ---- what the ray hits first --------------------------------------------------------------
    float bestT = NONE;
    float bestA = 0.0;
    int kind = -1;

    // (0) the base, from below: bisected between its lowest and highest height
    if (dy > 0.001) {
        float lo = (H_SHELF - H * 0.42) / dy;
        float hi = (H_TRAIL + H * 0.42) / dy;
        int iterations = quality == 0 ? 7 : (quality == 1 ? 9 : 11);
        for (int i = 0; i < 11; i++) {
            if (i >= iterations) {
                break;
            }
            float mid = 0.5 * (lo + hi);
            if (mid * dy > baseHeight(mid * ds, mid * dq)) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        float t = hi;
        float s = t * ds;
        float q = t * dq;
        float F = arcAt(Storm.x, q);
        float B = arcAt(Storm.y, q);
        float w = t * pixelAngle * 2.0 + 60.0;
        float rag = noiseAt(deckPos(s, q), vec2(3072.0)).g - 0.5;
        float a = smoothstep(-w, w, F + SHELF + rag * 900.0 - s);
        // the trailing deck breaks up into gaps towards its back edge (the sun pours through)
        float gaps = noiseAt(deckPos(s, q) + vec2(0.5 * 4096.0), vec2(4096.0)).r;
        a *= smoothstep(-0.02, 0.18, (s - B) / TRAIL + (gaps - 0.5) * 0.55);
        if (a > 0.002) {
            bestT = t;
            bestA = a;
            kind = 0;
        }
    }
    // (1) the towers' front face and (3) their rear face, seen only from outside
    for (int k = 0; k < 2; k++) {
        float c = k == 0 ? Storm.x : Storm.x - DEPTH;
        float t = arcHit(c, ds, dq);
        if (t < bestT) {
            float y = t * dy;
            float q = t * dq;
            float facing = arcFacing(ds, dq, q) * (k == 0 ? 1.0 : -1.0);
            if (facing < 0.0) {
                float w = t * pixelAngle * 2.0 + 40.0;
                float bottom = k == 0 ? baseHeight(t * ds, q) : H_DECK_TOP;
                float a = smoothstep(-w, w, towerTop(q) - y) * smoothstep(-w, w, y - bottom);
                if (a > 0.002) {
                    bestT = t;
                    bestA = a;
                    kind = k == 0 ? 1 : 3;
                }
            }
        }
    }
    // (2) the shelf's sloped face: s = F0 + SHELF - q²/2R - (y - H_SHELF) / K, with y = t dy
    {
        float t = arcHit(Storm.x + SHELF + H_SHELF / SHELF_K, ds + dy / SHELF_K, dq);
        if (t < bestT) {
            float y = t * dy;
            float s = t * ds;
            float q = t * dq;
            if (arcFacing(ds, dq, q) < 0.0) {
                float w = t * pixelAngle * 2.0 + 40.0;
                float a = smoothstep(-w, w, y - H_SHELF) * smoothstep(-w, w, s - arcAt(Storm.x, q));
                if (a > 0.002) {
                    bestT = t;
                    bestA = a;
                    kind = 2;
                }
            }
        }
    }
    // (4) the trailing deck's thin back edge, seen from behind
    if (ds > 0.0) {
        float t = arcHit(Storm.y, ds, dq);
        if (t < bestT) {
            float y = t * dy;
            float hb = baseHeight(t * ds, t * dq);
            float w = t * pixelAngle * 2.0 + 40.0;
            float a = smoothstep(-w, w, hb + H * 0.5 - y) * smoothstep(-w, w, y - hb) * 0.85;
            if (a > 0.002) {
                bestT = t;
                bestA = a;
                kind = 4;
            }
        }
    }

    float t = min(bestT, 80000.0);
    float s = t * ds;
    float y = t * dy;
    float q = t * dq;
    vec3 p = dir * t;
    float F = arcAt(Storm.x, q);
    float B = arcAt(Storm.y, q);
    vec3 dark = DeckDark.rgb;
    vec3 lite = DeckLight.rgb;
    vec3 col = vec3(0.0);
    float thin = 1.0;

    if (kind == 0) {
        // ---- the base from below: brighter where the cloud is thin, darker in the core --------
        vec2 dp = deckPos(s, q);
        float thick = mix(0.5, noiseAt(dp, vec2(3072.0, 4096.0)).r, lod(3072.0, t)) * 0.55
                + mix(0.5, noiseAt(dp * 1.0 + vec2(0.2 * 768.0, 0.1 * 768.0), vec2(768.0)).g, lod(768.0, t)) * 0.3
                + mix(0.5, noiseAt(dp + vec2(0.6 * 192.0, 0.3 * 192.0), vec2(192.0)).r, lod(192.0, t)) * 0.15;
        float core = smoothstep(B + TRAIL * 0.5, B + TRAIL, s) * (1.0 - smoothstep(F - 3000.0, F, s));
        float light = pow(clamp(1.35 - thick * 1.45 - core * 0.2, 0.0, 1.0), 1.7);
        col = mix(dark, lite, light);
        thin = clamp(1.3 - thick, 0.3, 1.2);
        // the shelf's underside: a paler lip right at the leading edge
        col = mix(col, lite * 1.3, smoothstep(F + SHELF * 0.6, F + SHELF, s) * 0.5);
        // mammatus: rounded pouches under the trailing deck, sculpted by a low sun
        float mam = (1.0 - smoothstep(B + TRAIL * 0.3, B + TRAIL * 0.85, s)) * smoothstep(B + 300.0, B + 1200.0, s) * DeckLight.w;
        if (mam > 0.002) {
            // forward differences: three taps instead of five, half a step off is invisible on 1.5 km pouches
            float e = 25.0;
            float p0 = pouch(s, q);
            float gs = (pouch(s + e, q) - p0) / e;
            float gq = (pouch(s, q + e) - p0) / e;
            vec3 n = normalize(vec3(gs * 260.0, -1.0, gq * 260.0));          // (s, y, q)
            vec3 l = normalize(vec3(sunS, sun.y, sunQ));
            float lam = max(dot(n, l), 0.0);
            float wrap = clamp(dot(n, l) * 0.5 + 0.5, 0.0, 1.0);
            vec3 pouchCol = dark * (0.9 + 0.8 * p0) + lite * 0.2 * p0
                    + golden * (pow(lam, 1.5) * lowSun * 0.9 + wrap * lowSun * 0.1) + lite * lam * day * 0.45;
            col = mix(col, pouchCol, mam * lod(1536.0, t));
        }
        // golden light slanting in under the trailing edge when a low sun is behind the storm
        col += golden * lowSun * clamp(-sunS, 0.0, 1.0) * exp(-max(s - B, 0.0) / 2000.0) * 0.4;
    } else if (kind == 1 || kind == 3) {
        // ---- the towers: cauliflower billows, the anvil shading their lower parts ---------------
        float b0 = billow(q, y);
        float e = 160.0;
        float gy = 0.0;
        float gq = 0.0;
        if (quality > 0) {
            gy = (billow(q, y + e) - billow(q, y - e)) / (2.0 * e);
            gq = (billow(q + e, y) - billow(q - e, y)) / (2.0 * e);
        }
        float faceS = kind == 3 ? -1.0 : 1.0;
        float bump = lod(4096.0, t);
        vec3 n = normalize(vec3(faceS, gy * 2400.0 * bump, gq * 2400.0 * bump));  // (s, y, q)
        vec3 l = normalize(vec3(sunS, sun.y, sunQ));
        float lit = clamp(dot(n, l) * 0.75 + 0.25, 0.0, 1.0);
        float height = clamp((y - H) / (H_TOWER - H), 0.0, 1.0);
        // Where would the sun's ray from here cross the anvil's height? Under the anvil = in shade.
        float sunElev = max(sun.y, 0.02);
        float reach = (H_ANVIL - y) / sunElev * length(sun.xz);
        float under = kind == 1 ? reach * sunS : -reach * sunS;
        float sunlit = smoothstep(ANVIL_REACH * 0.6, ANVIL_REACH, under) + smoothstep(0.0, -2000.0, under);
        sunlit = clamp(sunlit, 0.0, 1.0) * day;
        float skyLight = clamp(0.5 + 0.5 * n.y, 0.0, 1.0);
        float fine = mix(0.5, noiseAt(vec2(q + World.y, y), vec2(768.0)).r, lod(768.0, t)) - 0.5;
        col = dark * (1.0 + 0.6 * skyLight + fine * 0.25)
                + lite * (0.25 + 0.75 * height) * skyLight * (0.75 + 0.25 * b0)
                + SunColor.rgb * lit * sunlit * (0.35 + 0.65 * height) * 0.55
                + golden * lit * lowSun * sunlit * 0.4;
        // the lower towers disappear into the rain core
        col = mix(col, mix(dark, Haze.rgb, 0.6), smoothstep(H * 3.5, H, y) * 0.65 * DeckDark.w);
        thin = 0.5 + 0.5 * b0;
    } else if (kind == 2) {
        // ---- the shelf cloud's face: smooth laminar tiers, paler where they catch the light -----
        float warp = (noiseAt(vec2(q + World.y, s - Storm.x), vec2(6144.0)).r - 0.5) * 7.0
                + (noiseAt(vec2(q + World.y, s - Storm.x) + vec2(World.z * 0.3, 0.0), vec2(1536.0)).g - 0.5) * 1.5;
        float period = H * 0.125 * 6.2831853;
        float tiers = 0.5 + 0.5 * sin((y - H_SHELF) / (H * 0.125) + warp);
        tiers = mix(0.5, tiers, 1.0 - smoothstep(period * 0.12, period * 0.4, t * pixelAngle / max(dy, 0.05)));
        float rise = clamp((y - H_SHELF) / (H * 1.35), 0.0, 1.0);
        float band = tiers * tiers;
        // the anvil overhead keeps all but a low sun off the shelf
        float sunElev = max(sun.y, 0.02);
        float under = (H_ANVIL - y) / sunElev * length(sun.xz) * sunS;
        float sunlit = clamp(smoothstep(ANVIL_REACH * 0.6, ANVIL_REACH, under), 0.0, 1.0) * day;
        col = mix(dark * 1.1, lite * 1.9, clamp(0.2 + 0.5 * band * (0.4 + 0.6 * rise) + 0.3 * rise, 0.0, 1.0));
        col += SunColor.rgb * clamp(sunS, 0.0, 1.0) * sunlit * (0.3 + 0.7 * band) * 0.35;
        // the dark lower lip ("whale's mouth") right above the leading edge
        col *= 0.55 + 0.45 * smoothstep(0.0, 0.25, rise);
        thin = 0.8;
    } else if (kind == 4) {
        float b0 = billow(q, y);
        col = mix(dark * 1.5, lite * 1.8, 0.4 + 0.4 * b0) + golden * lowSun * clamp(-sunS, 0.0, 1.0) * 0.5;
    }
    if (kind >= 0) {
        col += flashLight(p, thin);
        // aerial perspective: rain-filled air greys everything towards the horizon
        col = mix(col, Haze.rgb, (1.0 - exp(-t / 30000.0)) * 0.9);
    }

    // ---- composite, back to front (premultiplied) -----------------------------------------------
    // the clear sky beyond: dimmed by the storm's gloom
    vec4 acc = vec4(dark * 0.6, 1.0) * Params.w;

    // the anvil aloft, spreading ahead of the towers
    if (dy > 0.001 && Params.z > 0.0) {
        float ta = H_ANVIL / dy;
        float sa = ta * ds;
        float qa = ta * dq;
        float Fa = arcAt(Storm.x, qa);
        vec2 ap = vec2(sa - Storm.x, qa + World.y);
        float fib = noiseAt(ap + vec2(World.z * 0.25, 0.0), vec2(12288.0, 49152.0)).r * 0.55
                + mix(0.5, noiseAt(ap + vec2(0.2 * 3072.0 + World.z * 0.25, 0.2 * 16384.0), vec2(3072.0, 16384.0)).g, lod(3072.0, ta)) * 0.45;
        float lead = Fa + ANVIL_REACH + (fib - 0.5) * 12000.0;
        float cover = smoothstep(lead, lead - 7000.0, sa) * smoothstep(Fa - DEPTH - 3000.0, Fa - DEPTH + 2000.0, sa);
        cover *= smoothstep(0.0, 0.06, dy) * exp(-ta / 120000.0) * Params.z;
        float nearTower = smoothstep(Fa + ANVIL_REACH * 0.6, Fa, sa);
        float a = clamp(cover * (0.25 + 0.9 * fib) + nearTower * cover * 0.5, 0.0, 0.97);
        vec3 c = mix(lite * 1.3, lite * 2.9, (fib * 0.6 + 0.4 * clamp(sun.y, 0.0, 1.0)) * (1.0 - 0.65 * nearTower));
        c += flashLight(vec3(dir.x, 0.0, dir.z) * ta + vec3(0.0, H_ANVIL * 0.4, 0.0), 0.35) + FlashColor.rgb * FlashColor.w * 0.3;
        acc = vec4(c * a, a) + acc * (1.0 - a);
    }

    // the storm itself
    if (kind >= 0) {
        acc = vec4(col * bestA, bestA) + acc * (1.0 - bestA);
    }

    // rain curtains hanging from the base, slanting with the wind
    if (DeckDark.w > 0.0) {
        int steps = quality == 0 ? 5 : (quality == 1 ? 7 : 9);
        float tr = min(t, 50000.0);
        if (dy < 0.0) {
            tr = min(tr, H * 0.15 / max(-dy, 1.0e-4));   // the ground, roughly
        }
        float optical = 0.0;
        for (int k = 0; k < 9; k++) {
            if (k >= steps) {
                break;
            }
            float f0 = pow((float(k) + jitter) / float(steps), 1.5);
            float dt = tr * (pow(float(k + 1) / float(steps), 1.5) - pow(float(k) / float(steps), 1.5));
            float tt = tr * f0;
            float ps = tt * ds;
            float py = tt * dy;
            float pq = tt * dq;
            float Fk = arcAt(Storm.x, pq);
            float Bk = arcAt(Storm.y, pq);
            float inRain = smoothstep(Bk + TRAIL * 0.3, Bk + TRAIL * 0.6, ps) * (1.0 - smoothstep(Fk - RAIN_LAG - 1800.0, Fk - RAIN_LAG, ps));
            if (inRain > 0.0) {
                float hb = baseHeight(ps, pq);
                float below = smoothstep(hb, hb - 120.0, py);
                float slant = py * 0.45;
                float shafts = noiseAt(vec2(ps - slant - Storm.x, pq + World.y) + vec2(World.z * 0.15, 0.0), vec2(3072.0, 1024.0)).g;
                optical += inRain * below * (0.4 + 0.6 * smoothstep(0.25, 0.75, shafts)) * dt / 2300.0;
            }
        }
        float a = (1.0 - exp(-optical * DeckDark.w));
        vec3 rain = mix(dark, Haze.rgb, 0.75) * 1.15 + FlashColor.rgb * FlashColor.w * 0.12;
        acc = vec4(rain * a, a) + acc * (1.0 - a);
    }

    // ragged scud racing under the deck (when there's a deck overhead)
    if (dy > 0.002) {
        float hs = H * 0.62;
        float ts = hs / dy;
        float ss = ts * ds;
        float qs = ts * dq;
        float under = smoothstep(arcAt(Storm.y, qs) + TRAIL * 0.4, arcAt(Storm.y, qs) + TRAIL, ss)
                * (1.0 - smoothstep(arcAt(Storm.x, qs) - 500.0, arcAt(Storm.x, qs) + SHELF, ss));
        if (under > 0.0) {
            vec2 sp = vec2(ss + World.x + World.z * 1.8, qs + World.y);
            float n = noiseAt(sp, vec2(3072.0, 1536.0)).r * 0.65 + mix(0.5, noiseAt(sp, vec2(768.0)).g, lod(768.0, ts)) * 0.35;
            float a = smoothstep(0.58, 0.82, n) * under * 0.75 * smoothstep(0.002, 0.05, dy) * exp(-ts / 20000.0);
            vec3 c = mix(dark * 0.8, lite * 0.9, n) + flashLight(dir * ts, 0.8) * 0.6;
            acc = vec4(c * a, a) + acc * (1.0 - a);
        }
    }

    // a flash brightens the whole sky a little, wherever there's storm
    acc.rgb += FlashColor.rgb * FlashColor.w * 0.25 * acc.a;
    fragColor = acc * Storm.w;
}
#endif

// =============================================================================================
#ifdef COMPOSITE
void main() {
    vec4 storm = texture(InSampler, texCoord);
    // Triangular dither (±1 LSB): the storm's dark gradients would band in 8 bits.
    vec2 p = gl_FragCoord.xy;
    float n = fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))))
            + fract(52.9829189 * fract(dot(p + 17.0, vec2(0.06711056, 0.00583715)))) - 1.0;
    storm.rgb = max(storm.rgb + n / 255.0 * storm.a, vec3(0.0));
    fragColor = storm;
}
#endif
