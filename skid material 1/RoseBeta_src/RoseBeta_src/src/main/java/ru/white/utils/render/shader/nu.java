package ru.white.utils.render.shader;

import com.mojang.blaze3d.shaders.ShaderType;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class nu {
   private static final String BLUR_FRAGMENT_SHADER = "#version 150\n\nin vec2 fragCoord;\nin vec2 pixelCoord;\nin vec2 texCoord;\nin vec2 rectSize;\nin vec4 cornerRadii;\nin float guiScale;\nin float blurRadius;\nin vec2 texelSize;\nin vec4 tintColor;\nin vec2 resolution;\nin vec4 glass;\n\nout vec4 fragColor;\n\nuniform sampler2D Sampler0;\n\nfloat roundedBoxSDF(vec2 p, vec2 b, vec4 r) {\n    r.xy = (p.x > 0.0) ? r.yz : r.xw;\n    r.x = (p.y > 0.0) ? r.y : r.x;\n\n    vec2 q = abs(p) - b + r.x;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;\n}\n\nvoid main() {\n    vec2 halfSize = rectSize * 0.5;\n    vec2 center = pixelCoord - halfSize;\n\n    float maxRadius = min(halfSize.x, halfSize.y);\n    vec4 rRadii = min(cornerRadii, vec4(maxRadius));\n\n    float dist = roundedBoxSDF(center, halfSize, rRadii);\n\n    float pixelWidth = fwidth(dist);\n    float smoothing = max(pixelWidth, 0.5 / guiScale);\n    float alpha = 1.0 - smoothstep(-smoothing, smoothing, dist);\n\n    if (alpha < 0.01) {\n        discard;\n    }\n\n    vec2 glassCoord = texCoord;\n    float rimHighlight = 0.0;\n\n    if (glass.x > 0.0) {\n        // Жидкое стекло (Liquid Glass): оптическое искажение выпуклой линзы и преломление кромок\n        vec2 rel = center / max(halfSize, vec2(1.0));\n        float lenRel = length(rel);\n\n        // 1. Искажение выпуклой линзы (эффект капли жидкого стекла)\n        float k = 0.22;\n        vec2 lensDist = rel * (1.0 + k * lenRel * lenRel);\n        vec2 lensOffset = (lensDist - rel) * halfSize * (guiScale / resolution);\n\n        // 2. Преломление по контуру скругленного прямоугольника\n        float maxNorm = max(2.0, min(halfSize.x, halfSize.y) * 0.4);\n        float distToEdge = clamp(-dist / maxNorm, 0.0, 1.0);\n        float rimCurve = 1.0 - distToEdge;\n        float rimFresnel = rimCurve * rimCurve;\n\n        vec2 grad = vec2(dFdx(dist), dFdy(dist));\n        float gmag = length(grad);\n        vec2 normal = gmag > 1e-5 ? grad / gmag : (lenRel > 1e-5 ? rel / lenRel : vec2(0.0, 1.0));\n\n        vec2 rimOffset = normal * (rimFresnel * 16.0) * texelSize;\n\n        glassCoord = texCoord - lensOffset - rimOffset;\n\n        // 3. Глянцевый стеклянный блик по контуру\n        float edgeDist = abs(dist);\n        rimHighlight = smoothstep(2.0, 0.0, edgeDist) * 0.35;\n        if (center.y < 0.0) {\n            rimHighlight += smoothstep(4.0, 0.0, edgeDist) * 0.22 * (1.0 - abs(rel.x) * 0.4);\n        }\n    }\n\n    vec4 blurred = texture(Sampler0, clamp(glassCoord, vec2(0.002), vec2(0.998)));\n\n    // Прозрачность тинта темы: максимум 42%, чтобы жидкое стекло и преломленный мир всегда были отчетливо видны\n    float tintMix = clamp(tintColor.a * 0.42, 0.0, 0.45);\n    vec3 baseColor = mix(blurred.rgb, tintColor.rgb, tintMix);\n    vec3 finalColor = baseColor + vec3(rimHighlight);\n\n    float globalAlpha = clamp(blurRadius, 0.0, 1.0);\n    fragColor = vec4(finalColor, alpha * globalAlpha);\n}\n";
   private static final String NAMESPACE = "client";
   private static final String JUMP_WAVE_VERTEX_SHADER = "#version 150\n\nlayout(std140) uniform Projection {\n    mat4 ProjMat;\n};\n\nlayout(std140) uniform JumpWaveUniforms {\n    vec4 params;\n    vec4 crestColor;\n    vec4 p1;\n    vec4 p2;\n    vec4 p3;\n    vec4 p4;\n};\n\nout vec2 uv;\n\nvoid main() {\n    vec4 positions[6] = vec4[](p1, p2, p3, p1, p3, p4);\n    vec2 uvs[6] = vec2[](\n        vec2(1.0, 1.0),\n        vec2(1.0, 0.0),\n        vec2(0.0, 0.0),\n        vec2(1.0, 1.0),\n        vec2(0.0, 0.0),\n        vec2(0.0, 1.0)\n    );\n    gl_Position = ProjMat * vec4(positions[gl_VertexID].xyz, 1.0);\n    uv = uvs[gl_VertexID];\n}\n";
   private static final byte[] KEY = "Nightix//shader-veil//2026".getBytes(StandardCharsets.UTF_8);
   private static final String INCLUDE_DIR = "shaders/include/";
   private static final String JUMP_WAVE_FRAGMENT_SHADER = "#version 150\n\nin vec2 uv;\nout vec4 finalColor;\n\nuniform sampler2D SceneSampler;\n\nlayout(std140) uniform JumpWaveUniforms {\n    vec4 params;\n    vec4 crestColor;\n    vec4 p1;\n    vec4 p2;\n    vec4 p3;\n    vec4 p4;\n};\n\n#define RING_WIDTH 0.22\n\nvoid main() {\n    float ringProgress = params.x;\n    float strength = params.y;\n    float fade = params.z;\n\n    vec2 centered = (uv - 0.5) * 2.0;\n    float dist = length(centered);\n    if (dist >= 1.0) {\n        discard;\n    }\n\n    float ringRadius = ringProgress;\n    float edge = dist - ringRadius;\n    float band = smoothstep(RING_WIDTH, 0.0, abs(edge));\n    band *= band;\n\n    float inside = 1.0 - smoothstep(0.65, 0.98, dist);\n\n    float coverage = max(inside, band);\n    if (coverage <= 0.001) {\n        discard;\n    }\n\n    vec2 dir = dist > 0.0001 ? centered / dist : vec2(0.0);\n\n    vec2 screenSize = vec2(textureSize(SceneSampler, 0));\n    vec2 screenUv = gl_FragCoord.xy / screenSize;\n\n    float ripple = sin(dist * 38.0 - ringProgress * 26.0) * 0.5 + 0.5;\n    float crest = band * mix(0.7, 1.3, ripple);\n\n    float innerDistort = inside * mix(0.55, 1.0, dist);\n    float offsetAmount = strength * fade * 0.05 * (innerDistort + crest * 1.6);\n\n    vec2 baseOffset = dir * offsetAmount;\n    vec3 refracted = texture(SceneSampler, clamp(screenUv + baseOffset, vec2(0.0), vec2(1.0))).rgb;\n\n    float crestHighlight = pow(band, 1.5) * fade * 0.5;\n    float sheen = inside * fade * 0.06;\n    vec3 crestTint = crestColor.rgb * pow(band, 1.2) * fade * 0.55;\n    vec3 color = refracted + vec3(crestHighlight + sheen) + crestTint;\n\n    float alpha = max(inside, band) * fade;\n    finalColor = vec4(color, alpha);\n}\n";

   private static String m1992(Identifier id) {
      try {
         MinecraftClient v1 = MinecraftClient.method_1551();
         if (v1 == null) {
            return null;
         } else {
            ResourceManager v2 = v1.method_1478();
            if (v2 == null) {
               return null;
            } else {
               Optional v3 = v2.method_14486(id);
               if (v3.isEmpty()) {
                  return null;
               } else {
                  String v5;
                  try (InputStream v4 = ((Resource)v3.get()).method_14482()) {
                     v5 = new String(v4.readAllBytes(), StandardCharsets.UTF_8);
                  }

                  return v5;
               }
            }
         }
      } catch (Exception v10) {
         return null;
      }
   }

   public static String getSource(Identifier id, ShaderType type) {
      if (id != null && "client".equals(id.method_12836())) {
         if (id.method_12832().equals("core/jump_wave")) {
            String v16;
            if (type == ShaderType.VERTEX) {
               v16 = "#version 150\n\nlayout(std140) uniform Projection {\n    mat4 ProjMat;\n};\n\nlayout(std140) uniform JumpWaveUniforms {\n    vec4 params;\n    vec4 crestColor;\n    vec4 p1;\n    vec4 p2;\n    vec4 p3;\n    vec4 p4;\n};\n\nout vec2 uv;\n\nvoid main() {\n    vec4 positions[6] = vec4[](p1, p2, p3, p1, p3, p4);\n    vec2 uvs[6] = vec2[](\n        vec2(1.0, 1.0),\n        vec2(1.0, 0.0),\n        vec2(0.0, 0.0),\n        vec2(1.0, 1.0),\n        vec2(0.0, 0.0),\n        vec2(0.0, 1.0)\n    );\n    gl_Position = ProjMat * vec4(positions[gl_VertexID].xyz, 1.0);\n    uv = uvs[gl_VertexID];\n}\n";
               
            } else {
               v16 = "#version 150\n\nin vec2 uv;\nout vec4 finalColor;\n\nuniform sampler2D SceneSampler;\n\nlayout(std140) uniform JumpWaveUniforms {\n    vec4 params;\n    vec4 crestColor;\n    vec4 p1;\n    vec4 p2;\n    vec4 p3;\n    vec4 p4;\n};\n\n#define RING_WIDTH 0.22\n\nvoid main() {\n    float ringProgress = params.x;\n    float strength = params.y;\n    float fade = params.z;\n\n    vec2 centered = (uv - 0.5) * 2.0;\n    float dist = length(centered);\n    if (dist >= 1.0) {\n        discard;\n    }\n\n    float ringRadius = ringProgress;\n    float edge = dist - ringRadius;\n    float band = smoothstep(RING_WIDTH, 0.0, abs(edge));\n    band *= band;\n\n    float inside = 1.0 - smoothstep(0.65, 0.98, dist);\n\n    float coverage = max(inside, band);\n    if (coverage <= 0.001) {\n        discard;\n    }\n\n    vec2 dir = dist > 0.0001 ? centered / dist : vec2(0.0);\n\n    vec2 screenSize = vec2(textureSize(SceneSampler, 0));\n    vec2 screenUv = gl_FragCoord.xy / screenSize;\n\n    float ripple = sin(dist * 38.0 - ringProgress * 26.0) * 0.5 + 0.5;\n    float crest = band * mix(0.7, 1.3, ripple);\n\n    float innerDistort = inside * mix(0.55, 1.0, dist);\n    float offsetAmount = strength * fade * 0.05 * (innerDistort + crest * 1.6);\n\n    vec2 baseOffset = dir * offsetAmount;\n    vec3 refracted = texture(SceneSampler, clamp(screenUv + baseOffset, vec2(0.0), vec2(1.0))).rgb;\n\n    float crestHighlight = pow(band, 1.5) * fade * 0.5;\n    float sheen = inside * fade * 0.06;\n    vec3 crestTint = crestColor.rgb * pow(band, 1.2) * fade * 0.55;\n    vec3 color = refracted + vec3(crestHighlight + sheen) + crestTint;\n\n    float alpha = max(inside, band) * fade;\n    finalColor = vec4(color, alpha);\n}\n";
            }

            return v16;
         } else {
            String v2 = type == ShaderType.VERTEX ? "vsh" : "fsh";
            String v9 = id.method_12832();
            String v3 = v9 + "|" + v2;
            String v4 = mu.SOURCES.get(v3);
            if (v4 == null) {
               return null;
            } else {
               String v5 = m1989(v4);
               if (id.method_12832().contains("shader_sky") && type == ShaderType.FRAGMENT) {
                  v5 = v5.replace("hash2", "hash2_");
                  v5 = v5.replace("noise2", "noise2_");
               }

               if (id.method_12832().equals("core/blur") && type == ShaderType.FRAGMENT) {
                  v5 = "#version 150\n\nin vec2 fragCoord;\nin vec2 pixelCoord;\nin vec2 texCoord;\nin vec2 rectSize;\nin vec4 cornerRadii;\nin float guiScale;\nin float blurRadius;\nin vec2 texelSize;\nin vec4 tintColor;\nin vec2 resolution;\nin vec4 glass;\n\nout vec4 fragColor;\n\nuniform sampler2D Sampler0;\n\nfloat roundedBoxSDF(vec2 p, vec2 b, vec4 r) {\n    r.xy = (p.x > 0.0) ? r.yz : r.xw;\n    r.x = (p.y > 0.0) ? r.y : r.x;\n\n    vec2 q = abs(p) - b + r.x;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;\n}\n\nvoid main() {\n    vec2 halfSize = rectSize * 0.5;\n    vec2 center = pixelCoord - halfSize;\n\n    float maxRadius = min(halfSize.x, halfSize.y);\n    vec4 rRadii = min(cornerRadii, vec4(maxRadius));\n\n    float dist = roundedBoxSDF(center, halfSize, rRadii);\n\n    float pixelWidth = fwidth(dist);\n    float smoothing = max(pixelWidth, 0.5 / guiScale);\n    float alpha = 1.0 - smoothstep(-smoothing, smoothing, dist);\n\n    if (alpha < 0.01) {\n        discard;\n    }\n\n    vec2 glassCoord = texCoord;\n    float rimHighlight = 0.0;\n\n    if (glass.x > 0.0) {\n        // Жидкое стекло (Liquid Glass): оптическое искажение выпуклой линзы и преломление кромок\n        vec2 rel = center / max(halfSize, vec2(1.0));\n        float lenRel = length(rel);\n\n        // 1. Искажение выпуклой линзы (эффект капли жидкого стекла)\n        float k = 0.22;\n        vec2 lensDist = rel * (1.0 + k * lenRel * lenRel);\n        vec2 lensOffset = (lensDist - rel) * halfSize * (guiScale / resolution);\n\n        // 2. Преломление по контуру скругленного прямоугольника\n        float maxNorm = max(2.0, min(halfSize.x, halfSize.y) * 0.4);\n        float distToEdge = clamp(-dist / maxNorm, 0.0, 1.0);\n        float rimCurve = 1.0 - distToEdge;\n        float rimFresnel = rimCurve * rimCurve;\n\n        vec2 grad = vec2(dFdx(dist), dFdy(dist));\n        float gmag = length(grad);\n        vec2 normal = gmag > 1e-5 ? grad / gmag : (lenRel > 1e-5 ? rel / lenRel : vec2(0.0, 1.0));\n\n        vec2 rimOffset = normal * (rimFresnel * 16.0) * texelSize;\n\n        glassCoord = texCoord - lensOffset - rimOffset;\n\n        // 3. Глянцевый стеклянный блик по контуру\n        float edgeDist = abs(dist);\n        rimHighlight = smoothstep(2.0, 0.0, edgeDist) * 0.35;\n        if (center.y < 0.0) {\n            rimHighlight += smoothstep(4.0, 0.0, edgeDist) * 0.22 * (1.0 - abs(rel.x) * 0.4);\n        }\n    }\n\n    vec4 blurred = texture(Sampler0, clamp(glassCoord, vec2(0.002), vec2(0.998)));\n\n    // Прозрачность тинта темы: максимум 42%, чтобы жидкое стекло и преломленный мир всегда были отчетливо видны\n    float tintMix = clamp(tintColor.a * 0.42, 0.0, 0.45);\n    vec3 baseColor = mix(blurred.rgb, tintColor.rgb, tintMix);\n    vec3 finalColor = baseColor + vec3(rimHighlight);\n\n    float globalAlpha = clamp(blurRadius, 0.0, 1.0);\n    fragColor = vec4(finalColor, alpha * globalAlpha);\n}\n";
               }

               boolean result = id.method_12832().contains("texture");

               if (result && type == ShaderType.VERTEX) {
                  v5 = v5.replace(
                     "vec2 localPos = (pos - 0.5) * inst.rect.zw;",
                     "vec2 localPos = (pos - 0.5) * inst.rect.zw;\n    localPos.x += localPos.y * inst.rotationData.y;"
                  );
               }

               String v11 = id.method_12832();
               Identifier v6 = Identifier.method_60655(id.method_12836(), "shaders/" + v11 + "." + v2);
               HashSet temp2 = new HashSet;

               temp2./* $VF: Unable to resugar constructor */<init>();
               String v7 = m1990(v5, v6, temp2);
               return m1988(v7);
            }
         }
      } else {
         return null;
      }
   }

   private nu() {
   }

   private static String m1988(String source) {
      StringBuilder v1 = new StringBuilder(source.length());
      boolean v2 = false;
      String[] v3 = source.split("\n", -1);
      int v4 = v3.length;

      for (int v5 = 0; v5 < v4; v5++) {
         String v6 = v3[v5];

         if (v6.trim().startsWith("#version")) {
            if (v2) {
               continue;
            }

            v2 = true;
         }

         v1.append(v6).append((char)(10));
         
      }

      return v1.toString();
   }

   private static String m1990(String source, Identifier owner, Set<String> seen) {
      StringBuilder v3 = new StringBuilder(source.length() + 256);

      for (String result : source.split("\n", -1)) {

         String v7 = result;

         String v8 = v7.trim();
         if (v8.startsWith("#moj_import")) {
            Identifier v9 = m1991(v8, owner);
            if (v9 != null) {
               if (seen.add(v9.toString())) {
                  String v10 = m1992(v9);
                  if (v10 != null) {
                     v3.append(m1990(v10, v9, seen));
                     if (v3.length() > 0 && v3.charAt(v3.length() - 1) != 10) {
                        v3.append((char)(10));
                     }
                  }
               }
               continue;
            }
         }

         v3.append(v7).append((char)(10));
      }

      return v3.toString();
   }

   private static Identifier m1991(String line, Identifier owner) {
      int v2 = line.indexOf(60);
      int v3 = line.indexOf(62);
      if (v2 >= 0) {

         if (v3 > v2) {
            String v15 = line.substring(v2 + 1, v3).trim();
            Identifier v16 = Identifier.method_60654(v15);
            String v10 = v16.method_12832();
            return Identifier.method_60655(v16.method_12836(), "shaders/include/" + v10);
         }
      }

      int v4 = line.indexOf(34);
      int v5 = line.lastIndexOf(34);
      if (v4 >= 0 && v5 > v4) {
         String v6 = line.substring(v4 + 1, v5).trim();
         String v7 = owner.method_12832();
         int v8 = v7.lastIndexOf(47);
         String v9 = v8 >= 0 ? v7.substring(0, v8 + 1) : "";
         String result = owner.method_12836();
         String temp1 = v9 + v6;

         return Identifier.method_60655(result, temp1);
      } else {
         return null;
      }
   }

   private static String m1989(String base64) {
      byte[] v1 = Base64.getDecoder().decode(base64);
      byte[] result = new byte[v1.length];

      byte[] v2 = result;

      for (int v3 = 0; v3 < v1.length; v3++) {
         v2[v3] = (byte)(v1[v3] ^ KEY[v3 % KEY.length]);
      }

      String v7 = new String;

      v7./* $VF: Unable to resugar constructor */<init>(v2, StandardCharsets.UTF_8);
      return v7;
   }

   
}
