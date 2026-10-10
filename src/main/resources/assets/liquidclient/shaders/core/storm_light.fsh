#version 330
#extension GL_ARB_separate_shader_objects : require

// Storm Chaser – lightning relighting the world (render.storm.StormRenderer), after the world and
// its haze, before the bolts, the hand and the GUI.
//
//   LIGHT  at the light texture's resolution (half by default, full on request): each pixel's
//          position from the depth buffer and its normal from the neighbour on the flatter side (so
//          edges don't smear), lit by the flash: every channel is a line light from the ground to
//          the cloud (the nearest point on it sets the distance and the direction; a line light
//          falls off ~1/d, not 1/d²: a strike half a kilometre away still lights the landscape,
//          and from above), with wrapped Lambert so the lit side reads, and the cloud's flash from
//          above for whatever is open to the sky – the weather columns (render.weather) say what's
//          under a roof, so caves and houses stay dark. Out: rgb light, a = distance (for the
//          depth-aware upsample).
//   APPLY  full resolution: the four nearest light texels weighted by how close their distance is
//          to this pixel's, multiplied into the frame (dst · (1 + light · gain)): bright where the
//          scene was lit, black stays black. The sky is left to the sky shader.

uniform sampler2D DepthSampler;
#ifdef LIGHT
uniform sampler2D ColumnSampler;
#endif
#ifdef APPLY
uniform sampler2D InSampler;
#endif

// Mirrors StormRenderer#relight (std140, mat4 + 17 x vec4).
layout(std140) uniform LiquidStormLight {
    mat4 InvViewProj;  // NDC -> camera-relative world position
    vec4 Light[6];     // xyz one end of a channel (camera-relative), w intensity
    vec4 LightEnd[6];  // xyz its other end (the same for a point light), w unused
    vec4 LightColor;   // rgb, w number of lights
    vec4 Sky;          // x flash from the cloud above, y gain, z light falloff distance (blocks), w columns valid (0 / 1)
    vec4 CameraBlock;  // xyz camera block, w unused
    vec4 CameraFrac;   // xyz camera position inside its block, w unused
    vec4 Screen;       // xy frame size (px), zw light texture size (px)
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

vec3 positionAt(vec2 uv, float depth) {
    #ifdef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    float z = depth;
    #else
    float z = depth * 2.0 - 1.0;
    #endif
    vec4 world = InvViewProj * vec4(uv * 2.0 - 1.0, z, 1.0);
    return world.xyz / world.w;
}

#ifdef LIGHT
vec3 pixelPosition(ivec2 p) {
    p = clamp(p, ivec2(0), ivec2(Screen.xy) - 1);
    float depth = texelFetch(DepthSampler, p, 0).r;
    return depth <= 0.0 ? vec3(1.0e6) : positionAt((vec2(p) + 0.5) / Screen.xy, depth);
}

// How open to the sky the point is: under its column's surface (roofs, leaves, overhangs) the flash
// from the cloud is mostly blocked; beyond the columns' reach everything counts as open.
float openSky(vec3 rel) {
    if (Sky.w < 0.5 || max(abs(rel.x), abs(rel.z)) > 30.0) {
        return 1.0;
    }
    ivec2 world = ivec2(CameraBlock.xz) + ivec2(floor(CameraFrac.xz + rel.xz));
    uvec4 b = uvec4(texelFetch(ColumnSampler, world & 63, 0) * 255.0 + 0.5);
    float surface = float(b.r | (b.g << 8u)) / 16.0 - 2048.0 - CameraBlock.y - CameraFrac.y;
    return mix(0.12, 1.0, smoothstep(surface - 1.5, surface + 0.5, rel.y));
}

void main() {
    ivec2 cell = ivec2(gl_FragCoord.xy);
    float scale = Screen.x / Screen.z;
    ivec2 p = min(ivec2(floor((vec2(cell) + 0.5) * scale)), ivec2(Screen.xy) - 1);
    float depth = texelFetch(DepthSampler, p, 0).r;
    if (depth <= 0.0) {
        fragColor = vec4(0.0);
        return;
    }
    vec3 pos = positionAt((vec2(p) + 0.5) / Screen.xy, depth);
    // the normal from the neighbours on the flatter side
    vec3 right = pixelPosition(p + ivec2(1, 0));
    vec3 left = pixelPosition(p - ivec2(1, 0));
    vec3 up = pixelPosition(p + ivec2(0, 1));
    vec3 down = pixelPosition(p - ivec2(0, 1));
    vec3 dx = abs(right.z - pos.z) + abs(right.x - pos.x) + abs(right.y - pos.y) < abs(left.z - pos.z) + abs(left.x - pos.x) + abs(left.y - pos.y)
            ? right - pos : pos - left;
    vec3 dy = abs(up.z - pos.z) + abs(up.x - pos.x) + abs(up.y - pos.y) < abs(down.z - pos.z) + abs(down.x - pos.x) + abs(down.y - pos.y)
            ? up - pos : pos - down;
    vec3 n = normalize(cross(dx, dy));
    if (dot(n, pos) > 0.0) {
        n = -n; // facing the camera
    }

    float light = 0.0;
    int count = int(LightColor.w + 0.5);
    for (int i = 0; i < 6; i++) {
        if (i >= count) {
            break;
        }
        // the nearest point on the channel
        vec3 a = Light[i].xyz;
        vec3 ab = LightEnd[i].xyz - a;
        float along = clamp(dot(pos - a, ab) / max(dot(ab, ab), 1.0e-4), 0.0, 1.0);
        vec3 toLight = a + ab * along - pos;
        float d = length(toLight);
        float lambert = clamp(dot(n, toLight / max(d, 1.0e-3)) * 0.85 + 0.15, 0.0, 1.0);
        light += Light[i].w * lambert * Sky.z / (d + Sky.z);
    }
    // the cloud lit above: soft light from the sky for what's open to it
    float fromAbove = Sky.x * (0.3 + 0.7 * max(n.y, 0.0)) * openSky(pos + n * 0.4);
    fragColor = vec4(LightColor.rgb * (light + fromAbove), length(pos));
}
#endif

#ifdef APPLY
void main() {
    vec2 pixel = gl_FragCoord.xy;
    float depth = texelFetch(DepthSampler, ivec2(pixel), 0).r;
    if (depth <= 0.0) {
        discard;
    }
    float distance = length(positionAt(pixel / Screen.xy, depth));
    // the four nearest light texels, weighted by bilinear position and by how alike their distances are
    vec2 lp = pixel * Screen.zw / Screen.xy - 0.5;
    ivec2 base = ivec2(floor(lp));
    vec2 f = lp - vec2(base);
    ivec2 limit = ivec2(Screen.zw) - 1;
    vec4 l00 = texelFetch(InSampler, clamp(base, ivec2(0), limit), 0);
    vec4 l10 = texelFetch(InSampler, clamp(base + ivec2(1, 0), ivec2(0), limit), 0);
    vec4 l01 = texelFetch(InSampler, clamp(base + ivec2(0, 1), ivec2(0), limit), 0);
    vec4 l11 = texelFetch(InSampler, clamp(base + ivec2(1, 1), ivec2(0), limit), 0);
    float tolerance = 0.02 * distance + 0.3;
    vec4 w = vec4((1.0 - f.x) * (1.0 - f.y), f.x * (1.0 - f.y), (1.0 - f.x) * f.y, f.x * f.y)
            * exp(-abs(vec4(l00.a, l10.a, l01.a, l11.a) - distance) / tolerance) + 1.0e-4;
    vec3 light = (l00.rgb * w.x + l10.rgb * w.y + l01.rgb * w.z + l11.rgb * w.w) / (w.x + w.y + w.z + w.w);
    fragColor = vec4(light * Sky.y, 0.0);
}
#endif
