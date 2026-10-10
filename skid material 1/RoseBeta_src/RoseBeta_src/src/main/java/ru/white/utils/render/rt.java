package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public abstract class rt implements wr.xr {
   private int f1614;
   private static final int HEADER_SIZE = 16;
   private final RenderPipeline f1604;
   private final int f1608;
   private GpuBuffer f1611;
   private final String f1605;
   private GpuBuffer[] f1609;
   private final int f1607;
   private boolean f1613;
   private final int f1606;
   protected static final float FIXED_GUI_SCALE = 2.0F;
   private int f1610 = 0;
   private final ArrayList<rt.st> f1615;
   private final String f1603;
   private ByteBuffer f1612;

   @Override
   public final void drawBatch(RenderPass pass) {
      if (!this.f1615.isEmpty()) {
         pass.setPipeline(this.f1604);
         pass.setVertexBuffer(0, this.f1611);

         for (rt.st v3 : this.f1615) {
            pass.setUniform(this.f1605, v3.buffer());
            int temp2 = v3.count();
            int temp3 = 6;

            pass.draw(0, temp2 * temp3);
         }

         this.f1615.clear();
      }
   }

   private void m1936() {
      if (this.f1614 != 0) {
         MinecraftClient v1 = MinecraftClient.method_1551();
         float v2 = (float)v1.method_22683().method_4489() / 2.0F;
         float v3 = (float)v1.method_22683().method_4506() / 2.0F;
         int v4 = this.f1612.position();
         this.f1612.position(0);
         this.f1612.putFloat(v2);
         this.f1612.putFloat(v3);
         this.f1612.putFloat(2.0F);
         this.f1612.putFloat(0.0F);
         this.f1612.position(0);

         this.f1612.limit(v4);
         GpuBuffer v5 = this.m1935();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(v5.slice(0L, (long)this.f1612.remaining()), this.f1612);
         this.f1612.limit(this.f1612.capacity());
         this.f1615.add(new rt.st(v5, this.f1614));
         this.f1614 = 0;
      }
   }

   private GpuBuffer m1935() {
      GpuBuffer v1 = this.f1609[this.f1610];
      if (v1 == null) {
         int v2 = this.f1610;
         v1 = RenderSystem.getDevice().createBuffer(() -> {
            String v3x = this.f1603;
            StringBuilder result = new StringBuilder;
            int v4x = 1119420357;
            int var10001x = v4x * (v4x + 1);

            result./* $VF: Unable to resugar constructor */<init>();
            return result.append("client:").append((Object)v3x).append("_uniform_").append(v2).toString();
         }, 136, (long)this.f1607);
         this.f1609[this.f1610] = v1;
      }

      int temp1 = this.f1610 + 1;

      this.f1610 = temp1 % this.f1608;
      return v1;
   }

   protected rt(String debugName, RenderPipeline renderPipeline, String uniformName, int maxItems, int itemSize, int uniformRing) {
      this.f1613 = false;
      this.f1614 = 0;
      this.f1615 = new ArrayList<>();
      this.f1603 = debugName;
      this.f1604 = renderPipeline;
      this.f1605 = uniformName;
      this.f1606 = maxItems;
      this.f1607 = 16 + maxItems * itemSize;
      this.f1608 = uniformRing;
   }

   public void close() {
      this.f1614 = 0;
      this.discardBatch();
      if (this.f1609 != null) {
         GpuBuffer[] v1 = this.f1609;
         int v2 = v1.length;
         int v3 = 0;

         while (true) {
            int v5 = -738174671;
            int temp2 = v5 * (v5 + 1) % 2;

            if (temp2 != 0) {
               }

            if (v3 >= v2) {
               this.f1609 = null;
               break;
            }

            GpuBuffer v4 = v1[v3];
            if (v4 != null) {
               v4.close();
            }

            v3++;
         }
      }

      this.f1610 = 0;
      if (this.f1611 != null) {
         this.f1611.close();
         this.f1611 = null;
         
      }

      if (this.f1612 != null) {
         MemoryUtil.memFree(this.f1612);
         this.f1612 = null;
      }

      this.f1613 = false;
   }

   protected final ByteBuffer beginItem() {
      this.m1934();
      if (this.f1614 == 0) {

         this.f1612.clear();
         this.f1612.position(16);
      }

      return this.f1612;
   }

   private void m1934() {
      if (!this.f1613) {
         this.f1612 = MemoryUtil.memAlloc(this.f1607);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         this.f1611 = RenderSystem.getDevice().createBuffer(() -> {
            String v1x = this.f1603;

            return "client:" + v1x + "_dummy_vertex";
         }, 32, v1);
         MemoryUtil.memFree(v1);

         this.f1609 = new GpuBuffer[this.f1608];

         this.f1613 = true;
      }
   }

   @Override
   public final void discardBatch() {
      int v1 = -1707138358;
      int v2 = 225553411;
      int temp3 = v2 * (v2 + 1);

      this.f1615.clear();
   }

   public final void flush() {
      if (this.f1614 == 0) {
         int v1 = 1744188181;

         boolean v4 = this.f1615.isEmpty();

         if (v4) {
            return;
         }
      }

      wr.drawImmediate(this);
   }

   protected final void endItem() {
      this.f1614++;
      if (wr.isEnabled()) {
         wr.register(this);
         if (this.f1614 >= this.f1606) {
            int result = this.f1615.size();

            if (result >= this.f1608 - 1) {
               wr.drawImmediate(this);
            } else {
               this.m1936();
            }
         }
      } else {
         wr.drawImmediate(this);
      }
   }

   @Override
   public final void uploadBatch(CommandEncoder encoder) {
      this.m1936();
      int v2 = -427483784;

      
   }

   @Environment(EnvType.CLIENT)
   private static record st(GpuBuffer buffer, int count) {
      private final GpuBuffer f1602;

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",rt.st,"buffer;count",rt.st::f1602,rt.st::count>(this);

         return result;
      }

      @Override
      public final boolean equals(Object o) {
         int v2 = 1148292416;

         int v5 = v2 * (v2 + 1);

         return ObjectMethods.bootstrap<"equals",rt.st,"buffer;count",rt.st::f1602,rt.st::count>(this, o);
      }

      private st(GpuBuffer buffer, int count) {
         this.f1602 = buffer;
         this.count = count;
      }

      public GpuBuffer buffer() {
         int v1 = 1876360160;
         int temp2 = v1 + 1;

         int temp1 = v1 * temp2;

         return this.f1602;
      }

      
   }
}
