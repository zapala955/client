package net.client.visuals.render.chunk;

import net.client.visuals.module.ModuleManager;
import net.client.visuals.module.render.ParticlesModule;
import net.client.visuals.module.render.WavingPlantsModule;
import net.client.visuals.render.shader.ShaderPacks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;

/**
 * Drives {@link WavingPlantsModule} on Sodium.
 *
 * <p><b>What sways</b> is decided while chunks are meshed: {@code SodiumBlockRendererMixin} asks
 * {@link #classify} for each block and stores the answer in two spare bits of the quad's material
 * byte (bits 3–4; Sodium only uses bits 0–2, for mipmapping and the alpha cutoff). The patched
 * vertex shader reads them back per vertex – exact block knowledge, like a shader pack's block ids,
 * without any extra vertex data.</p>
 *
 * <p><b>The wind</b> rides in the global terrain uniform Chunk Animations already borrows (see
 * {@code ChunkAnimator#sodiumFadeUniform}): {@link #sodiumBits} is its 7-bit share, read once per
 * frame, so wind changes are live. Plants keep their roots: the shader only moves the vertices at
 * the top of their texture (Sodium's per-vertex texture bias tells which side of the sprite a
 * vertex is on).</p>
 */
public final class FoliageWaving {
	public static final int NONE = 0;
	/** Leaves and vines: every vertex shimmers a little. */
	public static final int LEAVES = 1;
	/** Plants: the tips move, the base stays. */
	public static final int PLANT = 2;
	/** Upper half of a two-block plant: its base moves with the lower half's tip, its top twice as far. */
	public static final int TALL_UPPER = 3;

	private static final int MASK_LEAVES = 1;
	private static final int MASK_VINES = 2;
	private static final int MASK_PLANTS = 4;
	private static final int MASK_CROPS = 8;
	/** Everything sways until the module's switches have been read (that's what early chunks are meshed with). */
	private static volatile int mask = MASK_LEAVES | MASK_VINES | MASK_PLANTS | MASK_CROPS;
	private static @Nullable WavingPlantsModule module;

	private FoliageWaving() {
	}

	/** Chunk-meshing threads: how a block's quads should sway. */
	public static int classify(BlockState state) {
		Block block = state.getBlock();
		int m = mask;
		if (block instanceof LeavesBlock) {
			return (m & MASK_LEAVES) != 0 ? LEAVES : NONE;
		}
		if (block instanceof VineBlock) {
			return (m & MASK_VINES) != 0 ? LEAVES : NONE;
		}
		if (!(block instanceof VegetationBlock)) {
			return NONE;
		}
		if (block instanceof CropBlock || block instanceof StemBlock || block instanceof AttachedStemBlock || block instanceof NetherWartBlock) {
			return (m & MASK_CROPS) != 0 ? PLANT : NONE;
		}
		if ((m & MASK_PLANTS) == 0) {
			return NONE;
		}
		if (block instanceof DoublePlantBlock) {
			return state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? TALL_UPPER : PLANT;
		}
		// Flat or rigid vegetation stays still.
		if (block instanceof LilyPadBlock || block instanceof FlowerBedBlock || block instanceof LeafLitterBlock
				|| block instanceof MushroomBlock || block instanceof SeaPickleBlock || block instanceof AzaleaBlock) {
			return NONE;
		}
		return PLANT;
	}

	/** Client tick while the module is on: applies the "what sways" switches, re-meshing chunks once when they change. */
	public static void sync(WavingPlantsModule m, Minecraft minecraft) {
		int next = (m.leaves.get() ? MASK_LEAVES : 0) | (m.vines.get() ? MASK_VINES : 0)
				| (m.plants.get() ? MASK_PLANTS : 0) | (m.crops.get() ? MASK_CROPS : 0);
		if (next != mask) {
			mask = next;
			if (minecraft.level != null && minecraft.levelExtractor != null) {
				minecraft.levelExtractor.allChanged();
			}
		}
	}

	/**
	 * The wind for Sodium's packed terrain uniform: level (1–7) | direction (0–7, 45° steps clockwise
	 * from north) {@code << 3} | gusts {@code << 6}; 0 when the module is off or a shader pack draws
	 * the terrain. Render thread, once per frame.
	 */
	public static int sodiumBits() {
		WavingPlantsModule m = module;
		if (m == null) {
			m = module = ModuleManager.get(WavingPlantsModule.class);
		}
		if (!m.isEnabled() || ShaderPacks.inUse()) {
			return 0;
		}
		int compass;
		int direction = m.direction.index();
		if (direction == 0) {
			ParticlesModule particles = ParticlesModule.instance();
			double bearing = particles != null ? particles.windDirection.get() : 45.0;
			compass = Math.floorMod((int) Math.round(bearing / 45.0), 8);
		} else {
			compass = direction - 1;
		}
		return (m.wind.index() + 1) | compass << 3 | (m.gusts.get() ? 1 : 0) << 6;
	}
}
