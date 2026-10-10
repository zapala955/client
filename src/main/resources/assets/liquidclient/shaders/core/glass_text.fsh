#version 330
#extension GL_ARB_separate_shader_objects : require

// Liquid Glass text – the translucent lock-screen clock.
//   Sampler0: frosted wallpaper, screen-aligned (what's "behind" the glass)
//   Sampler1: glyph mask baked at exact device resolution – r = coverage (anti-aliased),
//             g = bevel height (blurred coverage), b = soft drop shadow
// The glyph interior shows the frosted wallpaper refracted along the bevel slope, lifted and tinted
// like thick glass, with a specular rim where the bevel faces the light (from above).
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 maskUv;
layout(location = 2) in vec2 wallUv;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 mask = texture(Sampler1, maskUv);
    float coverage = mask.r;
    float shadow = mask.b;

    // Bevel normal from the height channel (central differences, 1.5 texels apart).
    vec2 texel = 1.0 / vec2(textureSize(Sampler1, 0));
    float hl = texture(Sampler1, maskUv - vec2(texel.x * 1.5, 0.0)).g;
    float hr = texture(Sampler1, maskUv + vec2(texel.x * 1.5, 0.0)).g;
    float hu = texture(Sampler1, maskUv - vec2(0.0, texel.y * 1.5)).g;
    float hd = texture(Sampler1, maskUv + vec2(0.0, texel.y * 1.5)).g;
    vec2 slope = vec2(hr - hl, hd - hu);

    // Refraction: the backdrop seen through the glass bends along the bevel.
    vec3 behind = texture(Sampler0, wallUv - slope * 0.035).rgb;
    vec3 glass = behind * 1.08 + 0.07;
    glass = mix(glass, vec3(1.0), 0.30) * vertexColor.rgb;

    // Lighting from above: top-facing bevels catch a specular rim, bottom ones darken slightly.
    float rim = clamp(slope.y * 5.0, 0.0, 1.0);
    float under = clamp(-slope.y * 5.0, 0.0, 1.0);
    float side = clamp(abs(slope.x) * 3.0, 0.0, 1.0);
    glass += vec3(1.0) * (rim * 0.55 + side * 0.12);
    glass *= 1.0 - under * 0.14;

    // Composite glass over its own soft shadow (straight alpha).
    float shadowAlpha = shadow * (1.0 - coverage) * 0.30;
    float alpha = coverage + shadowAlpha;
    vec3 rgb = alpha > 0.0 ? glass * (coverage / alpha) : vec3(0.0);
    if (alpha <= 0.002) {
        discard;
    }
    fragColor = vec4(rgb, alpha * vertexColor.a) * ColorModulator;
}
