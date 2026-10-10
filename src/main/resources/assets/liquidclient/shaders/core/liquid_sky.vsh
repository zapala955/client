#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Sky – vertex stage. A unit cube centred on the camera, transformed with vanilla's own
// matrices (so FOV, view bobbing and nausea match the vanilla sky). The cube position doubles as an
// un-normalised view ray: linear interpolation across a planar face is exact for rays from the origin.
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

layout(location = 0) out vec3 viewRay;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    viewRay = Position;
}
