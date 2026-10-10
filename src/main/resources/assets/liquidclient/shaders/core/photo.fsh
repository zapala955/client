#version 330
#extension GL_ARB_separate_shader_objects : require

// Photo Mode (PhotoCapture).
//
//   ACCUMULATE (+ FIRST)
//              adds the finished frame (graded, before the GUI) to a float sum, in linear light –
//              edges average the way light does, not the way gamma-encoded numbers do. Every frame
//              of a photo is rendered with a different sub-pixel offset (and noise seed), so the
//              average is anti-aliased and Depth of Field / AO noise averages out. FIRST starts the sum.
//   RESOLVE    the sum divided by its frame count, back to gamma, dithered by a third of an 8-bit step
//              (smooth skies and bokeh never band in the PNG).
//   HISTOGRAM  the frame shrunk to 64 × 36 (16 taps per texel) for the viewfinder's live histogram.

#if defined(ACCUMULATE)
uniform sampler2D InSampler;  // the frame
#ifndef FIRST
uniform sampler2D AccSampler; // the sum so far
#endif
#elif defined(RESOLVE)
uniform sampler2D AccSampler;
#elif defined(HISTOGRAM)
uniform sampler2D InSampler;
#endif

// Mirrors PhotoCapture#params (std140, 1 x vec4).
layout(std140) uniform LiquidPhoto {
    vec4 Params; // x dither seed, y unused, zw one histogram texel (uv)
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float ign(vec2 pixel) {
    return fract(52.9829189 * fract(dot(pixel, vec2(0.06711056, 0.00583715))));
}

void main() {
    #if defined(ACCUMULATE)
    ivec2 p = ivec2(gl_FragCoord.xy);
    vec3 frame = pow(max(texelFetch(InSampler, p, 0).rgb, vec3(0.0)), vec3(2.2));
    #ifdef FIRST
    fragColor = vec4(frame, 1.0);
    #else
    fragColor = texelFetch(AccSampler, p, 0) + vec4(frame, 1.0);
    #endif
    #elif defined(RESOLVE)
    vec4 sum = texelFetch(AccSampler, ivec2(gl_FragCoord.xy), 0);
    vec3 color = pow(max(sum.rgb / max(sum.a, 1.0), vec3(0.0)), vec3(1.0 / 2.2));
    color += (ign(gl_FragCoord.xy + Params.x * 7.31) - 0.5) * (0.66 / 255.0);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
    #elif defined(HISTOGRAM)
    vec3 sum = vec3(0.0);
    for (int j = 0; j < 4; j++) {
        for (int i = 0; i < 4; i++) {
            vec2 offset = (vec2(i, j) + 0.5) / 4.0 - 0.5;
            sum += texture(InSampler, texCoord + offset * Params.zw).rgb;
        }
    }
    fragColor = vec4(sum / 16.0, 1.0);
    #endif
}
