#version 330
#extension GL_ARB_separate_shader_objects : require

// Hand Chams – fragment stage. Nine looks; all keep the texture's shape (its transparent pixels, so
// sleeves stay sleeves and items keep their outline), get a Fresnel rim in the second colour, and
// are either emissive (glowing, Bloom catches them) or lit by the world's light.
//
//   GLASS      first person: refracts the frame behind the hand (Sampler1, copied just before the
//              hands are drawn) with colour dispersion; in the world: clear crystal, see-through
//   HOLOGRAM   scanlines, a sweeping band, flicker, glowing edges (the vertex stage glitches slices)
//   CHROMA     a rainbow flowing along the model
//   GALAXY     a window into space: nebula and twinkling stars, fixed to the screen
//   WIREFRAME  glowing quad edges over a faint fill
//   FLAT       clean solid colour with soft shading
//   PLASMA     flowing two-colour plasma
//   TINT       the texture itself, tinted
//   GRADIENT   two colours across the screen
//
// Items (held items, not the arm) are drawn solid in every look but Hologram, Wireframe and world
// Glass: an extruded item is a staircase of thin faces, and seeing through one step into the next
// reads as broken tiles. For the same reason they skip the per-texel detail and get a softer rim.
//
// Order-independent-transparency variants (OIT_* defines) compute the same alpha for the alpha-only
// stages and premultiply for accumulation.

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:oit.glsl>

uniform sampler2D Sampler0;
#ifndef OIT_ALPHA_ONLY
uniform sampler2D Sampler1;
#endif

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 viewPos;
layout(location = 3) in vec3 viewNormal;
layout(location = 4) in vec3 modelPos;
layout(location = 5) in vec2 face;
layout(location = 6) in vec3 light;
layout(location = 7) flat in ivec4 params;

#ifndef OIT_ALPHA_ONLY
layout(location = 0) out vec4 fragColor;
#endif

const int GLASS = 0;
const int HOLOGRAM = 1;
const int CHROMA = 2;
const int GALAXY = 3;
const int WIREFRAME = 4;
const int FLAT = 5;
const int PLASMA = 6;
const int TINT = 7;

vec3 hue(float h) {
    return clamp(abs(fract(h + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0) - 1.0, 0.0, 1.0);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash13(i);
    float b = hash13(i + vec3(1.0, 0.0, 0.0));
    float c = hash13(i + vec3(0.0, 1.0, 0.0));
    float d = hash13(i + vec3(1.0, 1.0, 0.0));
    float e = hash13(i + vec3(0.0, 0.0, 1.0));
    float g = hash13(i + vec3(1.0, 0.0, 1.0));
    float h = hash13(i + vec3(0.0, 1.0, 1.0));
    float k = hash13(i + vec3(1.0, 1.0, 1.0));
    return mix(mix(mix(a, b, f.x), mix(c, d, f.x), f.y), mix(mix(e, g, f.x), mix(h, k, f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += amp * noise3(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.4);
        amp *= 0.5;
    }
    return v / 0.9375;
}

float edgeGlow(float width) {
    float d = min(min(face.x, 1.0 - face.x), min(face.y, 1.0 - face.y));
    float w = max(fwidth(face.x), fwidth(face.y)) * width;
    return 1.0 - smoothstep(w * 0.5, w * 1.5, d);
}

void main() {
    vec4 tex = texture(Sampler0, texCoord);
    if (tex.a < 0.1) {
        discard;
    }
    int mode = params.z & 15;
    bool emissive = (params.z & 16) != 0;
    bool world = (params.z & 8192) != 0;
    bool item = (params.z & 16384) != 0;
    vec3 c1 = mix(vec3(1.0), hue(float(params.w & 127) / 127.0), float((params.w >> 7) & 15) / 15.0);
    float opacity = float((params.w >> 11) & 31) / 31.0;
    vec3 c2 = hue(float(params.x & 127) / 127.0);
    float refraction = float((params.x >> 7) & 31) / 31.0 * 2.0;
    float detail = float((params.x >> 12) & 15) / 15.0;
    float rim = float(params.y & 63) / 63.0 * 2.0;
    float speed = float((params.y >> 6) & 63) / 63.0 * 3.0;
    float scale = float(((params.y >> 12) & 15) + 1) * 0.25;
    float t = GameTime * 1200.0 * speed;

    vec3 n = normalize(viewNormal);
    vec3 v = normalize(-viewPos);
    float facing = clamp(dot(n, v), 0.0, 1.0);
    float fresnel = pow(1.0 - facing, 2.6);
    vec2 screen = gl_FragCoord.xy / ScreenSize;

    // Alpha first: the OIT alpha-only stages need nothing else.
    float alpha = opacity;
    float lines = 0.0;
    float sweep = 0.0;
    float edges = 0.0;
    if (mode == HOLOGRAM) {
        lines = 0.5 + 0.5 * sin(gl_FragCoord.y * 1.4 - t * 7.0);
        float band = fract(screen.y * 0.8 - t * 0.35);
        sweep = smoothstep(0.0, 0.05, band) * (1.0 - smoothstep(0.05, 0.16, band));
        edges = edgeGlow(2.2);
        alpha *= clamp(0.22 + 0.5 * fresnel + 0.25 * lines + edges + sweep * 0.6, 0.0, 1.0);
    } else if (mode == WIREFRAME) {
        edges = edgeGlow(2.4);
        alpha *= clamp(0.1 + edges, 0.0, 1.0);
    } else if (mode == GLASS && world) {
        alpha *= clamp(0.28 + 0.62 * fresnel, 0.0, 1.0);
    } else if (item) {
        alpha = 1.0;
    }
    if (item) {
        detail = 0.0;
        rim *= 0.4;
    }

    #ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, clamp(alpha * ColorModulator.a, 0.0, 1.0));
    #else
    vec3 l = normalize(vec3(-0.45, 0.8, 0.4));
    float diffuse = 0.42 + 0.58 * max(dot(n, l), 0.0);
    vec3 texColor = tex.rgb * vertexColor.rgb;
    float texLuma = dot(texColor, vec3(0.2126, 0.7152, 0.0722));
    vec3 rgb;
    vec3 rimColor = c2;
    bool lit = !emissive;

    if (mode == GLASS) {
        float highlight = pow(max(dot(reflect(-l, n), v), 0.0), 36.0);
        if (world) {
            // Crystal: the world shows through by blending; tint, highlight and a bright rim.
            rgb = c1 * 0.5 + vec3(highlight * 1.2);
        } else {
            // The world behind, bent by the surface and split slightly by wavelength.
            vec2 bend = n.xy * 0.045 * refraction;
            vec3 behind;
            behind.r = texture(Sampler1, clamp(screen - bend * 1.0, 0.0, 1.0)).r;
            behind.g = texture(Sampler1, clamp(screen - bend * 1.08, 0.0, 1.0)).g;
            behind.b = texture(Sampler1, clamp(screen - bend * 1.16, 0.0, 1.0)).b;
            vec2 px = 1.5 / ScreenSize;
            vec3 frost = texture(Sampler1, clamp(screen - bend + px, 0.0, 1.0)).rgb + texture(Sampler1, clamp(screen - bend - px, 0.0, 1.0)).rgb;
            behind = mix(behind, frost * 0.5, 0.35);
            rgb = behind * mix(vec3(1.0), c1, 0.38) * 1.04 + vec3(highlight * 0.85);
        }
        rgb += vec3(0.12) * smoothstep(0.55, 0.0, abs(dot(n, normalize(vec3(1.0, 1.0, 0.0)))));
        rimColor = mix(vec3(1.0), c2, 0.5);
        lit = false;
    } else if (mode == HOLOGRAM) {
        float flicker = 0.86 + 0.14 * sin(t * 37.0) * sin(t * 13.0 + 1.0);
        rgb = c1 * (0.3 + 0.35 * lines + 0.9 * sweep + 1.3 * edges) * flicker;
        rimColor = c1;
    } else if (mode == CHROMA) {
        float h = fract(dot(modelPos, vec3(0.6, 1.0, 0.4)) * scale * 0.6 - t * 0.25);
        rgb = hue(h) * diffuse;
        rimColor = hue(h + 0.15);
    } else if (mode == GALAXY) {
        vec2 sp = screen * vec2(ScreenSize.x / ScreenSize.y, 1.0) * 3.0 * scale;
        float nebula = fbm(vec3(sp * 1.4, t * 0.03));
        float nebula2 = fbm(vec3(sp * 3.1 + 5.0, t * 0.05));
        rgb = mix(c1 * 0.08, c1 * 0.75, pow(nebula, 2.2)) + c2 * pow(nebula2, 3.0) * 0.8;
        vec2 cell = floor(sp * 40.0);
        float star = step(0.985, hash12(cell));
        float twinkle = 0.55 + 0.45 * sin(t * 3.0 + hash12(cell + 3.1) * 40.0);
        vec2 within = fract(sp * 40.0) - 0.5;
        rgb += vec3(star * twinkle * smoothstep(0.35, 0.0, length(within)) * 1.4);
        lit = false;
    } else if (mode == WIREFRAME) {
        float pulse = 0.75 + 0.25 * sin(t * 3.0 + modelPos.y * 6.0);
        rgb = c1 * (0.06 + edges * 1.6 * pulse);
        lit = false;
    } else if (mode == FLAT) {
        rgb = c1 * diffuse;
    } else if (mode == PLASMA) {
        vec3 p = modelPos * scale * 3.0;
        float f = fbm(p + vec3(0.0, -t * 0.9, t * 0.2));
        float g = fbm(p * 2.0 + vec3(t * 0.4, f * 2.0, 0.0));
        float heat = smoothstep(0.25, 0.85, f * 0.7 + g * 0.45);
        rgb = mix(c2 * 0.15, mix(c1, vec3(1.0, 0.96, 0.85), heat * heat), heat) * (0.7 + 0.5 * heat);
        lit = false;
    } else if (mode == TINT) {
        rgb = texColor * c1 * 1.35 * diffuse;
        detail = 0.0;
    } else {
        float g = clamp(screen.y * 1.25 - 0.1 + sin(t * 0.8 + screen.x * 3.0) * 0.08, 0.0, 1.0);
        rgb = mix(c1, c2, smoothstep(0.0, 1.0, g)) * diffuse;
    }

    // Detail: the texture's shading shows through the effect.
    rgb = mix(rgb, rgb * (0.35 + 1.1 * texLuma), detail);
    rgb += rimColor * fresnel * rim * (mode == GLASS ? 0.6 : 0.9);
    if (lit) {
        rgb *= light;
    }
    vec4 color = vec4(rgb, clamp(alpha, 0.0, 1.0)) * ColorModulator;
    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    #endif
    fragColor = color;
    #endif
}
