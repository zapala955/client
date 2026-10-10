import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static org.lwjgl.util.shaderc.Shaderc.*;

/**
 * Compiles every shader variant of {@code LiquidPipelines} to SPIR-V with shaderc, the way the
 * engine does (Vulkan 1.2, uniforms and locations bound automatically, {@code #include <ns:file>}),
 * with and without {@code RENDERPEARL_DEPTH_IS_ZERO_TO_ONE}. Order-independent-transparency sets are
 * compiled for each of their stages (depth bounds, transmittance, accumulate) with 8 and 16
 * coefficients. Not part of the build.
 *
 * <pre>
 * java -cp lwjgl.jar:lwjgl-natives-linux.jar:lwjgl-shaderc.jar:lwjgl-shaderc-natives-linux.jar \
 *      tools/ShaderCheck.java &lt;minecraft client jar | folder holding assets/minecraft/shaders or include/ + core/&gt;
 * </pre>
 * The LWJGL jars are in the Gradle cache once the project has been built (or on Maven Central).
 * Exits with 1 if anything fails to compile.
 */
public final class ShaderCheck {
	private static final Path ROOT = Path.of("src/main/resources/assets/liquidclient/shaders");
	private static final Path PIPELINES = Path.of("src/main/java/net/client/visuals/render/shader/LiquidPipelines.java");
	private static final Map<String, String> VANILLA = new LinkedHashMap<>();
	private static ZipFile jar;
	private static Path vanillaDir;

	record Variant(String name, String vertex, String fragment, List<String> defines) {
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("usage: ShaderCheck <minecraft client jar | vanilla shaders folder>");
			System.exit(2);
		}
		Path vanilla = Path.of(args[0]);
		if (vanilla.toString().endsWith(".jar")) {
			jar = new ZipFile(vanilla.toFile());
		} else {
			vanillaDir = Files.isDirectory(vanilla.resolve("assets/minecraft/shaders")) ? vanilla.resolve("assets/minecraft/shaders") : vanilla;
		}
		List<Variant> variants = variants(Files.readString(PIPELINES));

		long compiler = shaderc_compiler_initialize();
		int failures = 0;
		int compiled = 0;
		Set<String> seen = new LinkedHashSet<>();
		for (Variant variant : variants) {
			for (boolean zeroToOne : new boolean[]{false, true}) {
				for (int stage = 0; stage < 2; stage++) {
					String shader = stage == 0 ? variant.vertex() : variant.fragment();
					List<String> defines = new ArrayList<>(variant.defines());
					if (zeroToOne) {
						defines.add("RENDERPEARL_DEPTH_IS_ZERO_TO_ONE");
					}
					String key = shader + (stage == 0 ? ".vsh" : ".fsh") + " " + defines;
					if (!seen.add(key)) {
						continue;
					}
					String error = compile(compiler, shader, stage == 0, defines);
					compiled++;
					if (error != null) {
						failures++;
						System.out.println("FAIL  " + variant.name() + "  " + key + "\n" + error.indent(6));
					}
				}
			}
		}
		shaderc_compiler_release(compiler);
		System.out.println(compiled + " shader variants compiled (" + variants.size() + " pipeline variants), " + failures + " failed");
		System.exit(failures == 0 ? 0 : 1);
	}

	/** Pipelines and their shaders / defines, read from LiquidPipelines.java. */
	static List<Variant> variants(String source) {
		List<Variant> out = new ArrayList<>();
		Pattern statement = Pattern.compile("public static final (RenderPipeline|OitPipelineSet) (\\w+) = (.*?);\\s*\\n", Pattern.DOTALL);
		Pattern define = Pattern.compile("withShaderDefine\\(\"(\\w+)\"\\)");
		Pattern vertexId = Pattern.compile("withVertexShader\\(LiquidClient\\.id\\(\"([\\w/]+)\"\\)\\)");
		Pattern vertexVanilla = Pattern.compile("withVertexShader\\(\"([\\w/]+)\"\\)");
		Pattern fragmentId = Pattern.compile("withFragmentShader\\(LiquidClient\\.id\\(\"([\\w/]+)\"\\)\\)");
		Pattern post = Pattern.compile("^post\\(\"[\\w/]+\", \"([\\w/]+)\"");
		Pattern gui = Pattern.compile("^gui\\(\"[\\w/]+\", \"([\\w/]+)\"");
		Pattern dof = Pattern.compile("^dof\\(\"\\w+\", \"(\\w+)\"");
		Matcher m = statement.matcher(source);
		while (m.find()) {
			String name = m.group(2);
			String body = m.group(3).trim();
			List<String> defines = new ArrayList<>();
			Matcher d = define.matcher(body);
			while (d.find()) {
				defines.add(d.group(1));
			}
			String vertex = null;
			String fragment = null;
			Matcher x;
			if ((x = post.matcher(body)).find()) {
				vertex = "minecraft:core/screenquad";
				fragment = "liquidclient:" + x.group(1);
			} else if ((x = gui.matcher(body)).find()) {
				vertex = fragment = "liquidclient:" + x.group(1);
			} else if ((x = dof.matcher(body)).find()) {
				vertex = "minecraft:core/screenquad";
				fragment = "liquidclient:core/post_dof";
				defines.add(x.group(1));
			} else if (body.startsWith("sky(")) {
				vertex = fragment = "liquidclient:core/liquid_sky";
			} else if (body.startsWith("particle(")) {
				vertex = fragment = "liquidclient:core/ambient_particle";
			}
			if ((x = vertexId.matcher(body)).find()) {
				vertex = "liquidclient:" + x.group(1);
			} else if ((x = vertexVanilla.matcher(body)).find()) {
				vertex = "minecraft:" + x.group(1);
			}
			if ((x = fragmentId.matcher(body)).find()) {
				fragment = "liquidclient:" + x.group(1);
			}
			if (body.startsWith("OitPipelineSet") || body.contains("oitParticle()")) {
				if (body.contains("oitParticle()")) {
					vertex = fragment = "liquidclient:core/ambient_particle";
				}
				for (int coefficients : new int[]{8, 16}) {
					List<String> oit = List.of("OIT", "OIT_COEFF_COUNT=" + coefficients, "OIT_COEFF_ATTACHMENT_COUNT=" + coefficients / 4,
							"OIT_WAVELET_RANK=" + (Integer.numberOfTrailingZeros(coefficients) - 1));
					for (String[] stage : new String[][]{{"OIT_ALPHA_ONLY", "OIT_DEPTH_BOUNDS"}, {"OIT_ALPHA_ONLY", "OIT_TRANSMITTANCE"}, {"OIT_ACCUMULATE"}}) {
						List<String> all = new ArrayList<>(defines);
						all.addAll(oit);
						all.addAll(List.of(stage));
						out.add(new Variant(name + "/" + String.join("+", stage) + "/" + coefficients, vertex, fragment, all));
					}
				}
				continue;
			}
			if (vertex == null || fragment == null) {
				System.out.println("SKIP  " + name + " (shaders not recognised)");
				continue;
			}
			out.add(new Variant(name, vertex, fragment, defines));
		}
		return out;
	}

	/** Compiles one stage; null on success, else the compiler's message. */
	static String compile(long compiler, String shader, boolean vertex, List<String> defines) {
		String file = shader + (vertex ? ".vsh" : ".fsh");
		String source;
		try {
			source = load(file);
		} catch (IOException e) {
			return "cannot read " + file + ": " + e.getMessage();
		}
		long options = shaderc_compile_options_initialize();
		shaderc_compile_options_set_target_env(options, shaderc_target_env_vulkan, shaderc_env_version_vulkan_1_2);
		shaderc_compile_options_set_auto_bind_uniforms(options, true);
		shaderc_compile_options_set_auto_map_locations(options, true);
		for (String define : defines) {
			int eq = define.indexOf('=');
			if (eq < 0) {
				shaderc_compile_options_add_macro_definition(options, define, "1");
			} else {
				shaderc_compile_options_add_macro_definition(options, define.substring(0, eq), define.substring(eq + 1));
			}
		}
		ShadercIncludeResolveI resolver = (userData, requested, type, requesting, depth) -> {
			String name = MemoryUtil.memUTF8(requested);
			String content;
			try {
				content = load(name);
			} catch (IOException e) {
				content = "#error cannot include " + name;
			}
			ShadercIncludeResult result = ShadercIncludeResult.calloc();
			result.source_name(MemoryUtil.memUTF8(name, false));
			result.content(MemoryUtil.memUTF8(content, false));
			return result.address();
		};
		ShadercIncludeResultReleaseI releaser = (userData, address) -> {
			ShadercIncludeResult result = ShadercIncludeResult.create(address);
			MemoryUtil.memFree(result.source_name());
			MemoryUtil.memFree(result.content());
			result.free();
		};
		shaderc_compile_options_set_include_callbacks(options, resolver, releaser, 0L);
		long result = shaderc_compile_into_spv(compiler, source, vertex ? shaderc_glsl_vertex_shader : shaderc_glsl_fragment_shader,
				file, "main", options);
		String error = null;
		if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
			error = shaderc_result_get_error_message(result);
		}
		shaderc_result_release(result);
		shaderc_compile_options_release(options);
		return error;
	}

	/** "liquidclient:core/x.fsh", "minecraft:fog.glsl" (includes), "minecraft:core/screenquad.vsh". */
	static String load(String name) throws IOException {
		int colon = name.indexOf(':');
		String namespace = colon < 0 ? "minecraft" : name.substring(0, colon);
		String path = colon < 0 ? name : name.substring(colon + 1);
		if (path.endsWith(".glsl") && !path.startsWith("include/")) {
			path = "include/" + path;
		}
		if (namespace.equals("liquidclient")) {
			return Files.readString(ROOT.resolve(path));
		}
		String cached = VANILLA.get(path);
		if (cached != null) {
			return cached;
		}
		String content;
		if (jar != null) {
			var entry = jar.getEntry("assets/minecraft/shaders/" + path);
			if (entry == null) {
				throw new IOException("not in the jar: " + path);
			}
			try (InputStream in = jar.getInputStream(entry)) {
				content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			}
		} else {
			content = Files.readString(vanillaDir.resolve(path));
		}
		VANILLA.put(path, content);
		return content;
	}
}
