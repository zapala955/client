#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Glass – signed-distance-field rounded rectangle (vertex stage).
//
// Uniform blocks are declared inline (mirroring vanilla's core/gui.vsh) so the shader has no
// dependency on the include system. They are bound by the GUI renderer for every GUI draw.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

// Input names MUST match the VertexFormat attribute names in LiquidPipelines – the backend binds
// attributes by name via SPIR-V reflection, not by declaration order.
layout(location = 0) in vec3 Position;   // already transformed by the 2D pose on the CPU
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;        // fragment position relative to the rect centre (GUI units)
layout(location = 3) in vec2 UV3;        // rect half extents (GUI units)
layout(location = 4) in float LineWidth; // corner radius (GUI units)
layout(location = 5) in ivec2 UV2;       // x = stroke width * 16 (0 = solid fill), y = edge softness * 16

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPos;
layout(location = 2) flat out vec2 halfSize;
layout(location = 3) flat out vec3 shape; // radius, stroke, softness

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    localPos = UV0;
    halfSize = UV3;
    shape = vec3(LineWidth, float(UV2.x) / 16.0, float(UV2.y) / 16.0);
}
