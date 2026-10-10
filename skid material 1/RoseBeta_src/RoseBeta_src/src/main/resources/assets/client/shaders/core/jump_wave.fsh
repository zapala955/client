#version 150

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;

layout(std140) uniform JumpWaveUniforms {
    vec4 params;       // x = ring progress (0..1), y = strength, z = fade (0..1), w = unused
    vec4 crestColor;   // rgb = glow/jump color, a = color alpha
    vec4 p1;
    vec4 p2;
    vec4 p3;
    vec4 p4;
};

#define RING_WIDTH 0.22

void main() {
    float ringProgress = params.x;
    float strength = params.y;
    float fade = params.z;

    // Centered quad coords in [-1, 1], distance from the jump point.
    vec2 centered = (uv - 0.5) * 2.0;
    float dist = length(centered);
    if (dist >= 1.0) {
        discard;
    }

    // Expanding ring: a soft band located at the current progress radius (the crest).
    float ringRadius = ringProgress;
    float edge = dist - ringRadius;
    float band = smoothstep(RING_WIDTH, 0.0, abs(edge));
    band *= band; // sharper core, softer tails

    // Soft circular falloff at the quad perimeter (from 0.65 to 0.98) so it never has a hard edge
    float inside = 1.0 - smoothstep(0.65, 0.98, dist);

    // Total liquid-glass coverage: the whole disc plus the bright moving crest.
    float coverage = max(inside, band);
    if (coverage <= 0.001) {
        discard;
    }

    // Radial direction of the distortion in screen space.
    vec2 dir = dist > 0.0001 ? centered / dist : vec2(0.0);

    vec2 screenSize = vec2(textureSize(SceneSampler, 0));
    vec2 screenUv = gl_FragCoord.xy / screenSize;

    // A little ripple riding along the crest gives the glass a moving, liquid feel.
    float ripple = sin(dist * 38.0 - ringProgress * 26.0) * 0.5 + 0.5;
    float crest = band * mix(0.7, 1.3, ripple);

    // Distortion across the whole disc with a strong extra push on the moving crest
    float innerDistort = inside * mix(0.55, 1.0, dist);
    float offsetAmount = strength * fade * 0.05 * (innerDistort + crest * 1.6);

    // Single-tap refraction
    vec2 baseOffset = dir * offsetAmount;
    vec3 refracted = texture(SceneSampler, clamp(screenUv + baseOffset, vec2(0.0), vec2(1.0))).rgb;

    // Glassy rim on the leading crest tinted with the jump color
    float crestHighlight = pow(band, 1.5) * fade * 0.5;
    float sheen = inside * fade * 0.06;
    vec3 crestTint = crestColor.rgb * pow(band, 1.2) * fade * 0.55;
    vec3 color = refracted + vec3(crestHighlight + sheen) + crestTint;

    float alpha = max(inside, band) * fade;
    finalColor = vec4(color, alpha);
}
