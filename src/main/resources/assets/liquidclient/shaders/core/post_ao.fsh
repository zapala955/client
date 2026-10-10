#version 330
#extension GL_ARB_separate_shader_objects : require

// Ambient Occlusion (PostFx) – horizon-based ambient occlusion over the opaque world, before water,
// particles and weather (which shouldn't be occluded) and before the hand.
//
//   AO       (half or full resolution) for each pixel, a few directions around it in the image; along
//            each, the highest the surrounding geometry rises above the surface's tangent plane
//            (within the radius, fading with distance) – that horizon is how much sky this point
//            can't see. Directions are rotated per pixel (interleaved gradient noise) and the steps
//            jittered, so few samples give fine noise instead of banding. Output: r = visibility,
//            g = view distance (for the depth-aware passes).
//   BLUR_H / BLUR_V
//            a separable blur that only mixes pixels at similar depths: the noise goes, contact
//            shadows stay sharp along object edges.
//   APPLY    full resolution: the four nearest AO texels, weighted by how close their depth is to
//            this pixel's, multiplied into the frame (dst · ao). With PREVIEW the AO alone is shown.

#if defined(AO) || defined(APPLY)
uniform sampler2D DepthSampler;
#endif
#if defined(BLUR_H) || defined(BLUR_V) || defined(APPLY)
uniform sampler2D InSampler;
#endif

// Mirrors PostFx#writeAo (std140, mat4 + 4 x vec4).
layout(std140) uniform LiquidAo {
    mat4 InvProj;  // NDC -> view space (camera at the origin, looking down -z)
    vec4 Lens;     // x proj[0][0], y proj[1][1], z AO texture width, w AO texture height
    vec4 Shape;    // x radius (blocks), y strength, z darkness (power), w tangent bias
    vec4 Quality;  // x directions, y steps per direction, z fade start, w fade end (blocks)
    vec4 Screen;   // xy full-resolution size, zw unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float TAU = 6.28318530718;

float ign(vec2 pixel) {
    return fract(52.9829189 * fract(dot(pixel, vec2(0.06711056, 0.00583715))));
}

vec3 viewAt(vec2 uv, float depth) {
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = depth;
    #else
    float z = depth * 2.0 - 1.0;
    #endif
    vec4 view = InvProj * vec4(uv * 2.0 - 1.0, z, 1.0);
    return view.xyz / view.w;
}

#ifdef AO
vec3 viewAt(vec2 uv) {
    return viewAt(uv, texture(DepthSampler, uv).r);
}
#endif

#ifdef AO
void main() {
    vec2 uv = texCoord;
    float depth = texture(DepthSampler, uv).r;
    if (depth <= 0.0) {
        fragColor = vec4(1.0, 1.0e6, 0.0, 1.0);
        return;
    }
    vec3 p = viewAt(uv, depth);
    float dist = length(p);
    float fade = 1.0 - smoothstep(Quality.z, Quality.w, dist);
    if (fade <= 0.0) {
        fragColor = vec4(1.0, dist, 0.0, 1.0);
        return;
    }

    // Surface normal from the neighbouring depths, on whichever side doesn't cross an edge.
    vec2 texel = 1.0 / Lens.zw;
    vec3 px1 = viewAt(uv + vec2(texel.x, 0.0));
    vec3 px0 = viewAt(uv - vec2(texel.x, 0.0));
    vec3 py1 = viewAt(uv + vec2(0.0, texel.y));
    vec3 py0 = viewAt(uv - vec2(0.0, texel.y));
    vec3 dx = length(px1 - p) < length(p - px0) ? px1 - p : p - px0;
    vec3 dy = length(py1 - p) < length(p - py0) ? py1 - p : p - py0;
    vec3 n = normalize(cross(dx, dy));
    if (dot(n, p) > 0.0) {
        n = -n;
    }

    // The radius on screen, in uv (perspective: smaller further away).
    float radius = Shape.x;
    vec2 screenRadius = vec2(Lens.x, Lens.y) * 0.5 * radius / max(-p.z, 0.05);
    if (screenRadius.y * Lens.w < 1.0) {
        fragColor = vec4(1.0, dist, 0.0, 1.0);
        return;
    }
    screenRadius = min(screenRadius, vec2(0.25));

    int directions = int(Quality.x + 0.5);
    int steps = int(Quality.y + 0.5);
    float rotation = ign(gl_FragCoord.xy) * TAU;
    float jitter = fract(ign(gl_FragCoord.xy + vec2(13.0, 7.0)) + 0.5);
    float occlusion = 0.0;
    for (int d = 0; d < 16; d++) {
        if (d >= directions) {
            break;
        }
        float angle = rotation + float(d) * TAU / float(directions);
        vec2 dir = vec2(cos(angle), sin(angle)) * screenRadius;
        float horizon = 0.0;
        for (int s = 0; s < 16; s++) {
            if (s >= steps) {
                break;
            }
            float t = (float(s) + jitter) / float(steps);
            vec2 suv = uv + dir * t * t; // denser near the centre, where contact shadows live
            if (any(lessThan(suv, vec2(0.0))) || any(greaterThan(suv, vec2(1.0)))) {
                break;
            }
            float sd = texture(DepthSampler, suv).r;
            if (sd <= 0.0) {
                continue;
            }
            vec3 v = viewAt(suv, sd) - p;
            float len = length(v);
            float elevation = dot(n, v) / max(len, 1.0e-4) - Shape.w;
            float falloff = clamp(1.0 - (len * len) / (radius * radius), 0.0, 1.0);
            horizon = max(horizon, elevation * falloff);
        }
        occlusion += horizon;
    }
    occlusion /= float(directions);
    float visibility = pow(clamp(1.0 - occlusion, 0.0, 1.0), Shape.z);
    fragColor = vec4(mix(1.0, visibility, fade), dist, 0.0, 1.0);
}
#endif

#if defined(BLUR_H) || defined(BLUR_V)
void main() {
    #ifdef BLUR_H
    vec2 step = vec2(1.0 / Lens.z, 0.0);
    #else
    vec2 step = vec2(0.0, 1.0 / Lens.w);
    #endif
    vec2 center = texture(InSampler, texCoord).rg;
    float sum = center.r;
    float weight = 1.0;
    float tolerance = max(center.g * 0.035, 0.05);
    for (int i = 1; i <= 4; i++) {
        float spatial = exp(-float(i * i) / 8.0);
        for (int side = -1; side <= 1; side += 2) {
            vec2 s = texture(InSampler, texCoord + step * float(i * side)).rg;
            float w = spatial * exp(-abs(s.g - center.g) / tolerance);
            sum += s.r * w;
            weight += w;
        }
    }
    fragColor = vec4(sum / weight, center.g, 0.0, 1.0);
}
#endif

#ifdef APPLY
void main() {
    float depth = texture(DepthSampler, texCoord).r;
    if (depth <= 0.0) {
        fragColor = vec4(1.0);
        return;
    }
    float dist = length(viewAt(texCoord, depth));
    // The four AO texels around this pixel, weighted bilinearly and by depth similarity.
    vec2 pos = texCoord * Lens.zw - 0.5;
    vec2 base = floor(pos);
    vec2 f = pos - base;
    float sum = 0.0;
    float weight = 0.0;
    float tolerance = max(dist * 0.03, 0.04);
    for (int j = 0; j <= 1; j++) {
        for (int i = 0; i <= 1; i++) {
            ivec2 texelPos = clamp(ivec2(base) + ivec2(i, j), ivec2(0), ivec2(Lens.zw) - 1);
            vec2 s = texelFetch(InSampler, texelPos, 0).rg;
            float bilinear = (i == 0 ? 1.0 - f.x : f.x) * (j == 0 ? 1.0 - f.y : f.y);
            float w = bilinear * exp(-abs(s.g - dist) / tolerance) + 1.0e-4;
            sum += s.r * w;
            weight += w;
        }
    }
    float ao = sum / weight;
    // Strength scales how much light the occlusion takes away (2 = twice as much).
    float shade = 1.0 - (1.0 - ao) * Shape.y;
    #ifdef PREVIEW
    fragColor = vec4(vec3(ao), 1.0);
    #else
    fragColor = vec4(vec3(clamp(shade, 0.0, 1.0)), 1.0);
    #endif
}
#endif
