package com.shengzi.more_transmission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shengzi.more_transmission.custom_block.LampShaftBlock;
import com.simibubi.create.AllPartialModels;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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

	/** 方块 -> 它该旋转的轴杆 partial。183 种轴各一行，新增/修改某材质时在这里对号入座即可。 */
	private static final Map<Block, Model> PARTIALS;

	/** 贴图随方块状态变的轴（红石灯轴 + 8 根铜灯轴）的「熄灭 / 点亮」模型对，见 {@link #statePartial}。 */
	private static final Map<Block, LitModels> LIT_MODELS;

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
		// 铜的氧化 / 涂蜡变种（14 根，顺序与 ModBlocks / Config.IDS 一致）
		map.put(ModBlocks.EXPOSED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.EXPOSED_COPPER_SHAFT));
		map.put(ModBlocks.WEATHERED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WEATHERED_COPPER_SHAFT));
		map.put(ModBlocks.OXIDIZED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.OXIDIZED_COPPER_SHAFT));
		map.put(ModBlocks.EXPOSED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.EXPOSED_CUT_COPPER_SHAFT));
		map.put(ModBlocks.WEATHERED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WEATHERED_CUT_COPPER_SHAFT));
		map.put(ModBlocks.OXIDIZED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.OXIDIZED_CUT_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_COPPER_BLOCK_SHAFT.get(), Models.partial(MorePartialModels.WAXED_COPPER_BLOCK_SHAFT));
		map.put(ModBlocks.WAXED_EXPOSED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_EXPOSED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_WEATHERED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_WEATHERED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_OXIDIZED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_OXIDIZED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_CUT_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_EXPOSED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_EXPOSED_CUT_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_WEATHERED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_WEATHERED_CUT_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_OXIDIZED_CUT_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_OXIDIZED_CUT_COPPER_SHAFT));
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
		// 雕纹铜块 8 根（贴图不透明，普通处理）
		map.put(ModBlocks.CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.EXPOSED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.EXPOSED_CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.WEATHERED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WEATHERED_CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.OXIDIZED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.OXIDIZED_CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_EXPOSED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_EXPOSED_CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_WEATHERED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_WEATHERED_CHISELED_COPPER_SHAFT));
		map.put(ModBlocks.WAXED_OXIDIZED_CHISELED_COPPER_SHAFT.get(), Models.partial(MorePartialModels.WAXED_OXIDIZED_CHISELED_COPPER_SHAFT));
		// 铜格栅 8 根：用的是原版那张 30% 镂空的贴图（位置见各自模型的 uv），所以必须走 cutout 渲染层
		map.put(ModBlocks.COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.COPPER_GRATE_SHAFT));
		map.put(ModBlocks.EXPOSED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.EXPOSED_COPPER_GRATE_SHAFT));
		map.put(ModBlocks.WEATHERED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.WEATHERED_COPPER_GRATE_SHAFT));
		map.put(ModBlocks.OXIDIZED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.OXIDIZED_COPPER_GRATE_SHAFT));
		map.put(ModBlocks.WAXED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.WAXED_COPPER_GRATE_SHAFT));
		map.put(ModBlocks.WAXED_EXPOSED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.WAXED_EXPOSED_COPPER_GRATE_SHAFT));
		map.put(ModBlocks.WAXED_WEATHERED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.WAXED_WEATHERED_COPPER_GRATE_SHAFT));
		map.put(ModBlocks.WAXED_OXIDIZED_COPPER_GRATE_SHAFT.get(), cutout(MorePartialModels.WAXED_OXIDIZED_COPPER_GRATE_SHAFT));
		// 灯轴（红石灯 + 8 根铜灯）统一在这儿登记，顺序与 ModBlocks 一致
		Map<Block, LitModels> lit = new HashMap<>();
		litShaft(map, lit, ModBlocks.REDSTONE_LAMP_SHAFT, MorePartialModels.REDSTONE_LAMP_SHAFT, MorePartialModels.REDSTONE_LAMP_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.COPPER_BULB_SHAFT, MorePartialModels.COPPER_BULB_SHAFT, MorePartialModels.COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.EXPOSED_COPPER_BULB_SHAFT, MorePartialModels.EXPOSED_COPPER_BULB_SHAFT, MorePartialModels.EXPOSED_COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.WEATHERED_COPPER_BULB_SHAFT, MorePartialModels.WEATHERED_COPPER_BULB_SHAFT, MorePartialModels.WEATHERED_COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.OXIDIZED_COPPER_BULB_SHAFT, MorePartialModels.OXIDIZED_COPPER_BULB_SHAFT, MorePartialModels.OXIDIZED_COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.WAXED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.WAXED_EXPOSED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_EXPOSED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_EXPOSED_COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.WAXED_WEATHERED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_WEATHERED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_WEATHERED_COPPER_BULB_SHAFT_LIT);
		litShaft(map, lit, ModBlocks.WAXED_OXIDIZED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_OXIDIZED_COPPER_BULB_SHAFT, MorePartialModels.WAXED_OXIDIZED_COPPER_BULB_SHAFT_LIT);
		PARTIALS = Collections.unmodifiableMap(map);
		LIT_MODELS = Collections.unmodifiableMap(lit);
	}

	/** 一对模型：熄灭 / 点亮。 */
	private record LitModels(Model off, Model on) {
	}

	/**
	 * 登记一根「灯轴」：熄灭那份进 {@code partials} 当基准（拿不到状态时用它，也保证 PARTIALS 里每种轴都有一行），
	 * 两者一起进 {@link #LIT_MODELS} 供 {@link #statePartial} 按 LIT 取用。
	 */
	private static void litShaft(Map<Block, Model> partials, Map<Block, LitModels> lit,
		BlockEntry<LampShaftBlock> block, PartialModel off, PartialModel on) {
		Model offModel = Models.partial(off);
		partials.put(block.get(), offModel);
		lit.put(block.get(), new LitModels(offModel, Models.partial(on)));
	}


	/** 玻璃类轴杆贴图带透明像素，必须让 Flywheel 以 translucent 渲染层烘焙 partial，否则透明像素会按实心渲染成黑斑。 */
	private static Model translucent(PartialModel partial) {
		return bake(partial, ChunkRenderTypeSet.of(RenderType.translucent()), null);
	}

	/**
	 * 铜格栅这类**镂空**贴图：像素非全透明即不透明，没有半透明过渡，所以要的是 cutout 而不是 translucent。
	 * 原版也是把 8 种铜格栅放在 `RenderType.cutout()`（见 {@code ItemBlockRenderTypes}）。
	 */
	private static Model cutout(PartialModel partial) {
		return bake(partial, ChunkRenderTypeSet.of(RenderType.cutout()), null);
	}

	/**
	 * 把一个 partial 烘焙成 Flywheel 模型：强制它只声明给定的这些渲染层，需要的话在烘焙时施加
	 * {@code transform}（可为 null）。
	 *
	 * <p>为什么非强制不可：Flywheel 烘焙 partial 时用的虚拟 worldgetter 里 {@code getBlockState}
	 * 恒返回 AIR，`BakedModel.getRenderTypes` 于是落到默认实现 → 查 AIR 的渲染层 → 永远 solid。
	 * 透明像素就这么被按实心画成黑斑。
	 *
	 * <p>要变换也只能在烘焙时做——{@code RotatingInstance} 里没有变换矩阵，事后改不动顶点。
	 * 传送带那根杆就是靠它等比放大 5%，好把 Create 滑轮里那根原版杆整个包住（见 {@link MaterialBeltVisual}）。
	 */
	static Model bake(PartialModel partial, ChunkRenderTypeSet renderTypes, PoseStack transform) {
		return new BakedModelBuilder(new FixedRenderTypeModel(partial.get(), renderTypes))
			.poseStack(transform)
			.build();
	}

	/**
	 * 某种材质轴该按哪些渲染层烘焙 —— 判据不在这儿抄，直接问方块自己注册的那份。
	 * 玻璃 / 格栅的层在 {@code More_transmission.ClientModEvents} 里登记过（物品图标走的也是这一份），
	 * 所以以后新增带透明像素的材质，只要登记过就自动跟上；没登记过的方块返回 {@code {solid}}。
	 *
	 * <p>⚠️ 必须用 {@code getRenderLayers}（NeoForge），**不能**用 {@code getChunkRenderType}（原版）——
	 * 这两个方法读的是两张不同的表：{@code setRenderLayer} 只写 NeoForge 的 {@code BLOCK_RENDER_TYPES}，
	 * 而 {@code getChunkRenderType} 读原版的 {@code TYPE_BY_BLOCK}（后者仅在类加载时从前者拷过一份初始值）。
	 * 本模组的方块在原版那张表里根本没有条目，于是 {@code getChunkRenderType} 对每根轴都返回 solid。
	 * 2026-09-18 修传送带那根杆时第一次就是这么修瞎的：改了、编译过了、游戏里毫无变化。
	 *
	 * <p>{@code getRenderLayers} 也正是 {@code BakedModel.getRenderTypes} 的默认实现，所以拿它当
	 * "如果这里的方块状态是真的、Flywheel 会怎么烘"的答案，是准确的。
	 */
	@SuppressWarnings("deprecation")
	static ChunkRenderTypeSet renderTypesFor(Block material) {
		return ItemBlockRenderTypes.getRenderLayers(material.defaultBlockState());
	}

	/** 把普通 partial 包装成"只声明这些渲染层"的模型。 */
	private static class FixedRenderTypeModel extends BakedModelWrapper<BakedModel> {
		private final ChunkRenderTypeSet renderTypes;

		private FixedRenderTypeModel(BakedModel original, ChunkRenderTypeSet renderTypes) {
			super(original);
			this.renderTypes = renderTypes;
		}

		@Override
		public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
			return renderTypes;
		}
	}

	private MoreShaftVisual(VisualizationContext context, BracketedKineticBlockEntity blockEntity, float partialTick,
		Model model) {
		super(context, blockEntity, partialTick, model);
	}

	/**
	 * Flywheel visual 工厂，注册给 BE 类型后由 Flywheel 在方块进入视野时调用。
	 * 仿照 Create 的 BracketedKineticBlockEntityVisual.create：按方块从 PARTIALS 查旋转 partial。
	 *
	 * <p>红石灯轴的点亮/熄灭是两套模型，所以先问 {@link #statePartial}；其余轴仍走 PARTIALS。
	 * 不需要在这里做「状态变了就换模型」的动态逻辑——LIT 变化是一次方块状态变化，客户端
	 * {@code LevelRenderer#setBlockDirty} 会让 Flywheel 把整个 visual 删掉重建（见那个方法尾部的 mixin）。
	 */
	public static BlockEntityVisual<BracketedKineticBlockEntity> create(VisualizationContext context,
		BracketedKineticBlockEntity blockEntity, float partialTick) {
		BlockState state = blockEntity.getBlockState();
		Model model = statePartial(state);
		if (model == null)
			model = PARTIALS.get(state.getBlock());
		if (model == null)
			throw new IllegalArgumentException("未知的材质轴方块: " + state.getBlock() +
				"（请在 MoreShaftVisual.PARTIALS 里给它补一行）");
		return new MoreShaftVisual(context, blockEntity, partialTick, model);
	}

	/**
	 * 贴图会随方块状态变的轴的模型——目前是「灯轴」那一族（红石灯轴 + 8 根铜灯轴，转起来点亮）。
	 * 其它方块一律返回 null，由调用方回落到 {@link #PARTIALS}。
	 */
	private static Model statePartial(BlockState state) {
		LitModels lit = LIT_MODELS.get(state.getBlock());
		if (lit == null)
			return null;
		return state.getValue(LampShaftBlock.LIT) ? lit.on() : lit.off();
	}

	/**
	 * 按方块取它该旋转的模型，给拿不到 PARTIALS 的调用方用——目前是封套轴
	 * （{@link MoreEncasedShaftVisual}）：它里面包的是哪种材质，就转哪种材质的杆。
	 *
	 * 认不出来的方块（原版轴、或存档里的未知材质）回落到原版轴的 partial，
	 * 不会像 {@link #create} 那样抛异常——封套轴的 BE 数据来自存档，不该因此崩客户端。
	 *
	 * <p>注意这只拿到「方块」、拿不到状态，所以红石灯轴一律按**熄灭**处理：
	 * 封套轴只记了内轴是哪个方块，没记它当时亮没亮（见 {@link MoreEncasedShaftBlockEntity}）。
	 * 也就是说被机壳包住的灯轴不会亮——这是刻意的取舍，避免为了一个边角情况再往封套 BE 里塞一个状态。
	 */
	public static Model modelFor(Block block) {
		Model model = PARTIALS.get(block);
		return model != null ? model : Models.partial(AllPartialModels.SHAFT);
	}

}
