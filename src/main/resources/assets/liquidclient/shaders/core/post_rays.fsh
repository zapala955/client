#version 330
#extension GL_ARB_separate_shader_objects : require

// Sun Rays (PostFx) – crepuscular rays and a lens flare, after the world and before the hand.
//
//   MASK         half resolution: the sky around the light source (depth cleared to 0 = sky,
//                reverse-Z), weighted by how bright the sky is there – so terrain, trees and
//                clouds (vanilla's write depth, the Skybox's are darker) cut shadows into the light
//   BLUR_FINE    radial blur towards the light over a short span ...
//   BLUR_COARSE  ... then over the whole ray length: N² taps together, without banding
//   COMPOSITE    added onto the frame in the light's colour, with the flare when the light is visible

uniform sampler2D InSampler;
#if defined(MASK) || defined(COMPOSITE)
uniform sampler2D DepthSampler;
#endif

// Mirrors PostFx#writeRays (std140, mat4 + 5 x vec4).
layout(std140) uniform LiquidRays {
    mat4 InvViewProj; // NDC -> camera-relative position
    vec4 Light;       // xy light position on screen (uv), z visibility 0..1, w aspect (width / height)
    vec4 LightDir;    // xyz towards the light, w source tightness (exponent)
    vec4 RayColor;    // rgb colour × strength, w flare strength
    vec4 Blur;        // x ray length (fraction of the way to the light), y decay per coarse step, z taps per pass, w flare style
    vec4 Misc;        // x time (s), yzw unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float noise(vec2 pixel) {
    return fract(52.9829189 * fract(dot(pixel, vec2(0.06711056, 0.00583715))));
}

#ifdef MASK
void main() {
    if (texture(DepthSampler, texCoord).r > 0.0) {
        fragColor = vec4(0.0);
        return;
    }
    // Any depth between the planes gives the view direction (depth 0 itself is at infinity).
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = 0.5;
    #else
    float z = 0.0;
    #endif
    vec4 world = InvViewProj * vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec3 dir = normalize(world.xyz / world.w);
    float facing = max(dot(dir, LightDir.xyz), 0.0);
    float source = pow(facing, LightDir.w) + 0.12 * pow(facing, LightDir.w * 0.125);
    float sky = dot(texture(InSampler, texCoord).rgb, vec3(0.2126, 0.7152, 0.0722));
    fragColor = vec4(vec3(source * (0.25 + sky)), 1.0);
}
#endif

#if defined(BLUR_FINE) || defined(BLUR_COARSE)
void main() {
    int taps = int(Blur.z + 0.5);
    vec2 toLight = Light.xy - texCoord;
    #ifdef BLUR_FINE
    vec2 stride = toLight * Blur.x / (Blur.z * Blur.z);
    float decay = 1.0;
    #else
    vec2 stride = toLight * Blur.x / Blur.z;
    float decay = Blur.y;
    #endif
    vec2 uv = texCoord + stride * noise(gl_FragCoord.xy);
    vec3 sum = vec3(0.0);
    float weight = 1.0;
    for (int i = 0; i < 32; i++) {
        if (i >= taps) {
            break;
        }
        // Nothing beyond the screen edge (clamped edge texels would smear into streaks).
        vec2 inside = step(vec2(0.0), uv) * step(uv, vec2(1.0));
        sum += texture(InSampler, uv).rgb * weight * inside.x * inside.y;
        weight *= decay;
        uv += stride;
    }
    fragColor = vec4(sum / Blur.z, 1.0);
}
#endif

#ifdef COMPOSITE
const float GHOST_AT[6] = float[](-0.35, -0.62, -1.05, 0.42, 0.68, -1.55);
const float GHOST_SIZE[6] = float[](0.045, 0.09, 0.03, 0.06, 0.022, 0.14);
const float GHOST_GAIN[6] = float[](0.10, 0.05, 0.12, 0.06, 0.10, 0.03);

vec3 spectrum(float x) {
    return clamp(abs(fract(x + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0) - 1.0, 0.0, 1.0);
}

// How much of the light's disc is uncovered: depth taps on a small ring around it.
float visibility() {
    vec2 radius = vec2(0.012 / Light.w, 0.012);
    float open = step(texture(DepthSampler, Light.xy).r, 0.0);
    for (int i = 0; i < 12; i++) {
        float a = float(i) * 0.5235988;
        vec2 uv = Light.xy + vec2(cos(a), sin(a)) * radius;
        vec2 inside = step(vec2(0.0), uv) * step(uv, vec2(1.0));
        open += step(texture(DepthSampler, clamp(uv, 0.0, 1.0)).r, 0.0) * inside.x * inside.y;
    }
    return open / 13.0;
}

vec3 flare(vec2 uv, vec3 tint) {
    vec2 aspect = vec2(Light.w, 1.0);
    vec2 p = (uv - 0.5) * aspect;
    vec2 s = (Light.xy - 0.5) * aspect;
    vec2 d = p - s;
    float r = length(d);
    float angle = atan(d.y, d.x);

    // Glare: a soft core with faint starburst spokes.
    float spokes = 0.55 + 0.45 * pow(abs(cos(angle * 3.0 + 0.35)), 12.0) + 0.3 * pow(abs(sin(angle * 7.0)), 30.0);
    vec3 c = tint * (exp(-r * 22.0) * 0.6 + exp(-r * 6.0) * 0.08) * spokes;

    // Ghosts: soft discs mirrored through the centre, each tinted along the spectrum.
    for (int i = 0; i < 6; i++) {
        float gd = length(p - s * GHOST_AT[i]);
        float size = GHOST_SIZE[i];
        float disc = smoothstep(size, size * 0.55, gd);
        float ring = exp(-pow((gd - size * 0.92) / (size * 0.12), 2.0)) * 0.6;
        c += mix(spectrum(0.08 + float(i) * 0.17), tint, 0.35) * (disc + ring) * GHOST_GAIN[i];
    }
    // Halo: a faint rainbow ring around the light.
    float halo = exp(-pow((r - 0.3) / 0.018, 2.0));
    c += spectrum(clamp((r - 0.27) / 0.06, 0.0, 1.0) * 0.8) * halo * 0.035;
    // Cinematic: a long anamorphic streak through the light.
    if (Blur.w > 1.5) {
        c += vec3(0.5, 0.68, 1.0) * exp(-abs(d.y) * 180.0) * exp(-abs(d.x) * 1.8) * 0.45;
    }
    return c;
}

void main() {
    vec3 color = texture(InSampler, texCoord).rgb * RayColor.rgb;
    if (RayColor.w > 0.0 && Light.z > 0.0) {
        float seen = visibility() * Light.z;
        if (seen > 0.0) {
            vec3 tint = RayColor.rgb / max(max(RayColor.r, max(RayColor.g, RayColor.b)), 1.0e-3);
            color += flare(texCoord, tint) * seen * RayColor.w;
        }
    }
    // Dither the faint end of the gradient against banding.
    color += (noise(gl_FragCoord.xy) - 0.5) / 255.0 * step(0.004, max(color.r, max(color.g, color.b)));
    fragColor = vec4(max(color, vec3(0.0)), 0.0);
}
#endif
