# Project Directive: Liquid Glass UI Architecture (Minecraft 26.3)

## Technical Architecture & Performance Rules
- **Minecraft Version:** 26.3 (Fabric Loader & Fabric API)
- **Zero-Allocation Rendering Loop:** Do NOT allocate `new Vector()`, `new Matrix4f()`, or `new Color()` objects inside `render()` or `drawContext` calls. Use static pre-allocated instances or primitive color bitwise math (`ARGB` packed ints).
- **Lifecycle & Memory Safety:** On screen removal (`removed()` or `close()`), release framebuffers and shaders to prevent crashes when disconnecting from servers or switching screens.
- **Interpolation:** All transitions (hover scaling, opacity fades, window positioning) MUST use frame-rate independent linear interpolation (`lerp(current, target, delta * speed)`).
- **High DPI Scaling:** Dynamic coordinate math scaling relative to `client.getWindow().getScaleFactor()` for sharp, sub-pixel vector rendering.

## Visual Design System ("Sequoia Glass")
- **Palette:**
  - Background Panels: Translucent Dark Slate `rgba(15, 23, 42, 0.55)`
  - Panel Border Stroke: Thin gradient stroke `rgba(255, 255, 255, 0.18)` -> `rgba(255, 255, 255, 0.05)`
  - Active Accents: Apple System Blue (`#007AFF`) or Vibrant Pink/Purple glow gradient
  - Text: High-contrast Crisp White (`#F8FAFC`) with subtle drop shadow
- **Geometry:** Corner radii between 10px–14px; smooth drop shadows on floating cards.
- **Controls:** Include macOS traffic-light buttons (Red, Yellow, Green subtle pills) on window titlebars.

## Code Standards
- Separate visual rendering shaders into `net.client.visuals.render`
- Keep screen event handlers clean inside `net.client.visuals.gui`
- Provide fallback rendering routines if custom GL shaders fail or blur passes are disabled.

---

## Appendix: how these rules map onto 26.3 (read before writing code)

26.x is unobfuscated and uses official Mojang names; several terms above come from older Yarn-mapped
versions. The intent of every rule stands; this is how it is implemented here.

| Directive term | 26.3 reality in this codebase |
|---|---|
| `render()` / `drawContext` | `extractRenderState(GuiGraphicsExtractor, …)`. GUI rendering is deferred: screens record render states, `GuiRenderer` draws them later. |
| `client.getWindow().getScaleFactor()` | `Minecraft.getInstance().getWindow().getGuiScale()` (int), wrapped by `render.HighDpiEngine` (`px()`, `snap()`). |
| "custom GL shaders" | Raw OpenGL is unsupported since 26.2 (Vulkan backend). Use `RenderPipeline`s + GLSL in `assets/liquidclient/shaders/core`, compiled to SPIR-V by the engine. Vertex inputs bind **by name**. |
| `lerp(current, target, delta * speed)` | `render.anim.Smooth` uses the exact form `lerp(current, target, 1 - e^(-speed·dt))`. It is the same lerp, but stays stable at low FPS where `delta * speed` would exceed 1 and overshoot. |
| "release framebuffers and shaders" | Screens own no framebuffers; compiled pipelines belong to the engine's pipeline cache and must **not** be closed by mods. GPU resources we do own (textures, vertex buffers, uniform rings) live in `BackgroundEngine` / `LiquidSky` and are released on idle, world join/disconnect and client stop, always on the render thread between frames. |
| `WorldRenderer` | `LevelRenderer`. Sky drawing happens in `SkyRenderer#render`, executed by the frame-graph "sky" pass. |
| `shouldPause()` | `isPauseScreen()` |
| Key / mouse constants | 26.3 uses SDL, so always use `InputConstants` (`KEY_RSHIFT = 229`, `MOUSE_BUTTON_LEFT = 1`). Never GLFW values. |

Build: `./gradlew build` (wrapper Gradle 9.7.1, Java 25). Output in `build/libs/`.
The user prefers building the jar over launching the game; don't start `runClient` unless asked.
