#version 330
#extension GL_ARB_separate_shader_objects : require

// Wet World (PostFx) – rain-soaked surfaces, drawn over the opaque world before anything
// translucent (water, particles, weather), from a copy of the frame so far.
//
//   damp      water fills the pores: surfaces darken and their colour deepens, and they take on a
//             faint sky sheen that grows at grazing angles (Fresnel)
//   puddles   on flat ground, where a world-anchored noise dips below the water level (fewer on
//             grass and leaves): mirrors with screen-space reflections, the sky where those miss,
//             rings from raindrops while it's raining
//   sun       a glint off everything wet
//
// Only what the rain could reach: the weather columns (render.weather.WeatherColumns) hold, per
// block column, how far down rain falls and whether it rains or snows there. A surface is wet if the
// air just in front of it is open to the rain – roofs, overhangs and caves stay dry. Real shelters
// don't cut a sharp line (rain blows in, leaves drip), so the columns around the point are blended
// over about a block and a half, around a gently wobbling centre: the edge under a tree is a soft,
// organic fade, never the block grid. Towards the columns' 32-block reach this blends into the
// distant rule (up-facing surfaces damp, fading out with distance), so there's no boundary either.

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform sampler2D ColumnSampler;

// Mirrors PostFx#writeWet (std140, 2 x mat4 + 8 x vec4).
layout(std140) uniform LiquidWet {
    mat4 InvViewProj; // NDC -> camera-relative position
    mat4 ViewProj;    // camera-relative position -> clip (reflections)
    vec4 CameraBlock; // xyz camera block, w time (s)
    vec4 CameraFrac;  // xyz camera position inside its block, w columns valid (0 / 1)
    vec4 Wet;         // x wetness, y puddles, z rain now (ripples), w darkening
    vec4 SkyZenith;   // rgb, w reflections on (0 / 1)
    vec4 SkyHorizon;  // rgb, w reflection steps
    vec4 SunDir;      // xyz towards the sun (or moon), w glint strength
    vec4 SunColor;    // rgb, w unused
    vec4 Pixel;       // xy one pixel in uv, z puddle amount scale, w unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const uint TYPE_RAIN = 1u;

vec3 hash32(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yxz + 33.33);
    return fract((p3.xxy + p3.yzz) * p3.zyx);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash32(i).x;
    float b = hash32(i + vec2(1.0, 0.0)).x;
    float c = hash32(i + vec2(0.0, 1.0)).x;
    float d = hash32(i + vec2(1.0, 1.0)).x;
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += amp * valueNoise(p);
        p = p * 2.07 + vec2(3.1, 7.7);
        amp *= 0.5;
    }
    return v / 0.9375;
}

vec3 positionAt(vec2 uv, float depth) {
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = depth;
    #else
    float z = depth * 2.0 - 1.0;
    #endif
    vec4 world = InvViewProj * vec4(uv * 2.0 - 1.0, z, 1.0);
    return world.xyz / world.w;
}

vec3 positionAt(vec2 uv) {
    return positionAt(uv, texture(DepthSampler, uv).r);
}

// Whether rain reaches height y (camera-relative) in one block column.
float openAt(ivec2 world, float y) {
    uvec4 b = uvec4(texelFetch(ColumnSampler, world & 63, 0) * 255.0 + 0.5);
    if ((b.b & 16u) == 0u || (b.b & 3u) != TYPE_RAIN) {
        return 0.0;
    }
    float surface = float(b.r | (b.g << 8u)) / 16.0 - 2048.0 - CameraBlock.y - CameraFrac.y;
    return smoothstep(surface - 0.12, surface - 0.02, y);
}

// How much rain reaches this surface (0..1), from the columns around it; -1 outside the columns.
float exposure(vec3 rel, vec3 normal) {
    vec3 probe = rel + normal * 0.12;
    if (CameraFrac.w < 0.5 || max(abs(probe.x), abs(probe.z)) > 30.0) {
        return -1.0;
    }
    vec2 wp = mod(CameraBlock.xz, 4096.0) + CameraFrac.xz + probe.xz;
    vec2 wobble = (vec2(valueNoise(wp * 1.3), valueNoise(wp * 1.3 + 17.0)) - 0.5) * 0.9;
    vec2 at = CameraFrac.xz + probe.xz + wobble; // blocks from the camera block's corner
    ivec2 centre = ivec2(floor(at));
    ivec2 origin = ivec2(CameraBlock.xz);
    float sum = 0.0;
    float weight = 0.0;
    for (int dz = -1; dz <= 1; dz++) {
        for (int dx = -1; dx <= 1; dx++) {
            ivec2 cell = centre + ivec2(dx, dz);
            float w = max(0.0, 1.0 - length(vec2(cell) + 0.5 - at) / 1.4);
            w *= w;
            sum += openAt(origin + cell, probe.y) * w;
            weight += w;
        }
    }
    return weight > 0.0 ? sum / weight : 0.0;
}

// Rings from raindrops landing in a puddle: the slope of their ripples.
vec2 ripples(vec2 p, float t) {
    vec2 slope = vec2(0.0);
    for (int layer = 0; layer < 2; layer++) {
        vec2 g = p * 2.2 + float(layer) * vec2(0.37, 0.71);
        vec2 id = floor(g);
        vec2 f = fract(g) - 0.5;
        vec3 h = hash32(id + float(layer) * 19.1);
        float age = fract(t * (0.9 + 0.5 * h.z) + h.x);
        vec2 d = f - (h.yz - 0.5) * 0.5;
        float r = length(d);
        float front = r - age * 0.5;
        float wave = sin(front * 46.0) * exp(-abs(front) * 22.0) * (1.0 - age) * (1.0 - age);
        slope += d / max(r, 1.0e-3) * wave;
    }
    return slope;
}

vec3 skyColor(vec3 r) {
    float up = clamp(r.y, 0.0, 1.0);
    return r.y >= 0.0 ? mix(SkyHorizon.rgb, SkyZenith.rgb, sqrt(up)) : SkyHorizon.rgb * 0.55;
}

// Marches the reflected ray through the depth buffer; rgb = hit colour, a = confidence. Small steps
// that grow with distance; a hit needs the ray to have only just slipped behind a surface (thin things
// like legs and posts are not walls, so nothing smears behind them), and the deeper it is behind,
// the less it counts.
vec4 reflection(vec3 origin, vec3 dir, float jitter) {
    int steps = int(SkyHorizon.w + 0.5);
    float stride = 0.08;
    float t = 0.06 + jitter * stride;
    vec3 last = origin;
    for (int i = 0; i < 48; i++) {
        if (i >= steps) {
            break;
        }
        vec3 q = origin + dir * t;
        vec4 clip = ViewProj * vec4(q, 1.0);
        if (clip.w <= 0.05) {
            break;
        }
        vec2 uv = clip.xy / clip.w * 0.5 + 0.5;
        if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) {
            break;
        }
        float sceneDepth = texture(DepthSampler, uv).r;
        if (sceneDepth > 0.0) {
            float rayDist = length(q);
            float gap = rayDist - length(positionAt(uv, sceneDepth));
            float thickness = 0.12 + stride * 1.1 + rayDist * 0.003;
            if (gap > 0.0 && gap < thickness) {
                // Refine between the last two points.
                vec3 lo = last;
                vec3 hi = q;
                for (int k = 0; k < 6; k++) {
                    vec3 mid = (lo + hi) * 0.5;
                    vec4 mc = ViewProj * vec4(mid, 1.0);
                    vec2 muv = mc.xy / mc.w * 0.5 + 0.5;
                    float md = texture(DepthSampler, muv).r;
                    if (md > 0.0 && length(mid) > length(positionAt(muv, md))) {
                        hi = mid;
                    } else {
                        lo = mid;
                    }
                }
                vec4 hc = ViewProj * vec4(hi, 1.0);
                vec2 huv = hc.xy / hc.w * 0.5 + 0.5;
                float hd = texture(DepthSampler, huv).r;
                float behind = hd > 0.0 ? length(hi) - length(positionAt(huv, hd)) : thickness;
                float confidence = 1.0 - smoothstep(0.03, thickness, behind);
                vec2 edge = smoothstep(vec2(0.0), vec2(0.08), huv) * (1.0 - smoothstep(vec2(0.92), vec2(1.0), huv));
                float fade = edge.x * edge.y * confidence * sqrt(1.0 - float(i) / float(max(steps, 1)));
                return vec4(texture(InSampler, huv).rgb, fade);
            }
        }
        last = q;
        t += stride;
        stride *= 1.16;
    }
    return vec4(0.0);
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    if (depth <= 0.0) {
        discard;
    }
    vec3 p = positionAt(texCoord, depth);
    float dist = length(p);
    if (dist > 170.0) {
        discard;
    }

    // Surface normal from the neighbours, on whichever side doesn't cross an edge.
    vec3 px1 = positionAt(texCoord + vec2(Pixel.x, 0.0));
    vec3 px0 = positionAt(texCoord - vec2(Pixel.x, 0.0));
    vec3 py1 = positionAt(texCoord + vec2(0.0, Pixel.y));
    vec3 py0 = positionAt(texCoord - vec2(0.0, Pixel.y));
    vec3 dx = length(px1 - p) < length(p - px0) ? px1 - p : p - px0;
    vec3 dy = length(py1 - p) < length(p - py0) ? py1 - p : p - py0;
    vec3 n = normalize(cross(dx, dy));
    if (dot(n, p) > 0.0) {
        n = -n;
    }

    float reach = 1.0 - smoothstep(110.0, 170.0, dist);
    float distant = smoothstep(0.3, 0.8, n.y) * reach;
    float near = exposure(p, n);
    float open = near < 0.0 ? distant : mix(near, distant, smoothstep(22.0, 30.0, max(abs(p.x), abs(p.z))));
    float wet = Wet.x * open;
    if (wet < 0.004) {
        discard;
    }

    vec3 col = texture(InSampler, texCoord).rgb;
    // Grass and leaves, by how green they are relative to their brightness (so it works at night too).
    float leafy = clamp((col.g - max(col.r, col.b)) / max(col.g, 0.03) * 2.5, 0.0, 1.0);
    // Damp: darker, deeper colour.
    float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
    vec3 damp = mix(vec3(lum), col, 1.22) * (1.0 - 0.42 * Wet.w);
    col = mix(col, max(damp, vec3(0.0)), wet);

    // Puddles on flat ground.
    vec2 wp = mod(CameraBlock.xz, 4096.0) + CameraFrac.xz + p.xz;
    float puddle = 0.0;
    if (n.y > 0.94 && Wet.y > 0.0) {
        // Water pools on paths, stone and soil – not on grass or leaves.
        float field = fbm(wp * 0.32) * 0.75 + fbm(wp * 1.3 + 4.0) * 0.25 - leafy * 0.3;
        float level = mix(0.82, 0.38, clamp(Wet.y * Pixel.z, 0.0, 1.0));
        puddle = smoothstep(level, level + 0.05, field) * open * (1.0 - smoothstep(48.0, 96.0, dist)) * min(1.0, Wet.y * 3.0)
                * (1.0 - leafy);
    }

    vec3 v = p / max(dist, 1.0e-4);
    vec3 nf = n;
    if (puddle > 0.0 && Wet.z > 0.0) {
        vec2 slope = ripples(wp, CameraBlock.w) * Wet.z * 0.35;
        nf = normalize(n + vec3(slope.x, 0.0, slope.y) * puddle);
    }
    vec3 r = reflect(v, nf);
    float cosine = clamp(dot(-v, nf), 0.0, 1.0);
    float fresnel = 0.02 + 0.98 * pow(1.0 - cosine, 5.0);

    vec3 mirrored = skyColor(r);
    if (puddle > 0.05 && SkyZenith.w > 0.5) {
        float jitter = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
        vec4 hit = reflection(p + n * 0.02, r, jitter);
        mirrored = mix(mirrored, hit.rgb, hit.a);
    }
    float glint = pow(max(dot(r, SunDir.xyz), 0.0), puddle > 0.5 ? 900.0 : 160.0) * SunDir.w;
    mirrored += SunColor.rgb * glint * (puddle > 0.5 ? 5.0 : 0.6);

    // Damp surfaces get a sheen; puddles are mirrors (and darken what's under them).
    float sheen = fresnel * 0.6 * wet * (0.35 + 0.65 * clamp(n.y, 0.0, 1.0));
    float mirror = mix(0.22, 1.0, fresnel) * 0.92;
    col = mix(col, col * 0.62, puddle * 0.8);
    col = mix(col, mirrored, max(sheen, mirror * puddle));
    fragColor = vec4(col, 1.0);
}
