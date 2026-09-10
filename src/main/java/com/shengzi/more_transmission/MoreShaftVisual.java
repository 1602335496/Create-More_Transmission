package com.shengzi.more_transmission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * 泥土/石/铁……各种材质轴共用的 Flywheel visual：负责把一根"轴杆 partial"按转速转起来。
 *
 * 构造只做一件事——收下要旋转的模型（partial）。具体用哪个 partial 由 {@link #create} 工厂
 * 在运行时看方块决定，这样所有材质轴可以共用一个 BE 类型，不再需要每种材质一个 visual 类。
 */
public class MoreShaftVisual extends SingleAxisRotatingVisual<BracketedKineticBlockEntity> {

	/** 方块 -> 它该旋转的轴杆 partial。144 种轴各一行，新增/修改某材质时在这里对号入座即可。 */
	private static final Map<Block, Model> PARTIALS;

	static {
		Map<Block, Model> map = new HashMap<>();
		map.put(ModBlocks.OAK_LOG_SHAFT.get(), Models.partial(MorePartialModels.OAK_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_OAK_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_OAK_LOG_SHAFT));
		map.put(ModBlocks.OAK_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.OAK_PLANKS_SHAFT));
		map.put(ModBlocks.SPRUCE_LOG_SHAFT.get(), Models.partial(MorePartialModels.SPRUCE_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_SPRUCE_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_SPRUCE_LOG_SHAFT));
		map.put(ModBlocks.SPRUCE_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.SPRUCE_PLANKS_SHAFT));
		map.put(ModBlocks.BIRCH_LOG_SHAFT.get(), Models.partial(MorePartialModels.BIRCH_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_BIRCH_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_BIRCH_LOG_SHAFT));
		map.put(ModBlocks.BIRCH_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.BIRCH_PLANKS_SHAFT));
		map.put(ModBlocks.JUNGLE_LOG_SHAFT.get(), Models.partial(MorePartialModels.JUNGLE_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_JUNGLE_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_JUNGLE_LOG_SHAFT));
		map.put(ModBlocks.JUNGLE_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.JUNGLE_PLANKS_SHAFT));
		map.put(ModBlocks.ACACIA_LOG_SHAFT.get(), Models.partial(MorePartialModels.ACACIA_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_ACACIA_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_ACACIA_LOG_SHAFT));
		map.put(ModBlocks.ACACIA_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.ACACIA_PLANKS_SHAFT));
		map.put(ModBlocks.DARK_OAK_LOG_SHAFT.get(), Models.partial(MorePartialModels.DARK_OAK_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_DARK_OAK_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_DARK_OAK_LOG_SHAFT));
		map.put(ModBlocks.DARK_OAK_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.DARK_OAK_PLANKS_SHAFT));
		map.put(ModBlocks.MANGROVE_LOG_SHAFT.get(), Models.partial(MorePartialModels.MANGROVE_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_MANGROVE_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_MANGROVE_LOG_SHAFT));
		map.put(ModBlocks.MANGROVE_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.MANGROVE_PLANKS_SHAFT));
		map.put(ModBlocks.CHERRY_LOG_SHAFT.get(), Models.partial(MorePartialModels.CHERRY_LOG_SHAFT));
		map.put(ModBlocks.STRIPPED_CHERRY_LOG_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_CHERRY_LOG_SHAFT));
		map.put(ModBlocks.CHERRY_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.CHERRY_PLANKS_SHAFT));
		map.put(ModBlocks.BAMBOO_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.BAMBOO_BLOCK_SHAFT));
		map.put(ModBlocks.STRIPPED_BAMBOO_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_BAMBOO_BLOCK_SHAFT));
		map.put(ModBlocks.BAMBOO_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.BAMBOO_PLANKS_SHAFT));
		map.put(ModBlocks.CRIMSON_STEM_SHAFT.get(), Models.partial(MorePartialModels.CRIMSON_STEM_SHAFT));
		map.put(ModBlocks.STRIPPED_CRIMSON_STEM_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_CRIMSON_STEM_SHAFT));
		map.put(ModBlocks.CRIMSON_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.CRIMSON_PLANKS_SHAFT));
		map.put(ModBlocks.WARPED_STEM_SHAFT.get(), Models.partial(MorePartialModels.WARPED_STEM_SHAFT));
		map.put(ModBlocks.STRIPPED_WARPED_STEM_SHAFT.get(), Models.partial(MorePartialModels.STRIPPED_WARPED_STEM_SHAFT));
		map.put(ModBlocks.WARPED_PLANKS_SHAFT.get(), Models.partial(MorePartialModels.WARPED_PLANKS_SHAFT));
		map.put(ModBlocks.STONE_SHAFT.get(), Models.partial(MorePartialModels.STONE_SHAFT));
		map.put(ModBlocks.COBBLESTONE_SHAFT.get(), Models.partial(MorePartialModels.COBBLESTONE_SHAFT));
		map.put(ModBlocks.MOSSY_COBBLESTONE_SHAFT.get(), Models.partial(MorePartialModels.MOSSY_COBBLESTONE_SHAFT));
		map.put(ModBlocks.SMOOTH_STONE_SHAFT.get(), Models.partial(MorePartialModels.SMOOTH_STONE_SHAFT));
		map.put(ModBlocks.STONE_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.STONE_BRICKS_SHAFT));
		map.put(ModBlocks.MOSSY_STONE_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.MOSSY_STONE_BRICKS_SHAFT));
		map.put(ModBlocks.GRANITE_SHAFT.get(), Models.partial(MorePartialModels.GRANITE_SHAFT));
		map.put(ModBlocks.POLISHED_GRANITE_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_GRANITE_SHAFT));
		map.put(ModBlocks.DIORITE_SHAFT.get(), Models.partial(MorePartialModels.DIORITE_SHAFT));
		map.put(ModBlocks.POLISHED_DIORITE_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_DIORITE_SHAFT));
		map.put(ModBlocks.ANDESITE_SHAFT.get(), Models.partial(MorePartialModels.ANDESITE_SHAFT));
		map.put(ModBlocks.POLISHED_ANDESITE_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_ANDESITE_SHAFT));
		map.put(ModBlocks.DEEPSLATE_SHAFT.get(), Models.partial(MorePartialModels.DEEPSLATE_SHAFT));
		map.put(ModBlocks.COBBLED_DEEPSLATE_SHAFT.get(), Models.partial(MorePartialModels.COBBLED_DEEPSLATE_SHAFT));
		map.put(ModBlocks.POLISHED_DEEPSLATE_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_DEEPSLATE_SHAFT));
		map.put(ModBlocks.DEEPSLATE_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.DEEPSLATE_BRICKS_SHAFT));
		map.put(ModBlocks.TUFF_SHAFT.get(), Models.partial(MorePartialModels.TUFF_SHAFT));
		map.put(ModBlocks.POLISHED_TUFF_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_TUFF_SHAFT));
		map.put(ModBlocks.TUFF_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.TUFF_BRICKS_SHAFT));
		map.put(ModBlocks.BRICKS_SHAFT.get(), Models.partial(MorePartialModels.BRICKS_SHAFT));
		map.put(ModBlocks.PACKED_MUD_SHAFT.get(), Models.partial(MorePartialModels.PACKED_MUD_SHAFT));
		map.put(ModBlocks.MUD_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.MUD_BRICKS_SHAFT));
		map.put(ModBlocks.SANDSTONE_SHAFT.get(), Models.partial(MorePartialModels.SANDSTONE_SHAFT));
		map.put(ModBlocks.SMOOTH_SANDSTONE_SHAFT.get(), Models.partial(MorePartialModels.SMOOTH_SANDSTONE_SHAFT));
		map.put(ModBlocks.RED_SANDSTONE_SHAFT.get(), Models.partial(MorePartialModels.RED_SANDSTONE_SHAFT));
		map.put(ModBlocks.SMOOTH_RED_SANDSTONE_SHAFT.get(), Models.partial(MorePartialModels.SMOOTH_RED_SANDSTONE_SHAFT));
		map.put(ModBlocks.SEA_LANTERN_SHAFT.get(), Models.partial(MorePartialModels.SEA_LANTERN_SHAFT));
		map.put(ModBlocks.PRISMARINE_SHAFT.get(), Models.partial(MorePartialModels.PRISMARINE_SHAFT));
		map.put(ModBlocks.DARK_PRISMARINE_SHAFT.get(), Models.partial(MorePartialModels.DARK_PRISMARINE_SHAFT));
		map.put(ModBlocks.NETHERRACK_SHAFT.get(), Models.partial(MorePartialModels.NETHERRACK_SHAFT));
		map.put(ModBlocks.NETHER_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.NETHER_BRICKS_SHAFT));
		map.put(ModBlocks.RED_NETHER_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.RED_NETHER_BRICKS_SHAFT));
		map.put(ModBlocks.BASALT_SHAFT.get(), Models.partial(MorePartialModels.BASALT_SHAFT));
		map.put(ModBlocks.POLISHED_BASALT_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_BASALT_SHAFT));
		map.put(ModBlocks.BLACKSTONE_SHAFT.get(), Models.partial(MorePartialModels.BLACKSTONE_SHAFT));
		map.put(ModBlocks.GILDED_BLACKSTONE_SHAFT.get(), Models.partial(MorePartialModels.GILDED_BLACKSTONE_SHAFT));
		map.put(ModBlocks.POLISHED_BLACKSTONE_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_BLACKSTONE_SHAFT));
		map.put(ModBlocks.POLISHED_BLACKSTONE_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.POLISHED_BLACKSTONE_BRICKS_SHAFT));
		map.put(ModBlocks.END_STONE_SHAFT.get(), Models.partial(MorePartialModels.END_STONE_SHAFT));
		map.put(ModBlocks.END_STONE_BRICKS_SHAFT.get(), Models.partial(MorePartialModels.END_STONE_BRICKS_SHAFT));
		map.put(ModBlocks.PURPUR_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.PURPUR_BLOCK_SHAFT));
		map.put(ModBlocks.COAL_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.COAL_BLOCK_SHAFT));
		map.put(ModBlocks.IRON_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.IRON_BLOCK_SHAFT));
		map.put(ModBlocks.GOLD_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.GOLD_BLOCK_SHAFT));
		map.put(ModBlocks.REDSTONE_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.REDSTONE_BLOCK_SHAFT));
		map.put(ModBlocks.EMERALD_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.EMERALD_BLOCK_SHAFT));
		map.put(ModBlocks.LAPIS_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.LAPIS_BLOCK_SHAFT));
		map.put(ModBlocks.DIAMOND_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.DIAMOND_BLOCK_SHAFT));
		map.put(ModBlocks.NETHERITE_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.NETHERITE_BLOCK_SHAFT));
		map.put(ModBlocks.QUARTZ_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.QUARTZ_BLOCK_SHAFT));
		map.put(ModBlocks.QUARTZ_PILLAR_SHAFT.get(), Models.partial(MorePartialModels.QUARTZ_PILLAR_SHAFT));
		map.put(ModBlocks.AMETHYST_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.AMETHYST_BLOCK_SHAFT));
		map.put(ModBlocks.COPPER_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.COPPER_BLOCK_SHAFT));
		map.put(ModBlocks.CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.CUT_COPPER_SHAFT));
		map.put(ModBlocks.WHITE_WOOL_SHAFT.get(), Models.partial(MorePartialModels.WHITE_WOOL_SHAFT));
		map.put(ModBlocks.LIGHT_GRAY_WOOL_SHAFT.get(), Models.partial(MorePartialModels.LIGHT_GRAY_WOOL_SHAFT));
		map.put(ModBlocks.GRAY_WOOL_SHAFT.get(), Models.partial(MorePartialModels.GRAY_WOOL_SHAFT));
		map.put(ModBlocks.BLACK_WOOL_SHAFT.get(), Models.partial(MorePartialModels.BLACK_WOOL_SHAFT));
		map.put(ModBlocks.BROWN_WOOL_SHAFT.get(), Models.partial(MorePartialModels.BROWN_WOOL_SHAFT));
		map.put(ModBlocks.RED_WOOL_SHAFT.get(), Models.partial(MorePartialModels.RED_WOOL_SHAFT));
		map.put(ModBlocks.ORANGE_WOOL_SHAFT.get(), Models.partial(MorePartialModels.ORANGE_WOOL_SHAFT));
		map.put(ModBlocks.YELLOW_WOOL_SHAFT.get(), Models.partial(MorePartialModels.YELLOW_WOOL_SHAFT));
		map.put(ModBlocks.LIME_WOOL_SHAFT.get(), Models.partial(MorePartialModels.LIME_WOOL_SHAFT));
		map.put(ModBlocks.GREEN_WOOL_SHAFT.get(), Models.partial(MorePartialModels.GREEN_WOOL_SHAFT));
		map.put(ModBlocks.CYAN_WOOL_SHAFT.get(), Models.partial(MorePartialModels.CYAN_WOOL_SHAFT));
		map.put(ModBlocks.LIGHT_BLUE_WOOL_SHAFT.get(), Models.partial(MorePartialModels.LIGHT_BLUE_WOOL_SHAFT));
		map.put(ModBlocks.BLUE_WOOL_SHAFT.get(), Models.partial(MorePartialModels.BLUE_WOOL_SHAFT));
		map.put(ModBlocks.PURPLE_WOOL_SHAFT.get(), Models.partial(MorePartialModels.PURPLE_WOOL_SHAFT));
		map.put(ModBlocks.MAGENTA_WOOL_SHAFT.get(), Models.partial(MorePartialModels.MAGENTA_WOOL_SHAFT));
		map.put(ModBlocks.PINK_WOOL_SHAFT.get(), Models.partial(MorePartialModels.PINK_WOOL_SHAFT));
		map.put(ModBlocks.GLASS_SHAFT.get(), translucent(MorePartialModels.GLASS_SHAFT));
		map.put(ModBlocks.TINTED_GLASS_SHAFT.get(), translucent(MorePartialModels.TINTED_GLASS_SHAFT));
		map.put(ModBlocks.WHITE_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.WHITE_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.LIGHT_GRAY_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.LIGHT_GRAY_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.GRAY_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.GRAY_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.BLACK_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.BLACK_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.BROWN_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.BROWN_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.RED_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.RED_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.ORANGE_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.ORANGE_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.YELLOW_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.YELLOW_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.LIME_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.LIME_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.GREEN_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.GREEN_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.CYAN_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.CYAN_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.LIGHT_BLUE_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.LIGHT_BLUE_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.BLUE_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.BLUE_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.PURPLE_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.PURPLE_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.MAGENTA_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.MAGENTA_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.PINK_STAINED_GLASS_SHAFT.get(), translucent(MorePartialModels.PINK_STAINED_GLASS_SHAFT));
		map.put(ModBlocks.CLAY_SHAFT.get(), Models.partial(MorePartialModels.CLAY_SHAFT));
		map.put(ModBlocks.GRAVEL_SHAFT.get(), Models.partial(MorePartialModels.GRAVEL_SHAFT));
		map.put(ModBlocks.ICE_SHAFT.get(), Models.partial(MorePartialModels.ICE_SHAFT));
		map.put(ModBlocks.SNOW_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.SNOW_BLOCK_SHAFT));
		map.put(ModBlocks.MOSS_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.MOSS_BLOCK_SHAFT));
		map.put(ModBlocks.CALCITE_SHAFT.get(), Models.partial(MorePartialModels.CALCITE_SHAFT));
		map.put(ModBlocks.MAGMA_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.MAGMA_BLOCK_SHAFT));
		map.put(ModBlocks.OBSIDIAN_SHAFT.get(), Models.partial(MorePartialModels.OBSIDIAN_SHAFT));
		map.put(ModBlocks.SOUL_SAND_SHAFT.get(), Models.partial(MorePartialModels.SOUL_SAND_SHAFT));
		map.put(ModBlocks.SAND_SHAFT.get(), Models.partial(MorePartialModels.SAND_SHAFT));
		map.put(ModBlocks.RED_SAND_SHAFT.get(), Models.partial(MorePartialModels.RED_SAND_SHAFT));
		map.put(ModBlocks.DRIPSTONE_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.DRIPSTONE_BLOCK_SHAFT));
		map.put(ModBlocks.BONE_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.BONE_BLOCK_SHAFT));
		map.put(ModBlocks.GLOWSTONE_SHAFT.get(), Models.partial(MorePartialModels.GLOWSTONE_SHAFT));
		map.put(ModBlocks.SPONGE_SHAFT.get(), Models.partial(MorePartialModels.SPONGE_SHAFT));
		map.put(ModBlocks.HAY_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.HAY_BLOCK_SHAFT));
		map.put(ModBlocks.HONEYCOMB_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.HONEYCOMB_BLOCK_SHAFT));
		map.put(ModBlocks.SLIME_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.SLIME_BLOCK_SHAFT));
		map.put(ModBlocks.HONEY_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.HONEY_BLOCK_SHAFT));
		map.put(ModBlocks.SCULK_SHAFT.get(), Models.partial(MorePartialModels.SCULK_SHAFT));
		map.put(ModBlocks.BEDROCK_SHAFT.get(), Models.partial(MorePartialModels.BEDROCK_SHAFT));
		map.put(ModBlocks.TNT_SHAFT.get(), Models.partial(MorePartialModels.TNT_SHAFT));
		map.put(ModBlocks.DIRT_SHAFT.get(), Models.partial(MorePartialModels.DIRT_SHAFT));
		PARTIALS = Collections.unmodifiableMap(map);
	}


	/** 玻璃类轴杆贴图带透明像素，必须让 Flywheel 以 translucent 渲染层烘焙 partial，否则透明像素会按实心渲染成黑斑。 */
	private static Model translucent(PartialModel partial) {
		return new BakedModelBuilder(new TranslucentRenderModel(partial.get())).build();
	}

	/** 把普通 partial 包装成"只声明 translucent 渲染层"的模型。 */
	private static class TranslucentRenderModel extends BakedModelWrapper<BakedModel> {
		private TranslucentRenderModel(BakedModel original) {
			super(original);
		}

		@Override
		public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
			return ChunkRenderTypeSet.of(RenderType.translucent());
		}
	}

	private MoreShaftVisual(VisualizationContext context, BracketedKineticBlockEntity blockEntity, float partialTick,
		Model model) {
		super(context, blockEntity, partialTick, model);
	}

	/**
	 * Flywheel visual 工厂，注册给 BE 类型后由 Flywheel 在方块进入视野时调用。
	 * 仿照 Create 的 BracketedKineticBlockEntityVisual.create：按方块本体从 PARTIALS 查旋转 partial。
	 */
	public static BlockEntityVisual<BracketedKineticBlockEntity> create(VisualizationContext context,
		BracketedKineticBlockEntity blockEntity, float partialTick) {
		Block block = blockEntity.getBlockState().getBlock();
		Model model = PARTIALS.get(block);
		if (model == null)
			throw new IllegalArgumentException("未知的材质轴方块: " + block +
				"（请在 MoreShaftVisual.PARTIALS 里给它补一行）");
		return new MoreShaftVisual(context, blockEntity, partialTick, model);
	}

}
