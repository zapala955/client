---
name: apple-glass-ui
description: Enforces Apple-like Liquid Glass design standards, GL blending, and smooth animations for custom Minecraft 26.3 GUI screens.
---

# Apple Liquid Glass UI Skill Guidelines

When building or editing UI screens:
1. **Glassmorphism Layering:**
   - Always stack UI layers with depth: (Backdrop Blur / Tint) -> (Glass Card Surface) -> (Border Inset Highlight) -> (Text/Icons).
2. **Micro-Interactions:**
   - Hover states should slightly expand scale (`1.0` -> `1.03`), increase border brightness, and apply an active inner glow.
   - Click/Press states should contract scale (`0.97`) immediately with snappy easing.
3. **Typography & Hierarchy:**
   - Main header: Bold, tracked out, semi-transparent subtitle below it.
   - Section labels: Uppercase, 80% opacity text with light letter-spacing.
4. **Shader / Draw Context Standards:**
   - Use LWJGL / RenderSystem GL pipeline wrappers with dynamic vertex arrays or rounded corner shaders rather than blocky standard Minecraft rectangles.

## How to apply these in this codebase (26.3)

- Layering is implemented by `GlassRenderer.panel/surface`: backdrop (`GlassRenderer.Backdrop`, i.e. the
  engine blur or `BackgroundEngine` frost) → tint + sheen → gradient rim + inner highlight → text.
  Never draw text before its surface.
- Use `RenderUtils` primitives (SDF rounded rects, 1px hairlines via `HighDpiEngine.px()`), never
  vanilla `fill()` for visible chrome. Raw GL calls are not allowed in 26.3; "GL pipeline wrappers"
  means `RenderPipeline`s.
- Motion tokens live in `render.anim.Motion` (`HOVER_SCALE`, `PRESS_SCALE`, speeds). Animate with
  `Smooth` (frame-rate independent), never per-frame constants.
- Headers: `TextLabel.tracking(...)` for tracked type; section labels with `uppercase()` + `Palette.TEXT_SECTION`.
- Snap static geometry and text to the physical pixel grid with `HighDpiEngine.snap(...)`.
