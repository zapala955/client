#ifndef LIQUID_WEATHER_GLSL
#define LIQUID_WEATHER_GLSL

// Liquid Weather parameters (render.weather.LiquidWeather#fill). std140, vec4s only.
layout(std140) uniform LiquidWeather {
    // xyz camera block position, w time in seconds (wraps hourly)
    vec4 CameraBlock;
    // xyz camera position inside its block, w framebuffer height in pixels
    vec4 CameraFrac;
    // Per layer (rain near / far, snow near / far, splashes), two vec4s each:
    //   [2i]     xyz animation offset (fall and wind travel), w horizontal period
    //   [2i + 1] xyz camera position modulo the periods,       w vertical period
    vec4 Layers[10];
    // x / z rain wind (m/s), y rain fall speed (m/s), w unused
    vec4 Motion;
    // x rain opacity, y snow opacity, z snowflake size, w splash opacity
    vec4 Look;
    // x streak exposure (s), y wind strength 0..1, z thunder 0..1, w unused
    vec4 Extra;
};

#endif
