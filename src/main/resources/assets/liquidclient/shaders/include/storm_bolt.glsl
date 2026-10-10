#ifndef LIQUID_STORM_BOLT_GLSL
#define LIQUID_STORM_BOLT_GLSL

// One lightning channel (render.storm.StormRenderer#writeBolt). std140, vec4s only.
layout(std140) uniform LiquidBolt {
    // camera-relative world position -> clip
    mat4 ViewProj;
    // xyz the bolt's origin relative to the camera, w the cloud base's height above the camera
    vec4 Origin;
    // x main channel, y branches, z leader progress (> 1: the whole channel), w ground glow
    vec4 Envelope;
    // rgb the core's colour, w halo width (times the core's)
    vec4 CoreColor;
    // rgb the halo's colour, w visibility through the rain (blocks)
    vec4 GlowColor;
    // x framebuffer height (px), y projection[1][1], z furthest distance drawn, w unused
    vec4 Screen;
    // two per segment: (A, width at A), (B, packed order / arrival / brightness / width ratio)
    vec4 Seg[896];
};

#endif
