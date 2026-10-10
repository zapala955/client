package net.client.visuals.render.hand;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.client.visuals.LiquidClient;
import net.client.visuals.hud.HudManager;
import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.HandChamsModule;
import net.client.visuals.render.shader.LiquidPipelines;
import net.client.visuals.render.shader.ShaderPacks;
import net.client.visuals.render.shader.ShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Hand Chams: your arm and held items – and in third person what you hold – drawn with
 * {@code hand_chams} instead of their textures.
 *
 * <h2>How things get swapped</h2>
 * Rendering is deferred: renderers <em>submit</em> models and items, which are drawn later, batched by
 * render type. So the swap happens at submission, inside a context: {@link #beginFirstPerson} /
 * {@link #end} around the first-person hands ({@code FirstPersonHandsMixin}), {@link #beginThirdPerson}
 * around your own player's held-item layer ({@code ChamsHeldItemMixin}), so in third person only the
 * held items change. Inside it
 * ({@code ChamsSubmitMixin}):
 * <ul>
 *   <li>models (arm, armor, cape, shields, tridents…) get our render type built around their own
 *       texture (Sampler0), so their shape – transparent pixels – stays exact;</li>
 *   <li>items have no render type at submission (each quad brings its material's), so their light
 *       value is marked ({@link #MARKER}) and {@code ItemFeatureRenderer} swaps the type per quad when
 *       it builds them ({@code ChamsItemMixin}).</li>
 * </ul>
 * The look's settings travel in the light and overlay values (see {@code hand_chams.vsh}), so one
 * pipeline (plus its OIT variants, for improved transparency in the world) does every style.
 *
 * <p>Glass in first person refracts what's behind the hand: right before the hands are drawn the
 * frame is copied into {@link Backdrop}, registered under {@link #BACKDROP} so render types can bind
 * it (Sampler1). In the world, Glass is see-through crystal instead.</p>
 */
public final class HandChams {
	/** Marks a chams light value (vanilla's never sets bit 15). */
	public static final int MARKER = 0x8000;
	private static final int WORLD = 0x2000;
	private static final int ITEM = 0x4000;
	private static final Object FIRST_PERSON = new Object();
	private static final Identifier BACKDROP = LiquidClient.id("dynamic/hand_backdrop");
	private static final Map<Identifier, RenderType> TYPES = new HashMap<>();
	private static final Set<RenderType> OURS = Collections.newSetFromMap(new IdentityHashMap<>());

	private static @Nullable Object owner;
	private static boolean world;
	private static boolean items;
	private static @Nullable Identifier skin;
	private static @Nullable Backdrop backdrop;
	private static @Nullable HandChamsModule module;

	private HandChams() {
	}

	private static HandChamsModule module() {
		HandChamsModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(HandChamsModule.class);
		}
		return m;
	}

	/** Whether chams can be drawn at all this frame. */
	public static boolean active() {
		return module().isEnabled() && ShaderSupport.handChams() && !ShaderPacks.inUse() && ensureBackdrop();
	}

	/** Whether the sleeve layer is drawn on the first-person arm. */
	public static boolean sleeves(boolean vanilla) {
		return vanilla && module().sleeves.get();
	}

	// ---- Submission contexts ------------------------------------------------------------------

	/** Around the first-person hands and items. */
	public static void beginFirstPerson() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (owner == null && player != null && active()) {
			begin(FIRST_PERSON, false, module().heldItems.get(), player);
		}
	}

	/** Around a held-item layer's submission: only your own player's, in third person. */
	public static void beginThirdPerson(EntityRenderState state) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (owner == null && player != null && state instanceof AvatarRenderState avatar && avatar.id == player.getId()
				&& module().thirdPerson.get() && active()) {
			begin(state, true, true, player);
		}
	}

	private static void begin(Object by, boolean inWorld, boolean withItems, LocalPlayer player) {
		owner = by;
		world = inWorld;
		items = withItems;
		skin = player.getSkin().body().texturePath();
	}

	public static void endFirstPerson() {
		end(FIRST_PERSON);
	}

	public static void end(Object by) {
		if (owner == by) {
			owner = null;
		}
	}

	public static boolean capturing() {
		return owner != null;
	}

	/** Items are swapped in this context: mark their light. */
	public static boolean swapsItems() {
		return owner != null && items;
	}

	/** A model's render type in this context: ours around its texture, or unchanged. */
	public static RenderType swapModel(RenderType type) {
		if (owner == null || OURS.contains(type) || type.isOutline()) {
			return type;
		}
		Identifier texture = texture(type);
		if (texture == null || !items && !texture.equals(skin)) {
			return type;
		}
		return renderType(texture);
	}

	/** An item quad's render type: ours when its submission was marked. */
	public static RenderType swapItem(RenderType type, int submittedLight) {
		if ((submittedLight & MARKER) == 0 || OURS.contains(type) || type.isOutline()) {
			return type;
		}
		Identifier texture = texture(type);
		return texture != null ? renderType(texture) : type;
	}

	private static @Nullable Identifier texture(RenderType type) {
		RenderSetup.TextureBinding binding = type.state.textures.get("Sampler0");
		return binding != null ? binding.location() : null;
	}

	private static RenderType renderType(Identifier texture) {
		RenderType type = TYPES.get(texture);
		if (type == null) {
			type = RenderType.create("liquidclient_hand_chams",
					RenderSetup.builder(LiquidPipelines.HAND_CHAMS)
							.setOitPipelines(LiquidPipelines.OIT_HAND_CHAMS)
							.withTexture("Sampler0", texture)
							.withTexture("Sampler1", BACKDROP)
							.useLightmap()
							.sortOnUpload()
							.createRenderSetup());
			TYPES.put(texture, type);
			OURS.add(type);
		}
		return type;
	}

	// ---- Parameters ---------------------------------------------------------------------------

	/** Light for a swapped model: an item unless it's the player's own skin (the arm). */
	public static int modelLight(int vanillaLight, RenderType original) {
		Identifier texture = texture(original);
		return light(vanillaLight, texture == null || !texture.equals(skin));
	}

	/** Light for an item submission. */
	public static int itemLight(int vanillaLight) {
		return light(vanillaLight, true);
	}

	/** Mode, emissive, the world's light here, context and colour (UV2; see {@code hand_chams.vsh}). */
	private static int light(int vanillaLight, boolean item) {
		HandChamsModule m = module();
		int x = m.mode.index() & 15
				| (m.glow.get() ? 16 : 0)
				| LightCoordsUtil.block(vanillaLight) << 5
				| LightCoordsUtil.sky(vanillaLight) << 9
				| (world ? WORLD : 0)
				| (item ? ITEM : 0)
				| MARKER;
		float hue;
		float saturation;
		if (m.accent.get() && HudManager.theme() != null) {
			hue = HudManager.theme().accentHue.getFloat();
			saturation = 0.82f;
		} else {
			hue = m.hue.getFloat();
			saturation = m.saturation.getFloat() / 100f;
		}
		int y = quantize(((hue % 360f) + 360f) % 360f / 360f, 127)
				| quantize(saturation, 15) << 7
				| quantize(m.opacity.getFloat() / 100f, 31) << 11;
		return x & 0xFFFF | y << 16;
	}

	/** Second colour, refraction, detail, rim, speed and pattern scale (UV1). */
	public static int overlay() {
		HandChamsModule m = module();
		int x = quantize(m.hue2.getFloat() / 360f, 127)
				| quantize(m.refraction.getFloat() / 200f, 31) << 7
				| quantize(m.detail.getFloat() / 100f, 15) << 12;
		int y = quantize(m.rim.getFloat() / 200f, 63)
				| quantize(m.speed.getFloat() / 300f, 63) << 6
				| Math.clamp(Math.round(m.scale.getFloat() / 25f) - 1, 0, 15) << 12;
		return x & 0xFFFF | y << 16;
	}

	private static int quantize(float value, int max) {
		return Math.clamp(Math.round(value * max), 0, max);
	}

	// ---- Glass backdrop -----------------------------------------------------------------------

	/** Render thread, right before the hands are drawn: Glass gets a copy of the frame behind them. */
	public static void beforeHands() {
		HandChamsModule m = module();
		if (!m.isEnabled()) {
			if (backdrop != null && backdrop.large) {
				backdrop.shrink();
			}
			return;
		}
		if (m.mode.index() != HandChamsModule.MODE_GLASS || !active()) {
			return;
		}
		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuTexture color = target.getColorTexture();
		Backdrop b = backdrop;
		if (color == null || color.isClosed() || b == null) {
			return;
		}
		try {
			b.ensure(color);
			RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(color, b.getTexture(), 0, 0, 0, 0, 0,
					color.getWidth(0), color.getHeight(0));
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.warn("Hand Chams glass backdrop copy failed", e);
		}
	}

	/** The texture render types bind as Sampler1; created small, grown to frame size for Glass. */
	private static boolean ensureBackdrop() {
		if (backdrop != null) {
			return true;
		}
		try {
			Backdrop b = new Backdrop();
			Minecraft.getInstance().getTextureManager().register(BACKDROP, b);
			backdrop = b;
			return true;
		} catch (RuntimeException e) {
			LiquidClient.LOGGER.warn("Hand Chams backdrop unavailable", e);
			return false;
		}
	}

	/** World left: the frame-sized copy goes. */
	public static void release() {
		owner = null;
		if (backdrop != null) {
			backdrop.shrink();
		}
	}

	/** A copy of the frame (or a 1×1 stand-in when not in Glass mode). */
	private static final class Backdrop extends AbstractTexture {
		boolean large;

		Backdrop() {
			sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
			allocate(1, 1, GpuFormat.RGBA8_UNORM);
		}

		void ensure(GpuTexture color) {
			int w = color.getWidth(0);
			int h = color.getHeight(0);
			GpuTexture current = texture;
			if (current != null && !current.isClosed() && current.getWidth(0) == w && current.getHeight(0) == h
					&& current.getFormat() == color.getFormat()) {
				return;
			}
			releaseTextures();
			allocate(w, h, color.getFormat());
			large = true;
		}

		void shrink() {
			releaseTextures();
			allocate(1, 1, GpuFormat.RGBA8_UNORM);
			large = false;
		}

		private void allocate(int w, int h, GpuFormat format) {
			GpuDevice device = RenderSystem.getDevice();
			texture = device.createTexture(() -> "Liquid hand backdrop", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
					format, w, h, 1, 1);
			textureView = device.createTextureView(texture);
		}
	}
}
