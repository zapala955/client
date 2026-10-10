package net.client.visuals.render.post;

import net.minecraft.client.renderer.DynamicGpuDataStorage;
import org.joml.Matrix4fc;

import java.nio.ByteBuffer;

/**
 * A std140 uniform block made only of vec4s and mat4s (no padding), mirrored as one float array.
 * Rewritten in place every frame and streamed through a mapped ring, so nothing is allocated.
 */
public final class FloatBlock implements DynamicGpuDataStorage.DynamicGpuData {
	final float[] data;

	public FloatBlock(int vec4s) {
		data = new float[vec4s * 4];
	}

	public int size() {
		return data.length * Float.BYTES;
	}

	public void vec4(int slot, float x, float y, float z, float w) {
		int i = slot * 4;
		data[i] = x;
		data[i + 1] = y;
		data[i + 2] = z;
		data[i + 3] = w;
	}

	/** Column-major, occupying four slots. */
	public void mat4(int slot, Matrix4fc m) {
		vec4(slot, m.m00(), m.m01(), m.m02(), m.m03());
		vec4(slot + 1, m.m10(), m.m11(), m.m12(), m.m13());
		vec4(slot + 2, m.m20(), m.m21(), m.m22(), m.m23());
		vec4(slot + 3, m.m30(), m.m31(), m.m32(), m.m33());
	}

	/** Copies {@code floats} values from {@code source} into the block, starting at vec4 {@code slot}. */
	public void copy(int slot, float[] source, int from, int floats) {
		System.arraycopy(source, from, data, slot * 4, floats);
	}

	@Override
	public void write(ByteBuffer buffer) {
		for (float value : data) {
			buffer.putFloat(value);
		}
	}
}
