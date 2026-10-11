#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Water (render.water.LiquidWater) – real water in place of vanilla's flat sheets.
//
// The terrain shaders (Sodium's or vanilla's, patched by render.water.WaterShaderPatch) draw every
// water surface in the translucent pass as a little G-buffer: the colour is the water's lit tint
// (biome water colour × light, scaled by 0.6) and the depth carries a tag in its six lowest bits:
// 0b11 = water, bits 2-5 the sky light there. Everything else translucent clears those bits. The
// passes below run right after translucent terrain, before particles, clouds and weather:
//
//   NOISE      once: a tileable wave field (256², RGBA16F): r, g the slope, b the height, a caustics –
//              a sum of integer-frequency waves, so it tiles; caustics from how its longer waves
//              focus sunlight on the bed (the folds of the refracted light: bright connected lines).
//   REFLECT    half resolution, water only: the wave normal, then the reflection – marched through
//              the world as it was before water (screen-space), or the sky: where the reflected ray
//              leaves the screen or misses, the sky as it's actually drawn in that direction if it's
//              on screen (Skybox modes, the storm), else a gradient from the sky and fog colours.
//              Seen from below, past the critical angle, the surface mirrors the water body.
//              a = the surface's distance, for the depth-aware upsample.
//   SHAFTS     half resolution, only with the camera underwater: light shafts from the surface,
//              marched along the view ray.
//   COMPOSITE  full resolution: refraction (what's under the surface, bent by the waves), absorption
//              and scattering by the path through the water (clear turquoise shallows, deep blue,
//              murky where the biome's water is), caustics on the bed, Fresnel between that and the
//              reflection, foam at shores, around objects and under waterfalls, rain rings, the sun's
//              or moon's glints, then vanilla's fog. Underwater: caustics and depth on the terrain,
//              the shafts, Snell's window in the surface overhead.

#ifdef NOISE

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

uint hashU(uint x) {
    x ^= x >> 16u;
    x *= 0x7feb352du;
    x ^= x >> 15u;
    x *= 0x846ca68bu;
    x ^= x >> 16u;
    return x;
}

float hashF(uint x) {
    return float(hashU(x) & 0xFFFFFFu) / 16777216.0;
}

void main() {
    vec2 u = (floor(gl_FragCoord.xy) + 0.5) / 256.0;
    float h = 0.0;
    vec2 slope = vec2(0.0);
    // The Hessian of the longer waves only: caustics in cells about a seventh of the tile across.
    vec3 hess = vec3(0.0); // xx, zz, xz
    float norm = 0.0;
    float normLow = 0.0;
    // Integer wave vectors in a ring (whole periods per tile, so it tiles), amplitude falling with
    // frequency like a wind-sea spectrum, random phases.
    for (int i = 0; i < 96; i++) {
        uint seed = uint(i) * 3u + 17u;
        float angle = hashF(seed) * 6.2831853;
        float radius = mix(1.5, 19.5, pow(hashF(seed + 1u), 1.6));
        vec2 k = floor(vec2(cos(angle), sin(angle)) * radius + 0.5);
        if (k == vec2(0.0)) {
            k = vec2(1.0, 0.0);
        }
        float len = length(k);
        float a = pow(len, -1.75) * (0.6 + 0.8 * hashF(seed + 2u));
        float phase = 6.2831853 * (dot(k, u) + hashF(seed + 7u));
        float c = cos(phase);
        h += a * c;
        slope -= a * 6.2831853 * k * sin(phase);
        norm += a * a * 0.5;
        if (len <= 7.0) {
            hess -= a * 39.478418 * vec3(k.x * k.x, k.y * k.y, k.x * k.y) * c;
            normLow += a * a * 0.5;
        }
    }
    float s = 0.42 / sqrt(norm);
    h *= s;
    slope *= s;
    hess *= 0.42 / sqrt(normLow);
    // Sunlight refracted through a bumpy surface lands at x + D·∇h: its intensity on the bed is
    // 1 / |det J| with J = I + D·Hessian, infinite along the folds – the bright connected lines.
    const float D = 0.03;
    float det = (1.0 + D * hess.x) * (1.0 + D * hess.y) - D * D * hess.z * hess.z;
    float focus = 1.0 / max(abs(det), 0.05);
    float caustic = clamp((focus - 0.7) * 0.3, 0.0, 1.0);
    fragColor = vec4(slope, h, caustic);
}

#else

uniform sampler2D DepthSampler;
uniform sampler2D BedSampler;
uniform sampler2D BehindSampler;
uniform sampler2D WaveSampler;
#if defined(REFLECT) || defined(COMPOSITE)
uniform sampler2D FieldSampler;
#endif
#ifdef COMPOSITE
uniform sampler2D SceneSampler;
uniform sampler2D ReflectSampler;
uniform sampler2D ShaftSampler;
uniform sampler2D ColumnSampler;
#endif

// Mirrors LiquidWater#write (std140, 2 x mat4 + 16 x vec4).
layout(std140) uniform LiquidWater {
    mat4 InvViewProj;  // NDC -> camera-relative position
    mat4 ViewProj;     // camera-relative position -> clip
    vec4 CameraBlock;  // xyz camera block (x, z wrapped to 4096), w time (s)
    vec4 CameraFrac;   // xyz camera position inside its block, w rain columns valid (0 / 1)
    vec4 SunDir;       // xyz towards the sun (the moon at night), w glint strength
    vec4 SunColor;     // rgb, w daylight 0..1
    vec4 SkyZenith;    // rgb, w reflections (0 off, 1 sky only, 2 screen-space)
    vec4 SkyHorizon;   // rgb (the fog colour: Skybox modes and the storm included), w march steps
    vec4 Fog;          // rgb vanilla's fog colour, w its alpha
    vec4 FogRange;     // environmental start / end, render distance start / end (blocks)
    vec4 Wind;         // xy wind direction (unit), z wind 0..2, w wave strength (0..2)
    vec4 Look;         // x clarity, y refraction, z foam (0 / 1), w caustics (0 / 1)
    vec4 Absorb;       // rgb absorption per block, w scattering
    vec4 Scatter;      // rgb scattering colour, w style (0 natural, 1 tropical, 2 deep ocean)
    vec4 Rain;         // x rain now, y storm (whitecaps), z ripples on, w rain on water beyond the columns
    vec4 Under;        // x camera underwater, y the surface above it (camera-relative y), z effects, w shafts
    vec4 Screen;       // xy frame size, zw half-resolution size
    vec4 Params;       // x quality (0..2), y one pixel's angle (rad), z water field valid, w unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float WATER_IOR = 1.333;
const float TINT_SCALE = 0.6;
const uint TYPE_RAIN = 1u;

// ---- Tags and positions ---------------------------------------------------------------------

uint tagOf(float depth) {
    return floatBitsToUint(depth) & 63u;
}

// A water surface: the tag, and nearer than what was there before translucents.
bool waterAt(float depth, float bed) {
    return (tagOf(depth) & 3u) == 3u && depth > bed;
}

float skyLightOf(float depth) {
    return float(tagOf(depth) >> 2u) / 15.0;
}

vec3 positionAt(vec2 uv, float depth) {
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = depth;
    #else
    float z = depth * 2.0 - 1.0;
    #endif
    vec4 world = InvViewProj * vec4(uv * 2.0 - 1.0, z, 1.0);
    return world.xyz / world.w;
}

vec2 project(vec3 rel) {
    vec4 clip = ViewProj * vec4(rel, 1.0);
    return clip.xy / max(clip.w, 1.0e-4) * 0.5 + 0.5;
}

// World xz (wrapped to 4096 around the camera) of a camera-relative position.
vec2 worldXZ(vec3 rel) {
    return CameraBlock.xz + CameraFrac.xz + rel.xz;
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

float pixelNoise(vec2 frag) {
    return fract(52.9829189 * fract(dot(frag, vec2(0.06711056, 0.00583715))));
}

// The surface's geometric normal (facing the camera) from the neighbouring water pixels, on whichever
// side is closer (so it never crosses an edge).
vec3 surfaceNormal(ivec2 p, vec3 pos) {
    ivec2 size = ivec2(Screen.xy) - 1;
    vec3 n = vec3(0.0);
    vec3 dx = vec3(0.0);
    vec3 dy = vec3(0.0);
    bool okx = false;
    bool oky = false;
    for (int s = -1; s <= 1; s += 2) {
        ivec2 qx = clamp(p + ivec2(s, 0), ivec2(0), size);
        float dqx = texelFetch(DepthSampler, qx, 0).r;
        if (waterAt(dqx, texelFetch(BedSampler, qx, 0).r)) {
            vec3 e = (positionAt((vec2(qx) + 0.5) / Screen.xy, dqx) - pos) * float(s);
            if (!okx || dot(e, e) < dot(dx, dx)) {
                dx = e;
                okx = true;
            }
        }
        ivec2 qy = clamp(p + ivec2(0, s), ivec2(0), size);
        float dqy = texelFetch(DepthSampler, qy, 0).r;
        if (waterAt(dqy, texelFetch(BedSampler, qy, 0).r)) {
            vec3 e = (positionAt((vec2(qy) + 0.5) / Screen.xy, dqy) - pos) * float(s);
            if (!oky || dot(e, e) < dot(dy, dy)) {
                dy = e;
                oky = true;
            }
        }
    }
    if (okx && oky) {
        n = cross(dx, dy);
    }
    if (dot(n, n) < 1.0e-12) {
        n = vec3(0.0, 1.0, 0.0);
    }
    n = normalize(n);
    if (dot(n, pos) > 0.0) {
        n = -n;
    }
    // Snap the common case – flat water – to exactly flat: depth noise must not ripple a calm pond.
    if (abs(n.y) > 0.9985) {
        n = vec3(0.0, sign(n.y), 0.0);
    }
    return n;
}

// ---- Waves --------------------------------------------------------------------------------

vec2 rotate(vec2 v, vec2 d) {
    return vec2(v.x * d.x - v.y * d.y, v.x * d.y + v.y * d.x);
}

// One layer of the wave field: slope in blocks per block, for height amplitude amp (blocks) at
// period scale (blocks), turned to direction d, drifting at speed along it.
vec3 waveLayer(vec2 w, float scale, vec2 d, float speed, float amp, float t) {
    vec2 local = rotate(w, vec2(d.x, -d.y)) / scale;
    local.x -= t * speed / scale;
    vec4 s = texture(WaveSampler, local);
    return vec3(rotate(s.xy, d) * (amp / scale), s.z * amp);
}

// How much of a layer of this period survives at this pixel footprint (fades out before it aliases).
float layerFade(float footprint, float scale) {
    return 1.0 - smoothstep(0.08, 0.32, footprint / scale);
}

// Open water: swell along the wind, wind waves on top, ripples. Returns slope (xz) and crest height.
vec3 openWaves(vec2 w, float footprint, float strength, float t, int quality) {
    vec2 d = Wind.xy;
    vec3 sum = vec3(0.0);
    // Long swell: a few sines a little either side of the wind, deep-water speeds.
    float swellAmp = 0.11 * strength;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float lambda = 11.0 - fi * 3.1;
        vec2 dir = rotate(d, vec2(cos(fi * 0.42 - 0.35), sin(fi * 0.42 - 0.35)));
        float k = 6.2831853 / lambda;
        float omega = sqrt(9.81 * k) * 0.62;
        float phase = k * dot(dir, w) - omega * t + fi * 1.7;
        float a = swellAmp * (1.0 - fi * 0.22) * layerFade(footprint, lambda);
        sum += vec3(dir * (a * k * cos(phase)), a * sin(phase));
    }
    // Wind waves from the tiling field: three scales, each turned and drifting differently.
    sum += waveLayer(w, 9.0, d, 1.1, 0.16 * strength, t) * layerFade(footprint, 9.0);
    sum += waveLayer(w, 3.7, rotate(d, vec2(0.866, 0.5)), 0.7, 0.07 * strength, t) * layerFade(footprint, 3.7);
    if (quality > 0) {
        sum += waveLayer(w, 1.45, rotate(d, vec2(0.906, -0.423)), 0.45, 0.026 * (0.35 + strength), t) * layerFade(footprint, 1.45);
    }
    if (quality > 1) {
        sum += waveLayer(w, 0.53, rotate(d, vec2(0.2, 0.98)), 0.3, 0.009 * (0.5 + strength), t) * layerFade(footprint, 0.53);
    }
    return sum;
}

// Flowing water (slopes, falls): the field dragged along the flow, two phases cross-faded so it never
// stretches. axis = (along the flow, across it) coordinates.
vec3 flowWaves(vec2 along, float speed, float footprint, float t, float amp) {
    float cycle = 1.6;
    float p0 = fract(t / cycle);
    float p1 = fract(t / cycle + 0.5);
    float wgt = abs(p0 - 0.5) * 2.0;
    vec3 a = vec3(0.0);
    vec3 b = vec3(0.0);
    for (int i = 0; i < 2; i++) {
        float scale = i == 0 ? 2.6 : 0.9;
        float f = layerFade(footprint, scale) * (i == 0 ? 1.0 : 0.6);
        vec2 off0 = vec2(p0 * cycle * speed, 0.0);
        vec2 off1 = vec2(p1 * cycle * speed, 0.37 * scale);
        vec4 s0 = texture(WaveSampler, (along - off0) / scale + float(i) * 0.31);
        vec4 s1 = texture(WaveSampler, (along - off1) / scale + float(i) * 0.31);
        a += vec3(s0.xy * (amp / scale), s0.z * amp) * f;
        b += vec3(s1.xy * (amp / scale), s1.z * amp) * f;
    }
    return mix(a, b, 1.0 - wgt);
}

#ifdef COMPOSITE
// Rings from raindrops on the surface: slope.
vec2 rainRings(vec2 p, float t, float amount) {
    vec2 slope = vec2(0.0);
    for (int layer = 0; layer < 3; layer++) {
        float fl = float(layer);
        vec2 g = p * (1.9 + fl * 0.45) + fl * vec2(0.37, 0.71);
        vec2 id = floor(g);
        vec2 f = fract(g) - 0.5;
        vec2 h = hash22(id + fl * 19.1);
        float rate = 1.1 + 0.6 * h.y;
        float age = fract(t * rate + h.x);
        // Only some cells drop at all in light rain.
        if (hash12(id * 1.7 + floor(t * rate + h.x) + fl) > amount) {
            continue;
        }
        vec2 d = f - (h - 0.5) * 0.5;
        float r = length(d);
        float front = r - age * 0.48;
        float wave = sin(front * 52.0) * exp(-abs(front) * 24.0) * (1.0 - age) * (1.0 - age);
        slope += d / max(r, 1.0e-3) * wave;
    }
    return slope;
}

// Whether rain reaches this water column: the rain columns where they reach, the global rain beyond.
float rainOnWater(vec3 rel) {
    float far = Rain.w;
    if (CameraFrac.w < 0.5 || max(abs(rel.x), abs(rel.z)) > 30.0) {
        return far;
    }
    ivec2 world = ivec2(floor(CameraBlock.xz + CameraFrac.xz + rel.xz));
    uvec4 b = uvec4(texelFetch(ColumnSampler, world & 63, 0) * 255.0 + 0.5);
    if ((b.b & 16u) == 0u) {
        return far;
    }
    if ((b.b & 3u) != TYPE_RAIN) {
        return 0.0;
    }
    float surface = float(b.r | (b.g << 8u)) / 16.0 - 2048.0 - CameraBlock.y - CameraFrac.y;
    float near = 1.0 - smoothstep(0.15, 0.6, surface - rel.y); // a roof or a tree above the water keeps it dry
    return mix(near, far, smoothstep(24.0, 30.0, max(abs(rel.x), abs(rel.z))));
}

#endif

#if defined(REFLECT) || defined(COMPOSITE)
// The water field around the camera (render.water.WaterField): r openness (calm ponds stay glassy),
// g falling water nearby (foam). Beyond its reach: open water, no falls.
vec2 fieldAt(vec3 rel) {
    if (Params.z < 0.5 || max(abs(rel.x), abs(rel.z)) > 31.0) {
        return vec2(1.0, 0.0);
    }
    vec2 at = CameraFrac.xz + rel.xz - 0.5;
    vec2 base = floor(at);
    vec2 f = at - base;
    ivec2 origin = ivec2(CameraBlock.xz) + ivec2(base);
    vec2 a = texelFetch(FieldSampler, origin & 63, 0).rg;
    vec2 b = texelFetch(FieldSampler, (origin + ivec2(1, 0)) & 63, 0).rg;
    vec2 c = texelFetch(FieldSampler, (origin + ivec2(0, 1)) & 63, 0).rg;
    vec2 d = texelFetch(FieldSampler, (origin + ivec2(1, 1)) & 63, 0).rg;
    vec2 v = mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    float edge = smoothstep(26.0, 31.0, max(abs(rel.x), abs(rel.z)));
    return mix(v, vec2(1.0, 0.0), edge);
}
#endif

// Wave strength for open water at this point: wind, the setting, and how open the water is.
float waveStrength(float openness) {
    return Wind.w * (0.04 + 0.96 * clamp(Wind.z, 0.0, 2.0)) * mix(0.15, 1.0, openness);
}

// The full surface normal at a water pixel. geo = geometric normal (facing the camera), footprint = a
// pixel's size on the surface. crest: wave height (whitecaps); flow: (along the flow, across it,
// falling 0..1, flowing 0 / 1) – flow coordinates in blocks, for the foam's streaks.
vec3 waveNormal(vec3 geo, vec3 rel, float footprint, float strength, int quality, out float crest, out vec4 flow) {
    float t = CameraBlock.w;
    vec2 w = worldXZ(rel);
    float up = abs(geo.y);
    crest = 0.0;
    flow = vec4(0.0);
    if (up > 0.9985) {
        // Flat water: open-water waves.
        vec3 s = openWaves(w, footprint, strength, t, quality);
        crest = s.z;
        vec3 n = normalize(vec3(-s.x, 1.0, -s.y));
        return geo.y < 0.0 ? vec3(n.x, -n.y, n.z) : n;
    }
    // Sloped or falling water flows downhill: drag the field along the flow.
    vec3 down = normalize(vec3(0.0, -1.0, 0.0) - geo * -geo.y);
    vec3 across = normalize(cross(geo, down));
    vec3 p = vec3(w.x, rel.y + CameraBlock.y + CameraFrac.y, w.y);
    float falling = 1.0 - smoothstep(0.25, 0.6, up);
    vec2 along = vec2(dot(p, down), dot(p, across));
    flow = vec4(along, falling, 1.0);
    float speed = mix(1.6, 7.5, falling);
    // Falls are stretched along their drop.
    along.y *= mix(1.0, 2.2, falling);
    vec3 s = flowWaves(along, speed, footprint, t, mix(0.05, 0.11, falling));
    crest = s.z;
    return normalize(geo + down * s.x + across * s.y);
}

// ---- Sky and reflections -----------------------------------------------------------------------

vec3 skyGradient(vec3 r) {
    float up = clamp(r.y, 0.0, 1.0);
    vec3 sky = mix(SkyHorizon.rgb, SkyZenith.rgb, sqrt(up));
    return r.y >= 0.0 ? sky : SkyHorizon.rgb * mix(0.62, 0.45, clamp(-r.y * 3.0, 0.0, 1.0));
}

// The sky in direction r: as it's drawn on screen there (Skybox modes, the storm) when it is, else the gradient.
vec3 skyIn(vec3 r) {
    vec3 gradient = skyGradient(r);
    vec4 clip = ViewProj * vec4(r, 0.0);
    if (clip.w <= 1.0e-3) {
        return gradient;
    }
    vec2 uv = clip.xy / clip.w * 0.5 + 0.5;
    if (any(lessThan(uv, vec2(0.002))) || any(greaterThan(uv, vec2(0.998)))) {
        return gradient;
    }
    if (texture(BedSampler, uv).r > 0.0) {
        return gradient; // terrain in front of the sky there
    }
    vec2 edge = smoothstep(vec2(0.0), vec2(0.12), uv) * (1.0 - smoothstep(vec2(0.88), vec2(1.0), uv));
    return mix(gradient, texture(BehindSampler, uv).rgb, edge.x * edge.y);
}

#ifdef REFLECT
// Marches the reflected ray through the world before water; rgb = what it hits, a = confidence. Small
// steps growing with distance; a hit needs the ray to have only just slipped behind a surface (thin
// things aren't walls), refined by bisection.
vec4 march(vec3 origin, vec3 dir, float jitter, int steps) {
    float stride = 0.12;
    float t = 0.08 + jitter * stride;
    vec3 last = origin;
    for (int i = 0; i < 40; i++) {
        if (i >= steps) {
            break;
        }
        vec3 q = origin + dir * t;
        vec4 clip = ViewProj * vec4(q, 1.0);
        if (clip.w <= 0.05) {
            break;
        }
        vec2 uv = clip.xy / clip.w * 0.5 + 0.5;
        if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) {
            break;
        }
        float bed = texture(BedSampler, uv).r;
        if (bed > 0.0) {
            float rayDist = length(q);
            float gap = rayDist - length(positionAt(uv, bed));
            float thickness = 0.2 + stride * 1.4 + rayDist * 0.004;
            if (gap > 0.0 && gap < thickness) {
                vec3 lo = last;
                vec3 hi = q;
                for (int k = 0; k < 5; k++) {
                    vec3 mid = (lo + hi) * 0.5;
                    vec2 muv = project(mid);
                    float md = texture(BedSampler, muv).r;
                    if (md > 0.0 && length(mid) > length(positionAt(muv, md))) {
                        hi = mid;
                    } else {
                        lo = mid;
                    }
                }
                vec2 huv = project(hi);
                float hd = texture(BedSampler, huv).r;
                float behind = hd > 0.0 ? length(hi) - length(positionAt(huv, hd)) : thickness;
                float confidence = 1.0 - smoothstep(0.05, thickness, behind);
                vec2 edge = smoothstep(vec2(0.0), vec2(0.07), huv) * (1.0 - smoothstep(vec2(0.93), vec2(1.0), huv));
                float fade = edge.x * edge.y * confidence * (1.0 - smoothstep(0.6, 1.0, float(i) / float(max(steps, 1))));
                return vec4(texture(BehindSampler, huv).rgb, fade);
            }
        }
        last = q;
        t += stride;
        stride *= 1.17;
    }
    return vec4(0.0);
}

void main() {
    // One full-resolution pixel of this half-resolution texel: the first water pixel of its 2×2.
    ivec2 base = ivec2(gl_FragCoord.xy) * 2;
    ivec2 size = ivec2(Screen.xy) - 1;
    ivec2 p = ivec2(-1);
    float depth = 0.0;
    for (int i = 0; i < 4; i++) {
        ivec2 q = min(base + ivec2(i & 1, i >> 1), size);
        float d = texelFetch(DepthSampler, q, 0).r;
        if (waterAt(d, texelFetch(BedSampler, q, 0).r)) {
            p = q;
            depth = d;
            break;
        }
    }
    if (p.x < 0) {
        fragColor = vec4(0.0, 0.0, 0.0, -1.0);
        return;
    }
    vec2 uv = (vec2(p) + 0.5) / Screen.xy;
    vec3 pos = positionAt(uv, depth);
    float dist = length(pos);
    vec3 v = pos / max(dist, 1.0e-4);
    vec3 geo = surfaceNormal(p, pos);
    float footprint = dist * Params.y * 2.0 / max(abs(dot(v, geo)), 0.1);
    float crest;
    vec4 flow;
    int quality = int(Params.x + 0.5);
    // Reflections are softer than the surface: the finest layers stay out of the half-resolution pass.
    vec3 n = waveNormal(geo, pos, footprint * 1.6, waveStrength(fieldAt(pos).x), min(quality, 1), crest, flow);
    bool under = Under.x > 0.5;
    vec3 r = reflect(v, n);
    float mode = SkyZenith.w;
    vec3 col;
    if (under) {
        // From below the surface mirrors the water body (beyond the critical angle): the bed where the
        // ray finds it, else the water's own colour, lit from above. No dither here – it would sparkle.
        vec3 deep = Scatter.rgb * Absorb.w * (0.35 + 0.55 * SunColor.w);
        col = deep;
        if (mode > 1.5) {
            vec4 hit = march(pos + n * 0.03, r, 0.5, int(SkyHorizon.w + 0.5));
            col = mix(deep, hit.rgb, hit.a);
        }
    } else {
        if (r.y < 0.02 && geo.y > 0.9) {
            // A wave on level water can't really reflect below the horizon: turn it back up.
            r = normalize(vec3(r.x, 0.02 + (0.02 - r.y) * 0.25, r.z));
        }
        float sky = skyLightOf(depth);
        vec3 skyCol = skyIn(r) * (0.04 + 0.96 * sky * sky);
        col = skyCol;
        if (mode > 1.5) {
            vec4 hit = march(pos + vec3(0.0, 0.03, 0.0), r, pixelNoise(gl_FragCoord.xy), int(SkyHorizon.w + 0.5));
            col = mix(skyCol, hit.rgb, hit.a);
        } else if (mode < 0.5) {
            col = skyGradient(r) * 0.25 * (0.04 + 0.96 * sky);
        }
    }
    fragColor = vec4(col, dist);
}
#endif

#ifdef SHAFTS
// Light shafts under water: the surface's caustic pattern carried down along the sun, collected along
// the view ray, fading with the depth the light has come through and the distance to the eye.
void main() {
    ivec2 p = min(ivec2(gl_FragCoord.xy) * 2, ivec2(Screen.xy) - 1);
    vec2 uv = (vec2(p) + 0.5) / Screen.xy;
    float depth = texelFetch(DepthSampler, p, 0).r;
    vec3 far = positionAt(uv, max(depth, 1.0e-7));
    float sceneDist = depth > 0.0 ? length(far) : 64.0;
    vec3 v = normalize(positionAt(uv, 0.5));
    float reach = min(sceneDist, 40.0);
    vec3 sun = SunDir.xyz;
    // Refracted into the water, the light comes down steeper.
    vec3 into = refract(-sun, vec3(0.0, 1.0, 0.0), 1.0 / WATER_IOR);
    vec2 drift = into.xz / max(-into.y, 0.2);
    float t = CameraBlock.w;
    int steps = int(Params.x + 0.5) == 0 ? 10 : (int(Params.x + 0.5) == 1 ? 14 : 18);
    float jitter = pixelNoise(gl_FragCoord.xy);
    float sum = 0.0;
    float dt = reach / float(steps);
    for (int i = 0; i < 18; i++) {
        if (i >= steps) {
            break;
        }
        float s = (float(i) + jitter) * dt;
        vec3 x = v * s;
        float below = Under.y - x.y;
        if (below < 0.0) {
            continue;
        }
        vec2 w = worldXZ(x) - drift * below;
        // Broad, slowly wandering streaks: the caustic field blurred by the depth they've come through.
        float a = texture(WaveSampler, w / 7.0 + vec2(t * 0.013, t * 0.008)).a;
        float b = texture(WaveSampler, w / 11.0 - vec2(t * 0.009, -t * 0.011)).a;
        float shaft = smoothstep(0.1, 0.75, a * 0.6 + b * 0.6);
        sum += shaft * exp(-below * 0.07 - s * 0.06) * dt;
    }
    fragColor = vec4(sum * 0.11, 0.0, 0.0, 1.0);
}
#endif

#ifdef COMPOSITE
// Vanilla's terrain fog, exactly (render distance: cylindrical, environment: spherical).
vec3 vanillaFog(vec3 color, vec3 rel) {
    float spherical = length(rel);
    float cylindrical = max(length(rel.xz), abs(rel.y));
    float env = spherical <= FogRange.x ? 0.0 : (spherical >= FogRange.y ? 1.0 : (spherical - FogRange.x) / max(FogRange.y - FogRange.x, 1.0e-3));
    float dist = cylindrical <= FogRange.z ? 0.0 : (cylindrical >= FogRange.w ? 1.0 : (cylindrical - FogRange.z) / max(FogRange.w - FogRange.z, 1.0e-3));
    return mix(color, Fog.rgb, max(env, dist) * Fog.a);
}

// The half-resolution reflection, upsampled: the four nearest texels by how close their surface
// distance is to this pixel's.
vec3 reflectionAt(vec2 frag, float dist) {
    vec2 lp = frag * 0.5 - 0.5;
    ivec2 b = ivec2(floor(lp));
    vec2 f = lp - vec2(b);
    ivec2 limit = ivec2(Screen.zw) - 1;
    vec4 r00 = texelFetch(ReflectSampler, clamp(b, ivec2(0), limit), 0);
    vec4 r10 = texelFetch(ReflectSampler, clamp(b + ivec2(1, 0), ivec2(0), limit), 0);
    vec4 r01 = texelFetch(ReflectSampler, clamp(b + ivec2(0, 1), ivec2(0), limit), 0);
    vec4 r11 = texelFetch(ReflectSampler, clamp(b + ivec2(1, 1), ivec2(0), limit), 0);
    float tolerance = 0.03 * dist + 0.25;
    vec4 dists = vec4(r00.a, r10.a, r01.a, r11.a);
    vec4 w = vec4((1.0 - f.x) * (1.0 - f.y), f.x * (1.0 - f.y), (1.0 - f.x) * f.y, f.x * f.y)
            * step(0.0, dists) * exp(-abs(dists - dist) / tolerance) + 1.0e-5;
    return (r00.rgb * w.x + r10.rgb * w.y + r01.rgb * w.z + r11.rgb * w.w) / (w.x + w.y + w.z + w.w);
}

// Caustics on a submerged point: the field's focused light, two drifting layers, sharpest in the shallows.
float caustics(vec2 w, float below, float t, float footprint) {
    vec3 into = refract(-SunDir.xyz, vec3(0.0, 1.0, 0.0), 1.0 / WATER_IOR);
    w -= into.xz / max(-into.y, 0.2) * below;
    float a = texture(WaveSampler, w / 3.1 + vec2(t * 0.031, t * 0.017)).a;
    float b = texture(WaveSampler, w / 4.3 - vec2(t * 0.019, -t * 0.027) + 0.37).a;
    float c = (a + b) * 0.55 + a * b * 1.2;
    // The lines are thin: past where a pixel covers them, their average brightness instead of sparkle.
    c = mix(c, 0.2, smoothstep(0.004, 0.03, footprint));
    return c * exp(-below * 0.16) * smoothstep(0.0, 0.35, below);
}

// Foam texture: soft, bubbly, drifting.
float foamPattern(vec2 w, float t, float footprint) {
    float a = texture(WaveSampler, w / 1.9 + vec2(t * 0.021, -t * 0.013)).z;
    float b = texture(WaveSampler, w / 0.83 - vec2(t * 0.017, t * 0.029) + 0.5).z * layerFade(footprint, 0.83);
    return clamp(a * 0.9 + b * 0.7 + 0.5, 0.0, 1.0);
}

vec3 underwaterTerrain(vec3 col, vec3 pos, ivec2 p) {
    float dist = length(pos);
    float below = Under.y - pos.y;
    float t = CameraBlock.w;
    if (Under.z > 0.5) {
        if (below > 0.0 && dist < 48.0) {
            // Caustics on what's under the surface, on faces turned up to the light.
            ivec2 size = ivec2(Screen.xy) - 1;
            vec3 px = positionAt((vec2(min(p + ivec2(1, 0), size)) + 0.5) / Screen.xy, texelFetch(DepthSampler, min(p + ivec2(1, 0), size), 0).r);
            vec3 py = positionAt((vec2(min(p + ivec2(0, 1), size)) + 0.5) / Screen.xy, texelFetch(DepthSampler, min(p + ivec2(0, 1), size), 0).r);
            vec3 n = normalize(cross(px - pos, py - pos));
            if (dot(n, pos) > 0.0) {
                n = -n;
            }
            float facing = clamp(n.y * 0.8 + 0.2, 0.0, 1.0);
            float c = caustics(worldXZ(pos), below, t, dist * Params.y / max(facing, 0.2)) * facing * SunColor.w * Look.w
                    * (1.0 - smoothstep(24.0, 48.0, dist));
            col *= 1.0 + c * 0.55 * SunColor.rgb;
        }
        // Light through more water: dimmer and bluer with depth.
        float depthTint = clamp(max(below, 0.0) / 28.0, 0.0, 1.0);
        col *= mix(vec3(1.0), vec3(0.55, 0.75, 0.95), depthTint);
        // Shafts from the surface.
        // Blurred while upsampled: a 3×3 tent over half-resolution texels hides the march's dither.
        vec2 hp = gl_FragCoord.xy * 0.5 / Screen.zw;
        vec2 hs = 1.0 / Screen.zw;
        float shafts = 0.0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                float wgt = (2.0 - abs(float(dx))) * (2.0 - abs(float(dy)));
                shafts += texture(ShaftSampler, hp + vec2(dx, dy) * hs * 1.5).r * wgt;
            }
        }
        shafts /= 16.0;
        col += shafts * Under.w * SunColor.rgb * Scatter.rgb * 2.2 * SunColor.w;
    }
    return col;
}

void main() {
    ivec2 p = ivec2(gl_FragCoord.xy);
    vec2 uv = (vec2(p) + 0.5) / Screen.xy;
    float depth = texelFetch(DepthSampler, p, 0).r;
    float bedDepth = texelFetch(BedSampler, p, 0).r;
    bool under = Under.x > 0.5;
    if (!waterAt(depth, bedDepth)) {
        if (!under || depth <= 0.0) {
            discard;
        }
        // Underwater: the world seen through the water.
        vec3 pos = positionAt(uv, depth);
        fragColor = vec4(underwaterTerrain(texelFetch(SceneSampler, p, 0).rgb, pos, p), 1.0);
        return;
    }
    int quality = int(Params.x + 0.5);
    float t = CameraBlock.w;
    vec3 pos = positionAt(uv, depth);
    float dist = length(pos);
    vec3 v = pos / max(dist, 1.0e-4);
    vec3 geo = surfaceNormal(p, pos);
    float facing = abs(dot(v, geo));
    float footprint = dist * Params.y / max(facing, 0.1);
    float sky = skyLightOf(depth);

    // The water's colour: the lit biome tint the terrain shader left (light, time of day, biome).
    vec3 lit = texelFetch(SceneSampler, p, 0).rgb / TINT_SCALE;
    // Undo vanilla's face shading so falls and undersides aren't darker water.
    float shade = geo.y > 0.5 ? 1.0 : (geo.y < -0.5 ? 0.5 : 0.7);
    lit = min(lit / shade, vec3(1.6));
    float litMax = max(max(lit.r, lit.g), max(lit.b, 1.0e-3));
    vec3 tintN = lit / litMax;
    // Illumination without the tint (≈ white light × brightness): for foam, scattering, the bed.
    vec3 light = vec3(litMax / 0.85);

    vec2 field = fieldAt(pos);
    float openness = field.x;
    float strength = waveStrength(openness);
    float crest;
    vec4 flow;
    vec3 n = waveNormal(geo, pos, footprint, strength, quality, crest, flow);

    // Rain rings.
    float rain = 0.0;
    if (Rain.z > 0.5 && Rain.x > 0.0 && geo.y > 0.985 && !under) {
        rain = rainOnWater(pos) * Rain.x;
        if (rain > 0.01) {
            vec2 slope = rainRings(worldXZ(pos), t, clamp(rain * 1.4, 0.0, 1.0)) * (0.45 + 0.25 * Rain.y) * layerFade(footprint, 0.6);
            n = normalize(n + vec3(-slope.x, 0.0, -slope.y));
        }
    }

    float cosV = clamp(dot(-v, n), 0.0, 1.0);
    vec3 surfaceCol;
    if (!under) {
        // ---- From above ----
        float fresnel = 0.02 + 0.98 * pow(1.0 - cosV, 5.0);

        // Refraction: the waves bend the view of what's under the surface. Only their deviation from
        // the flat surface counts – the flat surface's own bend is already in the picture (and shifting
        // by it would pull in what's off screen).
        float bedDist = bedDepth > 0.0 ? length(positionAt(uv, bedDepth)) : dist + 64.0;
        float thickness = bedDist - dist;
        float reach = clamp(thickness, 0.0, 4.0);
        vec3 bent = refract(v, n, 1.0 / WATER_IOR);
        vec3 straight = refract(v, geo, 1.0 / WATER_IOR);
        vec2 ruv = uv + (project(pos + bent * reach) - project(pos + straight * reach)) * Look.y;
        ruv = clamp(ruv, vec2(0.5) / Screen.xy, vec2(1.0) - vec2(0.5) / Screen.xy);
        float rBed = texture(BedSampler, ruv).r;
        vec3 rPos = rBed > 0.0 ? positionAt(ruv, rBed) : pos + v * 96.0;
        if (rBed > 0.0 && length(rPos) < dist + 0.05) {
            // Something above the surface there (a post, the shore): don't pull it in.
            ruv = uv;
            rBed = bedDepth;
            rPos = rBed > 0.0 ? positionAt(uv, rBed) : pos + v * 96.0;
        }
        vec3 bedCol = texture(BehindSampler, ruv).rgb;
        float path = rBed > 0.0 ? length(rPos - pos) : 96.0;
        float bedBelow = max(pos.y - rPos.y, 0.0);

        // Caustics on the bed in sunlight.
        if (Look.w > 0.5 && rBed > 0.0 && SunColor.w > 0.0) {
            float c = caustics(worldXZ(rPos), bedBelow, t, length(rPos) * Params.y * 1.5) * SunColor.w * sky * (1.0 - smoothstep(32.0, 64.0, dist));
            bedCol *= 1.0 + c * 0.75 * SunColor.rgb;
        }

        // Through the water: absorbed by the way the light has come, scattered back by the water itself.
        // Absorption follows the biome's water: channels its colour is strong in pass furthest.
        vec3 sigma = Absorb.rgb * mix(vec3(1.7), vec3(0.42), tintN) / max(Look.x, 0.05);
        // Murky (unsaturated) water – swamps – scatters and absorbs more.
        float murk = smoothstep(0.35, 0.85, 1.0 - (litMax - min(min(lit.r, lit.g), lit.b)) / litMax);
        sigma *= 1.0 + murk * 1.6;
        vec3 transmit = exp(-sigma * path);
        // What the water itself sends back builds up with the path at one rate for all colours: deep
        // water is its own colour, not what's left of the red.
        float veil = 1.0 - exp(-dot(sigma, vec3(0.2, 0.5, 0.3)) * path * 1.15);
        vec3 scatterCol = (Scatter.rgb * 0.5 + tintN * 0.5) * light * Absorb.w * (0.55 + 0.45 * sky) * (1.0 + murk * 0.6);
        vec3 water = mix(bedCol * transmit, scatterCol, veil);
        // Falling water is full of air: hardly see-through, pale.
        water = mix(water, mix(scatterCol, light * 0.7, 0.45), flow.z * 0.75);

        // Reflection.
        vec3 reflected = reflectionAt(gl_FragCoord.xy, dist);
        // Sun / moon glints: a sharp highlight off the waves, and sparkles off the finest ripples.
        vec3 h = normalize(SunDir.xyz - v);
        float nh = max(dot(n, h), 0.0);
        float rough = mix(0.012, 0.06, clamp(strength, 0.0, 1.5) / 1.5) + footprint * 0.02;
        float a2 = rough * rough;
        float denom = nh * nh * (a2 - 1.0) + 1.0;
        float ggx = a2 / (3.14159 * denom * denom);
        float spec = ggx * fresnel * 0.25 / max(cosV, 0.1);
        float sparkle = pow(nh, 2400.0) * 18.0 * layerFade(footprint, 0.5) * step(0.01, strength);
        vec3 glint = SunColor.rgb * (spec + sparkle) * SunDir.w * sky;

        surfaceCol = mix(water, reflected, fresnel) + glint;

        // Foam: at shores, around things in the water, on rapids and falls, under falls, on wind-blown crests.
        if (Look.z > 0.5) {
            vec2 w = worldXZ(pos);
            float fp = foamPattern(w + n.xz * 0.15, t, footprint);
            // Lapping lines that follow the shore's contours, running in and out.
            float lap = smoothstep(0.55, 0.95, sin(bedBelow * 15.0 - t * 1.7 + fp * 3.0) * 0.5 + 0.5);
            // (Shores and things standing in the water only make sense on a level surface.)
            float level = 1.0 - flow.z;
            float shore = (1.0 - smoothstep(0.05, 0.5, bedBelow)) * max(lap * smoothstep(0.3, 0.6, fp + 0.2),
                    (1.0 - smoothstep(0.0, 0.12, bedBelow)) * fp) * level;
            float around = (1.0 - smoothstep(0.0, 0.08 + 0.1 * fp, thickness)) * smoothstep(0.3, 0.7, fp + 0.25) * level;
            float churn = field.y * smoothstep(0.25, 0.75, fp + field.y * 0.25);
            // Whitecaps: only the highest crests break, and only in a storm's wind.
            float caps = Rain.y * smoothstep(0.62, 0.85, crest / max(0.56 * strength, 0.05)) * smoothstep(0.45, 0.8, fp);
            float streaks = 0.0;
            if (flow.w > 0.5) {
                // Streaks dragged along the flow: white water down falls, a little on rapids.
                // Long and thin down a fall (stretched along the drop), shorter tongues on rapids.
                float speed = mix(1.6, 7.5, flow.z);
                vec2 fc = vec2(flow.y, flow.x - t * speed);
                vec2 scaleA = mix(vec2(1.4, 3.0), vec2(0.5, 4.5), flow.z);
                vec2 scaleB = mix(vec2(0.6, 1.3), vec2(0.22, 2.1), flow.z);
                float st = texture(WaveSampler, fc / scaleA).z * 0.8
                        + texture(WaveSampler, (fc + vec2(0.0, t * speed * 0.35)) / scaleB + vec2(0.31, 0.17)).z * 0.6 + 0.5;
                streaks = mix(smoothstep(0.62, 0.95, st) * 0.6, smoothstep(0.32, 0.66, st), flow.z);
            }
            float foam = max(max(shore, around), max(max(churn, caps), streaks));
            foam *= 1.0 - smoothstep(48.0, 96.0, dist);
            vec3 foamCol = light * mix(0.78, 0.96, sky) * (0.86 + 0.14 * cosV);
            surfaceCol = mix(surfaceCol, foamCol, clamp(foam, 0.0, 1.0) * 0.9);
        }
    } else {
        // ---- From below: Snell's window ----
        vec3 up = refract(v, n, WATER_IOR);
        float fresnel;
        vec3 above;
        if (dot(up, up) < 1.0e-6) {
            fresnel = 1.0; // total internal reflection
            above = vec3(0.0);
        } else {
            float cosT = abs(up.y);
            fresnel = 0.02 + 0.98 * pow(1.0 - cosT, 5.0);
            fresnel = mix(fresnel, 1.0, smoothstep(0.82, 0.995, 1.0 - cosV));
            vec2 ruv = uv + (project(pos + up * 4.0) - project(pos + v * 4.0)) * Look.y;
            ruv = clamp(ruv, vec2(0.5) / Screen.xy, vec2(1.0) - vec2(0.5) / Screen.xy);
            above = texture(BehindSampler, ruv).rgb;
        }
        vec3 reflected = reflectionAt(gl_FragCoord.xy, dist);
        // The window's rim burns bright with the sky gathered from the whole hemisphere.
        surfaceCol = mix(above, reflected, fresnel);
        surfaceCol += SunColor.rgb * pow(max(dot(up, SunDir.xyz), 0.0), 600.0) * 6.0 * SunDir.w * step(1.0e-6, dot(up, up));
        // The water between the eye and the surface.
        vec3 sigma = Absorb.rgb * mix(vec3(1.7), vec3(0.42), tintN) / max(Look.x, 0.05);
        vec3 transmit = exp(-sigma * dist * 0.5);
        surfaceCol = surfaceCol * transmit + Scatter.rgb * light * Absorb.w * 0.5 * (1.0 - transmit);
        surfaceCol = underwaterTerrain(surfaceCol, pos, p);
    }

    fragColor = vec4(vanillaFog(surfaceCol, pos), 1.0);
}
#endif

#endif
