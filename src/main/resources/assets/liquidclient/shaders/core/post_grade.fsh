#version 330
#extension GL_ARB_separate_shader_objects : require

// Color Grading + Bloom composite (PostFx#grade): the frame (world and hand, before the GUI) is
// rebuilt from a copy of itself.
//   optics : chromatic aberration, sharpening
//   linear : + bloom, exposure, white balance, tone curve
//   display: the look (lift / gamma / gain, contrast, saturation, monochrome, split toning), the
//            user's contrast / saturation / vibrance / shadows / highlights, vignette, film grain
// and a final dither so gradients never band in the 8-bit target.

uniform sampler2D InSampler;
uniform sampler2D BloomSampler;

// Mirrors PostFx#writeGrade (std140, 9 x vec4).
layout(std140) uniform LiquidGrade {
    vec4 Lift;      // rgb, w = look strength
    vec4 Gamma;     // rgb, w = look saturation
    vec4 Gain;      // rgb, w = look contrast
    vec4 SplitLow;  // rgb shadow tone, w = split-toning strength
    vec4 SplitHigh; // rgb highlight tone, w = look monochrome
    vec4 Adjust;    // x = exposure multiplier, y = contrast, z = saturation, w = vibrance
    vec4 Balance;   // rgb white-balance multiplier, w = tone curve (0 off, 1 filmic, 2 soft)
    vec4 Tone;      // x = shadows, y = highlights, z = bloom intensity, w = grain seed
    vec4 Effects;   // x = vignette, y = grain, z = chromatic aberration, w = sharpen
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float luma(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 overlay(vec3 base, vec3 blend) {
    return mix(2.0 * base * blend, 1.0 - 2.0 * (1.0 - base) * (1.0 - blend), step(0.5, base));
}

// Highlight shoulder in linear light (bloom can push past 1) and a gentle S-curve on top.
vec3 filmic(vec3 x) {
    vec3 shoulder = 0.8 + (1.0 - exp(-(x - 0.8) * 5.0)) * 0.2;
    return mix(x, shoulder, step(0.8, x));
}

vec3 soft(vec3 x) {
    return x / (1.0 + x * 0.25) * 1.25;
}

vec3 look(vec3 c) {
    vec3 g = c * Gain.rgb + Lift.rgb * (1.0 - c);
    g = pow(max(g, 0.0), 1.0 / Gamma.rgb);
    g = (g - 0.5) * Gain.w + 0.5;
    g = mix(vec3(luma(g)), g, Gamma.w);
    g = mix(g, vec3(luma(g)), SplitHigh.w);
    float l = clamp(luma(g), 0.0, 1.0);
    vec3 tone = mix(SplitLow.rgb, SplitHigh.rgb, smoothstep(0.1, 0.9, l));
    g = mix(g, overlay(clamp(g, 0.0, 1.0), tone), SplitLow.w);
    return mix(c, g, Lift.w);
}

void main() {
    vec2 uv = texCoord;
    vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
    vec2 centered = uv - 0.5;

    vec3 color;
    if (Effects.z > 0.0) {
        // Lens dispersion grows towards the edges.
        vec2 offset = centered * dot(centered, centered) * Effects.z * 0.06;
        color = vec3(texture(InSampler, uv - offset).r, texture(InSampler, uv).g, texture(InSampler, uv + offset).b);
    } else {
        color = texture(InSampler, uv).rgb;
    }
    if (Effects.w > 0.0) {
        vec3 around = texture(InSampler, uv + vec2(texel.x, 0.0)).rgb + texture(InSampler, uv - vec2(texel.x, 0.0)).rgb
                + texture(InSampler, uv + vec2(0.0, texel.y)).rgb + texture(InSampler, uv - vec2(0.0, texel.y)).rgb;
        color += (color - around * 0.25) * Effects.w * 1.2;
    }

    vec3 linear = pow(max(color, 0.0), vec3(2.2));
    if (Tone.z > 0.0) {
        linear += texture(BloomSampler, uv).rgb * Tone.z;
    }
    linear *= Adjust.x * Balance.rgb;
    if (Balance.w > 1.5) {
        linear = soft(linear);
    } else if (Balance.w > 0.5) {
        linear = filmic(linear);
    }
    color = pow(max(linear, 0.0), vec3(1.0 / 2.2));
    if (Balance.w > 0.5 && Balance.w < 1.5) {
        color = mix(color, color * color * (3.0 - 2.0 * clamp(color, 0.0, 1.0)), 0.3);
    }

    if (Lift.w > 0.0) {
        color = look(color);
    }

    float l = clamp(luma(color), 0.0, 1.0);
    color += Tone.x * 0.22 * (1.0 - l) * (1.0 - l);
    color += Tone.y * 0.22 * l * l;
    color = (color - 0.5) * Adjust.y + 0.5;
    float y = luma(color);
    color = mix(vec3(y), color, Adjust.z);
    float chroma = max(color.r, max(color.g, color.b)) - min(color.r, min(color.g, color.b));
    color = mix(vec3(y), color, 1.0 + Adjust.w * (1.0 - clamp(chroma, 0.0, 1.0)));

    if (Effects.x > 0.0) {
        float aspect = texel.y / texel.x;
        float d = length(centered * vec2(aspect, 1.0)) / length(vec2(aspect, 1.0) * 0.5);
        color *= 1.0 - Effects.x * 0.75 * smoothstep(0.3, 1.1, d);
    }
    if (Effects.y > 0.0) {
        float grain = hash(gl_FragCoord.xy + fract(Tone.w) * vec2(113.7, 71.3)) - 0.5;
        float lum = clamp(luma(color), 0.0, 1.0);
        color += grain * Effects.y * 0.12 * (1.0 - abs(lum - 0.5) * 1.2);
    }
    color += (hash(gl_FragCoord.xy * 1.37 + 17.0) - 0.5) / 255.0;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
