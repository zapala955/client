#version 150

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(std140) uniform JumpWaveUniforms {
    vec4 params;       // x = ring progress (0..1), y = strength, z = fade (0..1), w = unused
    vec4 crestColor;   // rgb = glow/jump color, a = color alpha
    vec4 p1;
    vec4 p2;
    vec4 p3;
    vec4 p4;
};

out vec2 uv;

void main() {
    vec4 positions[6] = vec4[](p1, p2, p3, p1, p3, p4);
    vec2 uvs[6] = vec2[](
        vec2(1.0, 1.0),
        vec2(1.0, 0.0),
        vec2(0.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 0.0),
        vec2(0.0, 1.0)
    );
    gl_Position = ProjMat * vec4(positions[gl_VertexID].xyz, 1.0);
    uv = uvs[gl_VertexID];
}
