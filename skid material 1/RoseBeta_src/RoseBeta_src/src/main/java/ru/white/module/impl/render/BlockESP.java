package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Block ESP",
   category = Category.RENDER,
   desc = "Подсвечивает блоки в радиусе"
)
@Environment(EnvType.CLIENT)
public class BlockESP extends Module {
   public SliderSetting radius = new SliderSetting(this, "Радиус", 30.0F, 5.0F, 60.0F, 5.0F);
   private final BufferAllocator f642;
   private final List<String> f640 = new CopyOnWriteArrayList<>();
   private static final RenderLayer BOX_LAYER = RenderLayer.method_75940(
      "block_esp", RenderSetup.method_75927(ui.BOX_PIPELINE).method_75929(4096).method_75938()
   );
   private final List<BlockPos> f641 = new CopyOnWriteArrayList<>();
   private static final RenderPipeline BOX_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "block_esp"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private int f643;
   private static ui instance;

   private static void m675(VertexConsumer b, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int cB, int cT) {
      b.method_22918(m, x0, y0, z0).method_39415(cB);
      b.method_22918(m, x1, y0, z0).method_39415(cB);
      b.method_22918(m, x1, y0, z1).method_39415(cB);

      b.method_22918(m, x0, y0, z1).method_39415(cB);
      b.method_22918(m, x0, y1, z0).method_39415(cT);
      b.method_22918(m, x0, y1, z1).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cT);
      b.method_22918(m, x1, y1, z0).method_39415(cT);
      b.method_22918(m, x0, y0, z1).method_39415(cB);
      b.method_22918(m, x1, y0, z1).method_39415(cB);
      b.method_22918(m, x1, y1, z1).method_39415(cT);
      b.method_22918(m, x0, y1, z1).method_39415(cT);
      b.method_22918(m, x1, y0, z0).method_39415(cB);
      b.method_22918(m, x0, y0, z0).method_39415(cB);
      b.method_22918(m, x0, y1, z0).method_39415(cT);
      b.method_22918(m, x1, y1, z0).method_39415(cT);
      b.method_22918(m, x0, y0, z0).method_39415(cB);
      b.method_22918(m, x0, y0, z1).method_39415(cB);
      b.method_22918(m, x0, y1, z1).method_39415(cT);
      b.method_22918(m, x0, y1, z0).method_39415(cT);
      b.method_22918(m, x1, y0, z1).method_39415(cB);
      b.method_22918(m, x1, y0, z0).method_39415(cB);
      b.method_22918(m, x1, y1, z0).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cT);
   }

   public Set<Block> getTargetBlocks() {
      HashSet v1 = new HashSet();

      for (String v3 : this.f640) {
         Identifier v4 = Identifier.method_12829(v3);

         if (v4 != null) {
            Registries.field_41175.method_17966(v4).ifPresent(v1::add);
         }
      }

      return v1;
   }

   public int clearBlocks() {
      int v1 = this.f640.size();
      this.f640.clear();

      this.rescan();
      return v1;
   }

   public void rescan() {
      this.f643 = 5;

      
   }

   @Override
   protected void onDisable() {

      this.f641.clear();
      this.f643 = 0;
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {

         if (!this.f641.isEmpty()) {
            Immediate v2 = VertexConsumerProvider.method_22991(this.f642);
            Matrix4f v3 = e.getMatrixStack().method_23760().method_23761();
            Vec3d v4 = mc.field_1773.method_19418().method_71156();
            int v5 = up.fade(1);
            int v6 = up.replAlpha(v5, 0.7F);
            int v7 = up.replAlpha(v5, 0.0F);
            VertexConsumer v8 = v2.method_73477(BOX_LAYER);

            for (BlockPos v10 : this.f641) {
               float v11 = (float)((double)v10.method_10263() - v4.field_1352);
               float v12 = (float)((double)v10.method_10264() - v4.field_1351);
               float v13 = (float)((double)v10.method_10260() - v4.field_1350);

               float v17 = v11 + 1.0F;
               float var10006 = v12 + 1.0F;
               float var10007 = v13 + 1.0F;

               m675(v8, v3, v11, v12, v13, v17, var10006, var10007, v6, v7);
            }

            v2.method_22993();
            return;
         }
      }
   }

   public boolean addBlock(Block block) {

      String v2 = Registries.field_41175.method_10221(block).toString();
      if (this.f640.contains(v2)) {
         return false;
      } else {
         this.f640.add(v2);
         this.rescan();

         return true;
      }
   }

   public BlockESP() {
      this.f642 = new BufferAllocator(262144);
      this.f643 = 0;
      instance = this;
   }

   public static ui getInstance() {

      return instance;
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         Set v2 = this.getTargetBlocks();
         if (v2.isEmpty()) {
            this.f641.clear();
         } else {

            if (++this.f643 >= 5) {
               this.f643 = 0;
               ArrayList v3 = new ArrayList();
               int v4 = this.radius.getValue().intValue();
               int v5 = mc.field_1724.method_31477();
               int v6 = mc.field_1724.method_31478();
               int v7 = mc.field_1724.method_31479();
               int v8 = mc.field_1687.method_31607();

               int v9 = mc.field_1687.method_31600();
               Mutable v10 = new Mutable();

               label60:
               for (int v11 = v5 - v4; v11 <= v5 + v4; v11++) {
                  for (int v12 = v7 - v4; v12 <= v7 + v4; v12++) {
                     if (mc.field_1687.method_8393(v11 >> 4, v12 >> 4)) {
                        for (int v13 = Math.max(v8, v6 - v4); v13 <= Math.min(v9, v6 + v4); v13++) {
                           v10.method_10103(v11, v13, v12);
                           Block v14 = mc.field_1687.method_8320(v10).method_26204();
                           if (v2.contains(v14)) {
                              v3.add(v10.method_10062());
                              if (v3.size() >= 2000) {
                                 break label60;
                              }
                           }
                        }
                     }
                  }
               }

               this.f641.clear();
               this.f641.addAll(v3);
            }
         }
      } else {
         this.f641.clear();
      }
   }

   public boolean removeBlock(Block block) {
      List result = this.f640;

      Identifier temp1 = Registries.field_41175.method_10221(block);

      boolean v2 = result.remove(temp1.toString());
      if (v2) {
         this.rescan();
      }

      return v2;
   }

   
}
