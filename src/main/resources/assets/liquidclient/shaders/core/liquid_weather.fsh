#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Weather – fragment stage: analytic shapes, no textures.
//
//   RAIN    a thin streak with a soft cross-section: bright leading drop, motion-blurred trail
//   SNOW    a soft flake; large near flakes show a six-armed crystal, very near ones blur into
//           bokeh discs; some catch the light and glint
//   SPLASH  a crown of droplets thrown up in a parabola, with a thin spray sheet at the base
//   RIPPLE  expanding rings on water
//
// Alpha-blended and fogged like vanilla particles; OIT variants come from the engine's OIT_* defines.

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:oit.glsl>

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec2 local;
layout(location = 3) in vec4 tint;
layout(location = 4) in vec4 shape;
layout(location = 5) flat in int kind;

#ifndef OIT_ALPHA_ONLY
layout(location = 0) out vec4 fragColor;
#endif

const int RAIN = 0;
const int SNOW = 1;
const int SPLASH = 2;
const int RIPPLE = 3;

const float SIXTH_TURN = 1.0471976;

// x: coverage, y: extra brightness
vec2 rainStreak() {
    float across = local.x;
    float along = local.y; // 0 at the drop, 1 at the end of its trail
    float profile = 1.0 - across * across;
    profile *= profile;
    float body = smoothstep(0.0, 0.1, along) * (1.0 - smoothstep(0.5, 1.0, along));
    float core = profile * profile * (1.0 - along);
    return vec2(profile * body * (0.65 + 0.35 * (1.0 - along)), core * 0.35);
}

vec2 snowFlake() {
    float crystal = shape.x;
    float bokeh = shape.y;
    float sparkle = shape.w;
    float r = length(local);
    float body = exp(-r * r * 4.2) * (1.0 - smoothstep(0.8, 1.0, r));

    float a = body;
    if (crystal > 0.0) {
        // Six arms: distance to the nearest arm line, with small side branches.
        float angle = atan(local.y, local.x);
        float theta = angle - floor(angle / SIXTH_TURN + 0.5) * SIXTH_TURN;
        float armDistance = r * abs(sin(theta));
        float arm = (1.0 - smoothstep(0.025, 0.085, armDistance)) * (1.0 - smoothstep(0.7, 0.95, r));
        float branchPhase = abs(fract(r * 3.2 + 0.25) - 0.5);
        float branches = (1.0 - smoothstep(0.03, 0.09, abs(abs(theta) * r * 2.0 - branchPhase * 0.6)))
                * smoothstep(0.25, 0.45, r) * (1.0 - smoothstep(0.65, 0.85, r)) * 0.7;
        float hub = 1.0 - smoothstep(0.14, 0.22, r);
        float star = max(max(arm, branches), hub);
        a = mix(body, max(body * 0.35, star), crystal);
    }
    if (bokeh > 0.0) {
        float disc = (1.0 - smoothstep(0.78, 1.0, r)) * (0.5 + 0.5 * smoothstep(0.45, 0.95, r));
        a = mix(a, disc * 0.65, bokeh);
    }
    return vec2(a + sparkle * body * 0.4, sparkle * 1.6);
}

float splashCrown() {
    float age = shape.x;
    vec2 q = vec2(local.x * 0.12, local.y * 0.12); // metres: x across, y up from the ground
    float flight = age / 0.65;
    float a = 0.0;
    if (flight < 1.0) {
        for (int j = 0; j < 5; j++) {
            float fj = float(j);
            float hj = fract(sin(fj * 12.9898 + shape.y * 78.233 + shape.z * 37.719) * 43758.5453);
            float spread = (fj - 2.0) * 0.5 * (0.55 + 0.45 * hj);
            vec2 c = vec2(spread * 0.1 * sqrt(flight), (0.7 + 0.6 * hj) * (flight - flight * flight) * 0.24);
            float d = length(q - c);
            a += 1.0 - smoothstep(0.0035, 0.009, d);
        }
        a *= 1.0 - flight * flight;
    }
    // A thin sheet of spray spreading at the base.
    float reach = 0.015 + 0.09 * sqrt(age);
    float sheet = exp(-q.y * q.y / 0.00008) * (1.0 - smoothstep(reach * 0.6, reach, abs(q.x))) * (1.0 - age) * 0.8;
    return clamp(a + sheet, 0.0, 1.0) * smoothstep(0.0, 0.04, age);
}

float ripple() {
    float age = shape.x;
    float r = length(local);
    float outer = sqrt(age) * 0.88;
    float width = 0.045 + age * 0.05;
    float ring = exp(-pow((r - outer) / width, 2.0));
    float inner = sqrt(max(age - 0.18, 0.0)) * 0.62;
    float ring2 = exp(-pow((r - inner) / (width * 0.8), 2.0)) * step(0.18, age) * 0.55;
    return (ring + ring2) * pow(1.0 - age, 1.5) * (1.0 - smoothstep(0.88, 1.0, r));
}

vec4 finalColor(vec4 color) {
    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd);
    // Weather dissolves into fog rather than turning into fog-coloured streaks.
    color.a = min(color.a * (1.0 - fog * 0.75), 1.0);
    #ifdef OIT_ACCUMULATE
    color = sampleColorForAccumulation(color);
    vec4 fogColor = vec4(FogColor.rgb * color.a, FogColor.a);
    #else
    vec4 fogColor = FogColor;
    #endif
    return vec4(mix(color.rgb, fogColor.rgb, fog * fogColor.a), color.a);
}

void main() {
    float a;
    vec3 rgb = tint.rgb;
    if (kind == RAIN) {
        vec2 s = rainStreak();
        a = s.x;
        rgb *= 1.0 + s.y;
    } else if (kind == SNOW) {
        vec2 s = snowFlake();
        a = s.x;
        rgb *= 1.0 + s.y;
    } else if (kind == SPLASH) {
        a = splashCrown();
    } else {
        a = ripple();
        rgb *= 1.15;
    }
    vec4 color = vec4(rgb, a * tint.a) * ColorModulator;
    if (color.a < 0.003) {
        discard;
    }
    #ifdef OIT_ALPHA_ONLY
    executeAlphaOnlyPhase(gl_FragCoord.z, min(color.a, 1.0));
    #else
    fragColor = finalColor(color);
    #endif
}
