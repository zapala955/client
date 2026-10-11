package net.client.visuals.render.water;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * What the water shader needs to know about the water within 32 blocks of the camera, as a 64×64
 * RGBA8 texture wrapped by world x / z (texel = (x &amp; 63, z &amp; 63)) like the weather columns:
 * <pre>
 *  R  openness: how much of the area around (a 13×13 blocks box) is water – a pond stays glassy, a
 *     lake gets the wind's waves
 *  G  churn: foam where a fall comes down (water columns next to and under water at least 1.5
 *     blocks higher), fading over three blocks
 *  B  unused
 *  A  255 where known
 * </pre>
 * Per column, the topmost water surface (from the motion-blocking heightmap, which counts fluids).
 * Computed on the client tick: columns that came into range, plus a few rows refreshed every tick
 * so changes show within a second; the blurred fields are re-derived only when a column changed.
 */
final class WaterField {
	static final int SIZE = 64;
	private static final int MASK = SIZE - 1;
	private static final int HALF = SIZE / 2;
	private static final int SWEEP_ROWS = 4;
	/** Openness box radius (blocks). */
	private static final int OPEN_RADIUS = 6;
	private static final int CHURN_RADIUS = 3;

	/** Per stored column: the water surface height, or NaN (no water at the top). */
	private final float[] surface = new float[SIZE * SIZE];
	private final int[] heldX = new int[SIZE * SIZE];
	private final int[] heldZ = new int[SIZE * SIZE];
	private final byte[] data = new byte[SIZE * SIZE * 4];
	/** Window-ordered scratch: water mask prefix sums and churn. */
	private final int[] prefix = new int[(SIZE + 1) * (SIZE + 1)];
	private final float[] window = new float[SIZE * SIZE];
	private final float[] churn = new float[SIZE * SIZE];
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private @Nullable ClientLevel level;
	private int originX;
	private int originZ;
	private int sweep;
	private boolean changed;
	private boolean ready;
	private boolean dirty = true;

	private @Nullable GpuTexture texture;
	private @Nullable GpuTextureView view;
	private @Nullable ByteBuffer upload;

	WaterField() {
		reset();
	}

	/** Client thread. Brings the window around the camera up to date. */
	void update(ClientLevel level, int cameraX, int cameraZ) {
		if (level != this.level) {
			reset();
			this.level = level;
		}
		int x0 = cameraX - HALF;
		int z0 = cameraZ - HALF;
		if (x0 != originX || z0 != originZ) {
			originX = x0;
			originZ = z0;
			changed = true;
		}
		for (int z = z0; z < z0 + SIZE; z++) {
			for (int x = x0; x < x0 + SIZE; x++) {
				int i = index(x, z);
				if (heldX[i] != x || heldZ[i] != z) {
					compute(level, i, x, z);
				}
			}
		}
		for (int r = 0; r < SWEEP_ROWS; r++) {
			int z = z0 + Math.floorMod(((sweep + r) & MASK) - z0, SIZE);
			for (int x = x0; x < x0 + SIZE; x++) {
				compute(level, index(x, z), x, z);
			}
		}
		sweep = (sweep + SWEEP_ROWS) & MASK;
		if (changed) {
			derive();
			changed = false;
		}
		ready = true;
	}

	private void compute(ClientLevel level, int i, int x, int z) {
		int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
		float value = Float.NaN;
		if (top > level.getMinY()) {
			pos.set(x, top - 1, z);
			FluidState fluid = level.getFluidState(pos);
			if (fluid.is(FluidTags.WATER)) {
				value = top - 1 + fluid.getHeight(level, pos);
			}
		}
		float old = surface[i];
		if (heldX[i] != x || heldZ[i] != z || Float.compare(old, value) != 0) {
			changed = true;
		}
		surface[i] = value;
		heldX[i] = x;
		heldZ[i] = z;
	}

	/** Openness and churn over the window, written into the texture data. */
	private void derive() {
		// The window in world order, and its water mask's prefix sums.
		for (int wz = 0; wz < SIZE; wz++) {
			for (int wx = 0; wx < SIZE; wx++) {
				window[wz * SIZE + wx] = surface[index(originX + wx, originZ + wz)];
			}
		}
		int stride = SIZE + 1;
		Arrays.fill(prefix, 0);
		for (int wz = 0; wz < SIZE; wz++) {
			int row = 0;
			for (int wx = 0; wx < SIZE; wx++) {
				row += Float.isNaN(window[wz * SIZE + wx]) ? 0 : 1;
				prefix[(wz + 1) * stride + wx + 1] = prefix[wz * stride + wx + 1] + row;
			}
		}
		// Churn: under every fall – a water column with a neighbour at least 1.5 blocks lower.
		Arrays.fill(churn, 0f);
		for (int wz = 0; wz < SIZE; wz++) {
			for (int wx = 0; wx < SIZE; wx++) {
				float s = window[wz * SIZE + wx];
				if (Float.isNaN(s) || !fallsHere(wx, wz, s)) {
					continue;
				}
				for (int dz = -CHURN_RADIUS; dz <= CHURN_RADIUS; dz++) {
					for (int dx = -CHURN_RADIUS; dx <= CHURN_RADIUS; dx++) {
						int nx = wx + dx;
						int nz = wz + dz;
						if (nx < 0 || nz < 0 || nx >= SIZE || nz >= SIZE) {
							continue;
						}
						float n = window[nz * SIZE + nx];
						if (Float.isNaN(n) || n > s - 1.0f) {
							continue;
						}
						float k = Math.max(0f, 1f - (float) Math.sqrt(dx * dx + dz * dz) / (CHURN_RADIUS + 0.5f));
						churn[nz * SIZE + nx] = Math.max(churn[nz * SIZE + nx], k);
					}
				}
			}
		}
		for (int wz = 0; wz < SIZE; wz++) {
			for (int wx = 0; wx < SIZE; wx++) {
				int x0 = Math.max(0, wx - OPEN_RADIUS);
				int z0 = Math.max(0, wz - OPEN_RADIUS);
				int x1 = Math.min(SIZE, wx + OPEN_RADIUS + 1);
				int z1 = Math.min(SIZE, wz + OPEN_RADIUS + 1);
				int water = prefix[z1 * stride + x1] - prefix[z0 * stride + x1] - prefix[z1 * stride + x0] + prefix[z0 * stride + x0];
				int cells = (x1 - x0) * (z1 - z0);
				// A full box is open water; a third of it already counts as a fair-sized pond.
				float open = Math.clamp((water / (float) cells - 0.2f) / 0.75f, 0f, 1f);
				int o = index(originX + wx, originZ + wz) * 4;
				data[o] = (byte) Math.round(open * 255f);
				data[o + 1] = (byte) Math.round(churn[wz * SIZE + wx] * 255f);
				data[o + 2] = 0;
				data[o + 3] = (byte) 255;
			}
		}
		dirty = true;
	}

	private boolean fallsHere(int wx, int wz, float s) {
		for (int k = 0; k < 4; k++) {
			int nx = wx + (k == 0 ? 1 : k == 1 ? -1 : 0);
			int nz = wz + (k == 2 ? 1 : k == 3 ? -1 : 0);
			if (nx < 0 || nz < 0 || nx >= SIZE || nz >= SIZE) {
				continue;
			}
			float n = window[nz * SIZE + nx];
			if (!Float.isNaN(n) && n < s - 1.5f) {
				return true;
			}
		}
		return false;
	}

	private static int index(int x, int z) {
		return (z & MASK) * SIZE + (x & MASK);
	}

	boolean ready(ClientLevel level) {
		return ready && level == this.level;
	}

	void reset() {
		Arrays.fill(heldX, Integer.MIN_VALUE);
		Arrays.fill(heldZ, Integer.MIN_VALUE);
		Arrays.fill(surface, Float.NaN);
		Arrays.fill(data, (byte) 0);
		originX = Integer.MIN_VALUE;
		originZ = Integer.MIN_VALUE;
		ready = false;
		changed = true;
		dirty = true;
		level = null;
	}

	// ---- GPU (render thread) ------------------------------------------------------------------

	/** Creates the texture if needed and uploads it when it changed. Outside render passes. */
	@Nullable GpuTextureView upload() {
		if (texture == null) {
			GpuDevice device = RenderSystem.getDevice();
			texture = device.createTexture("Liquid water field", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
					GpuFormat.RGBA8_UNORM, SIZE, SIZE, 1, 1);
			view = device.createTextureView(texture);
			upload = MemoryUtil.memAlloc(data.length);
			dirty = true;
		}
		if (dirty && upload != null) {
			ByteBuffer buffer = upload;
			buffer.clear();
			buffer.put(data).flip();
			RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, buffer, 0, 0, 0, 0, SIZE, SIZE);
			dirty = false;
		}
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
