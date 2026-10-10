#version 330
#extension GL_ARB_separate_shader_objects : require

// Depth of Field (PostFx) – a thin-lens camera over the finished world, after the haze, rainbows and
// sun rays, before Motion Blur and the hand (which stays sharp).
//
// Blur is measured as the circle of confusion (CoC): how big a point at some distance is smeared on
// the sensor. For a thin lens it's proportional to |1/focus − 1/distance|, so everything here works in
// diopters (1/blocks): the sky is 0, focus pulls are even, and near objects blur faster than far ones.
// Signed: negative in front of the focus plane, positive behind it.
//
//   FOCUS      1×1: autofocus. What's under the crosshair, steadied by the surface around it (samples
//              much nearer or further than the centre are ignored), eased from last frame's focus.
//   PREPARE    half resolution: the frame in linear light with its highlights expanded (so bright
//              points become bright discs, as they would on film) and the signed CoC; where a texel
//              straddles an edge, the nearer layer wins and only its pixels are averaged.
//   TILE       per 8×8 half-res texels: the biggest foreground CoC.
//   DILATE     spreads it to neighbouring tiles its blur can reach, so pixels next to a blurry
//              foreground know to gather it (foreground blur spills over sharp backgrounds, as it does
//              in a real lens; background blur never spills over a sharper foreground).
//   PREFILTER  each texel's light spread over the gather's tap spacing, within its layer.
//   GATHER     half resolution, scatter as gather: taps in a golden-angle spiral over the aperture's
//              shape (circle, hexagon, anamorphic oval, or cat's eye towards the frame's edges), scaled
//              to the larger of this texel's CoC and the foreground reaching it. A tap counts if its
//              own CoC covers this texel (per colour channel, slightly different – bokeh fringing),
//              weighted by its light density 1/CoC². Three layers: this texel's own (with the opacity
//              its blur leaves it), what's behind it (showing through a defocused rim) and what's in
//              front (laid over both). Output premultiplied over the sharp frame.
//   FILTER     CoC-aware smoothing of the gather's noise, wider where the blur is bigger.
//   COMPOSITE  full resolution: the four nearest half-res texels weighted by how alike their CoC is to
//              this pixel's own (from the full-resolution depth, so edges in focus stay crisp), laid
//              over the sharp frame, highlights put back. Optional focus assist: peaking or a CoC map.

#if defined(FOCUS)
uniform sampler2D InSampler;    // last frame's focus (1×1)
uniform sampler2D DepthSampler;
#elif defined(PREPARE)
uniform sampler2D InSampler;    // the frame
uniform sampler2D DepthSampler;
uniform sampler2D FocusSampler;
#elif defined(TILE) || defined(DILATE) || defined(PREFILTER)
uniform sampler2D InSampler;
#elif defined(GATHER)
uniform sampler2D InSampler;    // PREFILTER's output
uniform sampler2D TileSampler;  // DILATE's output
#elif defined(FILTER)
uniform sampler2D InSampler;    // GATHER's output
uniform sampler2D CocSampler;   // PREFILTER's output (its alpha: the CoC)
#elif defined(COMPOSITE)
uniform sampler2D InSampler;    // a copy of the sharp frame
uniform sampler2D BlurSampler;  // FILTER's output
uniform sampler2D CocSampler;   // PREFILTER's output (its alpha: the CoC)
uniform sampler2D DepthSampler;
uniform sampler2D FocusSampler;
#endif

// Mirrors PostFx#writeDof (std140, mat4 + 7 x vec4).
layout(std140) uniform LiquidDof {
    mat4 InvProj;  // NDC -> view space (camera at the origin, looking down -z)
    vec4 Lens;     // x CoC per diopter (half-res px), y largest CoC (half-res px), z near blur (0/1), w focus mode (0 auto, 1 manual, 2 tilt-shift)
    vec4 Focus;    // x manual focus (diopters), y autofocus easing this frame (1 = jump), z tilt band centre (uv y), w tilt band half-height (uv)
    vec4 Bokeh;    // x shape (0 circle, 1 hexagon, 2 anamorphic, 3 cat's eye), y highlight expansion (0..0.9), z fringing, w most taps
    vec4 Screen;   // xy full-resolution size, zw half-resolution size
    vec4 Tiles;    // xy tile texture size, z tile size (half-res texels), w dilation reach (tiles)
    vec4 View;     // x aspect (w / h), y cat's eye amount, z focus assist (0 off, 1 peaking, 2 CoC map), w noise seed (Photo Mode)
    vec4 Point;    // xy where autofocus looks (uv: the centre, or Photo Mode's focus point), zw unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float TAU = 6.28318530718;
const float GOLDEN_ANGLE = 2.39996322973;
const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

float ign(vec2 pixel) {
    return fract(52.9829189 * fract(dot(pixel, vec2(0.06711056, 0.00583715))));
}

#if defined(FOCUS) || defined(PREPARE) || defined(COMPOSITE)
// Distance along the view axis, in blocks.
float viewDepth(vec2 uv, float depth) {
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = depth;
    #else
    float z = depth * 2.0 - 1.0;
    #endif
    vec4 view = InvProj * vec4(uv * 2.0 - 1.0, z, 1.0);
    return -view.z / view.w;
}

// 1 / distance; the sky (depth cleared to 0) is infinitely far.
float diopters(vec2 uv, float depth) {
    if (depth <= 0.0) {
        return 0.0;
    }
    return 1.0 / max(viewDepth(uv, depth), 0.05);
}
#endif

#if defined(PREPARE) || defined(COMPOSITE)
// Signed CoC in half-res pixels: negative in front of the focus, positive behind it.
float signedCoc(vec2 uv, float depth, float focus) {
    float coc;
    if (Lens.w > 1.5) {
        // Tilt-shift: a sharp horizontal band, blur growing above and below it whatever the depth –
        // the miniature look. Both sides count as background, so neither bleeds over the band.
        float outside = abs(uv.y - Focus.z) - Focus.w;
        coc = Lens.y * smoothstep(0.0, 0.42, outside);
    } else {
        coc = (focus - diopters(uv, depth)) * Lens.x;
        if (Lens.z < 0.5) {
            coc = max(coc, 0.0);
        }
    }
    return clamp(coc, -Lens.y, Lens.y);
}
#endif

#ifdef FOCUS
// Autofocus points in screen heights around the crosshair: the centre, a ring, a wider ring.
const int AF_POINTS = 13;
const vec2 AF[13] = vec2[13](
    vec2(0.0, 0.0),
    vec2(0.012, 0.0), vec2(0.006, 0.0104), vec2(-0.006, 0.0104), vec2(-0.012, 0.0), vec2(-0.006, -0.0104), vec2(0.006, -0.0104),
    vec2(0.026, 0.015), vec2(0.0, 0.03), vec2(-0.026, 0.015), vec2(-0.026, -0.015), vec2(0.0, -0.03), vec2(0.026, -0.015)
);
const float AF_WEIGHT[3] = float[3](4.0, 2.0, 1.0);

void main() {
    float target;
    if (Lens.w > 0.5) {
        target = Focus.x;
    } else {
        // The crosshair decides what's in focus; the samples around it on the same surface steady it
        // (a single pixel flickers between a block and what's behind it), the rest are ignored.
        vec2 scale = vec2(1.0 / View.x, 1.0);
        float centre = diopters(Point.xy, texture(DepthSampler, Point.xy).r);
        float tolerance = 0.2 * centre + 0.004;
        float sum = 0.0;
        float weight = 0.0;
        for (int i = 0; i < AF_POINTS; i++) {
            vec2 uv = clamp(Point.xy + AF[i] * scale, vec2(0.001), vec2(0.999));
            float d = diopters(uv, texture(DepthSampler, uv).r);
            float off = (d - centre) / tolerance;
            float w = AF_WEIGHT[i == 0 ? 0 : (i < 7 ? 1 : 2)] * exp(-off * off);
            sum += d * w;
            weight += w;
        }
        target = sum / max(weight, 1.0e-4);
    }
    float previous = texelFetch(InSampler, ivec2(0), 0).r;
    // Eased in diopters, like turning a focus ring; a reset (or a broken previous value) jumps.
    float eased = Focus.y >= 1.0 || isnan(previous) || isinf(previous) ? target : mix(previous, target, Focus.y);
    fragColor = vec4(eased, target, 0.0, 1.0);
}
#endif

#if defined(PREPARE) || defined(COMPOSITE)
// Highlights start this bright (linear light, ≈ 0.8 in the gamma-encoded frame).
const float HIGHLIGHT_START = 0.6;

// 1 up to the highlights, then rising to 1 + Bokeh.y at full brightness.
float gain(float peak) {
    float t = max(peak - HIGHLIGHT_START, 0.0) / (1.0 - HIGHLIGHT_START);
    return 1.0 + Bokeh.y * t * t;
}

// How bright a colour looks for this: between its luminance and its brightest channel, so warm
// lamps and lava count but a saturated red texture doesn't.
float brightness(vec3 linear) {
    return 0.5 * (dot(linear, LUMA) + max(linear.r, max(linear.g, linear.b)));
}

// Into linear light (the frame is gamma-encoded; light mixes linearly, so bright things keep their
// weight in the blur), then highlights – lamps, lava, the sun, stars – made many times brighter,
// like the HDR light a real lens sees; mid-tones are left alone. COMPOSITE undoes both.
vec3 expand(vec3 c) {
    vec3 linear = pow(max(c, vec3(0.0)), vec3(2.2));
    return linear * gain(min(brightness(linear), 1.0));
}
#endif

#ifdef PREPARE
void main() {
    float focus = texelFetch(FocusSampler, ivec2(0), 0).r;
    ivec2 half_ = ivec2(texCoord * Screen.zw);
    ivec2 limit = ivec2(Screen.xy) - 1;
    vec3 colors[4];
    float cocs[4];
    float nearest = 1.0e6;
    for (int i = 0; i < 4; i++) {
        ivec2 p = min(half_ * 2 + ivec2(i & 1, i >> 1), limit);
        vec2 uv = (vec2(p) + 0.5) / Screen.xy;
        colors[i] = expand(texelFetch(InSampler, p, 0).rgb);
        cocs[i] = signedCoc(uv, texelFetch(DepthSampler, p, 0).r, focus);
        nearest = min(nearest, cocs[i]);
    }
    // Only the pixels of the layer we keep: a sharp leaf's colour mustn't be smeared as background.
    float tolerance = 1.0 + 0.15 * abs(nearest);
    vec3 sum = vec3(0.0);
    float weight = 0.0;
    for (int i = 0; i < 4; i++) {
        float w = clamp(2.0 - (cocs[i] - nearest) / tolerance, 0.0, 1.0);
        sum += colors[i] * w;
        weight += w;
    }
    fragColor = vec4(sum / weight, nearest);
}
#endif

#ifdef TILE
void main() {
    ivec2 tile = ivec2(texCoord * Tiles.xy);
    ivec2 base = tile * int(Tiles.z);
    ivec2 limit = ivec2(Screen.zw) - 1;
    float nearMax = 0.0;
    for (int y = 0; y < 8; y++) {
        for (int x = 0; x < 8; x++) {
            nearMax = max(nearMax, -texelFetch(InSampler, min(base + ivec2(x, y), limit), 0).a);
        }
    }
    fragColor = vec4(nearMax, 0.0, 0.0, 1.0);
}
#endif

#ifdef DILATE
void main() {
    ivec2 tile = ivec2(texCoord * Tiles.xy);
    ivec2 limit = ivec2(Tiles.xy) - 1;
    int reach = int(Tiles.w + 0.5);
    float nearMax = 0.0;
    for (int y = -6; y <= 6; y++) {
        if (abs(y) > reach) {
            continue;
        }
        for (int x = -6; x <= 6; x++) {
            if (abs(x) > reach) {
                continue;
            }
            float n = texelFetch(InSampler, clamp(tile + ivec2(x, y), ivec2(0), limit), 0).r;
            // It reaches this tile only if it's wider than the tiles in between.
            float gap = float(max(max(abs(x), abs(y)) - 1, 0)) * Tiles.z;
            if (n > gap) {
                nearMax = max(nearMax, n);
            }
        }
    }
    fragColor = vec4(nearMax, 0.0, 0.0, 1.0);
}
#endif

#ifdef PREFILTER
// Each texel's light spread over about the distance between the gather's taps (same layer only), so
// every tap that lands on a small bright light's disc catches some of it: smooth bokeh, not speckles.
void main() {
    vec4 centre = texture(InSampler, texCoord);
    float cc = centre.a;
    float size = min(abs(cc), Lens.y);
    float taps = clamp(size * size * 1.25 + 8.0, 8.0, Bokeh.w);
    float radius = size * sqrt(3.14159265 / taps) * 0.9;
    if (radius < 0.75) {
        fragColor = centre;
        return;
    }
    vec2 texel = radius / Screen.zw;
    float tolerance = 1.0 + 0.2 * abs(cc);
    vec3 sum = centre.rgb;
    float weight = 1.0;
    for (int i = 0; i < 12; i++) {
        float t = sqrt((float(i) + 0.5) / 12.0);
        float angle = float(i) * GOLDEN_ANGLE;
        vec4 s = texture(InSampler, texCoord + vec2(cos(angle), sin(angle)) * t * texel);
        float w = clamp(1.0 - abs(s.a - cc) / tolerance, 0.0, 1.0);
        sum += s.rgb * w;
        weight += w;
    }
    fragColor = vec4(sum / weight, cc);
}
#endif

#ifdef GATHER
// Radius of a regular polygon inscribed in the unit circle, in the direction of angle (aperture blades).
float polygon(float angle, float blades) {
    float sector = TAU / blades;
    float a = mod(angle + 0.26, sector) - sector * 0.5;
    return cos(sector * 0.5) / cos(a);
}

void main() {
    vec2 texel = 1.0 / Screen.zw;
    vec4 centre = texture(InSampler, texCoord);
    float cc = centre.a;
    // This texel's own tile (the tile texture is rounded up, so its uv doesn't line up with ours).
    ivec2 tile = min(ivec2(texCoord * Screen.zw) / int(Tiles.z), ivec2(Tiles.xy) - 1);
    float radius = min(max(abs(cc), texelFetch(TileSampler, tile, 0).r), Lens.y);
    if (radius < 0.5) {
        fragColor = vec4(0.0); // in focus, nothing in front: the sharp frame as it is
        return;
    }
    // About one tap per 0.8 px² of aperture, as many as the quality allows.
    int taps = int(clamp(radius * radius * 1.25 + 8.0, 8.0, Bokeh.w));
    int shape = int(Bokeh.x + 0.5);
    vec2 squeeze = shape == 2 ? vec2(0.6, 1.0) : vec2(1.0);
    // Cat's eye: towards the edges of the frame the lens barrel cuts the aperture into a lemon shape,
    // its long side along the edge, as on fast vintage lenses.
    vec2 fromCentre = (texCoord - 0.5) * vec2(View.x, 1.0) * 2.0 / length(vec2(View.x, 1.0));
    vec2 barrel = shape == 3 ? fromCentre * View.y : vec2(0.0);
    vec3 fringeFar = vec3(1.0 - Bokeh.z, 1.0 + Bokeh.z, 1.0 - Bokeh.z);  // green rims behind the focus
    vec3 fringeNear = vec3(1.0 + Bokeh.z, 1.0 - Bokeh.z, 1.0 + Bokeh.z); // magenta rims in front of it
    // A new pattern every frame of a Photo Mode exposure, so averaging them removes the noise.
    float rotation = ign(gl_FragCoord.xy + View.w * vec2(5.588238, 3.71)) * TAU;
    float layer = max(1.0, 0.1 * abs(cc));
    float own = abs(cc);

    // Scatter as gather, in three layers relative to this texel: in front of it, at its depth, behind
    // it. A point with CoC r spreads its light over π r², so a tap that covers this texel counts
    // (R / r)² (capped: tiny CoCs are hit too rarely to trust a huge weight).
    //   own layer  its colour, and its opacity: how much of this texel's own disc it covers – a
    //              defocused object is see-through at its rim, as with a real lens;
    //   behind     what shows through there: the colour around, whatever its blur;
    //   in front   laid over both with the coverage it adds up to (foreground blur spills outwards).
    float centreWeight = min(radius * radius / max(cc * cc, 0.25), 24.0);
    vec3 ownSum = centre.rgb * centreWeight;
    vec3 ownWeight = vec3(centreWeight);
    float ownTaps = 1.0;
    float ownCovered = 1.0;
    vec3 behindSum = vec3(0.0);
    float behindWeight = 0.0;
    vec3 nearSum = vec3(0.0);
    vec3 nearWeight = vec3(0.0);
    float used = 0.0;
    for (int i = 0; i < 128; i++) {
        if (i >= taps) {
            break;
        }
        float t = sqrt((float(i) + 0.5) / float(taps));
        float angle = float(i) * GOLDEN_ANGLE + rotation;
        float r = shape == 1 ? t * polygon(angle, 6.0) : t;
        vec2 unit = vec2(cos(angle), sin(angle)) * r;
        if (shape == 3 && length(unit + barrel) > 1.0) {
            continue;
        }
        used += 1.0;
        // Distance to this tap measured in the aperture's own shape.
        float dist = t * radius;
        vec4 s = texture(InSampler, texCoord + unit * squeeze * radius * texel);
        float size = abs(s.a);
        vec3 cover = smoothstep(vec3(dist - 0.5), vec3(dist + 0.5), size * (s.a > 0.0 ? fringeFar : fringeNear));
        vec3 w = cover * min(radius * radius / max(size * size, 0.25), 24.0);
        if (s.a < cc - layer) {
            nearSum += s.rgb * w;
            nearWeight += w;
            continue;
        }
        bool behind = s.a > cc + layer;
        if (behind) {
            float b = 1.0 / (1.0 + dist);
            behindSum += s.rgb * b;
            behindWeight += b;
        } else {
            ownSum += s.rgb * w;
            ownWeight += w;
        }
        if (dist < own) {
            ownTaps += 1.0;
            ownCovered += behind ? 0.0 : cover.g;
        }
    }
    vec3 ownColor = ownSum / ownWeight;
    float ownAlpha = behindWeight > 0.0 ? clamp(ownCovered / ownTaps, 0.0, 1.0) : 1.0;
    vec3 base = mix(behindSum / max(behindWeight, 1.0e-4), ownColor, ownAlpha);
    vec3 near = nearSum / max(nearWeight, vec3(1.0e-4));
    vec3 nearAlpha = clamp(nearWeight / max(used, 1.0), 0.0, 1.0);
    // Premultiplied over the sharp frame: final = sharp · (1 − a) + rgb.
    float blur = smoothstep(0.4, 1.4, abs(cc));
    vec3 premultiplied = base * blur * (1.0 - nearAlpha) + near * nearAlpha;
    float alpha = 1.0 - (1.0 - blur) * (1.0 - nearAlpha.g);
    fragColor = vec4(premultiplied, alpha);
}
#endif

#ifdef FILTER
void main() {
    vec2 texel = 1.0 / Screen.zw;
    vec4 centre = texture(InSampler, texCoord);
    float cc = texture(CocSampler, texCoord).a;
    // Wider where the blur is bigger (its taps are further apart there), never across layers.
    float stride = clamp(abs(cc) * 0.12, 1.0, 3.0);
    float tolerance = 1.0 + 0.2 * abs(cc);
    vec4 sum = centre * 4.0;
    float weight = 4.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            if (x == 0 && y == 0) {
                continue;
            }
            vec2 uv = texCoord + vec2(x, y) * stride * texel;
            float w = (x == 0 || y == 0) ? 2.0 : 1.0;
            w *= clamp(1.0 - abs(texture(CocSampler, uv).a - cc) / tolerance, 0.0, 1.0);
            sum += texture(InSampler, uv) * w;
            weight += w;
        }
    }
    fragColor = sum / weight;
}
#endif

#ifdef COMPOSITE
// Undoes expand(): brightness · gain(brightness) = brightness(e) (brightness scales with the colour),
// solved by Newton's method (from above; the curve is convex, so it converges without overshooting),
// then back to gamma.
vec3 compress(vec3 e) {
    float target = brightness(e);
    float peak = min(target, 1.0);
    for (int i = 0; i < 6; i++) {
        float t = max(peak - HIGHLIGHT_START, 0.0) / (1.0 - HIGHLIGHT_START);
        float g = 1.0 + Bokeh.y * t * t;
        float slope = g + peak * 2.0 * Bokeh.y * t / (1.0 - HIGHLIGHT_START);
        peak -= (peak * g - target) / slope;
    }
    return pow(max(e / gain(clamp(peak, 0.0, 1.0)), vec3(0.0)), vec3(1.0 / 2.2));
}

void main() {
    vec3 sharp = texture(InSampler, texCoord).rgb;
    float focus = texelFetch(FocusSampler, ivec2(0), 0).r;
    float coc = signedCoc(texCoord, texture(DepthSampler, texCoord).r, focus);

    // The four half-resolution texels around this pixel, bilinear and by how alike their CoC is,
    // so a sharp edge never picks up the blur of the layer next to it.
    vec2 pos = texCoord * Screen.zw - 0.5;
    vec2 base = floor(pos);
    vec2 f = pos - base;
    ivec2 limit = ivec2(Screen.zw) - 1;
    float tolerance = 1.0 + 0.2 * abs(coc);
    vec4 sum = vec4(0.0);
    float weight = 0.0;
    for (int j = 0; j <= 1; j++) {
        for (int i = 0; i <= 1; i++) {
            ivec2 q = clamp(ivec2(base) + ivec2(i, j), ivec2(0), limit);
            float bilinear = (i == 0 ? 1.0 - f.x : f.x) * (j == 0 ? 1.0 - f.y : f.y);
            float w = bilinear * (exp(-abs(texelFetch(CocSampler, q, 0).a - coc) / tolerance) + 1.0e-3);
            sum += texelFetch(BlurSampler, q, 0) * w;
            weight += w;
        }
    }
    vec4 blurred = sum / max(weight, 1.0e-6);
    // Premultiplied over the sharp frame, in the expanded light; pixels nothing blurs stay untouched.
    vec3 color = sharp;
    if (blurred.a > 0.002) {
        color = compress(expand(sharp) * (1.0 - clamp(blurred.a, 0.0, 1.0)) + blurred.rgb);
    }

    int assist = int(View.z + 0.5);
    if (assist == 1) {
        // Focus peaking: crisp edges in the focus plane light up, like on a cinema camera's monitor.
        vec2 px = 1.0 / Screen.xy;
        float l = dot(sharp, LUMA);
        float edge = abs(dot(texture(InSampler, texCoord + vec2(px.x, 0.0)).rgb, LUMA) - l)
                + abs(dot(texture(InSampler, texCoord - vec2(px.x, 0.0)).rgb, LUMA) - l)
                + abs(dot(texture(InSampler, texCoord + vec2(0.0, px.y)).rgb, LUMA) - l)
                + abs(dot(texture(InSampler, texCoord - vec2(0.0, px.y)).rgb, LUMA) - l);
        float inFocus = 1.0 - smoothstep(0.3, 1.0, abs(coc));
        color = mix(color, vec3(1.0, 0.22, 0.37), smoothstep(0.1, 0.3, edge) * inFocus);
    } else if (assist == 2) {
        // CoC map: blue in front of the focus, amber behind it, the focus plane in grey.
        float amount = sqrt(clamp(abs(coc) / max(Lens.y, 1.0e-3), 0.0, 1.0));
        float l = dot(sharp, LUMA);
        vec3 tint = coc < 0.0 ? vec3(0.04, 0.52, 1.0) : vec3(1.0, 0.62, 0.04);
        color = mix(vec3(l), tint * (0.35 + 0.65 * l), amount);
    }
    fragColor = vec4(color, 1.0);
}
#endif
