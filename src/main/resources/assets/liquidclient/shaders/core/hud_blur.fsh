#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid HUD glass – fragment stage.
//
// Sampler0 is a copy of the frame taken just before the GUI is drawn (BackdropCapture). Inside the
// rounded rectangle it's blurred with a gaussian over a disc – sampled every couple of pixels with
// bilinear taps, each of which already averages a 2×2 block – then given vibrancy (saturation),
// tinted, and optionally refracted towards the rim like a thick glass lens.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

uniform sampler2D Sampler0;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPos;
layout(location = 2) in vec2 screenUv;
layout(location = 3) flat in vec2 halfSize;
layout(location = 4) flat in float radius;
layout(location = 5) flat in vec4 params;

layout(location = 0) out vec4 fragColor;

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    // Derivatives first: they must be taken before any fragment of the quad can discard.
    vec2 du = dFdx(screenUv);
    vec2 dv = dFdy(screenUv);
    vec2 dLocalX = dFdx(localPos);
    vec2 dLocalY = dFdy(localPos);
    float r = min(radius, min(halfSize.x, halfSize.y));
    float dist = sdRoundedBox(localPos, halfSize, r);
    float aa = max(fwidth(dist), 1.0e-4);
    float coverage = clamp(0.5 - dist / aa, 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }

    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 uv = screenUv;

    // Lens: within a rim band the background is pulled inwards, strongest at the very edge.
    float refraction = max(params.w, 0.0);
    if (refraction > 0.0) {
        float band = max(r, 6.0);
        float edge = clamp(1.0 + dist / band, 0.0, 1.0); // 0 inside, 1 at the outline
        vec2 eps = vec2(0.5, 0.0);
        vec2 normal = normalize(vec2(
                sdRoundedBox(localPos + eps.xy, halfSize, r) - sdRoundedBox(localPos - eps.xy, halfSize, r),
                sdRoundedBox(localPos + eps.yx, halfSize, r) - sdRoundedBox(localPos - eps.yx, halfSize, r)) + 1.0e-5);
        // The uv direction of "inwards" depends on the backend's framebuffer orientation, so map the
        // local normal into uv through the screen-space derivatives instead of assuming one.
        float det = dLocalX.x * dLocalY.y - dLocalX.y * dLocalY.x;
        if (abs(det) > 1.0e-6) {
            // Screen-pixel step that moves one GUI unit along the normal, mapped into uv.
            vec2 pixelStep = vec2(normal.x * dLocalY.y - normal.y * dLocalY.x, normal.y * dLocalX.x - normal.x * dLocalX.y) / det;
            vec2 uvAlongNormal = du * pixelStep.x + dv * pixelStep.y;
            uv -= uvAlongNormal * (edge * edge * edge) * refraction * band;
        }
    }

    float blurPx = params.x;
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

    // Vibrancy, optional brightness lift (negative refraction slot), then the tint.
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luma), color, params.z);
    if (params.w < 0.0) {
        color += vec3(-params.w);
    }
    color = mix(color, vertexColor.rgb, vertexColor.a);

    fragColor = vec4(clamp(color, 0.0, 1.0), coverage * params.y) * ColorModulator;
}
