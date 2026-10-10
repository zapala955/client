#version 330
#extension GL_ARB_separate_shader_objects : require

// Ambient particles (Particles module) – vertex stage. Same inputs as vanilla's particle shader
// (DefaultVertexFormat.PARTICLE), so the vanilla particle renderer drives it unchanged. UV0 is an
// atlas uv for sprite shapes or an encoded procedural shape (see the fragment stage). A full-bright
// UV2 means "emissive": the exact colour, without the lightmap's block-light tint.

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 3) in ivec2 UV2;

uniform sampler2D Sampler2;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec2 texCoord0;
layout(location = 3) out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    texCoord0 = UV0;
    bool emissive = UV2.x >= 240 && UV2.y >= 240;
    vertexColor = emissive ? Color : Color * sample_lightmap(Sampler2, UV2);
}
