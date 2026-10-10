#version 330
#extension GL_ARB_separate_shader_objects : require

// Ambient particles (Particles module) – fragment stage.
//
// Every procedural shape is an analytic distance field, so particles stay crisp at any size and
// distance: edges are anti-aliased over one device pixel (fwidth), and a soft light halo around the
// shape follows the "Glow" setting.
//
// UV0 encoding (AmbientParticle#extract):
//   u < 3  : texture-atlas uv (sprite shapes: music notes, runes)
//   else   : u = 4·(shape + 1) + 1.5 … + 2.5,   v = 4·(glow·4 + variant) + 1.5 … + 2.5
// The 1.5 margins keep floor() stable in helper invocations just outside the quad, so the
// derivatives (taken once, in uniform control flow) never jump between shapes.
//
// ADDITIVE builds are light: src·a + dst, fading into fog like vanilla's lightning, with energy
// above 1 brightening the colour instead of being clipped. Other builds alpha-blend and fog towards
// the fog colour like vanilla particles. Both have OIT variants (OIT_* defines from the engine).

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:oit.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec2 texCoord0;
layout(location = 3) in vec4 vertexColor;

#ifndef OIT_ALPHA_ONLY
layout(location = 0) out vec4 fragColor;
#endif

// Mirrors ParticleShape.java.
const int ORB = 0;
const int SPARKLE = 1;
const int STAR = 2;
const int BOKEH = 3;
const int RING = 4;
const int SNOWFLAKE = 5;
const int PETAL = 6;
const int LEAF = 7;
const int HEART = 8;
const int DIAMOND = 9;
const int CONFETTI = 10;
const int BUBBLE = 11;
const int FLARE = 12;
// Trails: STREAK + head thickness level (0..15, thickness = (level + 1) / 64 of the half length).
const int STREAK = 16;

const float QUARTER_PI = 0.78539816;

// =============================================================================================
// Distance fields (Inigo Quilez). Negative inside.
// =============================================================================================
float dot2(vec2 v) {
    return dot(v, v);
}

vec2 rotate(vec2 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

float sdSegment(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p) - b;
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
}

float sdHexagon(vec2 p, float r) {
    const vec3 k = vec3(-0.866025404, 0.5, 0.577350269);
    p = abs(p);
    p -= 2.0 * min(dot(k.xy, p), 0.0) * k.xy;
    p -= vec2(clamp(p.x, -k.z * r, k.z * r), r);
    return length(p) * sign(p.y);
}

float sdStar5(vec2 p, float r, float rf) {
    const vec2 k1 = vec2(0.809016994375, -0.587785252292);
    const vec2 k2 = vec2(-k1.x, k1.y);
    p.x = abs(p.x);
    p -= 2.0 * max(dot(k1, p), 0.0) * k1;
    p -= 2.0 * max(dot(k2, p), 0.0) * k2;
    p.x = abs(p.x);
    p.y -= r;
    vec2 ba = rf * vec2(-k1.y, k1.x) - vec2(0.0, 1.0);
    float h = clamp(dot(p, ba) / dot(ba, ba), 0.0, r);
    return length(p - ba * h) * sign(p.y * ba.x - p.x * ba.y);
}

// Point at the origin, lobes up to y ≈ 1.1, x within ±0.6.
float sdHeart(vec2 p) {
    p.x = abs(p.x);
    if (p.y + p.x > 1.0) {
        return sqrt(dot2(p - vec2(0.25, 0.75))) - sqrt(2.0) / 4.0;
    }
    return sqrt(min(dot2(p - vec2(0.0, 1.0)), dot2(p - 0.5 * max(p.x + p.y, 0.0)))) * sign(p.x - p.y);
}

float sdVesica(vec2 p, float r, float d) {
    p = abs(p);
    float b = sqrt(r * r - d * d);
    return ((p.y - b) * d > p.x * b) ? length(p - vec2(0.0, b)) : length(p - vec2(-d, 0.0)) - r;
}

float ndot(vec2 a, vec2 b) {
    return a.x * b.x - a.y * b.y;
}

float sdRhombus(vec2 p, vec2 b) {
    p = abs(p);
    float h = clamp(ndot(b - 2.0 * p, b) / dot(b, b), -1.0, 1.0);
    float d = length(p - 0.5 * b * vec2(1.0 - h, 1.0 + h));
    return d * sign(p.x * b.y + p.y * b.x - b.x * b.y);
}

// Anti-aliased coverage of a distance field, one device pixel wide.
float fill(float d, float px) {
    return clamp(0.5 - d / px, 0.0, 1.0);
}

// Light falling off outside a shape.
float halo(float d, float falloff) {
    return exp(-max(d, 0.0) * falloff);
}

// =============================================================================================
// Shapes: x = intensity, y = white-hot amount, z = shade (darkens the colour), w = iridescence
// =============================================================================================
vec4 orb(vec2 p, float glow) {
    float r2 = dot2(p);
    float core = exp(-r2 * 22.0);
    float body = exp(-r2 * 7.0);
    float aura = exp(-r2 * 2.6) * (1.0 - smoothstep(0.55, 1.0, sqrt(r2)));
    return vec4(core * 0.9 + body * 0.55 + aura * glow * 0.5, core * 0.75, 1.0, 0.0);
}

vec4 sparkle(vec2 p, float px, float glow, int variant) {
    if ((variant & 1) == 1) {
        p = rotate(p, QUARTER_PI);
    }
    // Long rays thin out towards their tips; shorter diagonal rays make it read as a lens star.
    vec2 q = max(abs(p) - px * 0.5, 0.0);
    float rays = exp(-q.x * (18.0 + 90.0 * q.y)) * (1.0 - smoothstep(0.35, 0.95, q.y))
            + exp(-q.y * (18.0 + 90.0 * q.x)) * (1.0 - smoothstep(0.35, 0.95, q.x));
    vec2 d = max(abs(rotate(p, QUARTER_PI)) - px * 0.5, 0.0);
    float diag = exp(-d.x * (30.0 + 140.0 * d.y)) * (1.0 - smoothstep(0.12, 0.5, d.y))
            + exp(-d.y * (30.0 + 140.0 * d.x)) * (1.0 - smoothstep(0.12, 0.5, d.x));
    float r2 = dot2(p);
    float core = exp(-r2 * 40.0);
    float aura = exp(-r2 * 5.0) * (1.0 - smoothstep(0.5, 1.0, sqrt(r2)));
    return vec4(rays + diag * 0.45 + core + aura * glow * 0.45, core * 0.9 + rays * 0.35, 1.0, 0.0);
}

vec4 star(vec2 p, float px, float glow) {
    float d = sdStar5(p - vec2(0.0, -0.04), 0.66, 0.46) - 0.04;
    float body = fill(d, px);
    float inner = clamp(-d * 3.5, 0.0, 1.0);
    float aura = halo(d, 7.0) * (1.0 - body) * glow * 0.6;
    return vec4(body * 0.95 + aura, inner * 0.6, 1.0, 0.0);
}

vec4 bokeh(vec2 p, float px, float glow, int variant) {
    const float R = 0.8;
    // Variant 1: six aperture blades instead of a round iris.
    float d = (variant & 1) == 1 ? sdHexagon(rotate(p, 0.2617994), R * 0.93) : length(p) - R;
    float disc = clamp(0.5 - d / (px + 0.035), 0.0, 1.0);
    float rim = exp(-abs(d + 0.05) * 26.0) * disc;
    // Real lens bokeh is brighter towards the rim.
    float body = 0.32 + 0.2 * smoothstep(-R, 0.0, d);
    float aura = halo(d, 9.0) * (1.0 - disc) * glow * 0.5;
    return vec4(disc * body + rim * 0.55 + aura, rim * 0.18, 1.0, 0.0);
}

vec4 ring(vec2 p, float px, float glow) {
    float r = length(p);
    float band = fill(abs(r - 0.6) - 0.045, px);
    float aura = exp(-abs(r - 0.6) * 9.0) * (1.0 - smoothstep(0.85, 1.0, r)) * glow * 0.6;
    return vec4(band + aura, band * 0.35, 1.0, 0.0);
}

vec4 snowflake(vec2 p, float px, float glow, int variant) {
    float r = length(p);
    // Fold into one 60° sector, mirrored, so every arm and branch is drawn once.
    const float SECTOR = 1.04719755;
    float a = atan(p.y, p.x) + float(variant & 1) * 0.5235988;
    a = mod(a + SECTOR * 0.5, SECTOR) - SECTOR * 0.5;
    vec2 q = r * vec2(cos(a), abs(sin(a)));
    float d = sdSegment(q, vec2(0.0), vec2(0.8, 0.0)) - 0.05;
    d = min(d, sdSegment(q, vec2(0.36, 0.0), vec2(0.47, 0.19)) - 0.038);
    d = min(d, sdSegment(q, vec2(0.58, 0.0), vec2(0.655, 0.13)) - 0.032);
    d = min(d, r - 0.13);
    float body = fill(d, px);
    float aura = exp(-r * r * 4.0) * (1.0 - smoothstep(0.7, 1.0, r)) * glow * 0.3;
    return vec4(body * 0.95 + aura, body * 0.25, 1.0, 0.0);
}

vec4 petal(vec2 p, float px, float glow) {
    // A narrowed heart: the notch becomes the petal's tip, the point its base.
    vec2 h = vec2(p.x * 1.4, p.y + 0.8) / 1.5;
    float d = sdHeart(h) * (1.5 / 1.4);
    float body = fill(d, px);
    float base = 1.0 - smoothstep(-0.9, 0.15, p.y);
    float shade = 0.84 + 0.16 * smoothstep(-0.6, 0.7, p.y);
    float aura = halo(d, 8.0) * (1.0 - body) * glow * 0.35;
    return vec4(body + aura, body * base * 0.55, shade, 0.0);
}

vec4 leaf(vec2 p, float px, float glow, int variant) {
    vec2 q = p - vec2(0.0, 0.08);
    float d = sdVesica(q, 0.9, (variant & 1) == 1 ? 0.42 : 0.5);
    float stem = sdSegment(p, vec2(0.0, -0.64), vec2(0.05, -0.92)) - 0.035;
    float body = fill(min(d, stem), px);
    // Midrib and chevron veins, only once the leaf is large enough not to shimmer.
    float detail = 1.0 - smoothstep(0.025, 0.07, px);
    float rib = exp(-abs(q.x) * 70.0) * step(-0.72, q.y);
    float veins = smoothstep(0.42, 0.5, abs(fract((q.y - abs(q.x) * 0.8) * 3.5) - 0.5)) * smoothstep(0.03, 0.1, abs(q.x));
    float lit = 0.88 + 0.12 * smoothstep(0.4, -0.4, q.x);
    float shade = lit * (1.0 - detail * (0.36 * rib + 0.2 * veins));
    float aura = halo(d, 8.0) * (1.0 - body) * glow * 0.3;
    return vec4(body + aura, 0.0, shade, 0.0);
}

vec4 heart(vec2 p, float px, float glow) {
    vec2 h = vec2(p.x, p.y + 0.73) / 1.33;
    float d = sdHeart(h) * 1.33;
    float body = fill(d, px);
    float gloss = exp(-dot2(p - vec2(-0.3, 0.32)) * 22.0) * body;
    float aura = halo(d, 6.0) * (1.0 - body) * glow * 0.6;
    return vec4(body * 0.9 + aura, gloss * 0.8, 1.0, 0.0);
}

vec4 diamond(vec2 p, float px, float glow) {
    float d = sdRhombus(p, vec2(0.5, 0.8));
    float body = fill(d, px);
    // Facets lit from the upper left, a brighter table in the middle.
    float facet = (p.x < 0.0 ? 0.32 : 0.0) + (p.y > 0.0 ? 0.22 : 0.0);
    float table = fill(sdRhombus(p, vec2(0.22, 0.36)), px) * 0.25;
    float aura = halo(d, 7.0) * (1.0 - body) * glow * 0.55;
    return vec4(body * (0.55 + facet) + aura, body * (facet * 0.8 + table), 1.0, 0.0);
}

vec4 confetti(vec2 p, float px, int variant) {
    float d = (variant & 1) == 1 ? length(p) - 0.55 : sdBox(p, vec2(0.72, 0.4)) - 0.06;
    float body = fill(d, px);
    float shade = 0.85 + 0.15 * smoothstep(-0.4, 0.4, p.y);
    return vec4(body, body * 0.08, shade, 0.0);
}

vec4 bubble(vec2 p, float px, float glow) {
    float r = length(p);
    float d = r - 0.8;
    float inside = fill(d, px);
    float rim = fill(abs(d + 0.03) - 0.03, px);
    float fresnel = inside * smoothstep(0.35, 0.8, r) * 0.28;
    float spec = exp(-dot2(p - vec2(-0.3, 0.34)) * 26.0) * inside;
    float spec2 = exp(-dot2(p - vec2(0.3, -0.36)) * 70.0) * inside * 0.4;
    float aura = halo(d, 8.0) * (1.0 - inside) * glow * 0.3;
    float i = max(rim * 0.75, inside * 0.06 + fresnel) + spec * 0.85 + spec2 + aura;
    return vec4(i, spec + rim * 0.25, 1.0, (rim + fresnel) * 0.55);
}

vec4 flare(vec2 p, float px, float glow) {
    float r2 = dot2(p);
    float core = exp(-r2 * 24.0);
    float body = exp(-r2 * 8.0);
    // Anamorphic streak: horizontal on screen, like a cinema lens.
    float y = max(abs(p.y) - px * 0.5, 0.0);
    float streak = exp(-y * 40.0) * (1.0 - smoothstep(0.1, 1.0, abs(p.x)));
    float aura = exp(-r2 * 3.0) * (1.0 - smoothstep(0.55, 1.0, sqrt(r2)));
    return vec4(core * 0.9 + body * 0.45 + streak * (0.35 + 0.4 * glow) + aura * glow * 0.4, core * 0.8 + streak * 0.3, 1.0, 0.0);
}

// Motion trail, aligned with the motion on screen: the head (the particle) at +x, thinning and fading
// towards the tail. Never thinner than a pixel; energy is conserved when it's clamped.
vec4 streak(vec2 p, float px, float glow, float thickness) {
    float along = p.x * 0.5 + 0.5;
    float w = thickness * (0.15 + 0.85 * along);
    float wp = max(w, px * 0.75);
    float line = (1.0 - smoothstep(wp, wp + px, abs(p.y))) * (w / wp);
    float bloom = exp(-abs(p.y) / (wp * 3.0)) * (0.2 + 0.4 * glow);
    float fade = along * along;
    return vec4((line * 0.9 + bloom) * fade, line * fade * 0.5, 1.0, 0.0);
}

vec4 shapeField(int shape, vec2 p, float px, float glow, int variant) {
    if (shape >= STREAK) {
        return streak(p, px, glow, float(shape - STREAK + 1) / 64.0);
    }
    switch (shape) {
        case SPARKLE: return sparkle(p, px, glow, variant);
        case STAR: return star(p, px, glow);
        case BOKEH: return bokeh(p, px, glow, variant);
        case RING: return ring(p, px, glow);
        case SNOWFLAKE: return snowflake(p, px, glow, variant);
        case PETAL: return petal(p, px, glow);
        case LEAF: return leaf(p, px, glow, variant);
        case HEART: return heart(p, px, glow);
        case DIAMOND: return diamond(p, px, glow);
        case CONFETTI: return confetti(p, px, variant);
        case BUBBLE: return bubble(p, px, glow);
        case FLARE: return flare(p, px, glow);
        default: return orb(p, glow);
    }
}

// =============================================================================================
// Output
// =============================================================================================
vec4 calculateFinalColor(vec4 color) {
    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    #ifdef ADDITIVE
    float energy = color.a * (1.0 - fog);
    color = vec4(color.rgb * max(energy, 1.0), min(energy, 1.0));
    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    #endif
    return color;
    #else
    color.a = min(color.a, 1.0);
    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    vec4 fogColor = vec4(FogColor.rgb * color.a, FogColor.a);
    #else
    vec4 fogColor = FogColor;
    #endif
    return vec4(mix(color.rgb, fogColor.rgb, fog * fogColor.a), color.a);
    #endif
}

void main() {
    vec2 uv = texCoord0;
    // Decode and take derivatives before any branching.
    float cell = floor(uv.x * 0.25);
    float row = floor(uv.y * 0.25);
    vec2 p = vec2((uv.x - cell * 4.0 - 1.5) * 2.0 - 1.0, 1.0 - (uv.y - row * 4.0 - 1.5) * 2.0);
    float px = max(length(fwidth(p)) * 0.75, 1.0e-4);
    vec4 tex = texture(Sampler0, uv);

    vec4 color;
    if (uv.x < 3.0) {
        color = tex * vertexColor;
    } else {
        int code = int(row);
        int shape = int(cell) - 1;
        vec4 s = shapeField(shape, p, px, float(code >> 2) / 15.0, code & 3);
        #ifndef ADDITIVE
        // Painted (alpha-blended) light sources keep their colour instead of a white-hot core.
        if (shape == ORB || shape == FLARE) {
            s.y *= 0.3;
        }
        #endif
        // Nothing may touch the quad's border, or its square outline would show.
        float edge = 1.0 - smoothstep(0.88, 1.0, max(abs(p.x), abs(p.y)));
        vec3 rgb = mix(vertexColor.rgb * s.z, vec3(1.0), clamp(s.y, 0.0, 1.0));
        if (s.w > 0.0) {
            vec3 film = 0.5 + 0.5 * cos(6.2831853 * (atan(p.y, p.x) * 0.1591549 + length(p) * 0.6 + vec3(0.0, 0.33, 0.67)));
            rgb = mix(rgb, film, clamp(s.w, 0.0, 1.0));
        }
        color = vec4(rgb, s.x * edge * vertexColor.a);
    }
    color *= ColorModulator;
    if (color.a < 0.004) {
        discard;
    }
    #ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, min(color.a, 1.0));
    #else
    fragColor = calculateFinalColor(color);
    #endif
}
