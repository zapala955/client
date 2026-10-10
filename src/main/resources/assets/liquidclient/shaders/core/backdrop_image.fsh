#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid backdrop – photo wallpaper (the screen-sized sharp copy, or the frosted copy bound as
// Sampler0), optionally masked to a rounded rectangle for per-panel frosted glass.
// Vertex colour: rgb = tint, a = opacity. The frost "lift" is baked into the frost texture.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

uniform sampler2D Sampler0;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPos;
layout(location = 2) in vec2 texCoord;
layout(location = 3) flat in vec2 halfSize;
layout(location = 4) flat in float radius;

layout(location = 0) out vec4 fragColor;

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    float coverage = 1.0;
    if (radius >= 0.0) {
        float d = sdRoundedBox(localPos, halfSize, radius);
        coverage = clamp(0.5 - d / max(fwidth(d), 1.0e-4), 0.0, 1.0);
        if (coverage <= 0.0) {
            discard;
        }
    }
    vec3 col = texture(Sampler0, texCoord).rgb * vertexColor.rgb;
    fragColor = vec4(col, coverage * vertexColor.a) * ColorModulator;
}
