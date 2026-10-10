#ifndef LIQUIDCLIENT_CHUNK_ANIMATION_GLSL
#define LIQUIDCLIENT_CHUNK_ANIMATION_GLSL

// Liquid Client - Chunk Animations.
//
// Included into the terrain vertex shaders (Sodium's block_layer_opaque.vsh and vanilla's
// terrain.vsh) by ShaderManagerMixin; see render/chunk/ChunkAnimator.java for how the per-section
// progress and the settings reach the shader.
//
// Coordinates are camera-relative world space (y up). `origin` is the section's minimum corner,
// `t` its linear progress (0 = just appeared, >= 1 = settled). Ids must match
// ChunkAnimationsModule.MODES / EASINGS.

const float LIQUID_PI = 3.14159265;
const float LIQUID_TAU = 6.28318531;

float liquid_bounce_out(float x) {
    const float n1 = 7.5625;
    const float d1 = 2.75;
    if (x < 1.0 / d1) {
        return n1 * x * x;
    }
    if (x < 2.0 / d1) {
        x -= 1.5 / d1;
        return n1 * x * x + 0.75;
    }
    if (x < 2.5 / d1) {
        x -= 2.25 / d1;
        return n1 * x * x + 0.9375;
    }
    x -= 2.625 / d1;
    return n1 * x * x + 0.984375;
}

float liquid_ease(int easing, float x) {
    x = clamp(x, 0.0, 1.0);
    float u = 1.0 - x;
    switch (easing) {
        case 1: return sin(x * LIQUID_PI * 0.5);                       // sine
        case 2: return 1.0 - u * u;                                    // quad
        case 3: return 1.0 - u * u * u;                                // cubic
        case 4: return 1.0 - u * u * u * u;                            // quart
        case 5: return 1.0 - u * u * u * u * u;                        // quint
        case 6: return x >= 1.0 ? 1.0 : 1.0 - exp2(-10.0 * x);         // expo
        case 7: return sqrt(max(0.0, 1.0 - u * u));                   // circ
        case 8: {                                                      // back (overshoots)
            const float c1 = 1.70158;
            const float c3 = c1 + 1.0;
            return 1.0 + c3 * u * u * -u + c1 * u * u;
        }
        case 9: {                                                      // elastic
            if (x <= 0.0 || x >= 1.0) {
                return x;
            }
            return exp2(-10.0 * x) * sin((x * 10.0 - 0.75) * (LIQUID_TAU / 3.0)) + 1.0;
        }
        case 10: return liquid_bounce_out(x);                          // bounce
        case 11: return x < 0.5 ? 4.0 * x * x * x : 1.0 - u * u * u * 4.0; // in-out cubic
        case 12: return x * x * (3.0 - 2.0 * x);                       // smoothstep
        default: return x;                                             // linear
    }
}

float liquid_hash(uint n) {
    n = (n << 13u) ^ n;
    n = n * (n * n * 15731u + 789221u) + 1376312589u;
    return float(n & 0x7FFFFFFFu) / 2147483647.0;
}

vec3 liquid_rotate_y(vec3 v, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return vec3(c * v.x - s * v.z, v.y, s * v.x + c * v.z);
}

vec3 liquid_rotate_axis(vec3 v, vec3 axis, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return v * c + cross(axis, v) * s + axis * dot(axis, v) * (1.0 - c);
}

// Returns the animated vertex position; `visibility` (1 = fully visible, 0 = fog colour) feeds the
// shader's existing chunk fade.
vec3 liquid_chunk_animate(vec3 pos, vec3 origin, float t, int mode, int easing, float amount, bool fade, uint seed,
        out float visibility) {
    visibility = 1.0;
    if (t >= 1.0) {
        return pos;
    }
    t = max(t, 0.0);
    if (mode == 17) {
        // Random: a stable pick per section (glitch excluded, it's an acquired taste).
        mode = int(liquid_hash(seed ^ 0x9E3779B9u) * 15.0);
        if (mode >= 13) {
            mode += 1;
        }
    }

    float e = liquid_ease(easing, t);
    float rest = 1.0 - e;
    float grow = max(e, 0.0);
    vec3 center = origin + vec3(8.0);
    vec3 rel = pos - center;
    float flatDistance = length(center.xz);
    vec2 flatDir = flatDistance > 0.001 ? center.xz / flatDistance : vec2(0.0, 1.0);
    vec3 outward = vec3(flatDir.x, 0.0, flatDir.y);
    vec3 animated = pos;

    switch (mode) {
        case 0: // rise: from below
            animated.y -= rest * amount;
            break;
        case 1: // drop: from above
            animated.y += rest * amount;
            break;
        case 2: // horizon: below the camera rise, above it drop
            animated.y += (center.y < 0.0 ? -rest : rest) * amount;
            break;
        case 3: // slide in from the distance
            animated += outward * (rest * amount);
            break;
        case 4: // burst out from the player
            animated -= outward * (rest * min(amount, flatDistance * 0.8));
            break;
        case 5: // grow from the centre
            animated = center + rel * grow;
            break;
        case 6: { // sprout out of the ground
            float sideways = mix(0.8, 1.0, clamp(e, 0.0, 1.0));
            animated = vec3(center.x + rel.x * sideways, origin.y + (pos.y - origin.y) * grow, center.z + rel.z * sideways);
            break;
        }
        case 7: // spin while growing
            animated = center + liquid_rotate_y(rel, rest * LIQUID_TAU) * grow;
            break;
        case 8: { // flip up like a card, facing the player
            vec3 axis = vec3(-flatDir.y, 0.0, flatDir.x);
            animated = center + liquid_rotate_axis(rel, axis, rest * LIQUID_PI * 0.5);
            break;
        }
        case 9: // swirl: orbit around the player into place
            animated = liquid_rotate_y(pos, rest * amount / max(flatDistance, 16.0));
            break;
        case 10: { // assemble from a random direction
            vec3 jitter = vec3(liquid_hash(seed), liquid_hash(seed + 7919u), liquid_hash(seed + 104729u)) * 2.0 - 1.0;
            vec3 direction = normalize(jitter + vec3(0.0, 0.001, 0.0));
            animated = center + rel * mix(0.4, 1.0, clamp(e, 0.0, 1.0)) + direction * (rest * amount);
            break;
        }
        case 11: // zoom: shrink into place
            animated = center + rel * (1.0 + rest * amount * 0.125);
            break;
        case 12: { // jelly: squash and stretch
            float wobble = sin(t * LIQUID_TAU * 2.0) * (1.0 - t) * 0.45;
            float sideways = mix(0.6, 1.0, clamp(e, 0.0, 1.0)) * (1.0 - wobble * 0.5);
            animated = vec3(center.x + rel.x * sideways, origin.y + (pos.y - origin.y) * grow * (1.0 + wobble),
                    center.z + rel.z * sideways);
            break;
        }
        case 13: { // glitch: jittering, flickering steps
            uint frame = seed + uint(floor(t * 16.0)) * 2654435761u;
            vec3 jitter = vec3(liquid_hash(frame), liquid_hash(frame + 1u), liquid_hash(frame + 2u)) * 2.0 - 1.0;
            animated = pos + jitter * (rest * amount * 0.35);
            visibility = mix(liquid_hash(frame + 3u) > 0.3 ? 1.0 : 0.15, 1.0, clamp(e, 0.0, 1.0));
            break;
        }
        case 14: // ripple: settling waves spreading from the player
            animated.y += sin(flatDistance * 0.18 - t * 14.0) * rest * amount * 0.4;
            break;
        case 15: // spiral up from below
            animated = center + liquid_rotate_y(rel, rest * LIQUID_PI);
            animated.y -= rest * amount;
            break;
        case 16: // fade only
            visibility = clamp(e, 0.0, 1.0);
            break;
        default:
            break;
    }

    if (fade) {
        visibility = min(visibility, clamp(t * 1.6, 0.0, 1.0));
    }
    return animated;
}

#endif
