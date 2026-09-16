package com.shengzi.more_transmission;

import java.util.LinkedHashMap;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组公共配置。
 *
 * <p>顶层是一个总开关 {@code enable_shaft_max_speed}（**默认关闭**，即不限制转速），
 * 打开之后 {@code [shaft_max_speed]} 分组里逐根轴配置的转速上限才会生效。
 * 这样默认玩起来和原版 Create 一样没有转速惩罚，想要硬核设定的玩家可以自己开。
 *
 * <p>每根轴的默认值来自 shaft.txt 的强度档，与方块注册时机无关地写死在下面，
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
		"amethyst_block_shaft", "copper_block_shaft", "cut_copper_shaft", "exposed_copper_shaft", "weathered_copper_shaft", "oxidized_copper_shaft",
		"exposed_cut_copper_shaft", "weathered_cut_copper_shaft", "oxidized_cut_copper_shaft", "waxed_copper_block_shaft", "waxed_exposed_copper_shaft", "waxed_weathered_copper_shaft",
		"waxed_oxidized_copper_shaft", "waxed_cut_copper_shaft", "waxed_exposed_cut_copper_shaft", "waxed_weathered_cut_copper_shaft", "waxed_oxidized_cut_copper_shaft", "white_wool_shaft",
		"light_gray_wool_shaft", "gray_wool_shaft", "black_wool_shaft", "brown_wool_shaft", "red_wool_shaft", "orange_wool_shaft",
		"yellow_wool_shaft", "lime_wool_shaft", "green_wool_shaft", "cyan_wool_shaft", "light_blue_wool_shaft", "blue_wool_shaft",
		"purple_wool_shaft", "magenta_wool_shaft", "pink_wool_shaft", "glass_shaft", "tinted_glass_shaft", "white_stained_glass_shaft",
		"light_gray_stained_glass_shaft", "gray_stained_glass_shaft", "black_stained_glass_shaft", "brown_stained_glass_shaft", "red_stained_glass_shaft", "orange_stained_glass_shaft",
		"yellow_stained_glass_shaft", "lime_stained_glass_shaft", "green_stained_glass_shaft", "cyan_stained_glass_shaft", "light_blue_stained_glass_shaft", "blue_stained_glass_shaft",
		"purple_stained_glass_shaft", "magenta_stained_glass_shaft", "pink_stained_glass_shaft", "clay_shaft", "gravel_shaft", "ice_shaft",
		"snow_block_shaft", "moss_block_shaft", "calcite_shaft", "magma_block_shaft", "obsidian_shaft", "soul_sand_shaft",
		"sand_shaft", "red_sand_shaft", "dripstone_block_shaft", "bone_block_shaft", "glowstone_shaft", "sponge_shaft",
		"hay_block_shaft", "honeycomb_block_shaft", "slime_block_shaft", "honey_block_shaft", "sculk_shaft", "bedrock_shaft",
		"tnt_shaft", "dirt_shaft", "shaft"
	};

	private static final int[] DEFAULTS = {
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 64, 64, 64,
		64, 64, 64, 64, 64, 64, 64, 64, 64, 96, 96, 96,
		96, 64, 64, 64, 48, 48, 48, 48, 48, 48, 48, 48,
		64, 64, 48, 112, 112, 128, 128, 128, 128, 128, 128, 64,
		64, 128, 160, 192, 192, 192, 192, 192, 224, 256, 192, 192,
		192, 160, 160, 160, 160, 160, 160, 160, 160, 160, 160, 160,
		160, 160, 160, 160, 160, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
		16, 16, 16, 16, 16, 16, 16, 16, 64, 160, 256, 16,
		16, 16, 128, 64, 48, 16, 16, 16, 16, 16, 16, 256,
		16, 16, 128
	};

	/**
	 * 转速上限的取值上限：<b>不设人为封顶</b>。
	 *
	 * Create 里的转速能轻松超过 256（齿轮箱叠加、转速控制器，或者改 create-server.toml 里的
	 * kinetics.maxRotationSpeed），卡在 256 会让想把上限调高的人没辙。
	 *
	 * 用 {@code Integer.MAX_VALUE} 也是 NeoForge 明确支持的写法：它的 {@code Range#toString()}
	 * 会把这个范围渲染成「Range: > 0」（而不是「0 ~ 2147483647」），配置界面里则是一个数字输入框。
	 */
	private static final int MAX_SPEED_LIMIT = Integer.MAX_VALUE;

	private static final Map<String, ModConfigSpec.IntValue> MAX_SPEED = new LinkedHashMap<>();

	/** 「最大转速」总开关，默认关闭。 */
	private static final ModConfigSpec.BooleanValue ENABLE_MAX_SPEED;

	public static final ModConfigSpec SPEC;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		ENABLE_MAX_SPEED = builder.comment(
			"是否启用「最大转速」限制。默认 false = 不限制，所有传动轴想转多快就转多快。",
			"false 时：不会超速碎裂，TNT 轴也不会因转速爆炸（仍然可以被火/打火石点燃），物品提示里不再显示 Max Speed。",
			"true 时：下面 shaft_max_speed 分组里逐根配置的转速上限才会生效。",
			"修改后保存，重启游戏（或用配置界面重载）即生效。")
			.define("enable_shaft_max_speed", false);

		builder.comment(
			"每一根传动轴能承受的最大转速(RPM)。",
			"0 表示一有转速就散架；上限不设，Create 的转速能远超 256，想填多少填多少。",
			"仅在 enable_shaft_max_speed = true 时生效。",
			"修改后保存，重启游戏（或用配置界面重载）即生效。")
			.push("shaft_max_speed");

		for (int i = 0; i < IDS.length; i++) {
			String path = IDS[i];
			int def = DEFAULTS[i];
			MAX_SPEED.put(path,
				builder.comment("Max speed for " + path + " (default " + def + ")")
					.defineInRange(path, def, 0, MAX_SPEED_LIMIT));
		}

		builder.pop();
		SPEC = builder.build();
	}

	/**
	 * 查询某方块注册名(如 dirt_shaft)当前生效的最大转速。
	 *
	 * <p>返回 -1 表示「不限速」，有两种情况：<b>总开关没打开（默认）</b>，
	 * 或者该方块压根没有配置条目（如未来新增、未配置的方块）。
	 * 两个调用方（超速碎裂行为、物品提示）都按 -1 = 放行处理，所以总开关关掉时自然什么都不做。
	 */
	public static int maxSpeed(String blockPath) {
		ModConfigSpec.IntValue value = MAX_SPEED.get(blockPath);
		// 不是本模组的可碎轴就直接放行
		if (value == null)
			return -1;
		// 配置还没加载时也放行。本配置是 SERVER 类型，要等服务端起来才加载，
		// 而 tooltip 可能在那之前就问过来（比如标题界面上的物品），直接 .get() 会抛 IllegalStateException。
		if (!SPEC.isLoaded())
			return -1;
		if (!ENABLE_MAX_SPEED.get())
			return -1;
		return value.get();
	}
}
