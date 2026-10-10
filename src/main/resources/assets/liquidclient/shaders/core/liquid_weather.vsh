#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Weather – vertex stage. Every raindrop, snowflake and splash is a quad of a static buffer
// built once (render.weather.LiquidWeather): the vertex carries only random seeds, and its place in
// the world is computed here from time, so the CPU does no per-particle work.
//
// Drops live in world-anchored boxes that repeat around the camera (period = box size): a drop's
// world position depends only on its seed and the animation offset, so walking through the rain
// gives true parallax and nothing slides along with the camera. Each drop belongs to one of a few
// discrete speed classes (1 + k/10), so the animation offsets – wrapped at ten periods on the CPU –
// stay continuous for every class.
//
// The column texture (ColumnSampler, 64×64, wrapped by world x/z) holds per block column: the
// surface height (rain and snow stop on roofs, leaves and water), the precipitation there (biome
// and Ambience override), water / hot flags and the light level at the surface.
//
// Vertex: Position = seeds (x, y, z in [0, 1)), Color = (corner x, corner y, layer / 255, seed w).

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <liquidclient:weather.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;

uniform sampler2D ColumnSampler;
uniform sampler2D Sampler2;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec2 local;
layout(location = 3) out vec4 tint;
layout(location = 4) out vec4 shape;
layout(location = 5) flat out int kind;

// Mirrors LiquidWeather.
const int RAIN_FAR = 1;
const int SNOW_FAR = 3;

// Kinds (fragment stage).
const int RAIN = 0;
const int SNOW = 1;
const int SPLASH = 2;
const int RIPPLE = 3;

// Column type bits (WeatherColumns).
const uint TYPE_RAIN = 1u;
const uint TYPE_SNOW = 2u;

struct Column {
    float surface;  // relative to the camera
    uint type;
    bool water;
    bool hot;
    int block;
    int sky;
};

Column column(vec3 rel) {
    ivec2 world = ivec2(CameraBlock.xz) + ivec2(floor(CameraFrac.xz + rel.xz));
    uvec4 b = uvec4(texelFetch(ColumnSampler, world & 63, 0) * 255.0 + 0.5);
    Column c;
    c.surface = float(b.r | (b.g << 8u)) / 16.0 - 2048.0 - CameraBlock.y - CameraFrac.y;
    c.type = (b.b & 16u) != 0u ? (b.b & 3u) : 0u;
    c.water = (b.b & 4u) != 0u;
    c.hot = (b.b & 8u) != 0u;
    c.block = int(b.a & 15u);
    c.sky = int(b.a >> 4u);
    return c;
}

vec3 lightAt(int block, int sky) {
    return texelFetch(Sampler2, ivec2(block, sky), 0).rgb;
}

// Position of a seed in a layer's repeating box, centred on the camera.
vec3 inLayer(int layer, vec3 seed, float speed) {
    vec4 a = Layers[layer * 2];
    vec4 b = Layers[layer * 2 + 1];
    vec3 period = vec3(a.w, b.w, a.w);
    vec3 p = seed * period + a.xyz * speed - b.xyz + period * 0.5;
    return p - floor(p / period) * period - period * 0.5;
}

// Soft edges of a layer's box, so its border never shows.
float boxFade(int layer, vec3 rel) {
    float half_l = Layers[layer * 2].w * 0.5;
    float half_p = Layers[layer * 2 + 1].w * 0.5;
    return (1.0 - smoothstep(0.62, 1.0, length(rel.xz) / half_l)) * (1.0 - smoothstep(0.7, 1.0, abs(rel.y) / half_p));
}

float pixelsPerMeter(float distance) {
    return CameraFrac.w * 0.5 * ProjMat[1][1] / max(distance, 0.05);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

void emit(vec3 pos) {
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
}

void cull() {
    gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    sphericalVertexDistance = 0.0;
    cylindricalVertexDistance = 0.0;
    local = vec2(0.0);
    tint = vec4(0.0);
    shape = vec4(0.0);
}

// ---- Rain: thin motion-blurred streaks along the drop's velocity --------------------------------
void rain(int layer, vec3 seed, float w, vec2 corner) {
    kind = RAIN;
    float speed = 1.0 + (floor(w * 4.999) - 2.0) * 0.1;
    vec3 head = inLayer(layer, seed, speed);
    Column c = column(head);
    vec3 velocity = vec3(Motion.x, -Motion.y, Motion.z) * speed;
    vec3 tail = head - velocity * Extra.x * (0.85 + 0.3 * fract(w * 3.7));
    if (c.type != TYPE_RAIN || tail.y <= c.surface) {
        cull();
        return;
    }
    head.y = max(head.y, c.surface);

    vec3 axis = tail - head;
    vec3 mid = (head + tail) * 0.5;
    float distance = length(mid);
    vec3 side = cross(axis, mid);
    float sideLength = length(side);
    side = sideLength > 1.0e-6 ? side / sideLength : vec3(1.0, 0.0, 0.0);

    // Real drops are ~2 mm; anything under ~1.3 px is widened and made fainter instead (no shimmer).
    float ppm = pixelsPerMeter(distance);
    float width = 0.0105;
    float coverage = clamp(width * ppm / 1.3, 0.0, 1.0);
    width = max(width, 1.3 / ppm);

    vec3 pos = mix(head, tail, corner.y) + side * (corner.x * 2.0 - 1.0) * width * 0.5;
    float alpha = Look.x * coverage * boxFade(layer, mid) * smoothstep(0.3, 1.5, distance) * (0.7 + 0.6 * fract(w * 7.13));
    // Drops refract the bright sky: lit by the sky light at the column, never fully black.
    vec3 light = max(lightAt(c.block, c.sky), vec3(0.09, 0.10, 0.12));
    tint = vec4(light * vec3(0.84, 0.89, 0.98) * 1.18, alpha);
    local = vec2(corner.x * 2.0 - 1.0, corner.y);
    shape = vec4(0.0);
    emit(pos);
}

// ---- Snow: soft flakes that drift, flutter, spin and tumble -------------------------------------
void snow(int layer, vec3 seed, float w, vec2 corner) {
    kind = SNOW;
    float speed = 1.0 + (floor(w * 6.999) - 3.0) * 0.1;
    vec3 p = inLayer(layer, seed, speed);
    float t = CameraBlock.w;
    float h1 = fract(seed.x * 13.37 + seed.z * 7.91);
    float h2 = fract(seed.y * 17.13 + w * 3.71);
    float h3 = fract(seed.z * 23.70 + seed.x * 5.31);

    // Helical flutter, each flake at its own rate and radius.
    float rate = 0.7 + h1 * 1.5;
    float phase = h2 * 6.2831853;
    float radius = 0.06 + h3 * 0.2;
    p.x += sin(t * rate + phase) * radius;
    p.z += cos(t * rate * 0.87 + phase * 1.3) * radius;
    p.y += sin(t * rate * 1.9 + phase) * 0.035;
    // Gusts: a world-anchored swirl field, so neighbouring flakes move together.
    vec2 wp = mod(CameraBlock.xz, 4096.0) + CameraFrac.xz + p.xz;
    float gust = 0.2 + Extra.y * 0.8;
    p.x += sin(wp.y * 0.13 + t * 0.55 + sin(wp.x * 0.07 - t * 0.3) * 1.7) * 0.45 * gust;
    p.z += cos(wp.x * 0.11 - t * 0.47 + sin(wp.y * 0.09 + t * 0.21) * 1.7) * 0.45 * gust;

    Column c = column(p);
    if (c.type != TYPE_SNOW || p.y <= c.surface) {
        cull();
        return;
    }
    float distance = length(p);
    // Mostly small flakes, a few big clumps.
    float size = mix(0.008, 0.032, h2 * h2 * h2) * Look.z;
    float ppm = pixelsPerMeter(distance);
    float coverage = clamp(size * ppm / 1.1, 0.0, 1.0);
    coverage *= coverage;
    size = max(size, 1.1 / ppm);
    // Flakes right in front of the lens are out of focus: bigger, softer, fainter.
    float bokeh = 1.0 - smoothstep(0.35, 1.7, distance);
    size *= 1.0 + bokeh * 2.2;

    float alpha = Look.y * coverage * boxFade(layer, p) * smoothstep(0.0, 0.3, p.y - c.surface)
            * mix(1.0, 0.42, bokeh) * smoothstep(0.12, 0.35, distance);

    // A flat crystal: spins in its plane and tilts as it falls.
    float spin = t * (h1 - 0.5) * 2.4 + phase;
    float tilt = 0.5 + 0.5 * abs(cos(t * (0.5 + h3) + phase));
    vec2 q = corner * 2.0 - 1.0;
    vec2 s = vec2(q.x, q.y * tilt);
    vec2 r = vec2(cos(spin) * s.x - sin(spin) * s.y, sin(spin) * s.x + cos(spin) * s.y);
    vec3 right = vec3(ModelViewMat[0][0], ModelViewMat[1][0], ModelViewMat[2][0]);
    vec3 up = vec3(ModelViewMat[0][1], ModelViewMat[1][1], ModelViewMat[2][1]);
    vec3 pos = p + (right * r.x + up * r.y) * size;

    // Snow scatters light: brightened like vanilla's snow (light levels pulled towards full).
    vec3 light = lightAt((c.block * 3 + 15) / 4, (c.sky * 3 + 15) / 4);
    float px = size * ppm;
    float crystal = step(0.62, h1) * smoothstep(7.0, 18.0, px) * (1.0 - bokeh);
    float sparkle = pow(max(sin(t * (1.3 + h3 * 2.6) + h1 * 40.0), 0.0), 36.0) * step(0.6, h3);
    tint = vec4(light * vec3(0.97, 0.98, 1.0), alpha);
    shape = vec4(crystal, bokeh, h2, sparkle);
    local = q;
    emit(pos);
}

// ---- Splashes: crowns of droplets on the ground, rings on water ---------------------------------
void splash(int layer, vec3 seed, float w, vec2 corner) {
    float t = CameraBlock.w;
    float period = 0.4 + fract(seed.z * 11.3) * 0.35;
    float cycle = t / period + w;
    float k = floor(cycle);
    float age = cycle - k;

    // A new place every cycle, anchored in the world.
    vec4 a = Layers[layer * 2];
    vec4 b = Layers[layer * 2 + 1];
    float size = a.w;
    vec2 h = hash22(seed.xy * 157.31 + vec2(k * 0.6180339, k * 0.3819660));
    vec2 xz = h * size - b.xz + size * 0.5;
    xz = xz - floor(xz / size) * size - size * 0.5;
    vec3 base = vec3(xz.x, 0.0, xz.y);
    Column c = column(base);
    if (c.type != TYPE_RAIN || c.hot || abs(c.surface) > 14.0) {
        kind = SPLASH;
        cull();
        return;
    }
    base.y = c.surface;
    float distance = length(base);
    float fade = (1.0 - smoothstep(size * 0.3, size * 0.5, length(xz))) * smoothstep(0.4, 1.2, distance);
    vec2 q = corner * 2.0 - 1.0;
    vec3 pos;
    if (c.water) {
        kind = RIPPLE;
        float radius = 0.16 + fract(seed.x * 5.1) * 0.14;
        pos = base + vec3(q.x * radius, 0.012, q.y * radius);
        local = q;
    } else {
        kind = SPLASH;
        vec3 right = vec3(ModelViewMat[0][0], 0.0, ModelViewMat[2][0]);
        right = length(right) > 1.0e-4 ? normalize(right) : vec3(1.0, 0.0, 0.0);
        pos = base + right * q.x * 0.12 + vec3(0.0, corner.y * 0.12 - 0.005, 0.0);
        local = vec2(q.x, corner.y);
    }
    vec3 light = max(lightAt(c.block, c.sky), vec3(0.08));
    tint = vec4(light * vec3(0.88, 0.92, 1.0) * 1.1, Look.w * fade * (0.75 + 0.5 * fract(w * 9.1)));
    shape = vec4(age, h, fract(seed.z * 3.7));
    emit(pos);
}

void main() {
    int layer = int(Color.b * 255.0 + 0.5);
    vec2 corner = Color.rg;
    float w = Color.a;
    if (layer <= RAIN_FAR) {
        rain(layer, Position, w, corner);
    } else if (layer <= SNOW_FAR) {
        snow(layer, Position, w, corner);
    } else {
        splash(layer, Position, w, corner);
    }
}
