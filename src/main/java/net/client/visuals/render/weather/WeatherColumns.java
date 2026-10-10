package net.client.visuals.render.weather;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * What the weather needs to know about every block column within 32 blocks of the camera, as a
 * 64×64 RGBA8 texture wrapped by world x / z (texel = (x &amp; 63, z &amp; 63)), so moving the camera
 * only computes the columns that came into range:
 * <pre>
 *  R, G  surface height: (y + 2048) * 16, little-endian – the top of the highest motion-blocking
 *        block or fluid, using its real shape (slabs, snow layers, water level)
 *  B     bits 0-1 precipitation there (0 none, 1 rain, 2 snow – the client's view, so Ambience's
 *        override applies), bit 2 water surface, bit 3 hot surface (lava, magma, lit campfire),
 *        bit 4 known
 *  A     light at the surface: sky &lt;&lt; 4 | block
 * </pre>
 * Computed on the client tick (a few rows are refreshed every tick, so placed or broken blocks and
 * light changes show up within a second) and uploaded before the frame's passes.
 */
final class WeatherColumns {
	static final int SIZE = 64;
	private static final int MASK = SIZE - 1;
	private static final int HALF = SIZE / 2;
	/** Rows recomputed per tick: the whole window every 16 ticks. */
	private static final int SWEEP_ROWS = 4;

	static final int TYPE_RAIN = 1;
	static final int TYPE_SNOW = 2;
	private static final int FLAG_WATER = 4;
	private static final int FLAG_HOT = 8;
	private static final int FLAG_KNOWN = 16;

	private final byte[] data = new byte[SIZE * SIZE * 4];
	private final int[] heldX = new int[SIZE * SIZE];
	private final int[] heldZ = new int[SIZE * SIZE];
	private final byte[] types = new byte[SIZE * SIZE];
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private @Nullable ClientLevel level;
	private int rainColumns;
	private int snowColumns;
	private int sweep;
	private boolean ready;
	private boolean dirty = true;

	private @Nullable GpuTexture texture;
	private @Nullable GpuTextureView view;
	private @Nullable ByteBuffer upload;

	WeatherColumns() {
		reset();
	}

	/** Client thread. Brings the window around the camera up to date. */
	void update(ClientLevel level, int cameraX, int cameraY, int cameraZ) {
		if (level != this.level) {
			reset();
			this.level = level;
		}
		int x0 = cameraX - HALF;
		int z0 = cameraZ - HALF;
		for (int z = z0; z < z0 + SIZE; z++) {
			for (int x = x0; x < x0 + SIZE; x++) {
				int i = index(x, z);
				if (heldX[i] != x || heldZ[i] != z) {
					compute(level, i, x, z, cameraY);
				}
			}
		}
		// Refresh a few rows: blocks placed or broken, light, the camera's height (biome temperature).
		for (int r = 0; r < SWEEP_ROWS; r++) {
			int z = z0 + Math.floorMod(((sweep + r) & MASK) - z0, SIZE);
			for (int x = x0; x < x0 + SIZE; x++) {
				compute(level, index(x, z), x, z, cameraY);
			}
		}
		sweep = (sweep + SWEEP_ROWS) & MASK;
		ready = true;
	}

	private void compute(ClientLevel level, int i, int x, int z, int cameraY) {
		int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
		Biome.Precipitation precipitation = level.getPrecipitationAt(pos.set(x, cameraY, z));
		int type = precipitation == Biome.Precipitation.RAIN ? TYPE_RAIN : precipitation == Biome.Precipitation.SNOW ? TYPE_SNOW : 0;
		int flags = FLAG_KNOWN;
		double surface = top;
		if (top > level.getMinY()) {
			pos.set(x, top - 1, z);
			BlockState state = level.getBlockState(pos);
			FluidState fluid = state.getFluidState();
			if (!fluid.isEmpty()) {
				surface = top - 1 + fluid.getHeight(level, pos);
				if (fluid.is(FluidTags.WATER)) {
					flags |= FLAG_WATER;
				} else if (fluid.is(FluidTags.LAVA)) {
					flags |= FLAG_HOT;
				}
			} else {
				VoxelShape shape = state.getShape(level, pos);
				if (!shape.isEmpty()) {
					surface = top - 1 + shape.max(Direction.Axis.Y);
				}
				if (state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(state)) {
					flags |= FLAG_HOT;
				}
			}
		}
		int light = LightCoordsUtil.getLightCoords(level, pos.set(x, Math.max(cameraY, top), z));
		int height = (int) Math.clamp(Math.round((surface + 2048.0) * 16.0), 0L, 0xFFFFL);

		int o = i * 4;
		data[o] = (byte) height;
		data[o + 1] = (byte) (height >>> 8);
		data[o + 2] = (byte) (type | flags);
		data[o + 3] = (byte) (LightCoordsUtil.sky(light) << 4 | LightCoordsUtil.block(light));
		count(types[i], -1);
		types[i] = (byte) type;
		count(type, 1);
		heldX[i] = x;
		heldZ[i] = z;
		dirty = true;
	}

	private void count(int type, int delta) {
		if (type == TYPE_RAIN) {
			rainColumns += delta;
		} else if (type == TYPE_SNOW) {
			snowColumns += delta;
		}
	}

	private static int index(int x, int z) {
		return (z & MASK) * SIZE + (x & MASK);
	}

	/** Whether the window has been computed for {@code level}. */
	boolean ready(ClientLevel level) {
		return ready && level == this.level;
	}

	boolean anyRain() {
		return rainColumns > 0;
	}

	boolean anySnow() {
		return snowColumns > 0;
	}

	/** Forget everything (another level, or released). */
	void reset() {
		Arrays.fill(heldX, Integer.MIN_VALUE);
		Arrays.fill(heldZ, Integer.MIN_VALUE);
		Arrays.fill(data, (byte) 0);
		Arrays.fill(types, (byte) 0);
		rainColumns = 0;
		snowColumns = 0;
		ready = false;
		dirty = true;
		level = null;
	}

	// ---- GPU (render thread) ------------------------------------------------------------------

	/** Creates the texture if needed and uploads the columns that changed. Outside render passes. */
	void upload() {
		if (texture == null) {
			GpuDevice device = RenderSystem.getDevice();
			texture = device.createTexture("Liquid weather columns", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
					GpuFormat.RGBA8_UNORM, SIZE, SIZE, 1, 1);
			view = device.createTextureView(texture);
			upload = MemoryUtil.memAlloc(data.length);
			dirty = true;
		}
		if (!dirty) {
			return;
		}
		ByteBuffer buffer = upload;
		buffer.clear();
		buffer.put(data).flip();
		RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, buffer, 0, 0, 0, 0, SIZE, SIZE);
		dirty = false;
	}

	@Nullable GpuTextureView view() {
		return view;
	}

	boolean allocated() {
		return texture != null;
	}

	/** Frees the texture. Render thread, between frames. */
	void release() {
		if (view != null) {
			view.close();
			view = null;
		}
		if (texture != null) {
			texture.close();
			texture = null;
		}
		if (upload != null) {
			MemoryUtil.memFree(upload);
			upload = null;
		}
		dirty = true;
	}
}
