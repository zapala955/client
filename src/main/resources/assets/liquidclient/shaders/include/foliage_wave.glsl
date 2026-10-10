#ifndef LIQUIDCLIENT_FOLIAGE_WAVE_GLSL
#define LIQUIDCLIENT_FOLIAGE_WAVE_GLSL

// Liquid Client - Waving Plants (Sodium terrain shader). See render/chunk/FoliageWaving.java.
//
// anchor : region-local position. Sodium regions are 128 x 64 x 128 blocks and world-aligned, so this
//          is the world position modulo the region size. Every wave below has a whole number of
//          periods across a region, which keeps the motion continuous across region borders and
//          fixed in the world while the camera moves - no camera position needed.
// mode   : 1 leaves / vines (every vertex, zero-mean shimmer, so canopies never leave their trunks)
//          2 plant (only the vertices at the top of the texture; roots stay put)
//          3 upper half of a two-block plant (its bottom moves exactly like the lower half's tip)
// word   : packed terrain settings: bits 22-24 wind level (1-7), 25-27 direction (45 degree steps
//          clockwise from north), 28 gusts

const float LIQUID_REGION_K = 6.28318531 / 128.0;
const float LIQUID_HEIGHT_K = 6.28318531 / 64.0;

vec3 liquid_foliage_wave(vec3 anchor, int mode, bool top, uint word, float time) {
    float weight = mode == 1 ? 1.0 : (mode == 3 ? (top ? 2.0 : 1.0) : (top ? 1.0 : 0.0));
    if (weight == 0.0) {
        return vec3(0.0);
    }
    float level = float((word >> 22u) & 7u);
    int direction = int((word >> 25u) & 7u);
    bool gusts = ((word >> 28u) & 1u) == 1u;
    float amplitude = 0.012 + 0.018 * level;
    float speed = 0.9 + 0.3 * level;

    const vec2 DIRECTIONS[8] = vec2[8](vec2(0.0, -1.0), vec2(0.70710678, -0.70710678), vec2(1.0, 0.0),
            vec2(0.70710678, 0.70710678), vec2(0.0, 1.0), vec2(-0.70710678, 0.70710678), vec2(-1.0, 0.0),
            vec2(-0.70710678, -0.70710678));
    vec2 wind = DIRECTIONS[direction];
    // Whole wave numbers along the wind: 8 (or 6 + 6 diagonally) periods per region, ~16 blocks apart.
    vec2 waves = floor(wind * 8.0 + 0.5);
    float along = LIQUID_REGION_K * dot(waves, anchor.xz);

    // Gust fronts: a wave twice as long rolling slowly downwind, swelling and fading.
    float gust = gusts ? 0.35 + 0.65 * smoothstep(-0.4, 1.0, sin(along * 0.5 - time * speed * 0.35)) : 0.8;
    float sway = sin(along - time * speed);
    float flutter = sin(LIQUID_REGION_K * (23.0 * anchor.x - 11.0 * anchor.z) + LIQUID_HEIGHT_K * 9.0 * anchor.y + time * speed * 2.3)
            * sin(LIQUID_REGION_K * (5.0 * anchor.x + 29.0 * anchor.z) - time * speed * 1.7);
    vec2 across = vec2(-wind.y, wind.x);

    vec3 offset;
    if (mode == 1) {
        float shimmer = (sway * 0.6 + flutter * 0.55) * gust * amplitude * 0.45;
        offset = vec3(wind.x * shimmer + across.x * flutter * amplitude * 0.15, flutter * gust * amplitude * 0.15,
                wind.y * shimmer + across.y * flutter * amplitude * 0.15);
    } else {
        // Plants lean downwind and bob around that lean, dipping slightly as they bend.
        float lean = (0.55 + 0.45 * sway) * gust;
        vec2 side = across * flutter * 0.35;
        offset = vec3(wind.x * lean + side.x, -abs(lean) * 0.18, wind.y * lean + side.y) * amplitude;
    }
    return offset * weight;
}

#endif
