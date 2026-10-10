package net.client.visuals.render.sky;

import net.minecraft.client.renderer.DynamicGpuDataStorage;

import java.nio.ByteBuffer;

/**
 * CPU mirror of the {@code LiquidSky} std140 uniform block (8 × vec4 = 128 bytes).
 *
 * <p>One mutable instance is rewritten every frame and streamed through a mapped ring buffer, so no
 * objects are created per frame. Writes floats in block order exactly like vanilla's
 * {@code Std140Builder} (all members are vec4, so there is no padding to account for).</p>
 */
final class SkyUniforms implements DynamicGpuDataStorage.DynamicGpuData {
	static final int SIZE = 8 * 16;

	/** xyz sun direction, w clear-weather factor. */
	final float[] sunDir = new float[4];
	/** xyz moon direction, w moon phase. */
	final float[] moonDir = new float[4];
	/** rgb vanilla sky colour, w vanilla star brightness. */
	final float[] skyBase = new float[4];
	/** rgb tint, w tint strength. */
	final float[] tint = new float[4];
	/** x time, y star rotation, z star density, w horizon blend. */
	final float[] params = new float[4];
	/** x output alpha, y mode, z cloud pattern, w star style. */
	final float[] params2 = new float[4];
	/** vanilla sunrise / sunset colour. */
	final float[] sunset = new float[4];
	/** x brightness, y cloud coverage, zw unused. */
	final float[] params3 = new float[4];

	@Override
	public void write(ByteBuffer buffer) {
		put(buffer, sunDir);
		put(buffer, moonDir);
		put(buffer, skyBase);
		put(buffer, tint);
		put(buffer, params);
		put(buffer, params2);
		put(buffer, sunset);
		put(buffer, params3);
	}

	private static void put(ByteBuffer buffer, float[] vec4) {
		buffer.putFloat(vec4[0]);
		buffer.putFloat(vec4[1]);
		buffer.putFloat(vec4[2]);
		buffer.putFloat(vec4[3]);
	}

	static void set(float[] target, float x, float y, float z, float w) {
		target[0] = x;
		target[1] = y;
		target[2] = z;
		target[3] = w;
	}
}
