#version 330
#extension GL_ARB_separate_shader_objects : require

// Bloom (PostFx#bloom) – a physically-styled mip chain in RGBA16F:
//   PREFILTER  : frame -> 1/2 res, soft-knee threshold, Karis average (no flickering fireflies),
//                converted to linear light
//   DOWNSAMPLE : 13-tap filter (Jimenez, "Next Generation Post Processing in Call of Duty")
//   UPSAMPLE   : 3x3 tent, added onto the next larger level (additive blend), optionally stretched
//                horizontally for anamorphic streaks
// The grade pass adds level 1 to the frame.

uniform sampler2D InSampler;

// Mirrors PostFx#writeBloom.
layout(std140) uniform LiquidBloom {
    vec4 BloomParams; // x = threshold, y = knee (0..1), z = upsample radius (texels), w = anamorphic stretch (1 = round)
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float luma(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

vec3 tap(vec2 uv) {
    return texture(InSampler, uv).rgb;
}

#if defined(PREFILTER) || defined(DOWNSAMPLE)
vec3 karis(vec3 a, vec3 b, vec3 c, vec3 d) {
    vec3 avg = (a + b + c + d) * 0.25;
    #ifdef PREFILTER
    return avg / (1.0 + luma(avg));
    #else
    return avg;
    #endif
}

vec3 downsample(vec2 uv, vec2 t) {
    vec3 a = tap(uv + t * vec2(-2.0, 2.0));
    vec3 b = tap(uv + t * vec2(0.0, 2.0));
    vec3 c = tap(uv + t * vec2(2.0, 2.0));
    vec3 d = tap(uv + t * vec2(-2.0, 0.0));
    vec3 e = tap(uv);
    vec3 f = tap(uv + t * vec2(2.0, 0.0));
    vec3 g = tap(uv + t * vec2(-2.0, -2.0));
    vec3 h = tap(uv + t * vec2(0.0, -2.0));
    vec3 i = tap(uv + t * vec2(2.0, -2.0));
    vec3 j = tap(uv + t * vec2(-1.0, 1.0));
    vec3 k = tap(uv + t * vec2(1.0, 1.0));
    vec3 l = tap(uv + t * vec2(-1.0, -1.0));
    vec3 m = tap(uv + t * vec2(1.0, -1.0));
    vec3 result = karis(j, k, l, m) * 0.5;
    result += karis(a, b, d, e) * 0.125;
    result += karis(b, c, e, f) * 0.125;
    result += karis(d, e, g, h) * 0.125;
    result += karis(e, f, h, i) * 0.125;
    #ifdef PREFILTER
    // Undo the Karis weighting's average darkening.
    result /= max(1.0 - luma(result), 0.05);
    #endif
    return result;
}
#endif

#ifdef PREFILTER
vec3 threshold(vec3 c) {
    float brightness = max(c.r, max(c.g, c.b));
    float knee = BloomParams.x * BloomParams.y + 1.0e-4;
    float soft = clamp(brightness - BloomParams.x + knee, 0.0, 2.0 * knee);
    soft = soft * soft / (4.0 * knee);
    float contribution = max(soft, brightness - BloomParams.x) / max(brightness, 1.0e-4);
    return c * contribution;
}
#endif

void main() {
    vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
    #if defined(PREFILTER)
    vec3 c = threshold(downsample(texCoord, texel));
    fragColor = vec4(pow(max(c, 0.0), vec3(2.2)), 1.0);
    #elif defined(DOWNSAMPLE)
    fragColor = vec4(downsample(texCoord, texel), 1.0);
    #else
    vec2 d = texel * BloomParams.z * vec2(BloomParams.w, 1.0 / BloomParams.w);
    vec3 s = tap(texCoord + d * vec2(-1.0, -1.0)) + tap(texCoord + d * vec2(1.0, -1.0))
            + tap(texCoord + d * vec2(-1.0, 1.0)) + tap(texCoord + d * vec2(1.0, 1.0));
    s += 2.0 * (tap(texCoord + d * vec2(0.0, -1.0)) + tap(texCoord + d * vec2(0.0, 1.0))
            + tap(texCoord + d * vec2(-1.0, 0.0)) + tap(texCoord + d * vec2(1.0, 0.0)));
    s += 4.0 * tap(texCoord);
    fragColor = vec4(s / 16.0, 1.0);
    #endif
}
