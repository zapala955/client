#version 330
#extension GL_ARB_separate_shader_objects : require

// Hand Chams – vertex stage. Your arm and held items (in third person: held items), drawn by
// render.hand.HandChams in vanilla's entity vertex format. The look's parameters ride in the light
// (UV2) and overlay (UV1) attributes, which these models don't otherwise need here:
//   UV2.x  mode (0-3) | emissive (4) | block light (5-8) | sky light (9-12) | in the world (13) | item (14)
//          | marker (15)
//   UV2.y  hue (0-6, /127) | saturation (7-10, /15) | opacity (11-15, /31)
//   UV1.x  second hue (0-6, /127) | refraction (7-11, /31 x2) | skin detail (12-15, /15)
//   UV1.y  rim glow (0-5, /63 x2) | animation speed (6-11, /63 x3) | pattern scale (12-15, (v+1)/4)
// Color is the model's own vertex colour (item tints), used by the texture-based looks.

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:globals.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;

#ifndef OIT_ALPHA_ONLY
uniform sampler2D Sampler2;
#endif

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 texCoord;
layout(location = 2) out vec3 viewPos;
layout(location = 3) out vec3 viewNormal;
layout(location = 4) out vec3 modelPos;
layout(location = 5) out vec2 face;
layout(location = 6) out vec3 light;
layout(location = 7) flat out ivec4 params;

const int HOLOGRAM = 1;

float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    params = ivec4(UV1, UV2);
    int mode = UV2.x & 15;
    float speed = float((UV1.y >> 6) & 63) / 63.0 * 3.0;
    float t = GameTime * 1200.0 * speed;

    vec4 view = ModelViewMat * vec4(Position, 1.0);
    if (mode == HOLOGRAM) {
        // Glitch: now and then a thin slice jumps sideways.
        float slice = floor(view.y * 28.0);
        float tick = floor(t * 9.0);
        float jump = step(0.93, hash(vec2(slice, tick))) * (hash(vec2(tick, slice + 7.0)) - 0.5) * 0.04;
        view.x += jump;
    }
    gl_Position = ProjMat * view;

    vertexColor = Color;
    texCoord = UV0;
    viewPos = view.xyz;
    viewNormal = normalize(mat3(ModelViewMat) * Normal);
    modelPos = Position;
    // Corners of each quad, for edge distances (quads are drawn as four consecutive vertices).
    int corner = gl_VertexIndex & 3;
    face = vec2(corner == 1 || corner == 2 ? 1.0 : 0.0, corner >= 2 ? 1.0 : 0.0);
    #ifndef OIT_ALPHA_ONLY
    int block = (UV2.x >> 5) & 15;
    int sky = (UV2.x >> 9) & 15;
    light = texelFetch(Sampler2, ivec2(block, sky), 0).rgb;
    #else
    light = vec3(1.0);
    #endif
}
