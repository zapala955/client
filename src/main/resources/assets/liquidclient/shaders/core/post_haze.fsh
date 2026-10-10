#version 330
#extension GL_ARB_separate_shader_objects : require

// Atmosphere – aerial-perspective haze and valley mist (PostFx). Each pixel's position is rebuilt
// from the depth buffer, the fog is integrated along the view ray, and the result is alpha-blended
// over the frame (alpha = 1 - transmittance). Sky pixels (depth cleared to 0, reverse-Z) are left to
// the sky and vanilla's fog.
//
// Drawn twice: over the world right before particles (so particles, which write no depth, are drawn
// on top un-fogged), then with TRANSLUCENT_ONLY after the world for whatever came nearer since
// (clouds; with improved transparency also water and glass), against a copy of the earlier depth.

uniform sampler2D DepthSampler;
#ifdef TRANSLUCENT_ONLY
uniform sampler2D OpaqueDepthSampler;
#endif

// Mirrors PostFx#writeHaze (std140, mat4 + 5 x vec4).
layout(std140) uniform LiquidHaze {
    mat4 InvViewProj; // NDC -> camera-relative world position
    vec4 HazeColor;   // rgb, w = maximum opacity
    vec4 MistColor;   // rgb, w = mist density per block at the mist height
    vec4 SunColor;    // rgb, w = sun glow strength
    vec4 SunDir;      // xyz towards the sun, w = camera world y
    vec4 Params;      // x = haze density per block, y = haze start (blocks), z = mist height (y), w = mist falloff (blocks)
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    if (depth <= 0.0) {
        discard;
    }
    #ifdef TRANSLUCENT_ONLY
    if (depth <= texture(OpaqueDepthSampler, texCoord).r + 1.0e-7) {
        discard;
    }
    #endif
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = depth;
    #else
    float z = depth * 2.0 - 1.0;
    #endif
    vec4 world = InvViewProj * vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec3 rel = world.xyz / world.w;
    float dist = length(rel);
    vec3 dir = rel / max(dist, 1.0e-4);

    // Aerial perspective: exponential haze past the start distance.
    float haze = max(dist - Params.y, 0.0) * Params.x;

    // Valley mist: density falls off exponentially above the mist height and is capped below it, so
    // valleys fill up while peaks stay clear. A few samples along the ray integrate it.
    float mist = 0.0;
    if (MistColor.w > 0.0) {
        const int STEPS = 6;
        float stepLength = dist / float(STEPS);
        for (int i = 0; i < STEPS; i++) {
            float y = SunDir.w + rel.y * ((float(i) + 0.5) / float(STEPS));
            float h = clamp((y - Params.z) / Params.w, -1.5, 12.0);
            mist += exp(-h) * stepLength;
        }
        mist *= MistColor.w;
    }

    float opticalDepth = haze + mist;
    float amount = (1.0 - exp(-opticalDepth)) * HazeColor.w;
    if (amount < 0.002) {
        discard;
    }
    // Forward scattering: the air glows towards the sun.
    float glow = pow(max(dot(dir, SunDir.xyz), 0.0), 8.0) * SunColor.w;
    vec3 color = mix(HazeColor.rgb, MistColor.rgb, mist / max(opticalDepth, 1.0e-4));
    color = mix(color, SunColor.rgb, clamp(glow, 0.0, 1.0));
    fragColor = vec4(color, amount);
}
