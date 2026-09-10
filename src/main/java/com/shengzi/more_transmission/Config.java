package com.shengzi.more_transmission;

import java.util.LinkedHashMap;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组公共配置：为每个传动轴生成一条「最大转速(RPM)」。
 *
 * 默认值来自 shaft.txt 的强度档，与方块注册时机无关地写死在下面，
 * 保证第一次启动游戏时自动生成的配置文件一定包含全部条目。
 */
public class Config {

	private static final String[] IDS = {
		"oak_log_shaft", "stripped_oak_log_shaft", "oak_planks_shaft", "spruce_log_shaft", "stripped_spruce_log_shaft", "spruce_planks_shaft",
		"birch_log_shaft", "stripped_birch_log_shaft", "birch_planks_shaft", "jungle_log_shaft", "stripped_jungle_log_shaft", "jungle_planks_shaft",
		"acacia_log_shaft", "stripped_acacia_log_shaft", "acacia_planks_shaft", "dark_oak_log_shaft", "stripped_dark_oak_log_shaft", "dark_oak_planks_shaft",
		"mangrove_log_shaft", "stripped_mangrove_log_shaft", "mangrove_planks_shaft", "cherry_log_shaft", "stripped_cherry_log_shaft", "cherry_planks_shaft",
		"bamboo_block_shaft", "stripped_bamboo_block_shaft", "bamboo_planks_shaft", "crimson_stem_shaft", "stripped_crimson_stem_shaft", "crimson_planks_shaft",
		"warped_stem_shaft", "stripped_warped_stem_shaft", "warped_planks_shaft", "stone_shaft", "cobblestone_shaft", "mossy_cobblestone_shaft",
		"smooth_stone_shaft", "stone_bricks_shaft", "mossy_stone_bricks_shaft", "granite_shaft", "polished_granite_shaft", "diorite_shaft",
		"polished_diorite_shaft", "andesite_shaft", "polished_andesite_shaft", "deepslate_shaft", "cobbled_deepslate_shaft", "polished_deepslate_shaft",
		"deepslate_bricks_shaft", "tuff_shaft", "polished_tuff_shaft", "tuff_bricks_shaft", "bricks_shaft", "packed_mud_shaft",
		"mud_bricks_shaft", "sandstone_shaft", "smooth_sandstone_shaft", "red_sandstone_shaft", "smooth_red_sandstone_shaft", "sea_lantern_shaft",
		"prismarine_shaft", "dark_prismarine_shaft", "netherrack_shaft", "nether_bricks_shaft", "red_nether_bricks_shaft", "basalt_shaft",
		"polished_basalt_shaft", "blackstone_shaft", "gilded_blackstone_shaft", "polished_blackstone_shaft", "polished_blackstone_bricks_shaft", "end_stone_shaft",
		"end_stone_bricks_shaft", "purpur_block_shaft", "coal_block_shaft", "iron_block_shaft", "gold_block_shaft", "redstone_block_shaft",
		"emerald_block_shaft", "lapis_block_shaft", "diamond_block_shaft", "netherite_block_shaft", "quartz_block_shaft", "quartz_pillar_shaft",
		"amethyst_block_shaft", "copper_block_shaft", "cut_copper_shaft", "white_wool_shaft", "light_gray_wool_shaft", "gray_wool_shaft",
		"black_wool_shaft", "brown_wool_shaft", "red_wool_shaft", "orange_wool_shaft", "yellow_wool_shaft", "lime_wool_shaft",
		"green_wool_shaft", "cyan_wool_shaft", "light_blue_wool_shaft", "blue_wool_shaft", "purple_wool_shaft", "magenta_wool_shaft",
		"pink_wool_shaft", "glass_shaft", "tinted_glass_shaft", "white_stained_glass_shaft", "light_gray_stained_glass_shaft", "gray_stained_glass_shaft",
		"black_stained_glass_shaft", "brown_stained_glass_shaft", "red_stained_glass_shaft", "orange_stained_glass_shaft", "yellow_stained_glass_shaft", "lime_stained_glass_shaft",
		"green_stained_glass_shaft", "cyan_stained_glass_shaft", "light_blue_stained_glass_shaft", "blue_stained_glass_shaft", "purple_stained_glass_shaft", "magenta_stained_glass_shaft",
		"pink_stained_glass_shaft", "clay_shaft", "gravel_shaft", "ice_shaft", "snow_block_shaft", "moss_block_shaft",
		"calcite_shaft", "magma_block_shaft", "obsidian_shaft", "soul_sand_shaft", "sand_shaft", "red_sand_shaft",
		"dripstone_block_shaft", "bone_block_shaft", "glowstone_shaft", "sponge_shaft", "hay_block_shaft", "honeycomb_block_shaft",
		"slime_block_shaft", "honey_block_shaft", "sculk_shaft", "bedrock_shaft", "tnt_shaft", "dirt_shaft",
		"shaft",
	};

	private static final int[] DEFAULTS = {
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 64, 64, 64,
		64, 64, 64, 64, 64, 64, 64, 64, 64, 96, 96, 96,
		96, 64, 64, 64, 48, 48, 48, 48, 48, 48, 48, 48,
		64, 64, 48, 112, 112, 128, 128, 128, 128, 128, 128, 64,
		64, 128, 160, 192, 192, 192, 192, 192, 224, 256, 192, 192,
		192, 160, 160, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 64, 160, 256, 16, 16, 16,
		128, 64, 48, 16, 16, 16, 16, 16, 16, 256, 16, 16,
		128,
	};

	private static final Map<String, ModConfigSpec.IntValue> MAX_SPEED = new LinkedHashMap<>();

	public static final ModConfigSpec SPEC;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		builder.comment(
			"每一根传动轴能承受的最大转速(RPM)。",
			"值域 0~256；0 表示一有转速就散架。",
			"修改后保存，重启游戏（或用配置界面重载）即生效。")
			.push("shaft_max_speed");

		for (int i = 0; i < IDS.length; i++) {
			String path = IDS[i];
			int def = DEFAULTS[i];
			MAX_SPEED.put(path,
				builder.comment("Max speed for " + path + " (default " + def + ")")
					.defineInRange(path, def, 0, 256));
		}

		builder.pop();
		SPEC = builder.build();
	}

	/** 查询某方块注册名(如 dirt_shaft)配置的最大转速；没有对应条目返回 -1（视为不限/不参与碎裂）。 */
	public static int maxSpeed(String blockPath) {
		ModConfigSpec.IntValue value = MAX_SPEED.get(blockPath);
		return value == null ? -1 : value.get();
	}
}
