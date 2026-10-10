#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Glass – signed-distance-field rounded rectangle (fragment stage).
//
// One shader covers every glass primitive:
//   stroke == 0, softness == 0 : crisp anti-aliased fill
//   stroke  > 0                : anti-aliased ring (hairline borders)
//   stroke == 0, softness > 0  : soft falloff (drop shadows, light blobs)
//   stroke  < 0, softness > 0  : outer glow only – zero inside the shape, so a glow never
//                                washes out the glass it surrounds
//
// Anti-aliasing width comes from fwidth() of the distance field, i.e. exactly one physical
// pixel regardless of GUI scale (1x-6x) or any scale animation applied through the pose.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPos;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in vec3 shape;

layout(location = 0) out vec4 fragColor;

// Inigo Quilez' exact rounded-box SDF. Negative inside, positive outside.
float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    float radius = min(shape.x, min(halfSize.x, halfSize.y));
    float stroke = shape.y;
    float softness = shape.z;

    float d = sdRoundedBox(localPos, halfSize, radius);
    float aa = max(fwidth(d), 1.0e-4);

    float coverage;
    if (softness > 0.0 && stroke < 0.0) {
        // Outer glow: anti-aliased start at the edge, smooth gaussian-ish tail outwards.
        float outside = clamp(0.5 + d / aa, 0.0, 1.0);
        float tail = 1.0 - smoothstep(0.0, softness, d);
        coverage = outside * tail * tail;
    } else if (softness > 0.0) {
        // Smooth falloff centred on the edge; squared for a more gaussian-looking tail.
        coverage = 1.0 - smoothstep(-softness, softness, d);
        coverage *= coverage;
    } else if (stroke > 0.0) {
        float outer = clamp(0.5 - d / aa, 0.0, 1.0);
        float inner = clamp(0.5 + (d + stroke) / aa, 0.0, 1.0);
        coverage = outer * inner;
    } else {
        coverage = clamp(0.5 - d / aa, 0.0, 1.0);
    }

    float alpha = vertexColor.a * coverage;
    if (alpha <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, alpha) * ColorModulator;
}
