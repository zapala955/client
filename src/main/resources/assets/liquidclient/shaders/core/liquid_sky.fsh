#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Sky – fragment stage. Replaces the whole overworld sky; everything is a function of the
// view direction. 18 modes (Params2.y) + a cloud pattern layer (Params2.z) + a star style
// (Params2.w). GALAXY_TEXTURE builds sample an equirectangular 4K texture for mode 0.

layout(std140) uniform Fog {
    vec4 FogColor;
    float FogEnvironmentalStart;
    float FogEnvironmentalEnd;
    float FogRenderDistanceStart;
    float FogRenderDistanceEnd;
    float FogSkyEnd;
    float FogCloudsEnd;
};

// Mirrors SkyUniforms.java (std140, 8 x vec4).
layout(std140) uniform LiquidSky {
    vec4 SunDir;      // xyz sun direction,  w = clear-weather factor (1 = no rain)
    vec4 MoonDir;     // xyz moon direction, w = moon phase 0..7 (0 = full)
    vec4 SkyBase;     // rgb vanilla sky colour, w = vanilla star brightness
    vec4 TintColor;   // rgb tint colour, w = tint strength 0..1
    vec4 Params;      // x = time (s, scaled by animation speed), y = star rotation, z = star density, w = horizon blend
    vec4 Params2;     // x = output alpha, y = mode, z = cloud pattern, w = star style
    vec4 SunsetColor; // vanilla sunrise / sunset colour, a = strength
    vec4 Params3;     // x = brightness, y = cloud coverage
};

#ifdef GALAXY_TEXTURE
uniform sampler2D Sampler0;
#endif

layout(location = 0) in vec3 viewRay;

layout(location = 0) out vec4 fragColor;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

// =============================================================================================
// Noise
// =============================================================================================
float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

vec3 hash33(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yxz + 33.33);
    return fract((p.xxy + p.yxx) * p.zyx);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash13(i);
    float b = hash13(i + vec3(1.0, 0.0, 0.0));
    float c = hash13(i + vec3(0.0, 1.0, 0.0));
    float d = hash13(i + vec3(1.0, 1.0, 0.0));
    float e = hash13(i + vec3(0.0, 0.0, 1.0));
    float g = hash13(i + vec3(1.0, 0.0, 1.0));
    float h = hash13(i + vec3(0.0, 1.0, 1.0));
    float k = hash13(i + vec3(1.0, 1.0, 1.0));
    return mix(mix(mix(a, b, f.x), mix(c, d, f.x), f.y), mix(mix(e, g, f.x), mix(h, k, f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 5; i++) {
        v += amp * vnoise3(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.4);
        amp *= 0.5;
    }
    return v;
}

float fbm3(vec3 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 3; i++) {
        v += amp * vnoise3(p);
        p = p * 2.07 + vec3(4.1, 1.3, 7.7);
        amp *= 0.5;
    }
    return v / 0.875;
}

// Voronoi: x = distance to nearest feature, y = distance to second nearest.
vec2 voronoi(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    float d1 = 8.0;
    float d2 = 8.0;
    for (int z = -1; z <= 1; z++) {
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                vec3 o = vec3(float(x), float(y), float(z));
                vec3 r = o + hash33(i + o) - f;
                float d = dot(r, r);
                if (d < d1) {
                    d2 = d1;
                    d1 = d;
                } else if (d < d2) {
                    d2 = d;
                }
            }
        }
    }
    return sqrt(vec2(d1, d2));
}

vec3 rotateX(vec3 v, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(v.x, c * v.y - s * v.z, s * v.y + c * v.z);
}

vec3 rotateY(vec3 v, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(c * v.x + s * v.z, v.y, -s * v.x + c * v.z);
}

float luminance(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

vec3 hue(float h) {
    return clamp(abs(fract(h + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0) - 1.0, 0.0, 1.0);
}

// Seam-free azimuth coordinates for patterns that wrap around the horizon.
vec2 azimuth(vec3 dir) {
    vec2 xz = dir.xz;
    float len = length(xz);
    return len > 1.0e-4 ? xz / len : vec2(1.0, 0.0);
}

// Virtual cloud plane above the camera (perspective-correct towards the horizon).
vec2 skyPlane(vec3 dir) {
    return dir.xz / (max(dir.y, 0.0) + 0.09);
}

float softDisc(vec3 dir, vec3 center, float radius) {
    return smoothstep(cos(radius * 1.2), cos(radius), dot(dir, center));
}

// =============================================================================================
// Stars (4 styles)
// =============================================================================================
float starCell(vec3 dir, float scale, float chance, float time, int style, out vec3 tone) {
    vec3 p = dir * scale;
    vec3 cell = floor(p);
    float h = hash13(cell);
    tone = vec3(1.0);
    if (h > chance) {
        return 0.0;
    }
    vec3 star = 0.25 + 0.5 * hash33(cell + 17.0);
    vec3 d3 = fract(p) - star;
    float d = length(d3);
    float size = mix(0.05, 0.13, hash13(cell + 5.0));
    float core = 1.0 - smoothstep(0.0, size, d);
    core *= core;
    float twinkleSpeed = style == 3 ? 3.5 : 1.3;
    float twinkle = 0.7 + 0.3 * sin(time * (twinkleSpeed + 3.0 * hash13(cell + 2.0)) + h * 61.0);
    if (style == 1) {
        // Sparkle: four-point diffraction cross on the brightest stars.
        float bright = step(0.55, hash13(cell + 9.0));
        float flare = exp(-abs(d3.x) * 60.0) * exp(-abs(d3.y) * 6.0) + exp(-abs(d3.y) * 60.0) * exp(-abs(d3.x) * 6.0);
        core += flare * 0.6 * bright;
    }
    if (style == 3) {
        tone = mix(vec3(1.0), hue(hash13(cell + 11.0)), 0.75);
    } else {
        tone = mix(vec3(0.75, 0.85, 1.0), vec3(1.0, 0.88, 0.75), hash13(cell + 3.0));
    }
    return core * twinkle * (0.35 + 0.65 * hash13(cell + 9.0));
}

vec3 shootingStar(vec3 dir, float time) {
    float period = 3.2;
    float epoch = floor(time / period);
    float f = fract(time / period);
    if (f > 0.35) {
        return vec3(0.0);
    }
    vec3 rnd = hash33(vec3(epoch, 7.0, 3.0));
    vec3 a = normalize(vec3(rnd.x * 2.0 - 1.0, 0.45 + rnd.y * 0.5, rnd.z * 2.0 - 1.0));
    vec3 b = normalize(cross(a, normalize(vec3(rnd.z - 0.5, 0.1, rnd.x - 0.5) + 1.0e-3)));
    float travel = f / 0.35 * 0.55;
    vec3 n = normalize(cross(a, b));
    float off = abs(dot(dir, n));
    float along = atan(dot(dir, b), dot(dir, a));
    float inArc = step(0.0, along) * step(along, travel) * step(0.0, dot(dir, a) + 0.3);
    float tail = smoothstep(travel - 0.22, travel, along);
    float streak = exp(-off * 900.0) * tail * inArc * (1.0 - f / 0.35);
    return vec3(0.9, 0.95, 1.0) * streak * 2.5;
}

vec3 starField(vec3 rdir, vec3 dir, float density, float time, int style) {
    vec3 t1;
    vec3 t2;
    float s1 = starCell(rdir, 95.0, 0.06 * density, time, style, t1);
    float s2 = starCell(rdir, 210.0, 0.11 * density, time, style, t2);
    vec3 col = t1 * s1 + t2 * s2 * 0.6;
    if (style == 2) {
        col += shootingStar(dir, time);
    }
    return col;
}

vec3 milkyWay(vec3 rdir, float time) {
    vec3 bandNormal = normalize(vec3(0.35, 0.25, 0.9));
    float band = exp(-pow(dot(rdir, bandNormal) / 0.30, 2.0));
    if (band < 0.01) {
        return vec3(0.0);
    }
    float n = fbm(rdir * 3.0 + vec3(0.0, 0.0, time * 0.002));
    float tone = fbm3(rdir * 6.0 - 3.1);
    vec3 col = mix(vec3(0.32, 0.16, 0.55), vec3(0.14, 0.34, 0.78), tone);
    col = mix(col, vec3(0.95, 0.55, 0.78), smoothstep(0.55, 0.85, n) * 0.5);
    float dust = smoothstep(0.45, 0.75, fbm3(rdir * 11.0 + 2.0));
    return col * band * (0.2 + 0.9 * n) * (1.0 - 0.65 * dust);
}

// =============================================================================================
// Celestial bodies
// =============================================================================================
vec3 sunWithCorona(vec3 dir, vec3 sun, vec3 color, float radius, float time) {
    float c = dot(dir, sun);
    float disc = smoothstep(cos(radius * 1.15), cos(radius), c);
    float cp = max(c, 0.0);
    // Corona rays: angle around the sun axis, seam-free via a basis.
    vec3 right = normalize(cross(sun, vec3(0.0, 1.0, 0.001)));
    vec3 up = cross(right, sun);
    float ang = atan(dot(dir, up), dot(dir, right));
    float rays = pow(abs(sin(ang * 9.0 + time * 0.15)), 18.0) + pow(abs(sin(ang * 5.0 - time * 0.1)), 10.0) * 0.6;
    float halo = pow(cp, 900.0) * 2.0 + pow(cp, 60.0) * 0.45 + pow(cp, 8.0) * 0.12;
    return color * (disc * 6.0 + halo + rays * pow(cp, 160.0) * 1.4);
}

vec3 moonDisc(vec3 dir) {
    vec3 m = normalize(MoonDir.xyz);
    float radius = 0.034;
    float c = dot(dir, m);
    float glow = pow(max(c, 0.0), 220.0) * 0.18;
    float disc = smoothstep(cos(radius * 1.15), cos(radius), c);
    if (disc <= 0.0) {
        return vec3(0.75, 0.82, 1.0) * glow;
    }
    float k = cos(MoonDir.w * PI / 4.0);
    vec3 right = normalize(cross(m, vec3(0.0, 1.0, 0.0)) + vec3(1.0e-4));
    float x = dot(dir - m * c, right) / radius;
    float side = MoonDir.w < 4.0 ? 1.0 : -1.0;
    float lit = smoothstep(-k - 0.2, -k + 0.2, x * side);
    float craters = 0.75 + 0.25 * fbm3(dir * 90.0);
    return vec3(0.86, 0.9, 1.0) * disc * (0.08 + 0.92 * lit) * craters * 1.6 + vec3(0.75, 0.82, 1.0) * glow;
}

// A low "art direction" sun: vanilla's azimuth, clamped elevation – for stylised modes.
vec3 lowSun(float elevation) {
    vec2 az = normalize(vec2(SunDir.x, SunDir.z) + vec2(1.0e-3, 0.0));
    return normalize(vec3(az.x, elevation, az.y));
}

// =============================================================================================
// Modes. Each returns the sky colour; `day` is 0 at night, 1 at noon.
// =============================================================================================
vec3 modeProcedural(vec3 dir, vec3 rdir, float t, float density, int stars, float day) {
    float up = clamp(dir.y, 0.0, 1.0);
    vec3 dayZenith = vec3(0.04, 0.20, 0.72);
    vec3 dayHorizon = vec3(0.50, 0.80, 1.00);
    vec3 nightZenith = vec3(0.012, 0.012, 0.06);
    vec3 nightHorizon = vec3(0.06, 0.05, 0.16);
    vec3 col = mix(mix(nightHorizon, dayHorizon, day), mix(nightZenith, dayZenith, day), pow(up, 0.5));
    // Sunset band towards the sun, warm magenta-orange.
    vec2 fd = azimuth(dir);
    vec2 fs = azimuth(SunDir.xyz);
    float toward = max(dot(fd, fs), 0.0);
    float low = 1.0 - smoothstep(-0.05, 0.35, abs(SunDir.y));
    vec3 band = mix(vec3(1.0, 0.35, 0.45), vec3(1.0, 0.62, 0.25), toward);
    col = mix(col, band, low * (0.25 + 0.75 * pow(toward, 2.0)) * (1.0 - smoothstep(0.0, 0.5, dir.y)));
    float night = 1.0 - day;
    if (night > 0.01) {
        float visible = smoothstep(-0.05, 0.15, dir.y) * night;
        col += starField(rdir, dir, density, t, stars) * visible;
        col += milkyWay(rdir, t) * visible * (0.35 + 0.65 * density);
    }
    col += sunWithCorona(dir, normalize(SunDir.xyz), vec3(1.0, 0.93, 0.8), 0.05, t) * SunDir.w;
    col += moonDisc(dir) * SunDir.w * (0.35 + 0.65 * night);
    return col;
}

vec3 modePastel(vec3 dir, vec3 rdir, float t, float density, int stars) {
    float up = clamp(dir.y, -0.2, 1.0);
    vec3 top = vec3(0.56, 0.52, 0.88);
    vec3 mid = vec3(1.00, 0.71, 0.78);
    vec3 low = vec3(1.00, 0.83, 0.64);
    vec3 col = up > 0.25 ? mix(mid, top, smoothstep(0.25, 0.9, up)) : mix(low, mid, smoothstep(-0.05, 0.25, up));
    vec3 sun = lowSun(0.12);
    float c = max(dot(dir, sun), 0.0);
    col += vec3(1.0, 0.86, 0.68) * (pow(c, 900.0) * 1.4 + pow(c, 28.0) * 0.28);
    if (dir.y > 0.0) {
        float clouds = fbm(vec3(skyPlane(dir) * 1.3, t * 0.012));
        col = mix(col, vec3(1.0, 0.93, 0.96), smoothstep(0.55, 0.85, clouds) * 0.35 * smoothstep(0.02, 0.25, dir.y));
    }
    col += starField(rdir, dir, density, t, stars) * smoothstep(0.45, 1.0, up) * 0.35;
    return col;
}

vec3 modeFog(vec3 dir, vec3 rdir, float t, float density, int stars, float day) {
    vec3 zenith = mix(vec3(0.02, 0.03, 0.07), vec3(0.18, 0.36, 0.70), day);
    vec3 haze = mix(vec3(0.05, 0.06, 0.10), FogColor.rgb, 0.85);
    float h = pow(1.0 - clamp(dir.y, 0.0, 1.0), 4.0);
    vec3 col = mix(zenith, haze, h);
    vec3 sun = normalize(SunDir.xyz);
    float c = max(dot(dir, sun), 0.0);
    col += mix(vec3(1.0, 0.72, 0.48), vec3(1.0, 0.95, 0.86), day) * (pow(c, 8.0) * 0.35 + pow(c, 64.0) * 0.5) * SunDir.w;
    float band = fbm(vec3(dir.xz / max(dir.y + 0.3, 0.1) * 2.0, t * 0.02));
    col = mix(col, haze * 1.05, band * h * 0.35);
    col += starField(rdir, dir, density, t, stars) * (1.0 - day) * smoothstep(0.1, 0.5, dir.y) * 0.6;
    return col;
}

vec3 modeAurora(vec3 dir, vec3 rdir, float t, float density, int stars) {
    float up = clamp(dir.y, 0.0, 1.0);
    vec3 col = mix(vec3(0.02, 0.07, 0.10), vec3(0.005, 0.01, 0.035), pow(up, 0.6));
    col += starField(rdir, dir, density, t, stars) * smoothstep(-0.02, 0.2, dir.y);
    vec2 az = azimuth(dir);
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float drift = t * (0.03 + 0.01 * fi);
        float center = 0.32 + 0.1 * fi + 0.09 * (fbm3(vec3(az * (1.4 + fi * 0.3), drift + fi * 3.0)) - 0.5)
                + 0.05 * sin(dot(az, vec2(3.0, 1.0)) + drift * 4.0);
        float d = (dir.y - center) / (0.05 + 0.02 * fi);
        float curtain = d > 0.0 ? exp(-d * d * 0.18) : exp(-d * d * 3.0);
        float rays = fbm3(vec3(az * 34.0, t * 0.15 + fi));
        float pulse = 0.6 + 0.4 * sin(drift * 2.5 + fi * 2.0);
        vec3 c = i == 0 ? vec3(0.15, 1.0, 0.55) : (i == 1 ? vec3(0.1, 0.75, 1.0) : vec3(0.75, 0.3, 1.0));
        col += c * curtain * (0.3 + 1.1 * rays) * pulse * 0.55 * smoothstep(0.0, 0.08, dir.y);
    }
    return col;
}

vec3 modeNebula(vec3 dir, vec3 rdir, float t, float density, int stars) {
    vec3 q = rdir * 1.6;
    vec3 warp = vec3(fbm3(q + vec3(0.0, 0.0, t * 0.01)), fbm3(q + vec3(5.2, 1.3, 0.0)), fbm3(q + vec3(1.7, 9.2, 2.0)));
    float n1 = fbm(q + warp * 1.8);
    float n2 = fbm(q * 2.3 - warp * 1.2 + 4.0);
    vec3 col = vec3(0.01, 0.006, 0.03);
    col = mix(col, vec3(0.42, 0.10, 0.62), smoothstep(0.35, 0.85, n1) * 0.9);
    col = mix(col, vec3(0.05, 0.42, 0.85), smoothstep(0.45, 0.9, n2) * 0.7);
    col = mix(col, vec3(1.0, 0.4, 0.62), smoothstep(0.62, 0.95, n1 * n2 * 1.7) * 0.6);
    col += vec3(1.0, 0.8, 0.95) * pow(smoothstep(0.55, 1.0, n1 * n2 * 1.8), 3.0) * 0.6;
    col += starField(rdir, dir, density * 1.3, t, stars);
    return col;
}

vec3 modeSynthwave(vec3 dir, vec3 rdir, float t, float density, int stars) {
    float e = dir.y;
    vec3 top = vec3(0.06, 0.0, 0.16);
    vec3 mid = vec3(0.45, 0.04, 0.48);
    vec3 horizon = vec3(1.0, 0.32, 0.42);
    vec3 col = e > 0.18 ? mix(mid, top, smoothstep(0.18, 0.8, e)) : mix(horizon, mid, smoothstep(-0.02, 0.18, e));
    col += starField(rdir, dir, density, t, stars) * smoothstep(0.25, 0.7, e);
    // Neon sun with horizontal cut-out stripes on its lower half.
    vec3 sun = lowSun(0.16);
    float c = dot(dir, sun);
    float disc = smoothstep(cos(0.21), cos(0.20), c);
    vec3 right = normalize(cross(sun, vec3(0.0, 1.0, 0.0)) + vec3(1.0e-4));
    vec3 up = cross(right, sun);
    float v = dot(dir, up) / 0.2;
    float stripeWidth = clamp(-v * 0.5, 0.0, 0.45);
    float stripe = step(stripeWidth, fract(v * 6.0 + t * 0.35));
    vec3 sunCol = mix(vec3(1.0, 0.15, 0.55), vec3(1.0, 0.85, 0.2), clamp(v * 0.5 + 0.5, 0.0, 1.0));
    col = mix(col, sunCol * 1.4, disc * (v > 0.0 ? 1.0 : stripe));
    col += vec3(1.0, 0.3, 0.6) * pow(max(c, 0.0), 30.0) * 0.4;
    // Neon grid on the ground plane below the horizon.
    if (e < 0.0) {
        vec2 g = dir.xz / max(-e, 0.02) * 1.5;
        g.y += t * 1.2;
        vec2 gl = abs(fract(g) - 0.5);
        float w = 0.03 + 0.02 / max(-e * 6.0, 0.2);
        float line = max(1.0 - smoothstep(0.0, w, 0.5 - gl.x), 1.0 - smoothstep(0.0, w, 0.5 - gl.y));
        float fadeOut = exp(-1.5 / max(-e * 8.0, 0.05));
        col = mix(vec3(0.05, 0.0, 0.08), col, smoothstep(-0.06, 0.0, e));
        col += mix(vec3(1.0, 0.2, 0.8), vec3(0.2, 0.9, 1.0), smoothstep(-0.4, 0.0, e)) * line * fadeOut * 1.2;
    }
    col += vec3(1.0, 0.25, 0.6) * exp(-abs(e) * 40.0) * 0.5;
    return col;
}

vec3 modeBlackHole(vec3 dir, vec3 rdir, float t, float density, int stars) {
    vec3 bh = normalize(vec3(0.35, 0.55, -0.76));
    vec3 u = normalize(cross(bh, vec3(0.0, 1.0, 0.0)));
    vec3 v = cross(u, bh);
    float facing = dot(dir, bh);
    // Gravitational lensing: bend the background lookup away from the hole.
    vec3 lensed = rdir;
    vec2 q = vec2(0.0);
    float r = 10.0;
    if (facing > 0.0) {
        q = vec2(dot(dir, u), dot(dir, v)) / facing;
        r = length(q);
        float bend = 0.012 / max(r * r, 0.0025);
        lensed = normalize(rdir + (rdir - bh * dot(rdir, bh)) * bend);
    }
    vec3 col = vec3(0.004, 0.004, 0.012);
    col += starField(lensed, dir, density * 1.2, t, stars);
    col += milkyWay(lensed, t) * 0.5;
    if (facing > 0.0) {
        // Accretion disk (tilted) with a lensed arc over the top.
        vec2 dq = vec2(q.x, q.y / 0.26);
        float dr = length(dq);
        vec2 dang = dr > 1.0e-4 ? dq / dr : vec2(1.0, 0.0);
        float disk = smoothstep(0.12, 0.15, dr) * (1.0 - smoothstep(0.32, 0.55, dr));
        float streaks = fbm(vec3(dang * 3.0, dr * 9.0 - t * 0.6));
        float doppler = 0.55 + 0.45 * dang.x;
        vec3 hot = mix(vec3(1.0, 0.45, 0.12), vec3(1.0, 0.92, 0.78), smoothstep(0.35, 0.12, dr));
        vec3 diskCol = hot * disk * (0.4 + 1.2 * streaks) * doppler * 2.2;
        vec2 aq = vec2(q.x, (q.y - 0.02) / 0.85);
        float ar = length(aq);
        float arc = smoothstep(0.13, 0.15, ar) * (1.0 - smoothstep(0.17, 0.23, ar)) * step(0.0, q.y);
        diskCol += hot * arc * 1.4 * (0.6 + 0.6 * fbm3(vec3(aq * 6.0, t * 0.3)));
        float horizon = 1.0 - smoothstep(0.095, 0.105, r);
        float photon = exp(-pow((r - 0.108) / 0.006, 2.0)) * 2.0;
        col = mix(col + diskCol, vec3(0.0), horizon);
        col += vec3(1.0, 0.8, 0.6) * photon * (1.0 - horizon);
    }
    return col;
}

vec3 modeEclipse(vec3 dir, vec3 rdir, float t, float density, int stars) {
    float e = dir.y;
    vec3 col = mix(vec3(0.38, 0.10, 0.06), vec3(0.01, 0.01, 0.03), smoothstep(-0.02, 0.35, e));
    col += starField(rdir, dir, density, t, stars) * smoothstep(0.1, 0.5, e) * 0.8;
    vec3 sun = normalize(SunDir.y > 0.05 ? SunDir.xyz : lowSun(0.45));
    float c = dot(dir, sun);
    vec3 right = normalize(cross(sun, vec3(0.0, 1.0, 0.0)) + vec3(1.0e-4));
    vec3 up = cross(right, sun);
    vec2 lp = vec2(dot(dir, right), dot(dir, up));
    float ang = atan(lp.y, lp.x);
    vec2 ad = vec2(cos(ang), sin(ang));
    float rr = length(lp);
    float streamers = fbm(vec3(ad * 2.5, rr * 6.0 - t * 0.05));
    float corona = exp(-max(rr - 0.06, 0.0) * mix(18.0, 7.0, streamers)) * step(0.0, c);
    col += vec3(1.0, 0.96, 0.9) * corona * 1.6;
    col += vec3(1.0, 0.55, 0.3) * pow(max(c, 0.0), 40.0) * 0.4;
    float moonDisk = 1.0 - smoothstep(0.059, 0.062, rr);
    col = mix(col, vec3(0.0), moonDisk * step(0.0, c));
    // Diamond-ring flare on one edge.
    vec2 bead = vec2(0.042, 0.042);
    col += vec3(1.0, 0.97, 0.9) * exp(-length(lp - bead) * 140.0) * 3.0 * step(0.0, c);
    return col;
}

vec3 cloudShade(vec3 dir, float density, vec3 lit, vec3 shadow, float t, float coverage, float scale) {
    if (dir.y <= 0.0) {
        return vec3(0.0);
    }
    vec2 p = skyPlane(dir) * scale;
    float n = fbm(vec3(p, t * 0.015));
    float detail = fbm3(vec3(p * 3.0, t * 0.03));
    float cover = smoothstep(1.0 - coverage, 1.0 - coverage + 0.25, n * 0.8 + detail * 0.25);
    return mix(shadow, lit, smoothstep(0.3, 0.9, n)) * cover;
}

vec3 modeGolden(vec3 dir, vec3 rdir, float t, float density, int stars, float coverage) {
    float e = clamp(dir.y, -0.1, 1.0);
    vec3 col = mix(vec3(1.0, 0.66, 0.34), vec3(0.32, 0.52, 0.85), smoothstep(0.0, 0.7, e));
    vec3 sun = lowSun(0.08);
    float c = max(dot(dir, sun), 0.0);
    col += vec3(1.0, 0.8, 0.5) * (pow(c, 600.0) * 2.5 + pow(c, 20.0) * 0.45 + pow(c, 4.0) * 0.15);
    if (dir.y > 0.0) {
        vec2 p = skyPlane(dir) * 0.9;
        float n = fbm(vec3(p, t * 0.012));
        float cover = smoothstep(0.45 - coverage * 0.3, 0.75 - coverage * 0.3, n);
        float rim = pow(c, 6.0);
        vec3 cloud = mix(vec3(0.75, 0.42, 0.35), vec3(1.0, 0.85, 0.65), smoothstep(0.4, 0.9, n)) + vec3(1.0, 0.8, 0.5) * rim * 0.8;
        col = mix(col, cloud, cover * smoothstep(0.0, 0.12, dir.y));
    }
    return col;
}

vec3 modeStorm(vec3 dir, vec3 rdir, float t, float density, int stars, float coverage) {
    float e = clamp(dir.y, -0.1, 1.0);
    vec3 col = mix(vec3(0.42, 0.05, 0.05), vec3(0.04, 0.0, 0.02), smoothstep(-0.05, 0.6, e));
    vec2 p = skyPlane(dir) * 0.7;
    vec2 warp = vec2(fbm3(vec3(p, t * 0.05)), fbm3(vec3(p + 5.3, t * 0.05)));
    float n = fbm(vec3(p + warp * 2.0, t * 0.04));
    float flashSeed = hash12(vec2(floor(t * 3.0), 1.7));
    float flash = step(0.93, flashSeed) * exp(-fract(t * 3.0) * 6.0);
    vec2 flashPos = vec2(hash12(vec2(floor(t * 3.0), 3.1)), hash12(vec2(floor(t * 3.0), 8.4))) * 6.0 - 3.0;
    float flashLocal = exp(-length(p - flashPos) * 0.35) * flash;
    vec3 cloud = mix(vec3(0.07, 0.0, 0.01), vec3(0.55, 0.08, 0.06), smoothstep(0.3, 0.85, n));
    cloud += vec3(1.0, 0.6, 0.65) * flashLocal * smoothstep(0.35, 0.7, n) * 2.0;
    col = mix(col, cloud, smoothstep(0.2, 0.55, n + coverage * 0.3) * smoothstep(-0.02, 0.15, dir.y));
    col += vec3(1.0, 0.7, 0.7) * flash * 0.08;
    return col;
}

vec3 modeCandy(vec3 dir, vec3 rdir, float t, float density, int stars, float coverage) {
    float e = clamp(dir.y, -0.1, 1.0);
    vec3 col = mix(vec3(1.0, 0.72, 0.86), vec3(0.55, 0.78, 1.0), smoothstep(0.0, 0.75, e));
    col += starField(rdir, dir, density, t, 1) * 0.35 * smoothstep(0.3, 0.9, e);
    if (dir.y > 0.0) {
        vec2 p = skyPlane(dir) * 0.8;
        float n = fbm(vec3(p, t * 0.01));
        float puffs = smoothstep(0.5 - coverage * 0.25, 0.7 - coverage * 0.25, n);
        vec3 cloud = mix(vec3(1.0, 0.75, 0.88), vec3(1.0, 0.97, 1.0), smoothstep(0.5, 0.85, n));
        col = mix(col, cloud, puffs * smoothstep(0.0, 0.15, dir.y));
    }
    return col;
}

vec3 modeMatrix(vec3 dir, float t, float density) {
    vec3 col = mix(vec3(0.0, 0.06, 0.02), vec3(0.0, 0.01, 0.005), smoothstep(0.0, 0.8, dir.y));
    float columns = 160.0;
    float ang = atan(dir.z, dir.x) / TAU + 0.5;
    float colId = floor(ang * columns);
    float e = asin(clamp(dir.y, -1.0, 1.0)) / (PI * 0.5);
    float rows = 70.0;
    float rowF = e * rows;
    float rowId = floor(rowF);
    float speed = 6.0 + 14.0 * hash12(vec2(colId, 1.0));
    float offset = hash12(vec2(colId, 2.0)) * 200.0;
    float head = rows - mod(t * speed + offset, rows * 1.6);
    float dist = rowId - head;
    float trail = dist > 0.0 ? exp(-dist * 0.12) : 0.0;
    // Glyph: a random 3x5 bit pattern per cell, re-rolled a few times a second.
    vec2 cellUv = vec2(fract(ang * columns), fract(rowF));
    vec2 bit = floor(cellUv * vec2(3.0, 5.0));
    float glyphSeed = floor(t * (2.0 + 6.0 * hash12(vec2(colId, rowId))));
    float on = step(0.45, hash13(vec3(colId * 7.0 + bit.x, rowId * 11.0 + bit.y, glyphSeed)));
    float margin = step(0.12, cellUv.x) * step(cellUv.x, 0.88) * step(0.1, cellUv.y) * step(cellUv.y, 0.9);
    float alive = step(1.0 - 0.75 * max(density, 0.2), hash12(vec2(colId, 7.0)));
    float glyph = on * margin * trail * alive;
    float headGlow = exp(-abs(dist) * 1.5) * alive;
    col += vec3(0.1, 1.0, 0.35) * glyph * 0.9 + vec3(0.75, 1.0, 0.8) * headGlow * on * margin;
    return col;
}

vec3 modeVoid(vec3 dir, vec3 rdir, float t, float density, int stars) {
    vec3 col = vec3(0.0);
    vec3 drift = rotateY(rdir, t * 0.01);
    col += starField(drift, dir, min(1.0, density * 1.8 + 0.2), t, stars) * 1.2;
    vec3 t3;
    float far = starCell(drift, 420.0, 0.25 * density, t, 0, t3);
    col += t3 * far * 0.5;
    float neb = fbm(drift * 2.0 + vec3(0.0, t * 0.003, 0.0));
    col += mix(vec3(0.05, 0.0, 0.1), vec3(0.0, 0.05, 0.12), neb) * smoothstep(0.5, 0.9, neb) * 0.6;
    return col;
}

vec3 modePrism(vec3 dir, vec3 rdir, float t, float density, int stars) {
    vec2 az = azimuth(dir);
    float e = dir.y;
    float wave = fbm3(vec3(az * 1.5, t * 0.03)) * 0.25;
    float h = e * 0.9 + wave + t * 0.01;
    vec3 rainbow = hue(fract(h));
    vec3 col = mix(vec3(1.0), rainbow, 0.45) * mix(0.75, 1.0, smoothstep(-0.1, 0.6, e));
    float sheen = pow(abs(sin(dot(az, vec2(5.0, 3.0)) + e * 9.0 + t * 0.3)), 24.0);
    col += vec3(1.0) * sheen * 0.15;
    col += starField(rdir, dir, density, t, 3) * 0.6 * smoothstep(0.2, 0.9, e);
    return col;
}

vec3 modeOcean(vec3 dir, vec3 rdir, float t, float density, int stars) {
    float e = clamp(dir.y, -0.2, 1.0);
    vec3 col = mix(vec3(0.0, 0.32, 0.42), vec3(0.0, 0.07, 0.14), smoothstep(-0.1, 0.9, e));
    if (dir.y > 0.0) {
        vec2 p = skyPlane(dir) * 1.4;
        vec2 v1 = voronoi(vec3(p, t * 0.25));
        vec2 v2 = voronoi(vec3(p * 1.7 + 3.0, t * 0.3));
        float caustic = pow(1.0 - smoothstep(0.0, 0.18, v1.y - v1.x), 2.0) + pow(1.0 - smoothstep(0.0, 0.15, v2.y - v2.x), 2.0) * 0.6;
        col += vec3(0.3, 0.95, 0.9) * caustic * 0.35 * smoothstep(0.0, 0.5, dir.y);
        // Light shafts from straight up.
        vec2 az = azimuth(dir);
        float shafts = pow(fbm3(vec3(az * 6.0, t * 0.05)), 3.0) * smoothstep(0.3, 1.0, dir.y);
        col += vec3(0.4, 0.9, 1.0) * shafts * 0.5;
    }
    // Bubbles drifting up.
    vec3 bp = rdir * 60.0 + vec3(0.0, -t * 0.6, 0.0);
    vec3 cell = floor(bp);
    if (hash13(cell) < 0.04 * density) {
        float d = length(fract(bp) - 0.5);
        col += vec3(0.6, 1.0, 1.0) * (smoothstep(0.22, 0.18, d) - smoothstep(0.16, 0.1, d)) * 0.6;
    }
    return col;
}

// Radiant Sunbeams: towering clouds around the real sun, dark-cored with brilliant silver linings,
// and bright gaps between them – made for Sun Rays, which pours light through the gaps. Uses the
// true sun (not an art-directed one), so the rays line up with it; by night the moon lights the
// edges instead.
vec3 modeRadiant(vec3 dir, vec3 rdir, float t, float density, int stars, float day, float coverage) {
    vec3 sun = normalize(SunDir.xyz);
    vec3 moon = normalize(MoonDir.xyz);
    float up = clamp(dir.y, 0.0, 1.0);
    float low = 1.0 - smoothstep(0.02, 0.5, sun.y);
    float night = 1.0 - day;

    vec3 zenith = mix(vec3(0.008, 0.012, 0.04), mix(vec3(0.05, 0.22, 0.6), vec3(0.14, 0.17, 0.38), low), day);
    vec3 horizon = mix(vec3(0.045, 0.055, 0.12), mix(vec3(0.6, 0.79, 0.98), vec3(1.0, 0.6, 0.34), low), day);
    vec3 col = mix(horizon, zenith, pow(up, 0.45));
    float toSun = max(dot(dir, sun), 0.0);
    float toMoon = max(dot(dir, moon), 0.0);
    vec3 sunTint = mix(vec3(1.0, 0.95, 0.86), vec3(1.0, 0.64, 0.34), low);
    vec3 moonTint = vec3(0.62, 0.72, 0.95);
    // Forward scattering: the air glows around the light – what the rays are made of.
    col += sunTint * (pow(toSun, 5.0) * 0.32 + pow(toSun, 40.0) * 0.55) * day;
    col += moonTint * (pow(toMoon, 6.0) * 0.06 + pow(toMoon, 60.0) * 0.12) * night;
    if (night > 0.01) {
        col += starField(rdir, dir, density, t, stars) * smoothstep(-0.05, 0.15, dir.y) * night;
    }
    col += sunWithCorona(dir, sun, sunTint, 0.045, t) * SunDir.w;
    col += moonDisc(dir) * (0.35 + 0.65 * night);

    if (dir.y > 0.0) {
        vec2 p = skyPlane(dir) * 0.5;
        vec2 warp = vec2(fbm3(vec3(p * 0.7, t * 0.01)), fbm3(vec3(p * 0.7 + 7.1, t * 0.01))) - 0.5;
        vec2 q = p + vec2(t * 0.006, t * 0.002) + warp * 0.9;
        float n = fbm(vec3(q, t * 0.008));
        float threshold = 0.62 - coverage * 0.3;
        float body = smoothstep(threshold, threshold + 0.2, n);
        if (body > 0.0) {
            // Self-shadowing: how much cloud lies between this point and the light.
            vec3 light = day > 0.0 ? sun : moon;
            vec2 towards = normalize(light.xz + vec2(1.0e-4)) * 0.16;
            float occluder = fbm(vec3(q + towards, t * 0.008));
            float lit = clamp(1.0 - (occluder - threshold) * 2.4, 0.0, 1.0);
            vec3 dark = mix(vec3(0.025, 0.03, 0.055), mix(vec3(0.27, 0.31, 0.41), vec3(0.4, 0.25, 0.28), low), day);
            vec3 bright = mix(vec3(0.14, 0.16, 0.24), mix(vec3(1.0, 0.98, 0.95), vec3(1.0, 0.76, 0.52), low), day);
            vec3 cloud = mix(dark, bright, lit * (0.4 + 0.6 * pow(max(dot(dir, light), 0.0), 2.0)));
            // Silver lining: the thin edges near the light blaze.
            float edge = body * (1.0 - smoothstep(threshold + 0.01, threshold + 0.16, n));
            cloud += sunTint * edge * (pow(toSun, 9.0) * 2.6 + pow(toSun, 3.0) * 0.25) * day;
            cloud += moonTint * edge * (pow(toMoon, 9.0) * 0.6 + pow(toMoon, 3.0) * 0.05) * night;
            col = mix(col, cloud, body * smoothstep(0.0, 0.1, dir.y));
        }
    }
    return col;
}

// =============================================================================================
// Silver Lining: a volumetric cloud deck, lit from behind by the moon (or the sun by day).
// =============================================================================================
// The deck is a slab between two heights above the camera; each view ray is marched through it.
// Density: a slow "weather" map decides where the deck is thick and where it breaks open, a billowy
// shape noise carves the clouds, a finer noise erodes their edges (wispy on top, billowy below).
// Light: Beer–Lambert towards the light with a multiple-scattering floor, a two-lobe Henyey–Greenstein
// phase – strongly forward, so thin edges between you and the moon blaze silver – a powder term that
// darkens the cores, and an ambient sky term brighter at the tops. The moon (its real position, so
// Sun Rays' moonbeams line up) glows through the gaps.
const float DECK_LOW = 1.0;
const float DECK_HIGH = 1.55;
const float DECK_EXTINCTION = 16.0;

float remap(float v, float lo, float hi, float newLo, float newHi) {
    return newLo + (v - lo) * (newHi - newLo) / (hi - lo);
}

float henyeyGreenstein(float cosine, float g) {
    float g2 = g * g;
    return (1.0 - g2) / pow(1.0 + g2 - 2.0 * g * cosine, 1.5);
}

// Cloud density at p (camera-relative, deck units). `cover` comes from the weather map; `softness`
// widens the edges and `erosion` scales the detail with distance, so far clouds don't alias.
float deckDensity(vec3 p, float cover, vec3 wind, float softness, float erosion, bool eroded) {
    vec3 q = p + wind;
    float h = clamp((p.y - DECK_LOW) / (DECK_HIGH - DECK_LOW), 0.0, 1.0);
    float gradient = smoothstep(0.0, 0.22, h) * (1.0 - smoothstep(0.5, 1.0, h));
    float shape = fbm3(q * vec3(1.4, 2.52, 1.4));
    float base = clamp((shape * gradient - (1.0 - cover)) / softness, 0.0, 1.0);
    if (eroded && base > 0.0) {
        float detail = fbm3(q * 5.0 + vec3(wind.x * 3.0, 0.0, 0.0));
        float erode = mix(1.0 - detail, detail, h) * erosion;
        base = clamp(remap(base, erode, 1.0, 0.0, 1.0), 0.0, 1.0);
    }
    return base;
}

vec3 modeSilver(vec3 dir, vec3 rdir, float t, float density, int stars, float day, float coverage) {
    vec3 sun = normalize(SunDir.xyz);
    vec3 moon = normalize(MoonDir.xyz);
    float night = 1.0 - day;
    bool byDay = day > 0.5;
    vec3 light = byDay ? sun : moon;
    // The light fades out around the hand-over from moon to sun, so the switch never shows.
    float lightAmount = smoothstep(0.0, 0.35, abs(day - 0.5)) * smoothstep(-0.08, 0.12, light.y);
    float moonFull = 0.5 + 0.5 * cos(MoonDir.w * PI / 4.0);
    float low = 1.0 - smoothstep(0.05, 0.45, sun.y);
    vec3 lightColor = byDay
            ? mix(vec3(1.0, 0.97, 0.92), vec3(1.0, 0.7, 0.42), low) * 2.6
            : vec3(0.93, 0.91, 0.84) * (0.55 + 0.75 * moonFull);
    vec3 ambientTop = mix(vec3(0.03, 0.038, 0.06), vec3(0.58, 0.64, 0.74), day);
    vec3 ambientBottom = ambientTop * mix(0.3, 0.45, day);

    // The sky behind: deep and nearly colourless at night, pale steel by day.
    float up = clamp(dir.y, 0.0, 1.0);
    vec3 sky = mix(mix(vec3(0.03, 0.034, 0.048), vec3(0.006, 0.008, 0.016), sqrt(up)),
            mix(vec3(0.6, 0.66, 0.74), vec3(0.2, 0.33, 0.56), sqrt(up)), day);
    float toLight = max(dot(dir, light), 0.0);
    sky += lightColor * lightAmount * (pow(toLight, 6.0) * 0.06 + pow(toLight, 48.0) * 0.22 + pow(toLight, 600.0) * 0.6);
    if (night > 0.01) {
        sky += starField(rdir, dir, density, t, stars) * smoothstep(-0.05, 0.15, dir.y) * night;
    }
    sky += sunWithCorona(dir, sun, mix(vec3(1.0, 0.95, 0.88), vec3(1.0, 0.7, 0.45), low), 0.045, t) * SunDir.w * 0.6;
    sky += moonDisc(dir) * (0.35 + 0.65 * night);
    if (dir.y <= 0.012) {
        return vec3(1.0) - exp(-sky * 1.15);
    }

    // March the deck.
    float t0 = DECK_LOW / dir.y;
    if (t0 > 40.0) {
        return vec3(1.0) - exp(-sky * 1.15);
    }
    float t1 = min(DECK_HIGH / dir.y, t0 + 3.0);
    float softness = 0.2 * (1.0 + t0 * 0.05);
    float erosion = 0.55 * (0.4 + 0.6 * (1.0 - smoothstep(8.0, 30.0, t0)));
    vec3 wind = vec3(t * 0.014, 0.0, t * 0.005);
    vec3 middle = dir * ((t0 + t1) * 0.5) + wind;
    float weather = fbm3(vec3(middle.xz * 0.085, 2.7));
    float cover = clamp(coverage * 0.7 + weather * 0.6 - 0.12, 0.0, 1.0);

    const int STEPS = 16;
    float stepLength = (t1 - t0) / float(STEPS);
    float jitter = hash13(vec3(gl_FragCoord.xy, 7.0));
    float distanceAlong = t0 + stepLength * jitter;
    float cosine = dot(dir, light);
    float phase = mix(henyeyGreenstein(cosine, 0.72), henyeyGreenstein(cosine, -0.18), 0.32);
    float transmittance = 1.0;
    vec3 scattered = vec3(0.0);
    for (int i = 0; i < STEPS; i++) {
        vec3 p = dir * distanceAlong;
        float d = deckDensity(p, cover, wind, softness, erosion, true);
        if (d > 0.004) {
            // How much cloud lies between this point and the light.
            float towards = deckDensity(p + light * 0.14, cover, wind, softness, erosion, false) * 0.14
                    + deckDensity(p + light * 0.42, cover, wind, softness, erosion, false) * 0.28;
            float depth = towards * DECK_EXTINCTION;
            // Direct light plus a faint multiple-scattering term (light that found its way through).
            float beer = exp(-depth) + exp(-depth * 0.25) * 0.12;
            float powder = 1.0 - exp(-d * 3.5);
            float h = clamp((p.y - DECK_LOW) / (DECK_HIGH - DECK_LOW), 0.0, 1.0);
            vec3 ambient = mix(ambientBottom, ambientTop, h);
            vec3 radiance = lightColor * lightAmount * beer * phase * mix(1.0, powder, 0.6) * 0.45 + ambient;
            float alpha = 1.0 - exp(-d * stepLength * DECK_EXTINCTION);
            scattered += transmittance * alpha * radiance;
            transmittance *= 1.0 - alpha;
            if (transmittance < 0.025) {
                break;
            }
        }
        distanceAlong += stepLength;
    }
    // Far away the deck thins into the horizon haze.
    float fade = 1.0 - smoothstep(12.0, 40.0, t0);
    vec3 clouded = sky * transmittance + scattered;
    // A filmic roll-off: blazing silver edges stay bright without clipping into flat white.
    return vec3(1.0) - exp(-mix(sky, clouded, fade) * 1.15);
}

#ifdef GALAXY_TEXTURE
vec3 modeGalaxy(vec3 rdir, vec3 dir, float t, float density, int stars) {
    float u = atan(rdir.z, rdir.x) / TAU + 0.5;
    float v = acos(clamp(rdir.y, -1.0, 1.0)) / PI;
    vec3 col = texture(Sampler0, vec2(u, v)).rgb;
    col += starField(rdir, dir, density * 0.6, t, stars) * 0.8;
    return col;
}
#endif

// =============================================================================================
// Cloud pattern layer (any mode)
// =============================================================================================
float cloudPattern(vec3 dir, int pattern, float t, float coverage) {
    if (pattern == 0 || dir.y <= 0.0) {
        return 0.0;
    }
    vec2 p = skyPlane(dir);
    float n;
    if (pattern == 1) {        // wispy
        n = fbm(vec3(p.x * 0.6, p.y * 2.2, t * 0.02));
        n = smoothstep(0.5, 0.75, n) * 0.8;
    } else if (pattern == 2) { // puffy
        n = fbm(vec3(p * 0.9, t * 0.015));
        n = smoothstep(0.48, 0.62, n + fbm3(vec3(p * 3.0, t * 0.02)) * 0.12);
    } else if (pattern == 3) { // streaks
        n = fbm3(vec3(p.x * 0.25 + t * 0.03, p.y * 5.0, 1.0));
        n = smoothstep(0.55, 0.8, n);
    } else if (pattern == 4) { // swirl
        float r = length(p * 0.25);
        float a = r * 2.0 - t * 0.04;
        vec2 sp = mat2(cos(a), -sin(a), sin(a), cos(a)) * p * 0.6;
        n = smoothstep(0.5, 0.75, fbm(vec3(sp, t * 0.01)));
    } else if (pattern == 5) { // cellular "mackerel sky"
        vec2 v = voronoi(vec3(p * 1.6, t * 0.05));
        n = smoothstep(0.08, 0.28, v.y - v.x) * smoothstep(0.35, 0.65, fbm3(vec3(p * 0.4, t * 0.01)));
    } else {                   // ripples
        float w = fbm3(vec3(p * 0.4, t * 0.02)) * 6.0;
        n = pow(0.5 + 0.5 * sin(p.y * 4.0 + w + t * 0.2), 6.0) * smoothstep(0.3, 0.6, fbm3(vec3(p * 0.3, 2.0)));
    }
    float cover = clamp(coverage * 1.6, 0.0, 1.6);
    return clamp(n * cover, 0.0, 1.0) * smoothstep(0.0, 0.18, dir.y);
}

// =============================================================================================
void main() {
    vec3 dir = normalize(viewRay);
    float t = Params.x;
    float density = Params.z;
    int mode = int(Params2.y + 0.5);
    int clouds = int(Params2.z + 0.5);
    int stars = int(Params2.w + 0.5);
    float coverage = Params3.y;
    float day = smoothstep(-0.22, 0.22, SunDir.y);
    // Stars and galaxies turn around the celestial (east-west) axis, like vanilla's star field.
    vec3 rdir = rotateX(dir, Params.y);

    vec3 col;
#ifdef GALAXY_TEXTURE
    col = modeGalaxy(rdir, dir, t, density, stars);
#else
    if (mode == 1) {
        col = modeProcedural(dir, rdir, t, density, stars, day);
    } else if (mode == 2) {
        col = modePastel(dir, rdir, t, density, stars);
    } else if (mode == 3) {
        col = modeFog(dir, rdir, t, density, stars, day);
    } else if (mode == 4) {
        col = modeAurora(dir, rdir, t, density, stars);
    } else if (mode == 5) {
        col = modeNebula(dir, rdir, t, density, stars);
    } else if (mode == 6) {
        col = modeSynthwave(dir, rdir, t, density, stars);
    } else if (mode == 7) {
        col = modeBlackHole(dir, rdir, t, density, stars);
    } else if (mode == 8) {
        col = modeEclipse(dir, rdir, t, density, stars);
    } else if (mode == 9) {
        col = modeGolden(dir, rdir, t, density, stars, coverage);
    } else if (mode == 10) {
        col = modeStorm(dir, rdir, t, density, stars, coverage);
    } else if (mode == 11) {
        col = modeCandy(dir, rdir, t, density, stars, coverage);
    } else if (mode == 12) {
        col = modeMatrix(dir, t, density);
    } else if (mode == 13) {
        col = modeVoid(dir, rdir, t, density, stars);
    } else if (mode == 14) {
        col = modePrism(dir, rdir, t, density, stars);
    } else if (mode == 15) {
        col = modeOcean(dir, rdir, t, density, stars);
    } else if (mode == 16) {
        col = modeRadiant(dir, rdir, t, density, stars, day, coverage);
    } else if (mode == 17) {
        col = modeSilver(dir, rdir, t, density, stars, day, coverage);
    } else {
        // Galaxy selected but its texture isn't bound in this variant: procedural night stand-in.
        col = modeVoid(dir, rdir, t, density, stars) + milkyWay(rdir, t);
    }
#endif

    // Cloud pattern layer, lit with the sky's own colour so it suits every mode.
    float cloud = cloudPattern(dir, clouds, t, coverage);
    if (cloud > 0.0) {
        float skyLum = luminance(col);
        vec3 cloudCol = mix(col * 0.55 + vec3(0.08), mix(col, vec3(1.0), 0.75), smoothstep(0.05, 0.6, skyLum));
        col = mix(col, cloudCol, cloud * 0.85);
    }

    // Rain: desaturate and dim.
    float rain = 1.0 - SunDir.w;
    col = mix(col, vec3(luminance(col)) * 0.6, rain * 0.6);

    // Colour tint: recolour by luminance, keeping a little of the original.
    float lum = luminance(col);
    col = mix(col, TintColor.rgb * lum * 1.6 + col * 0.15, TintColor.w);

    col *= Params3.x;

    // Horizon blending into the fog colour (which the Skybox also drives) for a seamless horizon.
    float blend = Params.w * (1.0 - smoothstep(-0.02, 0.28, dir.y));
    col = mix(col, FogColor.rgb, clamp(blend, 0.0, 1.0));
    // Below the horizon fade towards fog to hide the void (synthwave keeps its grid).
    if (mode != 6) {
        col = mix(col, FogColor.rgb * 0.85, 1.0 - smoothstep(-0.25, 0.0, dir.y));
    }

    // Dither against banding in smooth gradients.
    col += (hash13(vec3(gl_FragCoord.xy, fract(t))) - 0.5) / 255.0;
    fragColor = vec4(max(col, vec3(0.0)), Params2.x);
}
