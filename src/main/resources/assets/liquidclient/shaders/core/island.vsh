#version 330
#extension GL_ARB_separate_shader_objects : require

// Dynamic Island – one gooey shape: the island's body (a rounded box) and an optional bubble beside
// it, melted together where they're close (vertex stage). See island.fsh.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

// Input names MUST match the VertexFormat attribute names (LiquidPipelines.HUD_BLUR_FORMAT).
layout(location = 0) in vec3 Position;   // GUI position after the 2D pose
layout(location = 1) in vec4 Color;      // fill / shadow colour, or the glass tint; a = opacity
layout(location = 2) in vec2 UV0;        // position relative to the body's centre (GUI units)
layout(location = 3) in vec2 UV3;        // body half width / half height
layout(location = 4) in float LineWidth; // body corner radius
layout(location = 5) in ivec2 UV1;       // bubble centre (x offset from the body's centre) * 16, bubble radius * 16 (0 = none)
layout(location = 6) in ivec2 UV2;       // x = shadow softness / glass blur (device px) * 16, y = rim light / glass tint * 1000

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPos;
layout(location = 2) out vec2 screenUv;
layout(location = 3) flat out vec2 halfSize;
layout(location = 4) flat out float radius;
layout(location = 5) flat out vec4 params; // bubble x, bubble radius, softness / blur, rim / tint

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Render targets are sampled the way vanilla's screenquad does (uv = ndc * 0.5 + 0.5), which
    // holds on both backends whatever their framebuffer origin.
    screenUv = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
    vertexColor = Color;
    localPos = UV0;
    halfSize = UV3;
    radius = LineWidth;
    params = vec4(float(UV1.x) / 16.0, float(UV1.y) / 16.0, float(UV2.x) / 16.0, float(UV2.y) / 1000.0);
}
