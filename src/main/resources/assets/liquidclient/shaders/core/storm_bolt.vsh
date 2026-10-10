#version 330
#extension GL_ARB_separate_shader_objects : require

// Storm Chaser – a lightning channel (render.storm.StormRenderer). A static buffer of quads whose
// vertices carry only (segment index, corner); the segments themselves are in the uniform block
// (render.storm.BoltGenerator), so a new bolt is just new uniform data.
//
// Each segment becomes a ribbon facing the camera: a white-hot core at least a pixel wide (thinner
// than that it widens and fades instead, so it never shimmers) inside a coloured halo several times
// wider. The stepped leader shows only what it has reached; branches light with the first stroke
// only; the channel's top fades into the cloud base. Bolts far beyond the terrain are drawn on a
// sphere just inside the fog's end (the direction kept, so they're occluded by any terrain in front
// and never clipped by the far plane); their light is dimmed by the rain between. Order 3 marks the
// ground glow: a camera-facing sprite where the bolt struck.

#include <liquidclient:storm_bolt.glsl>

layout(location = 0) in vec3 Position; // x segment, y corner across, z corner along

layout(location = 0) out vec2 local;
layout(location = 1) out vec3 tint;
layout(location = 2) flat out vec2 shape; // x core share of the half-width, y sprite (1) or ribbon (0)

vec3 squeeze(vec3 p) {
    float d = length(p);
    return d > Screen.z ? p * (Screen.z / d) : p;
}

float hash11(float n) {
    return fract(sin(n * 12.9898) * 43758.5453);
}

void cull() {
    gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    local = vec2(0.0);
    tint = vec3(0.0);
    shape = vec2(0.0);
}

void main() {
    int i = int(Position.x + 0.5);
    vec4 a = Seg[i * 2];
    vec4 b = Seg[i * 2 + 1];
    uint bits = uint(b.w + 0.5);
    int order = int(bits >> 22u);
    float arrival = float((bits >> 12u) & 1023u) / 1023.0;
    float bright = float((bits >> 4u) & 255u) / 255.0;
    float ratio = float(bits & 15u) / 15.0;
    vec2 corner = Position.yz;

    vec3 A = Origin.xyz + a.xyz;
    if (order == 3) {
        // The ground glow: a sprite at the strike point, lifted towards the camera so the ground
        // doesn't swallow it.
        if (Envelope.w <= 0.001) {
            cull();
            return;
        }
        float distance = length(A);
        vec3 toward = A / max(distance, 1.0e-3);
        vec3 p = squeeze(A - toward * min(a.w * 0.8, distance * 0.5));
        float d = length(p);
        float ppm = Screen.x * 0.5 * Screen.y / max(d, 0.05);
        float radius = max(a.w, 7.0 / ppm);
        vec3 right = normalize(cross(toward, vec3(0.0, 1.0, 0.0)) + vec3(1.0e-5, 0.0, 0.0));
        vec3 up = cross(right, toward);
        vec2 q = corner * 2.0 - 1.0;
        gl_Position = ViewProj * vec4(p + (right * q.x + up * q.y) * radius, 1.0);
        local = q;
        tint = vec3(Envelope.w * exp(-distance / GlowColor.w));
        shape = vec2(0.0, 1.0);
        return;
    }
    // The leader shows only what it has reached; then everything is lit.
    if (arrival > Envelope.z) {
        cull();
        return;
    }
    vec3 B = Origin.xyz + b.xyz;
    vec3 pa = squeeze(A);
    vec3 pb = squeeze(B);
    vec3 axis = pb - pa;
    vec3 mid = (pa + pb) * 0.5;
    vec3 side = cross(axis, mid);
    float sideLength = length(side);
    side = sideLength > 1.0e-6 ? side / sideLength : vec3(1.0, 0.0, 0.0);

    // Core half-width in blocks, at least ~0.8 px (fainter where it had to be widened).
    vec3 p = mix(pa, pb, corner.y);
    float d = max(length(p), 0.05);
    float ppm = Screen.x * 0.5 * Screen.y / d;
    float width = a.w * mix(1.0, ratio, corner.y);
    float coverage = clamp(width * ppm / 0.8, 0.0, 1.0);
    float core = max(width, 0.8 / ppm);
    float halo = max(core * CoreColor.w, 4.5 / ppm);

    float trueDistance = length(mix(A, B, corner.y));
    float intensity = (order == 0 ? Envelope.x : Envelope.y) * bright;
    // a little life along the channel: segments aren't all equally bright
    intensity *= 0.82 + 0.36 * hash11(float(i) * 1.37);
    // into the cloud base: the channel disappears into the cloud
    float y = mix(A.y, B.y, corner.y);
    intensity *= 1.0 - smoothstep(Origin.w - 40.0, Origin.w + 30.0, y);
    // the rain between swallows far bolts
    intensity *= exp(-trueDistance / GlowColor.w);

    gl_Position = ViewProj * vec4(p + side * (corner.x * 2.0 - 1.0) * halo, 1.0);
    local = vec2(corner.x * 2.0 - 1.0, corner.y);
    tint = vec3(intensity * coverage, intensity, 0.0);
    shape = vec2(core / halo, 0.0);
}
