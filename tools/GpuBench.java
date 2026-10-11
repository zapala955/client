import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.lwjgl.opengl.GL45C.*;

/**
 * Times Liquid Water's GPU passes on your own graphics card, offscreen, without the game: the real
 * shaders ({@code liquid_water.fsh}) on a synthetic 1080p lake (a water plane under a tilted camera,
 * tagged the way the terrain shaders tag it, a bed five blocks down, a far shore and sky), with the
 * real uniform layout. Each pass – and the copies the frame takes – is timed with GPU timer queries:
 * many short single-frame samples, and the <b>minimum</b> is what counts (if Minecraft or anything
 * else is using the GPU at the same time, the minimum still finds the moments it had the card to
 * itself; the median shows how noisy it was). Not part of the build.
 *
 * <pre>
 * java -cp lwjgl.jar:lwjgl-glfw.jar:lwjgl-opengl.jar:lwjgl-natives-&lt;os&gt;.jar:lwjgl-glfw-natives-&lt;os&gt;.jar:lwjgl-opengl-natives-&lt;os&gt;.jar \
 *      tools/GpuBench.java [width height] [samples] [--save]
 * </pre>
 * {@code --save} also writes each scene's picture to {@code gpubench-*.png}, to see what was timed.
 * The LWJGL jars are in the Gradle cache once the project has been built (or on Maven Central). It
 * opens an invisible window for an OpenGL 4.5 context; run it from the project folder.
 */
public final class GpuBench {
	private static final Path SHADER = Path.of("src/main/resources/assets/liquidclient/shaders/core/liquid_water.fsh");
	private static final String VERTEX = """
			#version 330
			#extension GL_ARB_separate_shader_objects : require
			layout(location = 0) out vec2 texCoord;
			void main() {
			    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
			    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);
			    texCoord = uv;
			}
			""";
	private static final float NEAR = 0.05f;
	private static final float FAR = 1024f;
	private static final float FOV = 70f;

	/** One situation to time: camera height and pitch, quality, reflections mode, under water. */
	record Scene(String name, float height, float pitch, int quality, int reflections, boolean under) {
	}

	private static int width = 1920;
	private static int height = 1080;
	private static int samples = 200;
	private static boolean save;

	public static void main(String[] args) throws IOException {
		if (args.length >= 2) {
			width = Integer.parseInt(args[0]);
			height = Integer.parseInt(args[1]);
		}
		if (args.length >= 3 && !args[2].startsWith("--")) {
			samples = Integer.parseInt(args[2]);
		}
		save = Arrays.asList(args).contains("--save");
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("GLFW failed to start");
		}
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
		long window = GLFW.glfwCreateWindow(64, 64, "GpuBench", 0L, 0L);
		if (window == 0L) {
			throw new IllegalStateException("No OpenGL 4.5 context");
		}
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		System.out.println("GPU:        " + glGetString(GL_RENDERER));
		System.out.println("OpenGL:     " + glGetString(GL_VERSION));
		System.out.printf(Locale.ROOT, "Resolution: %d x %d, %d samples per pass (min = the number that counts)%n%n", width, height, samples);

		String source = Files.readString(SHADER);
		int noise = program(source, "NOISE");
		int reflect = program(source, "REFLECT");
		int shafts = program(source, "SHAFTS");
		int composite = program(source, "COMPOSITE");
		int vao = glCreateVertexArrays();
		glBindVertexArray(vao);

		int hw = (width + 1) / 2;
		int hh = (height + 1) / 2;
		int waveTex = texture(GL_RGBA16F, 256, 256);
		int reflectTex = texture(GL_RGBA16F, hw, hh);
		int shaftTex = texture(GL_RGBA16F, hw, hh);
		int outTex = texture(GL_RGBA8, width, height);
		int depthTex = texture(GL_R32F, width, height);
		int bedTex = texture(GL_R32F, width, height);
		int behindTex = texture(GL_RGBA8, width, height);
		int sceneTex = texture(GL_RGBA8, width, height);
		int copyColor = texture(GL_RGBA8, width, height);
		int copyDepth = texture(GL_R32F, width, height);
		int fieldTex = texture(GL_RGBA8, 64, 64);
		int columnTex = texture(GL_RGBA8, 64, 64);
		upload(fieldTex, 64, 64, fieldData());
		upload(columnTex, 64, 64, columnData());
		int nearest = sampler(GL_NEAREST, GL_CLAMP_TO_EDGE);
		int linear = sampler(GL_LINEAR, GL_CLAMP_TO_EDGE);
		int repeat = sampler(GL_LINEAR, GL_REPEAT);
		int ubo = glCreateBuffers();
		glNamedBufferStorage(ubo, 24 * 16, GL_DYNAMIC_STORAGE_BIT);

		// The wave field, once (as in the game).
		draw(noise, waveTex, 256, 256, new int[0], new int[0], new int[0]);

		List<Scene> scenes = List.of(
				new Scene("lake, balanced, screen-space reflections", 6f, -18f, 1, 2, false),
				new Scene("lake, performance", 6f, -18f, 0, 2, false),
				new Scene("lake, ultra", 6f, -18f, 2, 2, false),
				new Scene("lake, sky-only reflections", 6f, -18f, 1, 1, false),
				new Scene("ocean to the horizon (most of the screen water)", 14f, -32f, 1, 2, false),
				new Scene("under water, looking up", -3f, 14f, 1, 2, true));
		int query = glGenQueries();
		double worst = 0.0;
		for (Scene scene : scenes) {
			fillScene(scene, depthTex, bedTex, behindTex, sceneTex, ubo);
			int[] reflectInputs = {depthTex, bedTex, behindTex, waveTex, fieldTex};
			String[] reflectNames = {"DepthSampler", "BedSampler", "BehindSampler", "WaveSampler", "FieldSampler"};
			int[] reflectSamplers = {nearest, nearest, linear, repeat, nearest};
			int[] compositeInputs = {depthTex, bedTex, behindTex, waveTex, fieldTex, sceneTex, reflectTex, scene.under() ? shaftTex : reflectTex, columnTex};
			String[] compositeNames = {"DepthSampler", "BedSampler", "BehindSampler", "WaveSampler", "FieldSampler", "SceneSampler", "ReflectSampler",
					"ShaftSampler", "ColumnSampler"};
			int[] compositeSamplers = {nearest, nearest, linear, repeat, nearest, nearest, nearest, linear, nearest};
			glBindBufferBase(GL_UNIFORM_BUFFER, 0, ubo);
			System.out.println(scene.name());
			double copies = time(query, () -> {
				glCopyImageSubData(behindTex, GL_TEXTURE_2D, 0, 0, 0, 0, copyColor, GL_TEXTURE_2D, 0, 0, 0, 0, width, height, 1);
				glCopyImageSubData(depthTex, GL_TEXTURE_2D, 0, 0, 0, 0, copyDepth, GL_TEXTURE_2D, 0, 0, 0, 0, width, height, 1);
				glCopyImageSubData(sceneTex, GL_TEXTURE_2D, 0, 0, 0, 0, copyColor, GL_TEXTURE_2D, 0, 0, 0, 0, width, height, 1);
			}, "copies (world before water, its depth, the tint)");
			double r = time(query, () -> drawNamed(reflect, reflectTex, hw, hh, reflectInputs, reflectNames, reflectSamplers), "reflections (half res)");
			double s = 0.0;
			if (scene.under()) {
				s = time(query, () -> drawNamed(shafts, shaftTex, hw, hh, Arrays.copyOf(reflectInputs, 4), Arrays.copyOf(reflectNames, 4),
						Arrays.copyOf(reflectSamplers, 4)), "light shafts (half res)");
			}
			double c = time(query, () -> drawNamed(composite, outTex, width, height, compositeInputs, compositeNames, compositeSamplers),
					"composite (full res)");
			if (save) {
				// The composite only writes water (and, under water, everything): the rest is the scene.
				glCopyImageSubData(sceneTex, GL_TEXTURE_2D, 0, 0, 0, 0, outTex, GL_TEXTURE_2D, 0, 0, 0, 0, width, height, 1);
				drawNamed(composite, outTex, width, height, compositeInputs, compositeNames, compositeSamplers);
				savePng(outTex, "gpubench-" + scene.name().replaceAll("[^a-z]+", "-") + ".png");
			}
			double total = copies + r + s + c;
			worst = Math.max(worst, total);
			System.out.printf(Locale.ROOT, "  %-48s %7.3f ms%n%n", "total", total);
		}
		System.out.printf(Locale.ROOT, "Worst case: %.3f ms (Liquid Water's budget: about 1.5 ms at 1920 x 1080 with water on screen)%n", worst);
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

	/** Times a GPU job: a few warm-ups, then many single samples; prints and returns the minimum (ms). */
	private static double time(int query, Runnable job, String label) {
		for (int i = 0; i < 10; i++) {
			job.run();
		}
		glFinish();
		double[] ms = new double[samples];
		for (int i = 0; i < samples; i++) {
			glBeginQuery(GL_TIME_ELAPSED, query);
			job.run();
			glEndQuery(GL_TIME_ELAPSED);
			ms[i] = glGetQueryObjectui64(query, GL_QUERY_RESULT) * 1.0e-6;
		}
		Arrays.sort(ms);
		System.out.printf(Locale.ROOT, "  %-48s %7.3f ms   (median %.3f)%n", label, ms[0], ms[ms.length / 2]);
		return ms[0];
	}

	private static void drawNamed(int program, int target, int w, int h, int[] textures, String[] names, int[] samplers) {
		glUseProgram(program);
		for (int i = 0; i < textures.length; i++) {
			int location = glGetUniformLocation(program, names[i]);
			if (location >= 0) {
				glUniform1i(location, i);
			}
		}
		draw(program, target, w, h, textures, samplers, new int[0]);
	}

	private static void draw(int program, int target, int w, int h, int[] textures, int[] samplers, int[] unused) {
		int fbo = FBOS.computeIfAbsent(target, t -> {
			int f = glCreateFramebuffers();
			glNamedFramebufferTexture(f, GL_COLOR_ATTACHMENT0, t, 0);
			return f;
		});
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glViewport(0, 0, w, h);
		glUseProgram(program);
		int block = glGetUniformBlockIndex(program, "LiquidWater");
		if (block != GL_INVALID_INDEX) {
			glUniformBlockBinding(program, block, 0);
		}
		for (int i = 0; i < textures.length; i++) {
			glBindTextureUnit(i, textures[i]);
			glBindSampler(i, samplers[i]);
		}
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	private static final java.util.Map<Integer, Integer> FBOS = new java.util.HashMap<>();

	// ---- The synthetic scene -------------------------------------------------------------------------

	/**
	 * A camera {@code height} blocks above a water plane (below it when negative), pitched down by
	 * {@code pitch}: water pixels tagged like the terrain shaders tag them, the bed five blocks down,
	 * a far shore near the horizon, sky above it. Fills the depth / colour inputs and the uniform block.
	 */
	private static void fillScene(Scene scene, int depthTex, int bedTex, int behindTex, int sceneTex, int ubo) {
		float aspect = width / (float) height;
		float f = (float) (1.0 / Math.tan(Math.toRadians(FOV) / 2.0));
		double pitch = Math.toRadians(scene.pitch());
		// Camera looking along +z, pitched; rows of the view rotation are right, up, -forward.
		double[] fwd = {0, Math.sin(pitch), Math.cos(pitch)};
		double[] right = {1, 0, 0};
		double[] up = {0, Math.cos(pitch), -Math.sin(pitch)};
		float[] depth = new float[width * height];
		float[] bed = new float[width * height];
		ByteBuffer behind = MemoryUtil.memAlloc(width * height * 4);
		ByteBuffer tint = MemoryUtil.memAlloc(width * height * 4);
		double surface = -scene.height();
		for (int y = 0; y < height; y++) {
			double ny = (y + 0.5) / height * 2 - 1;
			for (int x = 0; x < width; x++) {
				double nx = (x + 0.5) / width * 2 - 1;
				double dx = right[0] * nx * aspect / f + up[0] * ny / f + fwd[0];
				double dy = right[1] * nx * aspect / f + up[1] * ny / f + fwd[1];
				double dz = right[2] * nx * aspect / f + up[2] * ny / f + fwd[2];
				double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
				dx /= len;
				dy /= len;
				dz /= len;
				double along = dx * fwd[0] + dy * fwd[1] + dz * fwd[2];
				int i = y * width + x;
				float d;
				float b;
				int r;
				int g;
				int bl;
				boolean water;
				if (!scene.under()) {
					double tWater = dy < -1.0e-4 ? surface / dy : Double.POSITIVE_INFINITY;
					double tShore = 180.0;
					if (tWater < tShore) {
						water = true;
						d = reverseZ(tWater * along);
						b = reverseZ((surface - 5.0) / dy * along);
						r = 120;
						g = 104;
						bl = 82;
					} else if (dy < 0.09) {
						water = false;
						d = b = reverseZ(tShore * along);
						r = 70;
						g = 110;
						bl = 50;
					} else {
						water = false;
						d = b = 0f;
						r = 150;
						g = 190;
						bl = 235;
					}
				} else {
					double tWater = dy > 1.0e-4 ? surface / dy : Double.POSITIVE_INFINITY;
					double tBed = dy < -1.0e-4 ? (surface - 12.0) / dy : 60.0;
					if (tWater < 200.0) {
						water = true;
						d = reverseZ(tWater * along);
						b = 0f; // the sky beyond the surface
						r = 150;
						g = 190;
						bl = 235;
					} else {
						water = false;
						d = b = reverseZ(Math.min(tBed, 60.0) * along);
						r = 40;
						g = 90;
						bl = 120;
					}
				}
				int bits = Float.floatToRawIntBits(d);
				if (water) {
					int tagged = (bits & ~63) | 3 | 15 << 2;
					d = Float.intBitsToFloat(Integer.compareUnsigned(tagged, bits) > 0 ? tagged - 64 : tagged);
				} else if (d > 0f) {
					d = Float.intBitsToFloat(bits & ~3);
				}
				depth[i] = d;
				bed[i] = b;
				behind.put(i * 4, (byte) r).put(i * 4 + 1, (byte) g).put(i * 4 + 2, (byte) bl).put(i * 4 + 3, (byte) 255);
				if (water) {
					tint.put(i * 4, (byte) 38).put(i * 4 + 1, (byte) 71).put(i * 4 + 2, (byte) 137).put(i * 4 + 3, (byte) 255);
				} else {
					tint.put(i * 4, (byte) r).put(i * 4 + 1, (byte) g).put(i * 4 + 2, (byte) bl).put(i * 4 + 3, (byte) 255);
				}
			}
		}
		glTextureSubImage2D(depthTex, 0, 0, 0, width, height, GL_RED, GL_FLOAT, depth);
		glTextureSubImage2D(bedTex, 0, 0, 0, width, height, GL_RED, GL_FLOAT, bed);
		glTextureSubImage2D(behindTex, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, behind);
		glTextureSubImage2D(sceneTex, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, tint);
		MemoryUtil.memFree(behind);
		MemoryUtil.memFree(tint);

		// The uniform block, laid out like LiquidWater#write.
		float[] proj = new float[16];
		proj[0] = f / aspect;
		proj[5] = f;
		proj[10] = NEAR / (FAR - NEAR);
		proj[14] = FAR * NEAR / (FAR - NEAR);
		proj[11] = -1f;
		float[] view = {
				(float) right[0], (float) up[0], (float) -fwd[0], 0,
				(float) right[1], (float) up[1], (float) -fwd[1], 0,
				(float) right[2], (float) up[2], (float) -fwd[2], 0,
				0, 0, 0, 1};
		float[] viewProj = mul(proj, view);
		float[] inverse = invert(viewProj);
		float[] block = new float[24 * 4];
		System.arraycopy(inverse, 0, block, 0, 16);
		System.arraycopy(viewProj, 0, block, 16, 16);
		set(block, 8, 120, 70, 340, 12.5f);
		set(block, 9, 0.5f, 0.5f, 0.5f, 1f);
		float sx = 0.2f;
		float sy = 0.6f;
		float sz = 0.77f;
		set(block, 10, sx, sy, sz, 1f);
		set(block, 11, 1f, 0.94f, 0.84f, 1f);
		set(block, 12, 0.36f, 0.55f, 0.88f, scene.reflections());
		set(block, 13, 0.72f, 0.82f, 0.94f, new float[]{14, 22, 32}[scene.quality()]);
		set(block, 14, 0.72f, 0.82f, 0.94f, 1f);
		if (scene.under()) {
			set(block, 15, -8f, 48f, 180f, 200f);
		} else {
			set(block, 15, 160f, 200f, 180f, 200f);
		}
		set(block, 16, 0.866f, 0.5f, 0.8f, 1f);
		set(block, 17, 1f, 1f, 1f, 1f);
		set(block, 18, 0.30f, 0.10f, 0.055f, 0.85f);
		set(block, 19, 0.07f, 0.26f, 0.5f, 0f);
		set(block, 20, 0.6f, 0.3f, 1f, 0.6f);
		set(block, 21, scene.under() ? 1f : 0f, scene.under() ? (float) surface : 0f, 1f, scene.under() ? 1f : 0f);
		set(block, 22, width, height, (width + 1) / 2f, (height + 1) / 2f);
		set(block, 23, scene.quality(), 2f / (f * height), 1f, 0f);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.mallocFloat(block.length);
			buffer.put(block).flip();
			glNamedBufferSubData(ubo, 0, buffer);
		}
	}

	private static float reverseZ(double viewDepth) {
		return (float) (NEAR / (FAR - NEAR) * (FAR / Math.max(viewDepth, 1.0e-3) - 1.0));
	}

	private static void set(float[] block, int slot, float x, float y, float z, float w) {
		block[slot * 4] = x;
		block[slot * 4 + 1] = y;
		block[slot * 4 + 2] = z;
		block[slot * 4 + 3] = w;
	}

	/** Column-major 4×4 product a·b. */
	private static float[] mul(float[] a, float[] b) {
		float[] out = new float[16];
		for (int c = 0; c < 4; c++) {
			for (int r = 0; r < 4; r++) {
				float sum = 0f;
				for (int k = 0; k < 4; k++) {
					sum += a[k * 4 + r] * b[c * 4 + k];
				}
				out[c * 4 + r] = sum;
			}
		}
		return out;
	}

	/** Column-major 4×4 inverse (Gauss-Jordan). */
	private static float[] invert(float[] m) {
		double[][] a = new double[4][8];
		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				a[r][c] = m[c * 4 + r];
			}
			a[r][4 + r] = 1.0;
		}
		for (int col = 0; col < 4; col++) {
			int pivot = col;
			for (int r = col + 1; r < 4; r++) {
				if (Math.abs(a[r][col]) > Math.abs(a[pivot][col])) {
					pivot = r;
				}
			}
			double[] t = a[col];
			a[col] = a[pivot];
			a[pivot] = t;
			double p = a[col][col];
			for (int c = 0; c < 8; c++) {
				a[col][c] /= p;
			}
			for (int r = 0; r < 4; r++) {
				if (r != col) {
					double k = a[r][col];
					for (int c = 0; c < 8; c++) {
						a[r][c] -= k * a[col][c];
					}
				}
			}
		}
		float[] out = new float[16];
		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				out[c * 4 + r] = (float) a[r][4 + c];
			}
		}
		return out;
	}

	private static ByteBuffer fieldData() {
		ByteBuffer b = MemoryUtil.memAlloc(64 * 64 * 4);
		for (int i = 0; i < 64 * 64; i++) {
			b.put((byte) 255).put((byte) 0).put((byte) 0).put((byte) 255);
		}
		return b.flip();
	}

	private static ByteBuffer columnData() {
		ByteBuffer b = MemoryUtil.memAlloc(64 * 64 * 4);
		int surface = (int) ((63.0 + 2048.0) * 16.0);
		for (int i = 0; i < 64 * 64; i++) {
			b.put((byte) surface).put((byte) (surface >> 8)).put((byte) (1 | 4 | 16)).put((byte) 0xF0);
		}
		return b.flip();
	}

	private static void upload(int texture, int w, int h, ByteBuffer data) {
		glTextureSubImage2D(texture, 0, 0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, data);
		MemoryUtil.memFree(data);
	}

	private static void savePng(int texture, String name) {
		ByteBuffer pixels = MemoryUtil.memAlloc(width * height * 4);
		glGetTextureImage(texture, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = ((height - 1 - y) * width + x) * 4;
				image.setRGB(x, y, (pixels.get(i) & 0xFF) << 16 | (pixels.get(i + 1) & 0xFF) << 8 | (pixels.get(i + 2) & 0xFF));
			}
		}
		MemoryUtil.memFree(pixels);
		try {
			javax.imageio.ImageIO.write(image, "png", new java.io.File(name));
			System.out.println("  saved " + name);
		} catch (IOException e) {
			System.out.println("  couldn't save " + name + ": " + e);
		}
	}

	// ---- GL helpers ------------------------------------------------------------------------------------

	private static int texture(int format, int w, int h) {
		int t = glCreateTextures(GL_TEXTURE_2D);
		glTextureStorage2D(t, 1, format, w, h);
		return t;
	}

	private static int sampler(int filter, int wrap) {
		int s = glCreateSamplers();
		glSamplerParameteri(s, GL_TEXTURE_MIN_FILTER, filter);
		glSamplerParameteri(s, GL_TEXTURE_MAG_FILTER, filter);
		glSamplerParameteri(s, GL_TEXTURE_WRAP_S, wrap);
		glSamplerParameteri(s, GL_TEXTURE_WRAP_T, wrap);
		return s;
	}

	/** The fragment variant: our defines (and the engine's zero-to-one depth) after #version / #extension. */
	private static int program(String source, String variant) {
		List<String> lines = new ArrayList<>(Arrays.asList(source.split("\n", -1)));
		int at = 0;
		while (at < lines.size() && (lines.get(at).startsWith("#version") || lines.get(at).startsWith("#extension"))) {
			at++;
		}
		lines.add(at, "#define " + variant);
		lines.add(at + 1, "#define RENDERPEARL_DEPTH_IS_ZERO_TO_ONE");
		int vs = shader(GL_VERTEX_SHADER, VERTEX, "screen triangle");
		int fs = shader(GL_FRAGMENT_SHADER, String.join("\n", lines), variant);
		int p = glCreateProgram();
		glAttachShader(p, vs);
		glAttachShader(p, fs);
		glLinkProgram(p);
		if (glGetProgrami(p, GL_LINK_STATUS) == GL_FALSE) {
			throw new IllegalStateException(variant + " didn't link:\n" + glGetProgramInfoLog(p));
		}
		return p;
	}

	private static int shader(int type, String source, String name) {
		int s = glCreateShader(type);
		glShaderSource(s, source);
		glCompileShader(s);
		if (glGetShaderi(s, GL_COMPILE_STATUS) == GL_FALSE) {
			throw new IllegalStateException(name + " didn't compile:\n" + glGetShaderInfoLog(s));
		}
		return s;
	}
}
