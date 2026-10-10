#version 330
#extension GL_ARB_separate_shader_objects : require

// Dynamic Island – fragment stage.
//
// The shape is a signed distance field: the body (a rounded box, a pill when compact) united with an
// optional bubble – the second live activity, which splits off to the side. Where the two are close
// they're melted together with a smooth minimum, and how far that melting reaches shrinks as the gap
// between them opens: while the bubble pushes out of the body it pulls a thick neck, the neck thins
// as it travels and snaps once there's a gap of about a quarter of the bubble, then the two sit apart
// with clean edges – like two drops of ink, or the iPhone's island.
//
// Three variants (pipelines in LiquidPipelines):
//   default  the island's fill, anti-aliased to one device pixel, with an optional rim light: a hairline
//            just inside the edge, brightest along the top, so a black island still reads on a dark night.
//   SHADOW   a soft shadow of the same shape (softness in params.z).
//   GLASS    Liquid Glass: the frame behind the island (Sampler0, BackdropCapture), refracted towards
//            the rim, blurred, given vibrancy and tinted; plus the rim light.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

#ifdef GLASS
uniform sampler2D Sampler0;
#endif

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPos;
layout(location = 2) in vec2 screenUv;
layout(location = 3) flat in vec2 halfSize;
layout(location = 4) flat in float radius;
layout(location = 5) flat in vec4 params; // bubble x, bubble radius, softness / blur, rim / tint

layout(location = 0) out vec4 fragColor;

// Inigo Quilez' exact rounded-box SDF. Negative inside, positive outside.
float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

// Polynomial smooth minimum: the union of a and b with a fillet of size k between them.
float smin(float a, float b, float k) {
    if (k <= 1.0e-4) {
        return min(a, b);
    }
    float h = max(k - abs(a - b), 0.0) / k;
    return min(a, b) - h * h * k * 0.25;
}

float island(vec2 p, float r, float k) {
    float d = sdRoundedBox(p, halfSize, r);
    float bubbleRadius = params.y;
    if (bubbleRadius > 0.05) {
        d = smin(d, length(p - vec2(params.x, 0.0)) - bubbleRadius, k);
    }
    return d;
}

// Surface normal of the shape (outwards), from central differences.
vec2 islandNormal(vec2 p, float r, float k) {
    vec2 e = vec2(0.35, 0.0);
    vec2 g = vec2(island(p + e.xy, r, k) - island(p - e.xy, r, k), island(p + e.yx, r, k) - island(p - e.yx, r, k));
    return g / max(length(g), 1.0e-5);
}

// The rim light: a line about a device pixel wide just inside the edge, lit from above.
float rimLight(float d, float aa, vec2 n) {
    float width = aa * 1.25;
    float band = clamp(0.5 - d / aa, 0.0, 1.0) - clamp(0.5 - (d + width) / aa, 0.0, 1.0);
    float lit = 0.3 + 0.7 * clamp(-n.y, 0.0, 1.0);
    return band * lit;
}

void main() {
    #ifdef GLASS
    // Derivatives first: they must be taken before any fragment of the quad can discard.
    vec2 du = dFdx(screenUv);
    vec2 dv = dFdy(screenUv);
    vec2 dLocalX = dFdx(localPos);
    vec2 dLocalY = dFdy(localPos);
    #endif
    float r = min(radius, min(halfSize.x, halfSize.y));
    float bubbleRadius = params.y;
    // How far the melting reaches: wide while the bubble overlaps or touches the body, nothing once
    // the gap between them is wider than ~45 % of the bubble.
    float k = 0.0;
    if (bubbleRadius > 0.05) {
        float gap = sdRoundedBox(vec2(params.x, 0.0), halfSize, r) - bubbleRadius;
        k = bubbleRadius * 1.2 * (1.0 - smoothstep(0.0, bubbleRadius * 0.45, gap));
    }
    float d = island(localPos, r, k);
    float aa = max(fwidth(d), 1.0e-4);

    #ifdef SHADOW
    float softness = max(params.z, 0.0625);
    float shadow = 1.0 - smoothstep(-softness, softness, d);
    shadow *= shadow;
    float alpha = vertexColor.a * shadow;
    if (alpha <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, alpha) * ColorModulator;
    #else
    float coverage = clamp(0.5 - d / aa, 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }
    vec2 n = islandNormal(localPos, r, k);

    #ifdef GLASS
    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 uv = screenUv;
    // Lens: within a rim band the frame behind is pulled inwards, strongest right at the edge.
    float band = max(min(halfSize.y, 8.0), 4.0);
    float edge = clamp(1.0 + d / band, 0.0, 1.0);
    float det = dLocalX.x * dLocalY.y - dLocalX.y * dLocalY.x;
    if (abs(det) > 1.0e-6) {
        // The uv direction of "inwards" depends on the backend's framebuffer orientation, so the local
        // normal is mapped into uv through the screen-space derivatives instead of assuming one.
        vec2 pixelStep = vec2(n.x * dLocalY.y - n.y * dLocalY.x, n.y * dLocalX.x - n.x * dLocalX.y) / det;
        vec2 uvAlongNormal = du * pixelStep.x + dv * pixelStep.y;
        uv -= uvAlongNormal * (edge * edge * edge) * 0.55 * band;
    }
    float blurPx = params.z;
    vec3 color;
    if (blurPx > 0.75) {
        float sigma = blurPx * 0.5;
        float inv2s2 = 1.0 / (2.0 * sigma * sigma);
        float stepPx = max(1.5, blurPx / 6.0);
        vec3 sum = vec3(0.0);
        float total = 0.0;
        for (float y = -blurPx; y <= blurPx + 0.01; y += stepPx) {
            for (float x = -blurPx; x <= blurPx + 0.01; x += stepPx) {
                float d2 = x * x + y * y;
                if (d2 > blurPx * blurPx) {
                    continue;
                }
                float w = exp(-d2 * inv2s2);
                sum += texture(Sampler0, uv + vec2(x, y) * texel).rgb * w;
                total += w;
            }
        }
        color = sum / max(total, 1.0e-4);
    } else {
        color = texture(Sampler0, uv).rgb;
    }
    // Vibrancy, then the tint (dark glass keeps white text legible over snow and sky).
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luma), color, 1.35);
    color = mix(color, vertexColor.rgb, clamp(params.w, 0.0, 1.0));
    // A soft sheen across the top half, then the rim.
    color += vec3(0.06) * clamp(1.0 - (localPos.y + halfSize.y) / max(halfSize.y, 1.0), 0.0, 1.0);
    float rim = rimLight(d, aa, n) * 0.55;
    color = mix(clamp(color, 0.0, 1.0), vec3(1.0), rim);
    fragColor = vec4(color, coverage * vertexColor.a) * ColorModulator;
    #else
    // The rim only lightens the colour: the island's opacity (and any fade) applies to it too.
    float rim = rimLight(d, aa, n) * clamp(params.w, 0.0, 1.0);
    vec3 color = mix(vertexColor.rgb, vec3(1.0), rim);
    fragColor = vec4(color, vertexColor.a * coverage) * ColorModulator;
    #endif
    #endif
}
