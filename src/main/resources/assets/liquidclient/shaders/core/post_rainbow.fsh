#version 330
#extension GL_ARB_separate_shader_objects : require

// Rainbows (PostFx) – after the world, before the hand. A rainbow is sunlight refracted and
// reflected inside raindrops: a circle of ~42° around the point opposite the sun.
//
//   primary    every wavelength at its own angle (violet 40.6° … red 42.4°), each smeared by the sun's
//              half-degree disc, so the colours overlap like the real thing instead of a painted band
//   secondary  ~50.4–53.5°, colours reversed, wider and fainter
//   Alexander  the darker sky between the two bows; the sky inside the primary is brighter
//   bands      faint pink / green supernumerary bands just inside the violet
//
// Light (rgb) is added and the dark band subtracted in one premultiplied blend: dst·(1 - a) + rgb.
// Over sky it shows fully; over terrain only with distance, as if seen through the rain beyond.

uniform sampler2D DepthSampler;

// Mirrors PostFx#writeRainbow (std140, mat4 + 3 x vec4).
layout(std140) uniform LiquidRainbow {
    mat4 InvViewProj; // NDC -> camera-relative position
    vec4 AntiLight;   // xyz away from the light (the bow's centre), w strength
    vec4 LightColor;  // rgb light colour (white at noon, warm at sunset, pale for moonbows), w saturation
    vec4 Options;     // x secondary bow, y supernumerary bands, z time, w unused
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float DEG = 57.2957795;

// Visible wavelength (nm) to display colour: a smooth piecewise fit of the colour-matching curves.
vec3 wavelengthColor(float nm) {
    vec3 c;
    if (nm < 440.0) {
        c = vec3(-(nm - 440.0) / 60.0, 0.0, 1.0);
    } else if (nm < 490.0) {
        c = vec3(0.0, (nm - 440.0) / 50.0, 1.0);
    } else if (nm < 510.0) {
        c = vec3(0.0, 1.0, -(nm - 510.0) / 20.0);
    } else if (nm < 580.0) {
        c = vec3((nm - 510.0) / 70.0, 1.0, 0.0);
    } else if (nm < 645.0) {
        c = vec3(1.0, -(nm - 645.0) / 65.0, 0.0);
    } else {
        c = vec3(1.0, 0.0, 0.0);
    }
    // The eye is less sensitive at both ends.
    float edge = nm < 420.0 ? 0.3 + 0.7 * (nm - 380.0) / 40.0 : nm > 650.0 ? 0.3 + 0.7 * (700.0 - nm) / 50.0 : 1.0;
    return c * edge;
}

float gaussian(float x, float sigma) {
    return exp(-0.5 * x * x / (sigma * sigma));
}

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float noise1(float x) {
    float i = floor(x);
    float f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(hash11(i), hash11(i + 1.0), f);
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    float visible = 1.0;
    vec3 dir;
    if (depth > 0.0) {
        #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
        float z = depth;
        #else
        float z = depth * 2.0 - 1.0;
        #endif
        vec4 world = InvViewProj * vec4(texCoord * 2.0 - 1.0, z, 1.0);
        vec3 rel = world.xyz / world.w;
        float dist = length(rel);
        // In front of terrain only far away – the rain curtain lies between us and the hills.
        visible = smoothstep(28.0, 110.0, dist) * 0.85;
        dir = rel / max(dist, 1.0e-4);
    } else {
        #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
        float z = 0.5;
        #else
        float z = 0.0;
        #endif
        vec4 world = InvViewProj * vec4(texCoord * 2.0 - 1.0, z, 1.0);
        dir = normalize(world.xyz / world.w);
    }
    if (visible <= 0.0) {
        discard;
    }
    float angle = acos(clamp(dot(dir, AntiLight.xyz), -1.0, 1.0)) * DEG;
    if (angle > 58.0) {
        discard;
    }

    // Brightness varies along the arc where the rain is denser or thinner.
    vec3 axis = AntiLight.xyz;
    vec3 side = normalize(cross(axis, vec3(0.0, 1.0, 0.0)) + vec3(1.0e-4, 0.0, 0.0));
    vec3 up = cross(side, axis);
    float around = atan(dot(dir, up), dot(dir, side));
    float patchy = 0.6 + 0.4 * noise1(around * 2.3 + Options.z * 0.015) * (0.75 + 0.25 * noise1(around * 7.1 - Options.z * 0.02));

    vec3 light = vec3(0.0);
    float dark = 0.0;
    // Primary bow.
    if (angle > 38.0 && angle < 44.0) {
        for (int i = 0; i < 12; i++) {
            float nm = 400.0 + float(i) * 27.0;
            float peak = 42.4 - (700.0 - nm) / 300.0 * 1.8;
            light += wavelengthColor(nm) * gaussian(angle - peak, 0.32);
        }
        light *= 0.13;
    }
    // Supernumerary bands just inside the violet.
    if (Options.y > 0.5 && angle > 37.5 && angle < 40.7) {
        float inside = 40.7 - angle;
        float band = 0.5 + 0.5 * cos(inside / 0.62 * 6.2831853);
        vec3 pastel = mix(vec3(0.62, 0.95, 0.68), vec3(0.95, 0.62, 0.9), band);
        light += pastel * exp(-inside * 1.4) * smoothstep(0.0, 0.25, inside) * 0.08;
    }
    // Secondary bow, reversed and fainter.
    if (Options.x > 0.5 && angle > 49.0 && angle < 55.5) {
        vec3 second = vec3(0.0);
        for (int i = 0; i < 12; i++) {
            float nm = 400.0 + float(i) * 27.0;
            float peak = 50.4 + (700.0 - nm) / 300.0 * 3.1;
            second += wavelengthColor(nm) * gaussian(angle - peak, 0.45);
        }
        light += second * 0.13 * 0.42;
    }
    // Brighter sky inside the primary; Alexander's dark band between the bows.
    light += vec3(0.05) * smoothstep(18.0, 40.0, angle) * (1.0 - smoothstep(40.5, 41.6, angle));
    if (Options.x > 0.5) {
        dark = 0.14 * smoothstep(42.2, 43.4, angle) * (1.0 - smoothstep(49.3, 50.5, angle));
    }

    // Saturation: moonbows look almost white to our night vision.
    float lum = dot(light, vec3(0.2126, 0.7152, 0.0722));
    light = mix(vec3(lum), light, LightColor.w);

    float strength = AntiLight.w * visible * patchy;
    fragColor = vec4(light * LightColor.rgb * strength, dark * strength);
}
