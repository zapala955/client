#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid HUD glass – a rounded panel that shows a blurred copy of the game behind it (vertex stage).
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(location = 0) in vec3 Position;   // GUI position after the 2D pose
layout(location = 1) in vec4 Color;      // rgb = tint, a = how much of the tint replaces the blur
layout(location = 2) in vec2 UV0;        // position relative to the panel centre (GUI units)
layout(location = 3) in vec2 UV3;        // panel half extents (GUI units)
layout(location = 4) in float LineWidth; // corner radius (GUI units)
layout(location = 5) in ivec2 UV1;       // x = blur radius (device px) * 16, y = opacity * 10000
layout(location = 6) in ivec2 UV2;       // x = (saturation) * 1000, y = refraction * 1000 (< 0: brightness lift)

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPos;
layout(location = 2) out vec2 screenUv;
layout(location = 3) flat out vec2 halfSize;
layout(location = 4) flat out float radius;
layout(location = 5) flat out vec4 params; // blur px, opacity, saturation, refraction

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Render targets are sampled the way vanilla's screenquad does: uv = ndc * 0.5 + 0.5. That
    // mapping holds on both backends, whatever their framebuffer origin.
    screenUv = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
    vertexColor = Color;
    localPos = UV0;
    halfSize = UV3;
    radius = LineWidth;
    params = vec4(float(UV1.x) / 16.0, float(UV1.y) / 10000.0, float(UV2.x) / 1000.0, float(UV2.y) / 1000.0);
}
