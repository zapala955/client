#version 330
#extension GL_ARB_separate_shader_objects : require

// Motion Blur (PostFx) – camera motion blur after the world, before the hand (which stays sharp).
//
// Every pixel is put back where it was last frame: its position rebuilt from the depth buffer,
// moved by how far the camera travelled, projected with last frame's view and projection. The
// difference is its motion on screen; the frame copy is sampled along it, centred, with a per-pixel
// offset so few samples don't band. The sky (depth cleared to 0) only moves with the camera's
// rotation. The CPU scales the motion to a fixed shutter (frame-rate independent) and drops it on
// teleports.

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

// Mirrors PostFx#writeMotion (std140, 2 x mat4 + 2 x vec4).
layout(std140) uniform LiquidMotion {
    mat4 InvViewProj;  // this frame: NDC -> camera-relative position
    mat4 PrevViewProj; // last frame: (its) camera-relative position -> clip
    vec4 Delta;        // xyz this camera minus last camera (blocks) x movement weight, w motion scale
    vec4 Params;       // x samples, y longest blur (uv), zw one pixel (uv)
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 uv = texCoord;
    float depth = texture(DepthSampler, uv).r;
    vec4 previous;
    if (depth > 0.0) {
        #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
        float z = depth;
        #else
        float z = depth * 2.0 - 1.0;
        #endif
        vec4 world = InvViewProj * vec4(uv * 2.0 - 1.0, z, 1.0);
        vec3 rel = world.xyz / world.w;
        previous = PrevViewProj * vec4(rel + Delta.xyz, 1.0);
    } else {
        #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
        float z = 0.5;
        #else
        float z = 0.0;
        #endif
        vec4 world = InvViewProj * vec4(uv * 2.0 - 1.0, z, 1.0);
        previous = PrevViewProj * vec4(normalize(world.xyz / world.w), 0.0);
    }
    vec2 velocity = vec2(0.0);
    if (previous.w > 1.0e-4) {
        velocity = (uv - (previous.xy / previous.w * 0.5 + 0.5)) * Delta.w;
    }
    float len = length(velocity);
    if (len > Params.y) {
        velocity *= Params.y / len;
    }
    vec3 center = texture(InSampler, uv).rgb;
    // Under half a pixel of motion: nothing to blur.
    if (length(velocity / Params.zw) < 0.5) {
        fragColor = vec4(center, 1.0);
        return;
    }
    int samples = int(Params.x + 0.5);
    float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    vec3 sum = vec3(0.0);
    for (int i = 0; i < 32; i++) {
        if (i >= samples) {
            break;
        }
        float t = (float(i) + jitter) / float(samples) - 0.5;
        sum += texture(InSampler, clamp(uv + velocity * t, Params.zw, 1.0 - Params.zw)).rgb;
    }
    fragColor = vec4(sum / float(samples), 1.0);
}
