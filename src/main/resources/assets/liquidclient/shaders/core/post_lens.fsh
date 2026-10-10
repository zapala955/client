#version 330
#extension GL_ARB_separate_shader_objects : require

// Wet Lens (PostFx) – water on the camera, drawn over the finished frame (hand included, GUI not)
// from a copy of it. Everything is procedural and a function of time, so it costs one pass.
//
//   beads   rain lands as round beads; each is a tiny lens, so the world shows up inside it
//           upside-down and wide-angle, darker at the rim with a bright highlight; they evaporate
//   runs    bigger drops slide down in fits and starts, leaving a trail of droplets
//   drench  after surfacing: a film of water sheeting down, full of runs, draining from the top
//   frost   snow frosts the lens from the edges: icy, blurred, crystalline
//
// Work space p = (u · aspect, v): one unit is the screen height, so drops stay round. v grows
// upwards (texCoord = NDC · 0.5 + 0.5), so "down the lens" is -v.

uniform sampler2D InSampler;

// Mirrors PostFx#writeLens (std140, 3 x vec4).
layout(std140) uniform LiquidLens {
    vec4 Timing; // x time (s), y aspect (width / height), z rain wetness 0..1, w drench 0..1
    vec4 Look;   // x frost 0..1, y amount, z drop size, w refraction
    vec4 Pixel;  // xy one pixel in uv, z drain progress 0..1 (drench), w unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

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
        p = p * 2.03 + vec2(1.7, 9.2);
        amp *= 0.5;
    }
    return v / 0.9375;
}

// What the drops do to this pixel. The first (top-most) drop covering it wins.
struct Lens {
    vec2 offset; // where to look instead, in p units
    float cover;
    float rim;
    float glint;
};

// A drop centred d away (p units) with this radius. Like a real bead it's a little convex lens: the
// pixel sees the world at centre - d · magnify, i.e. flipped and much wider than the drop itself.
void drop(inout Lens lens, vec2 d, float radius, float magnify) {
    float r = length(d) / radius;
    if (r >= 1.0 || lens.cover >= 0.999) {
        return;
    }
    float aa = Pixel.y * 1.5 / radius;
    float w = (1.0 - smoothstep(1.0 - aa, 1.0, r)) * (1.0 - lens.cover);
    lens.offset += -d * (1.0 + magnify) * w;
    lens.cover += w;
    lens.rim += smoothstep(0.5, 1.0, r) * w;
    lens.glint += (1.0 - smoothstep(0.0, 0.3, length(d / radius - vec2(-0.34, 0.4)))) * w;
}

// Beads: one per grid cell at most; each lands at a random moment and evaporates over its life,
// then another lands somewhere else in the cell.
void beads(inout Lens lens, vec2 p, float cells, float density, float seed) {
    vec2 g = p * cells;
    vec2 id = floor(g);
    vec2 f = fract(g) - 0.5;
    vec3 h = hash32(id + seed * 17.31);
    // Cells beyond the wetness have no bead; drying shrinks them away instead of popping.
    float present = 1.0 - smoothstep(density - 0.15, density, h.z);
    if (present <= 0.0) {
        return;
    }
    float life = mix(5.0, 11.0, fract(h.x * 7.13));
    float cycle = Timing.x / life + h.y;
    float age = fract(cycle);
    vec3 spot = hash32(id + floor(cycle) * 3.7 + seed);
    float radius = mix(0.13, 0.34, spot.x * spot.x) * smoothstep(0.0, 0.012, age) * (1.0 - age * 0.55) * present;
    if (radius < 0.02) {
        return;
    }
    vec2 c = (spot.yz - 0.5) * (0.92 - 2.0 * radius);
    drop(lens, (f - c) / cells, radius / cells, 3.4);
}

// Runs: in each column cell (four columns tall) one drop slides down in fits and starts, then a
// new one forms at the top. Behind it, a trail of small droplets.
void runs(inout Lens lens, vec2 p, float columns, float density, float speed, float seed) {
    vec2 g = vec2(p.x * columns, p.y * columns * 0.25);
    vec2 id = floor(g);
    vec2 f = fract(g) - 0.5;
    vec3 h = hash32(id + seed * 31.7);
    float present = 1.0 - smoothstep(density - 0.2, density, h.z);
    if (present <= 0.0) {
        return;
    }
    float t = Timing.x * speed * (0.6 + 0.8 * h.y) + h.x * 13.0;
    float s = fract(t);
    // Monotonic stick-slip: rushes, nearly stops, rushes again.
    float y = 0.45 - 0.9 * (s + 0.13 * sin(s * 6.2831853));
    float wobble = 0.07 * sin(f.y * 11.0 + h.y * 6.2831853);
    float x = (h.x - 0.5) * 0.45 + wobble;
    float fade = smoothstep(0.0, 0.06, s) * (1.0 - smoothstep(0.88, 1.0, s)) * present;

    // Isotropic column units: a cell is 1 wide and 4 tall.
    vec2 d = vec2(f.x - x, (f.y - y) * 4.0);
    float radius = mix(0.2, 0.3, h.y) * fade;
    if (radius > 0.02) {
        vec2 shaped = vec2(d.x, d.y > 0.0 ? d.y * 0.62 : d.y * 1.05); // a tail stretching upwards
        drop(lens, shaped / columns, radius / columns, 2.6);
    }

    // Trail: droplets left where it passed (above it), drying out further up.
    float above = (f.y - y) * 4.0;
    if (above > 0.25 && above < 3.4) {
        float rows = 12.0;
        vec2 tid = vec2(id.x, floor(g.y * rows));
        vec3 th = hash32(tid + seed * 5.3 + floor(t) * 1.7);
        float ty = (fract(g.y * rows) - 0.5) * 4.0 / rows; // column units
        vec2 td = vec2(f.x - x, ty);
        float tr = 0.06 * (0.6 + 0.7 * th.x) * (1.0 - smoothstep(0.6, 3.4, above)) * step(0.4, th.y) * fade;
        if (tr > 0.01) {
            drop(lens, td / columns, tr / columns, 2.0);
        }
    }
}

void main() {
    vec2 uv = texCoord;
    float aspect = Timing.y;
    vec2 p = vec2(uv.x * aspect, uv.y);
    float size = Look.z;
    float rain = Timing.z * Look.y;
    float drench = Timing.w;

    Lens lens = Lens(vec2(0.0), 0.0, 0.0, 0.0);
    vec2 film = vec2(0.0);
    float sheet = 0.0;
    if (drench > 0.0) {
        // The sheet drains from the top; below the line the lens is still streaming.
        float line = 1.1 - Pixel.z * 1.3;
        sheet = drench * (1.0 - smoothstep(line - 0.08, line + 0.08, uv.y));
        film = vec2(sin(p.y * 21.0 + Timing.x * 6.0 + sin(p.x * 9.0) * 2.0), cos(p.x * 17.0 - p.y * 6.0 + Timing.x * 4.5)) * 0.0045 * sheet;
        runs(lens, p, 11.0 / size, min(1.0, drench * 1.1), 1.8, 7.0);
        runs(lens, p, 21.0 / size, min(1.0, drench * 0.9), 2.4, 8.0);
    }
    if (rain > 0.0) {
        runs(lens, p, 12.0 / size, min(1.0, rain * 0.5), 0.17, 1.0);
        beads(lens, p, 8.0 / size, min(1.0, rain * 0.45), 2.0);
        beads(lens, p, 16.0 / size, min(1.0, rain * 0.65), 3.0);
        beads(lens, p, 32.0 / size, min(1.0, rain * 0.85), 4.0);
    }

    vec2 offset = (lens.offset * Look.w + film) / vec2(aspect, 1.0);
    vec2 sampleUv = clamp(uv + offset, Pixel.xy, 1.0 - Pixel.xy);
    vec3 col = texture(InSampler, sampleUv).rgb;
    // Light bends away at a drop's rim; a glint of the sky near its top.
    col *= 1.0 - lens.rim * 0.38;
    col += lens.glint * 0.45 * (0.35 + dot(col, vec3(0.333)));
    col *= 1.0 - sheet * 0.08;

    float frost = Look.x;
    if (frost > 0.0) {
        float edge = min(min(uv.x, 1.0 - uv.x) * aspect, min(uv.y, 1.0 - uv.y));
        float n = fbm(p * 7.0 + 3.1);
        float reach = frost * 0.42;
        float mask = 1.0 - smoothstep(reach * 0.35, reach, edge + (n - 0.5) * 0.14);
        if (mask > 0.0) {
            // Ice crystals: ridged noise, refracting and blurring what's behind them.
            float ridge = 1.0 - abs(fbm(p * 22.0) * 2.0 - 1.0);
            vec2 bend = (vec2(fbm(p * 13.0 + 5.0), fbm(p * 13.0 + 9.0)) - 0.5) * 0.02 * mask;
            vec2 at = clamp(uv + bend, Pixel.xy, 1.0 - Pixel.xy);
            vec3 blur = vec3(0.0);
            for (int i = 0; i < 6; i++) {
                float a = float(i) * 1.0471976;
                blur += texture(InSampler, clamp(at + vec2(cos(a), sin(a)) * Pixel.xy * 5.0, 0.0, 1.0)).rgb;
            }
            blur /= 6.0;
            vec3 ice = blur * 0.7 + vec3(0.62, 0.74, 0.86) * 0.32 + vec3(0.9, 0.96, 1.0) * pow(ridge, 6.0) * 0.35;
            col = mix(col, ice, mask * 0.9);
        }
    }
    fragColor = vec4(col, 1.0);
}
