#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid backdrop – vertex stage for PNG wallpapers and their frosted copies.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;      // rgb = tint, a = opacity
layout(location = 2) in vec2 UV0;        // position relative to mask centre (GUI units)
layout(location = 3) in vec2 UV3;        // texture coordinate (cover-fit computed on the CPU)
layout(location = 4) in float LineWidth; // mask radius, < 0 = unmasked
layout(location = 5) in ivec2 UV1;       // mask half size * 8
layout(location = 6) in ivec2 UV2;       // mask centre * 8 (unused here)

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPos;
layout(location = 2) out vec2 texCoord;
layout(location = 3) flat out vec2 halfSize;
layout(location = 4) flat out float radius;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    localPos = UV0;
    texCoord = UV3;
    halfSize = vec2(UV1) / 8.0;
    radius = LineWidth;
}
