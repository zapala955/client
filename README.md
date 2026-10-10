# Liquid Client — visual framework (Minecraft 26.3 · Fabric)

A "Liquid Glass" (macOS Sequoia / iOS) visual layer: a custom main menu with a floating dock and
shader wallpapers, a keybind-activated ClickGUI, a module system and the first visual module,
**Skybox**. Project rules live in `CLAUDE.md` and `.claude/skills/apple-glass-ui/SKILL.md`.

| | |
|---|---|
| Minecraft | 26.3 (unobfuscated, official Mojang names) |
| Fabric Loader / API | 0.19.5 / 0.161.0+26.3 |
| Loom / Gradle / Java | 1.18 (`net.fabricmc.fabric-loom`, non-remapping) / 9.7.1 / 25 |

```
./gradlew runClient   # dev client
./gradlew build       # build/libs/liquid-client-0.1.0.jar
```

## Features

* **Main menu** – macOS lock-screen style: a sharp photo resampled to the exact screen resolution,
  the date, a large Liquid Glass clock (rasterised at device resolution, frosted photo refracted
  inside the glyphs), user-picker style entries (Singleplayer · Multiplayer · Options) and
  menu-bar status icons (Relog · Mods · ClickGUI · Quit).
* **Singleplayer · Multiplayer · Settings** – macOS windows over the wallpaper (or the blurred
  world in game) with traffic lights, a unified toolbar, sheets, pop-up menus, tooltips and
  keyboard navigation ("Liquid Menus" in Settings switches back to vanilla). They replace the
  vanilla screens wherever they're opened (title screen, pause menu, other mods).
  * *Singleplayer* – world cards with icon, mode / cheats, relative "last played" and version,
    status badges (in use, incompatible, upgrade, hardcore), search, Recent / Name sorting, play
    from the icon or by double-click, Edit / Re-Create / Delete (confirmation sheet), saves folder.
    All flows are vanilla's (backups, symlink checks, data packs).
  * *Multiplayer* – server cards with favicon, full-colour MOTD (`StyledText`, incl. legacy `§`
    codes), signal bars + latency, players with the sample list as a tooltip, version pill for
    incompatible servers, a Local Network section, drag-to-reorder (or Shift+↑/↓), sheets for Add /
    Edit / Direct Connect / Delete, F5 refresh.
  * *Settings* – System-Settings layout: glass sidebar with coloured panes and search across every
    option; grouped rows with switches, sliders (value beside them) and pop-up menus driving vanilla's
    `OptionInstance`s through their own write paths; descriptions from the vanilla tooltips. Panes:
    Liquid Client, General (about, FOV, online, folders), Video, Audio, Controls (+ Key Binds), Chat,
    Skin, Language (apply on demand), Accessibility; shortcuts to Resource Packs, Iris shader packs,
    Mod Menu and Sodium's video settings. Vanilla's side effects are kept (texture reload after
    mipmap/filtering changes, fullscreen mode, unsupported-GPU warning).
* **Game menu** – minimal: over the frosted world, the world's name, one solid white Back to Game, a grouped glass list (Options, Advancements,
  Statistics, World Options and the server's own pause-menu entry), Save and Quit / Disconnect on
  its own (vanilla's draft-report handling), a row of small monochrome icons (ClickGUI, HUD
  editor, Mod Menu, screenshots, feedback, bug reports, friends with a request dot, player
  reporting) and one line naming what's playing (click it to play / pause the PC's player). It
  subclasses `PauseScreen`, so vanilla's `instanceof` checks keep working.
* **Resource packs** (also data packs) – Available / Selected columns of pack cards with icons and
  full-colour descriptions, hover buttons to enable, disable and reorder, locks on required packs,
  an Incompatible pill with vanilla's confirmation, search, drag-and-drop install, folder watching.
  Driven by vanilla's `PackSelectionModel`.
* **Loading** – the resource-loading overlay is the client's splash: the menu wallpaper frosted
  (decoded on a worker thread), the app icon, the "Liquid Client" wordmark and a glass capsule bar;
  it dissolves into the main menu, which starts frosted and sharpens. Drawn with vanilla pipelines
  and jar-loaded art only, since the client's own shaders and fonts load in that very reload
  (`render/loading`, art from `tools/loading_art.py`).
  Connecting, world loading (with a live chunk map), saving and "reading world data" share one
  frosted card that morphs from step to step ("Liquid Loading Screens" in Settings).
* **Wallpapers** (`BackgroundEngine`, list in `Wallpaper`) – 11 photos at 3840 px (Alpine Lake,
  Tahoe Dusk, Half Dome, Big Sur, Desert, White Sands, Northern Lights, Milky Way, Mist, Tokyo,
  Iceland), resampled once to the exact framebuffer; 7 live GLSL wallpapers evaluated per device
  pixel, so they're sharp at 4K and beyond (Aurora over a mirrored lake, Nebula, Silk, Glow, Liquid
  Glass drops, Ribbons, Dunes); any image from `config/liquidclient/backgrounds/`; or the vanilla
  panorama. Each live wallpaper has an analytic frosted version for glass panels, and a pre-rendered
  frosted first frame for the boot screen (rendered offline from the same shader). Glass panels
  frost only what's behind them.
* **ClickGUI** (Right Shift) – draggable window, traffic lights (close / collapse / zoom), a sidebar
  of macOS Settings-style coloured category tiles with enabled-module counts under a liquid
  selection indicator (empty categories are hidden), and module cards with an icon tile that lights
  up while the module is on, sliders, dropdowns and switches. Type anywhere (or Ctrl+F) to search
  every module by name or description; Escape clears the search, then closes. Every module has a
  key chip: click it, press a key to bind (Escape cancels, Backspace / Delete unbinds); bound keys
  toggle the module in game and show a notification. The window comes back exactly as it was left –
  position, category, size, collapse, scroll, search and open settings drawers – also after a restart.
* **Ambience** (Render) – client-side time of day (a 24 h clock; the day count and moon phases keep
  going), weather (server / clear / rain / thunderstorm / snow, eased like vanilla's transitions) and
  fullbright with a strength. Overrides the world clock and weather only for what you see.
  **Liquid Weather** replaces vanilla's rain and snow sheets: thin motion-blurred rain streaks slanted
  by the wind, snowflakes that drift, flutter, spin and tilt (crystals up close, soft bokeh right in
  front of the lens, the odd glint), crowns of droplets where rain hits the ground and rings on
  water. Rain and snow stop on roofs, leaves and water surfaces and take the light of where they
  fall (dim at night, warm near torches). Precipitation can follow the biome or always rain / snow
  (deserts included, sounds too); rain and snow density, snowflake size, wind, splashes and range.
  Everything is placed by the GPU from a static buffer – no per-particle CPU work.
* **Custom Fog** (Render) – atmospheric fog distance, distant-chunk edge fade on / off and a tint
  colour + strength. Fog in water, lava, powder snow and from Blindness / Darkness is left alone.
* **Removals** (Render) – hurt camera shake, fire overlay (normal / low / hidden), pumpkin overlay,
  powder-snow frost, nausea and portal warp, underwater overlay, in-wall overlay, totem animation,
  vignette, boss bars and the scoreboard, each separately.
* **View Model** (Render) – main and off hand X / Y / Z offsets and held-item size, swing speed
  (purely visual: 26.3 has no swing packet) and "no equip / cooldown dip".
* **Kill Effects** (Render) – where something you hit dies: lightning (a visual-only bolt with a
  local, quiet thunder), totem burst, hearts, flame helix, souls or sparks, with sound.
* **Particles** (Render) – particles floating in the air around you. 22 styles: Fireflies, Snowfall,
  Sakura Petals, Autumn Leaves, Stardust, Bokeh Lights, Embers, Spirit Wisps, Bubbles, Hearts, Arcane
  Runes, Dust Motes, Orbit Halo, Confetti, Diamond Dust, Music Notes, Void Motes, Shooting Stars,
  Bioluminescence, Sky Lanterns, Starlight, Aurora Vortex. Shape (15: glow orb, sparkle, star, bokeh
  with round or hexagonal iris, ring, snowflake, petal, leaf, heart, faceted diamond, confetti,
  iridescent bubble, anamorphic lens flare, music note, rune) and motion (10: float, fall, flutter
  with 3D tumble, rise, wander, swirl vortex, orbit, hover, spiral, meteor) can be mixed freely.
  Colour: style palette, custom, two-hue gradient, rainbow, chroma wave, over lifetime, HUD accent or
  the album art of what's playing. Amount, area, size, speed, lifetime, opacity, glow, twinkle,
  spin, turbulence (a coherent flow field, so neighbours drift together), wind with a compass
  direction, glow / soft blending, motion trails, glow in the dark, move with player, land on
  blocks (snow settles on the ground), outdoors only, fade near camera, and show always / at night /
  by day / in rain / in clear weather. Looks apply live to particles already in the air; enabling,
  disabling and restyling cross-fade. Shapes are distance fields in their own shader
  (`ambient_particle.fsh`), crisp at any size, drawn through vanilla's particle engine (no registry
  entries, so vanilla servers are fine); without that shader, or under an Iris shader pack, they use
  vanilla's particle pipeline and sprites.
* **Atmosphere** (Render) – depth for the landscape: distance haze (aerial perspective, from a
  start distance), valley mist that pools below a chosen height while peaks rise out of it
  (density, height, thickness), a glow in the air towards the sun (a faint cool one towards the moon
  at night), colour from the sky's fog colour or warm / cool / custom, and a maximum density. Rebuilt
  from the depth buffer, before particles are drawn so they stay crisp; off underwater and in lava.
* **Lighting** (Render) – recolours the light itself: silver-blue moonlight (strength and colour)
  instead of vanilla's flat lavender-grey nights, sunlight warmth, sky and torch light strength, torch
  colour and tint with its own soft flicker, shadow brightness and overall brightness. Works in the
  lightmap, so every renderer picks it up for free.
* **Color Grading** (Render) – 13 film looks (Cinematic teal & orange, Golden Hour, Moody, Noir,
  Pastel, Vintage Film, Cyberpunk, Arctic, Vivid, Sepia, Matrix, Dream) with a strength, exposure,
  tone curve (linear / filmic / soft), contrast, saturation, vibrance, temperature, tint, shadows,
  highlights, vignette, film grain, chromatic aberration and sharpening. Grades the world and the hand,
  never the GUI; dithered so gradients don't band.
* **Bloom** (Render) – bright light (sun, lava, torches, glowing particles) bleeds softly into its
  surroundings: intensity, threshold, softness, radius and anamorphic streaks. A half-float mip
  chain (13-tap downsample with Karis averaging, tent upsample); works with or without Color Grading.
* **Sun Rays** (Render) – crepuscular rays: shafts of sunlight streaming through gaps in clouds,
  leaves and doorways, faint moonbeams at night, and a lens flare (subtle, or cinematic with an
  anamorphic streak) only while the sun is actually visible. Intensity, ray length, colour (natural
  – following sunrise / sunset –, golden, warm, cool), flare strength and quality. A half-resolution
  sky mask around the sun and two radial blurs towards it (N² taps without banding), added before the
  hand so the hand covers them; Bloom glows on top.
* **Motion Blur** (Render) – cinematic camera motion blur: every pixel is reprojected with last
  frame's camera (rotation, movement and zoom; the sky by rotation only) and the frame is blurred along
  that motion, after the world and before the hand, so the hand stays sharp. A 60 FPS shutter
  (frame-rate independent), capped length, no blur across teleports. Strength, blur when moving,
  max length, quality.
* **Wet Lens** (Render) – water on the camera. Under open sky in the rain, beads land on the lens –
  each a tiny lens with the world upside-down inside it, darker at the rim with a glint – and evaporate,
  while bigger drops run down in fits and starts leaving trails; looking up catches more. Coming out
  of water leaves the lens streaming until the sheet drains from the top; snow frosts it from the
  edges (icy, blurred, crystalline). Amount, drop size, refraction, and Always Wet for screenshots.
  One procedural full-screen pass, before Bloom so the drops catch the light.
* **Waving Plants** (Render, Sodium) – leaves, vines, grass, flowers and crops sway: gust fronts roll
  across fields in the wind's direction, plants lean and bob from fixed roots, two-block plants bend
  along their whole height, canopies shimmer without leaving their trunks. Seven wind levels, eight
  directions or "same as Particles", gusts, and switches per plant type. Runs in Sodium's terrain
  vertex shader; which blocks sway is tagged while chunks are meshed, like a shader pack's block ids.
* **Showcase** (Player, F6) – the camera glides out of your eyes into a cinematic shot of your
  character and back again: Turntable Orbit, Hero Portrait (from the front, gently swaying),
  Cinematic Drone (sweeping distance and height) or Dolly (slow push-in and pull-back), through a
  longer lens, with a breath of handheld drift, letterbox bars and a glass name card, HUD hidden.
  Drag the mouse to orbit (with easing), scroll to zoom; the camera pulls in in front of walls.
  Distance, height, focus point, speed and direction, lens, transition time, exit when moving.
* **Zoom** (Utility) – hold C: a smooth, frame-rate independent zoom (factor and speed), scroll to
  zoom further, sensitivity that follows the zoom, optional cinematic camera.
* **Rainbows** (Render) – when a real rain clears and the sun comes out, a rainbow arcs across the sky
  opposite the sun: placed physically (42° around the antisolar point, so it moves with the sun),
  every wavelength at its own angle and smeared by the sun's disc, a reversed secondary bow,
  Alexander's dark band between them, faint supernumerary bands, orange-red bows at sunset, pale
  moonbows at night, patchy where the rain is thinner, and visible over distant hills. After rain /
  during light rain too / always, intensity and duration.
* **Wet World** (Render) – rain soaks the world: everything the rain reaches darkens and turns
  glossy (Fresnel sheen), puddles gather on flat ground (fewer on grass and leaves) and mirror the
  world with screen-space reflections, rings from raindrops dance in them while it rains, and the
  sun or moon glints off it all – then it dries over the drying time. Roofs, overhangs and caves
  stay dry: it uses Liquid Weather's per-column rain heights. Drawn over the opaque world before
  water, particles and weather.
* **Ambient Occlusion** (Render) – soft contact shadows where light can't reach: in corners, where
  blocks meet, under leaves and overhangs, around mobs' feet. Horizon-based (HBAO) from the depth
  buffer: per pixel, the horizon the surrounding geometry raises above the surface in several rotated
  directions, then a depth-aware blur and upsample so edges stay crisp. Only the opaque world is
  shaded (water, glass, particles, weather and the hand are untouched), and it fades out with distance.
  Strength, radius, contrast, quality (half resolution, or full on Ultra) and a preview of the AO alone.
* **Depth of Field** (Render) – the world through a real camera lens: what you look at stays sharp,
  everything nearer or further melts into soft blur, and lamps, lava, stars and the sun bloom into
  bokeh discs – circles, six-bladed hexagons, anamorphic ovals or vintage cat's eyes that turn into
  lemons towards the frame's edges, with faint magenta / green fringes like real glass. A thin-lens
  model in diopters (aperture f/1–f/16, focal length 18–200 mm, longer while zoomed), so focus pulls
  feel even and close objects blur faster than far ones. Autofocus follows the crosshair, steadied by
  the surface around it and eased like a focus ring; or a manual distance, or a tilt-shift miniature
  band. Foreground blur spills softly over a sharp background and the rims of defocused objects turn
  see-through, as with a real lens. Focus assist: peaking (sharp edges glow) or a blur map. Half
  resolution scatter-as-gather in linear light, a CoC-aware composite at full resolution; after the
  world and before the hand, so the hand and the HUD stay sharp.
* **Hand Chams** (Render) – your first-person arm and held items, and in third person (F5, Showcase)
  the items you hold, drawn with a shader instead of their textures:
  Liquid Glass (first person: refracts the world behind your hand with colour dispersion; in the world:
  see-through crystal), Hologram (scanlines, sweep, flicker, glitching slices, glowing edges), Chroma,
  Galaxy (a window into space), Wireframe, Flat Color, Plasma, Tinted Skin and Gradient. Two colours
  (or the HUD accent), saturation, opacity, rim glow, texture detail, refraction, animation speed,
  pattern scale, emissive or world-lit, sleeves, held items, third person. Swapped at submission: models
  get a chams render type around their own texture (shapes stay exact), item submissions are marked and
  each quad's type swapped when built; one pipeline plus OIT variants. Only your own view changes.
* **Skybox** (Render) – replaces the whole overworld sky. 18 modes: 4K Night Galaxy Texture,
  Procedural Sun/Moon, Pastel Sunset, Atmospheric Fog, Aurora Borealis, Cosmic Nebula, Synthwave
  Horizon, Black Hole, Solar Eclipse, Golden Hour Clouds, Crimson Storm, Cotton Candy, Digital Rain,
  Starlight Void, Prism Rainbow, Deep Ocean, Radiant Sunbeams (towering clouds with silver linings
  around the real sun – made for Sun Rays, which pours light through the gaps), Silver Lining (a
  volumetric cloud deck ray-marched per pixel: weather-map coverage, billowy shapes with eroded edges,
  Beer–Lambert light with a two-lobe Henyey–Greenstein phase, so edges between you and the moon or
  sun blaze silver while cores stay dark, and the light glows through the gaps). Plus cloud patterns (wispy, puffy, streaks, swirl,
  mackerel cells, ripples), star styles (dots, sparkle flares, shooting stars, rainbow twinkle),
  tint, rotation / animation speed, star density, cloud coverage, brightness, horizon blending,
  terrain-fog sync and "hide vanilla clouds". The galaxy uses `config/liquidclient/sky/galaxy.png`
  (equirectangular) if present, otherwise a generated 4096×2048 galaxy. Disabling cross-fades back
  to vanilla and frees all GPU resources.
* **HUD + HUD editor** (HUD category, "Edit HUD" in the ClickGUI title bar or the "Open HUD Editor"
  key) – 18 elements: Dynamic Island (below), Music, Lyrics, FPS, Ping, CPS, Coordinates, Speed, Compass, Clock, Memory, Server,
  Keystrokes, Armor, Potion Effects, Target HUD (face or spawn egg, animated health bar with a
  trailing damage segment and absorption, distance, armour), Module List (enabled modules as glass
  capsules that slide in from the anchored edge) and Notifications (keybind toggles). 15 styles – Frosted / Vibrant / Liquid Glass Blur (real blur of the game
  behind the element: the frame is copied right before the GUI draws, `BackdropCapture` + `hud_blur.fsh`), Liquid Glass, Clear Glass, Frost, Minimal, Dark Pill,
  Dynamic Island, Neon Glow, Accent Gradient, Outline, Sidebar Accent, Classic, Chroma Border) as a
  theme with per-element overrides; accent from a colour, animated chroma or the album art. The
  editor drags with snapping (screen edges, centre lines, other elements), scroll resizes, and
  positions are anchored to the nearest screen third so layouts survive resolution changes.
* **Dynamic Island** (HUD) – a black pill at the top of the screen that comes alive, like the
  iPhone's. Live activities sit in the compact pill (a glyph at the left end, an accessory at the
  right): the zoom factor while zooming, the face and health of what you're fighting, a heart that
  beats faster as your health drops (with a red glow), your air underwater as a ring, and the song
  that's playing (album art and a level meter in the album's colour). The most important one fills
  the pill; the next one splits off into a bubble beside it – it pushes out of the pill, pulls a
  melted neck that thins and snaps, and settles apart. Alerts expand the island for a few seconds
  and queue up: module toggles (the module's tile and its switch flipping – these replace the
  Notifications toasts while the island is on), a new song (the Now Playing card: art, title,
  artist, a timeline), joining a server (name, address and live ping bars), changing dimension,
  levelling up (a ring filling to your progress), eliminations (the victim's face, with a streak
  badge for quick kills), gear about to break (the item and what's left) and screenshots (with a
  thumbnail of the picture). Idle it's a pill, a clock or nothing. Width, height, corners and the
  bubble move on damped springs (growing overshoots a little and settles, shrinking is snappier);
  content waits for the shape, then fades and scales in, clipped to the island, and a repeat alert
  bumps it instead. Black (with a rim light so it reads at night) or Liquid Glass (the game behind
  it, blurred and refracted at the rim). The shape is one signed distance field (`island.fsh`):
  body and bubble joined by a smooth minimum whose reach shrinks as the gap opens. Each activity can
  be turned off; alert duration and scale are adjustable.
* **Music** – Spotify (or any player) from the Windows media session via a small PowerShell bridge
  (`media/now_playing.ps1`, no Spotify account or app key). Six layouts: Card, Compact, Dynamic
  Island, Vinyl, Ambient (album glow), Minimal. Album art is looked up by title + artist from the
  public iTunes / Deezer search (only that text is sent; "Album Art Lookup" turns it off). Media
  keys (play/pause, next, previous) are bindable. The main menu shows a lock-screen "Now Playing"
  card with working controls ("Menu Media Player" in Settings).
* **Lyrics** (HUD) – synced lyrics of what's playing, from LRCLIB (lrclib.net; only title, artist,
  album and length are sent, "Online Lyrics" turns it off). Six displays: Karaoke (words fill with
  light as they're sung, next line waiting), Floating Words (each word pops in and drifts away like
  a particle), Glass Bubbles (words in Liquid Glass bubbles that float up and pop), Wave, Cinema
  Subtitles, and In the World (3D words appearing in the air in front of you as they're sung). Text
  size, highlight colour (album art, HUD accent, white, rainbow), glow, next line and a sync offset.
  Ten fonts – Modern, Elegant Serif, Script, Poster, Rounded, Monospace, Marker, Futuristic,
  Handwriting and Minecraft – each in Regular, Bold, Italic and Bold Italic (synthesised where a
  family has no such cut), plus All Caps, Letter Spacing and Outline, in the HUD and in the world.
  Per-word timing is used when the lyrics have it, otherwise estimated within each line; every
  animation follows the song position, so pausing and seeking stay in sync.
* **Chunk Animations** (Render) – newly loaded chunk sections animate in instead of popping up.
  18 modes: Rise From Below, Drop From Above, Horizon (rise below / drop above the camera), Slide In,
  Burst Out, Grow, Sprout, Spin, Flip Up, Swirl, Assemble, Zoom, Jelly, Glitch, Ripple, Spiral,
  Fade Only, Random Mix; 13 easings (incl. back, elastic, bounce), duration, travel distance, fade-in
  and "animate nearby chunks". Runs entirely in the terrain vertex shader (Sodium or vanilla), so it
  costs no CPU time; it's inactive while an Iris shader pack is in use.

## Package layout

```
net.client.visuals
├── LiquidClient            entrypoint: keybind, tick listener, "last server" tracking
├── LiquidConfig            interface preferences → config/liquidclient.json
├── module                  Module (icon, keybind), Category, ModuleManager (JSON persistence, keybinds), setting/*,
│                           render/ (Skybox, ChunkAnimations, Ambience, CustomFog, Removals, ViewModel, KillEffects,
│                           Particles, Atmosphere, Lighting, ColorGrading, Bloom, WavingPlants),
│                           utility/ (Zoom, Browser)
├── hud                     HudManager, HudModule, HudStyle, HudTheme, element/* (incl. TargetHud, ModuleList, Notifications, Lyrics)
├── gui
│   ├── LiquidScreen        marker interface for our screens
│   ├── menu/CustomMainMenuScreen
│   ├── clickgui/           ClickGuiScreen (open/close lifecycle), ClickGuiWindow
│   ├── component/          UiComponent, ModuleCard, ToggleSwitch, SliderComponent, DropdownComponent, ToggleRow
│   ├── window/             WindowScreen (window chrome, input routing, focus, tooltips), Ui (tokens), TextField,
│   │                       PillButton, Segmented, Slider, Popover, PopupButton, Sheet, ScrollArea
│   ├── worlds/             WorldsScreen (Singleplayer)
│   ├── servers/            ServersScreen (Multiplayer)
│   ├── settings/           SettingsScreen, SettingsPage, SettingRow, OptionRow (vanilla options), Rows
│   ├── pause/              LiquidPauseScreen (game menu)
│   ├── packs/              PacksScreen (resource / data packs)
│   ├── loading/            LoadingScene (connecting, world loading, saving cards)
│   └── widget/             GlassButton, DockButton, DockIcons (vector icons)
├── render
│   ├── RenderUtils         primitive API (fill/gradient/stroke/hairline/shadow/glow/arc), opacity stack, fallback
│   ├── HighDpiEngine       GUI scale, device-pixel size, pixel-grid snapping
│   ├── RoundedRectState    pooled GuiElementRenderState for the SDF shader
│   ├── GlassRenderer       the glass material, in the skill's layer order
│   ├── LiquidText/TextLabel Inter typeface, per-GUI-scale variants, tracking, uppercase, cached glyph runs;
│   │                       also the Lyrics faces (TextStyle.LYRIC_*)
│   ├── StyledText          multi-colour chat components (MOTDs) as cached sharp-text runs
│   ├── loading/            BootScreen (resource-loading overlay), BootArt (exact-size boot textures)
│   ├── Palette, ColorUtil  design tokens as packed ARGB ints
│   ├── UiSounds            interface sounds (sounds.json; Material Design sound resources, built by tools/ui_sounds.py)
│   ├── anim/               Smooth, Tween, Easing, Motion tokens, DeltaTimer, UiFrame
│   ├── background/         BackgroundEngine, WallpaperTexture, BackdropState
│   ├── sky/                LiquidSky (renderer + lifecycle), SkyUniforms, GalaxyTexture
│   ├── post/               PostFx (wet world, atmosphere, rainbows, sun rays, wet lens, bloom, grading passes and
│   │                       resources), WetWorld, Rainbow, WetLens, GradingLook, FloatBlock
│   ├── chunk/              ChunkAnimator, TerrainShaderPatch, FoliageWaving (waving plants)
│   ├── lyrics/             WorldLyrics (Lyrics "In the World" 3D words), LyricFont (font × style table)
│   ├── particle/           AmbientParticles (spawner, shared settings), AmbientParticle, ParticleStyle,
│   │                       ParticleShape, ParticleMotion
│   ├── weather/            LiquidWeather (GPU rain, snow, splashes), WeatherColumns (surface / type / light per column)
│   └── shader/             LiquidPipelines, ShaderSupport, ShaderPacks (Iris check), BlurEngine
└── mixin                   GuiMixin, GuiRenderStateMixin, GameRendererMixin, SkyRendererMixin, FogRendererMixin, CloudRendererMixin,
                            LoadingOverlayMixin, loading/ (Connect, GenericMessage, LevelLoading, Progress screens),
                            access/ (OptionInstance, Tooltip, SelectWorldScreen, JoinMultiplayerScreen, PackSelection* accessors),
                            visual/ (keybinds, ambience, removals, view model, zoom hooks)
```

## Hook points

| What | Where | Why there |
|---|---|---|
| Replace title screen | `GuiMixin` → `@ModifyVariable` at `HEAD` and at the `STORE` of `screen = new TitleScreen()` in `Gui#setScreen` | Since 26.2, screens are managed by `Minecraft.gui`. Both points run before the parameter is loaded, so the field and `init()` get the same screen. (Hooking the `PUTFIELD` was the cause of the "leave server → crash" bug: the original value was already on the stack.) |
| Liquid windows | Same `GuiMixin` hooks: exact `SelectWorldScreen` / `JoinMultiplayerScreen` / `OptionsScreen` → `WorldsScreen` / `ServersScreen` / `SettingsScreen`, keeping the parent (`access/*ScreenAccessor`, `OptionsScreen#getLastScreen`) | One place covers every caller (title screen, pause menu, safety notice, mods). |
| Game menu / packs | `GuiMixin`: exact `PauseScreen` with the menu → `LiquidPauseScreen` (F3+Esc stays vanilla); `PackSelectionScreen` → `PacksScreen` with its repository, callback and folder (`access.PackSelection*Accessor`, vanilla's watcher closed) | Covers the pause key, focus loss, Options, data packs and mods. |
| Boot screen | `LoadingOverlayMixin` → cancel `extractRenderState` at HEAD | `tick()` (reload completion, errors, screen re-init) stays vanilla; only drawing and the fade timeline change. |
| Loading cards | `loading/*ScreenMixin` → background + content of `ConnectScreen`, `LevelLoadingScreen` (non-portal), `GenericMessageScreen`, `ProgressScreen` | The screens stay (game code checks `instanceof LevelLoadingScreen`, calls `update`); narration, `ProgressScreen`'s stop logic and Connect's Cancel button are kept. |
| Settings rows | `access.OptionInstanceAccessor` (caption, value formatter, tooltip), `access.TooltipAccessor` (message) | Labels, value texts and descriptions stay exactly vanilla's, in every language. |
| Sky | `SkyRendererMixin` → `SkyRenderer#render` HEAD (cancel when fully faded in) / RETURN (cross-fade overlay) | Yarn's `WorldRenderer` is `LevelRenderer`; it only schedules the frame-graph sky pass, whose body calls `SkyRenderer#render`. |
| Fog colour | `FogRendererMixin` → `computeFogColor` TAIL | Terrain fog follows the sky mode's horizon. |
| Clouds | `CloudRendererMixin` → `render` / `renderOit` HEAD | "Hide Vanilla Clouds". |
| Chunk animation shader | `ShaderManagerMixin` → wrap `Resource#readAllAsString` in `ShaderManager#loadShader` | Patches `sodium:blocks/block_layer_opaque.vsh` and `minecraft:core/terrain.vsh` at source level (`TerrainShaderPatch`), independent of resource-pack order; unknown layouts stay untouched. |
| Chunk animation inputs (Sodium) | `sodium.SodiumUniformBufferManagerMixin` (`@Pseudo`) → `u_FadePeriodInv` argument of `GlobalUniforms`; `sodium.SodiumRenderRegionManagerMixin` → near-fade distance | Sodium already stores a per-section "first meshed" time for its fade; the settings ride in the one float we repurpose, as raw bits marked by the sign (Sodium's own value is positive), shared with Waving Plants' wind. Without an animation the bits carry Sodium's own fade period, so its fade-in is unchanged. |
| Chunk animation inputs (vanilla) | `LevelRendererMixin` → wrap `RenderSection#getVisibility` in `extractSectionDrawGroups` | Each section's `ChunkVisibility` carries its packed animation state while it animates. |
| Open ClickGUI | `ClientTickEvents.END_CLIENT_TICK` + `KeyMappingHelper.registerKeyMapping` | Default `InputConstants.KEY_RSHIFT`. 26.3 replaced GLFW with SDL, so GLFW constants are wrong now. |
| Module keybinds | `visual.KeyboardHandlerMixin` → before `KeyMapping#click` in `KeyboardHandler#keyPress` | Exactly where vanilla fires gameplay keys: never while a screen (chat, text fields) is open. Binds are SDL scancodes saved per module. |
| Ambience | `visual.ClientClockInstanceMixin` (`totalTicks` / `partialTick` of the client's world-clock instances), `visual.LevelWeatherMixin` (`getRainLevel` / `getThunderLevel` on `ClientLevel` only), `visual.LightmapMixin` (`extract` TAIL) | 26.x drives the sun, sky and daylight from world-clock timelines, all sampled through the clock instances. The End's own clock is left alone. |
| Liquid Weather | `visual.WeatherEffectRendererMixin` (`extractRenderState` HEAD, cancellable; `prepare` HEAD; `render` / `renderOit` HEAD), `visual.ClientLevelWeatherMixin` (`getPrecipitationAt` return; rain-splash `addParticle` in `tickWeatherEffects`) | Vanilla's columns are never extracted, so its own prepare / draw do nothing; ours draws in vanilla's slot (translucent pass or each OIT stage). `getPrecipitationAt` is only used by weather visuals and sounds on the client. Falls back to vanilla with an Iris shader pack or if the shader can't compile. |
| Custom Fog | `FogRendererMixin` → `setupFog` RETURN | Runs after the Skybox fog sync, on the finished `FogData`. |
| Removals | `visual.GameRendererEffectsMixin` (`bobHurt`, the portal / nausea intensities read in `renderLevel`), `visual.ScreenEffectRendererMixin` (`submitFire` / `submitWater` / `submitBlockSprite` / `renderItemActivationAnimation`), `visual.HudOverlaysMixin` (camera overlays, vignette, boss bars, scoreboard) | Only drawing changes; the effects themselves are untouched. |
| View Model | `visual.FirstPersonHandsMixin` (after `pushPose` in `submitArmWithItem`, `applyItemArmTransform` TAIL, arm height argument), `visual.LivingEntitySwingMixin` (`getModifiedSwingDuration`, local player only) | Offsets move arms, maps and items alike; scale applies where the item is positioned, so it shrinks in place. |
| Zoom | `visual.CameraZoomMixin` (`Camera#calculateFov`), `visual.MouseHandlerZoomMixin` (`LocalPlayer#turn` in `turnPlayer`, `Options.smoothCamera`, `ScrollWheelHandler#onMouseScroll` in `onScroll`) | The hand keeps its own FOV; the hotbar doesn't scroll while zooming. |
| Particles | `AmbientParticles.tick` (`END_CLIENT_TICK`) adds `AmbientParticle`s to `Minecraft.particleEngine`; each overrides `SingleQuadParticle#extract` and submits to custom `SingleQuadParticle.Layer`s (`LiquidPipelines.PARTICLE_GLOW` / `PARTICLE_SOFT` + their OIT sets) | No mixin and no particle type: vanilla ticks, culls and draws them, and the quad renderer binds each layer's own pipeline. The live count is taken from the particles' own ticks, so engine clears (world change, reload) never desync it. |
| Atmosphere | `GameRendererMixin` (projection before `LevelRenderer#render`, second pass before `render3dHud`), `LevelRendererMixin` (`@ModifyVariable` on the render pass at `executeTranslucentAfterTerrain` in `executeClassicTransparency`, closed at `RETURN`; `executeOit` HEAD) | Vanilla draws water, then particles, clouds and weather in one render pass. That pass is closed (idempotent), the world hazed from a depth copy, and reopened, so particles – no depth writes – aren't fogged like the terrain behind them. A second pass hazes only what came nearer since. Falls back to one pass after the world. |
| Bloom / Color Grading | `GameRendererMixin` → before `applyPostEffects` in `render` | After the hand and entity outlines, before vanilla's post effects and the GUI: one frame copy, the bloom chain, one grade / composite pass. |
| Lighting | `visual.LightmapMixin` → `LightmapRenderStateExtractor#extract` TAIL, before Ambience fullbright | Sky light colour / factor, block-light tint / factor and ambient colour are all in the render state, once per tick. |
| Waving Plants | `sodium.SodiumBlockRendererMixin` (`@Pseudo`: classify the block at `renderModel` HEAD, `@ModifyExpressionValue` on `Material#bits()` in `bufferQuad`), `ShaderManagerMixin` (`loadInclude`: Sodium's `chunk_vertex.glsl`), the Sodium terrain patch | The sway mode rides in two unused bits of the per-quad material byte; the wind shares Sodium's `u_FadePeriodInv` with Chunk Animations as raw bits (sign + bit 30 set, bit 29 clear: always a normal float). The wave code only compiles once the include is confirmed. Waves are whole periods per 128-block Sodium region, so they're world-anchored without a camera position. |
| Kill Effects / Target HUD | Fabric `AttackEntityCallback` (client side) | Remembers what you hit by entity id; a tick check fires the effect at zero health. |
| Recycle primitives | `GuiRenderStateMixin` → `GuiRenderState#reset` TAIL | The one GUI render state is reset before extraction and after drawing, so the pooled states are free again. |
| Blur strength | `GameRendererMixin` → redirect `Options#getMenuBackgroundBlurriness` in `extractOptions` | The blur post-chain reads the `MenuBlurRadius` global uniform. It's raised only while a `LiquidScreen` is open. |
| Private GUI internals | `liquidclient.classtweaker` | Opens `GuiGraphicsExtractor.guiRenderState` / `scissorStack` (so custom elements can be submitted and honour scissor) and `GuiRenderState.firstStratumAfterBlur` (to check whether the frame's single blur is already used). |

## Rendering pipeline

26.x GUIs are **deferred**: `extractRenderState(GuiGraphicsExtractor …)` records render states into
strata, and `GuiRenderer` draws them later. Raw OpenGL isn't supported since 26.2 (a Vulkan backend
exists), so all custom drawing goes through RenderPearl/Blaze3D `RenderPipeline`s.

**SDF rounded rectangles** (`shaders/core/rounded_rect.{vsh,fsh}`): one quad per shape. The
fragment shader evaluates an exact rounded-box distance field. Anti-aliasing uses `fwidth(d)`, so
edges stay exactly one physical pixel wide at GUI scale 1×–4×+ and under scale animations. The same
shader handles fills, hairline rings (`stroke`) and soft shadows/glows (`softness`). Parameters are
carried in the vertex semantics `BufferBuilder` can write (`UV0`, `UV3`, `LineWidth`, `UV2`). The
backend binds attributes **by name** via SPIR-V reflection, so the shader input names must match.

**Frosted glass**: `BlurEngine.frostBackdrop` flags the screen's background stratum
"blur before this". The engine renders the world/panorama/HUD, runs its box-blur post chain, then
draws our translucent glass on top. If blur is disabled or another mod already took the frame's only
blur pass, panels switch to an opaque slate tint (`Palette.GLASS_FILL_OPAQUE`).

**Fallback**: `ShaderSupport.revalidate()` (on every screen `init`) asks the pipeline cache to
compile our pipelines. If that fails, every primitive degrades to vanilla fills: chamfered
corners, layered shadow rects, no additive light.

## Performance notes

* Our render path allocates nothing in steady state: pooled element states with cached bounds,
  primitive ARGB ints, `TextLabel`s that pre-shape text once, mutable `Smooth`/`Tween` animators,
  and no lambdas or boxing per frame.
* Vanilla itself still allocates a small record per `GuiGraphicsExtractor.text(...)` call (deferred
  text state). That can't be avoided without replacing the text pipeline.
* Text is crisp because each style ships `oversample` 1–6 variants (glyph atlases are sampled
  NEAREST), and the variant matching the live GUI scale is chosen at draw time.

## Interaction model

* Animations use wall-clock delta (`DeltaTimer`), not partial ticks, which freeze while paused.
* `Smooth` = exponential follower `v += (t - v)(1 - e^(-k·dt))`, which is frame-rate independent.
* Motion tokens (`Motion`): ease-out when appearing, faster ease-in when leaving. *Reduce Motion*
  makes everything finish near-instantly.
* Mouse buttons are compared against `InputConstants.MOUSE_BUTTON_*` (SDL changed the raw values).

## Extending the ClickGUI

* New module: extend `Module` (id, category, Lucide icon codepoint), add `Setting`s, register it in
  `ModuleManager#init` and add `liquidclient.module.<id>` / `.desc` translations. Cards, search,
  keybinds and the module list pick it up automatically.
* New control: extend `UiComponent` (`preferredHeight`, `extract`, mouse callbacks) and map its
  setting type in `SettingComponents`. It appears in the card's right-click drawer.
* New category content: `ClickGuiWindow#createCards`.

## Credits

* Inter (UI text) and Nunito (lock-screen clock) are bundled under the SIL Open Font License 1.1
  (`INTER-OFL.txt`, `NUNITO-OFL.txt`).
* The Lyrics fonts are subset, renamed instances (`assets/liquidclient/font/lyrics/`, built by
  `tools/lyric_fonts.py`) of Playfair Display, Dancing Script, Bebas Neue, Nunito, JetBrains Mono,
  Orbitron and Caveat (SIL OFL 1.1) and Permanent Marker (Apache 2.0); licences in `LYRIC-FONTS-LICENSES.txt`.
* Menu icons are a subset of [Lucide](https://lucide.dev) (ISC, with Feather-derived icons under MIT),
  bundled as `assets/liquidclient/icons/lucide.ttf` (`LUCIDE-LICENSE.txt`).
* Filled transport glyphs (play, pause, skip, note) are a subset of Google's Material Symbols Rounded
  (Apache 2.0), instanced at FILL 1 / weight 500: `assets/liquidclient/icons/transport.ttf` (`MATERIAL-SYMBOLS-LICENSE.txt`).
* Interface sounds are Google's [Material Design sound resources](https://m2.material.io/design/sound/sound-resources.html)
  (CC BY 4.0), trimmed, level-matched and re-encoded by `tools/ui_sounds.py` (`MATERIAL-SOUNDS-LICENSE.txt`).
* Wallpapers from Unsplash under the [Unsplash License](https://unsplash.com/license):
  [alpine lake](https://unsplash.com/photos/a-blue-lake-surrounded-by-mountains-under-a-cloudy-sky-8zN9-vTCKvc) (`lake.jpg`) and
  [Lake Tahoe at dusk](https://unsplash.com/photos/a-body-of-water-with-mountains-in-the-background-8xtJFOxGJhw) (`dusk.jpg`),
  [Half Dome](https://unsplash.com/photos/T2vkCx_b04E) by Leo Serrat (`halfdome.jpg`),
  [Big Sur, McWay Falls](https://unsplash.com/photos/WkkKSJi1_-M) by Venti Views (`bigsur.jpg`),
  [desert dunes](https://unsplash.com/photos/88dHrVRUIuQ) by Shane Wilson (`desert.jpg`),
  [White Sands](https://unsplash.com/photos/h4eHV9CRxUc) by Joseph Corl (`whitesands.jpg`),
  [northern lights over a lake](https://unsplash.com/photos/ZPbfvIN4NXs) by Niklas Jonasson (`northernlights.jpg`),
  [Milky Way over a sea of clouds](https://unsplash.com/photos/eYXrvDWeJWs) and
  [Vestrahorn, Iceland](https://unsplash.com/photos/W3Ys641WCdQ) by Colin Watts (`milkyway.jpg`, `iceland.jpg`),
  [misty mountains](https://unsplash.com/photos/O8Y4TPR2tEk) by yangzhiyuan (`mist.jpg`),
  [Tokyo Tower](https://unsplash.com/photos/t1JoTScJs-c) by T Y (`tokyo.jpg`).
