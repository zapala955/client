#version 330
#extension GL_ARB_separate_shader_objects : require

// Storm Chaser – a lightning channel's light (see storm_bolt.vsh), added to the frame. The core is
// white-hot (it clips, and Bloom spreads it), the halo carries the colour.

#include <liquidclient:storm_bolt.glsl>

layout(location = 0) in vec2 local;
layout(location = 1) in vec3 tint;
layout(location = 2) flat in vec2 shape;

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 color;
    if (shape.y > 0.5) {
        // ground glow: a hot centre in a wide soft bloom
        float d = length(local);
        if (d > 1.0) {
            discard;
        }
        float hot = exp(-d * d * 18.0);
        float soft = exp(-d * 4.0) * (1.0 - d);
        color = (CoreColor.rgb * hot * 1.6 + GlowColor.rgb * soft * 0.9) * tint.x;
    } else {
        float x = abs(local.x);
        float core = exp(-(x * x) / (shape.x * shape.x) * 2.2);
        float halo = exp(-x * 3.2) * (1.0 - x);
        color = CoreColor.rgb * core * tint.x * 1.8 + GlowColor.rgb * halo * tint.y * 0.75;
    }
    fragColor = vec4(color, 0.0);
}
