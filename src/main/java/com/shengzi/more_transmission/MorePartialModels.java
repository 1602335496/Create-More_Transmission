package com.shengzi.more_transmission;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class MorePartialModels {
    /**
     * 旋转渲染用的 partial，直接复用方块静态模型 block/dirt_shaft（Create 的
     * AllPartialModels.SHAFT 也是这么干的）。改贴图只需改那一个 JSON。
     */
    public static final PartialModel DIRT_SHAFT = PartialModel.of(More_transmission.modLoc("block/dirt_shaft"));

    /** 石轴的旋转 partial，复用方块静态模型 block/stone_shaft。 */
    public static final PartialModel STONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/stone_shaft"));

    // 序号 1~9：橡木/云杉/白桦 原木·去皮原木·木板轴的旋转 partial（同样复用各自方块静态模型）
    public static final PartialModel OAK_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/oak_log_shaft"));

    public static final PartialModel STRIPPED_OAK_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_oak_log_shaft"));

    public static final PartialModel OAK_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/oak_planks_shaft"));

    public static final PartialModel SPRUCE_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/spruce_log_shaft"));

    public static final PartialModel STRIPPED_SPRUCE_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_spruce_log_shaft"));

    public static final PartialModel SPRUCE_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/spruce_planks_shaft"));

    public static final PartialModel BIRCH_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/birch_log_shaft"));

    public static final PartialModel STRIPPED_BIRCH_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_birch_log_shaft"));

    public static final PartialModel BIRCH_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/birch_planks_shaft"));

    /** 空方法，仅用于强制触发本类的静态初始化。 */

    public static final PartialModel JUNGLE_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/jungle_log_shaft"));

    public static final PartialModel STRIPPED_JUNGLE_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_jungle_log_shaft"));

    public static final PartialModel JUNGLE_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/jungle_planks_shaft"));

    public static final PartialModel ACACIA_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/acacia_log_shaft"));

    public static final PartialModel STRIPPED_ACACIA_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_acacia_log_shaft"));

    public static final PartialModel ACACIA_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/acacia_planks_shaft"));

    public static final PartialModel DARK_OAK_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/dark_oak_log_shaft"));

    public static final PartialModel STRIPPED_DARK_OAK_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_dark_oak_log_shaft"));

    public static final PartialModel DARK_OAK_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/dark_oak_planks_shaft"));

    public static final PartialModel MANGROVE_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/mangrove_log_shaft"));

    public static final PartialModel STRIPPED_MANGROVE_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_mangrove_log_shaft"));

    public static final PartialModel MANGROVE_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/mangrove_planks_shaft"));

    public static final PartialModel CHERRY_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/cherry_log_shaft"));

    public static final PartialModel STRIPPED_CHERRY_LOG_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_cherry_log_shaft"));

    public static final PartialModel CHERRY_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/cherry_planks_shaft"));

    public static final PartialModel BAMBOO_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/bamboo_block_shaft"));

    public static final PartialModel STRIPPED_BAMBOO_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_bamboo_block_shaft"));

    public static final PartialModel BAMBOO_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/bamboo_planks_shaft"));

    public static final PartialModel CRIMSON_STEM_SHAFT = PartialModel.of(More_transmission.modLoc("block/crimson_stem_shaft"));

    public static final PartialModel STRIPPED_CRIMSON_STEM_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_crimson_stem_shaft"));

    public static final PartialModel CRIMSON_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/crimson_planks_shaft"));

    public static final PartialModel WARPED_STEM_SHAFT = PartialModel.of(More_transmission.modLoc("block/warped_stem_shaft"));

    public static final PartialModel STRIPPED_WARPED_STEM_SHAFT = PartialModel.of(More_transmission.modLoc("block/stripped_warped_stem_shaft"));

    public static final PartialModel WARPED_PLANKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/warped_planks_shaft"));

    public static final PartialModel COBBLESTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/cobblestone_shaft"));

    public static final PartialModel MOSSY_COBBLESTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/mossy_cobblestone_shaft"));

    public static final PartialModel SMOOTH_STONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/smooth_stone_shaft"));

    public static final PartialModel STONE_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/stone_bricks_shaft"));

    public static final PartialModel MOSSY_STONE_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/mossy_stone_bricks_shaft"));

    public static final PartialModel GRANITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/granite_shaft"));

    public static final PartialModel POLISHED_GRANITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_granite_shaft"));

    public static final PartialModel DIORITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/diorite_shaft"));

    public static final PartialModel POLISHED_DIORITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_diorite_shaft"));

    public static final PartialModel ANDESITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/andesite_shaft"));

    public static final PartialModel POLISHED_ANDESITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_andesite_shaft"));

    public static final PartialModel DEEPSLATE_SHAFT = PartialModel.of(More_transmission.modLoc("block/deepslate_shaft"));

    public static final PartialModel COBBLED_DEEPSLATE_SHAFT = PartialModel.of(More_transmission.modLoc("block/cobbled_deepslate_shaft"));

    public static final PartialModel POLISHED_DEEPSLATE_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_deepslate_shaft"));

    public static final PartialModel DEEPSLATE_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/deepslate_bricks_shaft"));

    public static final PartialModel TUFF_SHAFT = PartialModel.of(More_transmission.modLoc("block/tuff_shaft"));

    public static final PartialModel POLISHED_TUFF_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_tuff_shaft"));

    public static final PartialModel TUFF_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/tuff_bricks_shaft"));

    public static final PartialModel BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/bricks_shaft"));

    public static final PartialModel PACKED_MUD_SHAFT = PartialModel.of(More_transmission.modLoc("block/packed_mud_shaft"));

    public static final PartialModel MUD_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/mud_bricks_shaft"));

    public static final PartialModel SANDSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/sandstone_shaft"));

    public static final PartialModel SMOOTH_SANDSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/smooth_sandstone_shaft"));

    public static final PartialModel RED_SANDSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/red_sandstone_shaft"));

    public static final PartialModel SMOOTH_RED_SANDSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/smooth_red_sandstone_shaft"));

    public static final PartialModel SEA_LANTERN_SHAFT = PartialModel.of(More_transmission.modLoc("block/sea_lantern_shaft"));

    public static final PartialModel PRISMARINE_SHAFT = PartialModel.of(More_transmission.modLoc("block/prismarine_shaft"));

    public static final PartialModel DARK_PRISMARINE_SHAFT = PartialModel.of(More_transmission.modLoc("block/dark_prismarine_shaft"));

    public static final PartialModel NETHERRACK_SHAFT = PartialModel.of(More_transmission.modLoc("block/netherrack_shaft"));

    public static final PartialModel NETHER_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/nether_bricks_shaft"));

    public static final PartialModel RED_NETHER_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/red_nether_bricks_shaft"));

    public static final PartialModel BASALT_SHAFT = PartialModel.of(More_transmission.modLoc("block/basalt_shaft"));

    public static final PartialModel POLISHED_BASALT_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_basalt_shaft"));

    public static final PartialModel BLACKSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/blackstone_shaft"));

    public static final PartialModel GILDED_BLACKSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/gilded_blackstone_shaft"));

    public static final PartialModel POLISHED_BLACKSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_blackstone_shaft"));

    public static final PartialModel POLISHED_BLACKSTONE_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/polished_blackstone_bricks_shaft"));

    public static final PartialModel END_STONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/end_stone_shaft"));

    public static final PartialModel END_STONE_BRICKS_SHAFT = PartialModel.of(More_transmission.modLoc("block/end_stone_bricks_shaft"));

    public static final PartialModel PURPUR_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/purpur_block_shaft"));

    public static final PartialModel COAL_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/coal_block_shaft"));

    public static final PartialModel IRON_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/iron_block_shaft"));

    public static final PartialModel GOLD_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/gold_block_shaft"));

    public static final PartialModel REDSTONE_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/redstone_block_shaft"));

    public static final PartialModel EMERALD_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/emerald_block_shaft"));

    public static final PartialModel LAPIS_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/lapis_block_shaft"));

    public static final PartialModel DIAMOND_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/diamond_block_shaft"));

    public static final PartialModel NETHERITE_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/netherite_block_shaft"));

    public static final PartialModel QUARTZ_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/quartz_block_shaft"));

    public static final PartialModel QUARTZ_PILLAR_SHAFT = PartialModel.of(More_transmission.modLoc("block/quartz_pillar_shaft"));

    public static final PartialModel AMETHYST_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/amethyst_block_shaft"));

    public static final PartialModel COPPER_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/copper_block_shaft"));

    public static final PartialModel CUT_COPPER_SHAFT = PartialModel.of(More_transmission.modLoc("block/cut_copper_shaft"));

    public static final PartialModel WHITE_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/white_wool_shaft"));

    public static final PartialModel LIGHT_GRAY_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/light_gray_wool_shaft"));

    public static final PartialModel GRAY_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/gray_wool_shaft"));

    public static final PartialModel BLACK_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/black_wool_shaft"));

    public static final PartialModel BROWN_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/brown_wool_shaft"));

    public static final PartialModel RED_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/red_wool_shaft"));

    public static final PartialModel ORANGE_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/orange_wool_shaft"));

    public static final PartialModel YELLOW_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/yellow_wool_shaft"));

    public static final PartialModel LIME_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/lime_wool_shaft"));

    public static final PartialModel GREEN_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/green_wool_shaft"));

    public static final PartialModel CYAN_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/cyan_wool_shaft"));

    public static final PartialModel LIGHT_BLUE_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/light_blue_wool_shaft"));

    public static final PartialModel BLUE_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/blue_wool_shaft"));

    public static final PartialModel PURPLE_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/purple_wool_shaft"));

    public static final PartialModel MAGENTA_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/magenta_wool_shaft"));

    public static final PartialModel PINK_WOOL_SHAFT = PartialModel.of(More_transmission.modLoc("block/pink_wool_shaft"));

    public static final PartialModel GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/glass_shaft"));

    public static final PartialModel TINTED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/tinted_glass_shaft"));

    public static final PartialModel WHITE_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/white_stained_glass_shaft"));

    public static final PartialModel LIGHT_GRAY_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/light_gray_stained_glass_shaft"));

    public static final PartialModel GRAY_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/gray_stained_glass_shaft"));

    public static final PartialModel BLACK_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/black_stained_glass_shaft"));

    public static final PartialModel BROWN_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/brown_stained_glass_shaft"));

    public static final PartialModel RED_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/red_stained_glass_shaft"));

    public static final PartialModel ORANGE_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/orange_stained_glass_shaft"));

    public static final PartialModel YELLOW_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/yellow_stained_glass_shaft"));

    public static final PartialModel LIME_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/lime_stained_glass_shaft"));

    public static final PartialModel GREEN_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/green_stained_glass_shaft"));

    public static final PartialModel CYAN_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/cyan_stained_glass_shaft"));

    public static final PartialModel LIGHT_BLUE_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/light_blue_stained_glass_shaft"));

    public static final PartialModel BLUE_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/blue_stained_glass_shaft"));

    public static final PartialModel PURPLE_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/purple_stained_glass_shaft"));

    public static final PartialModel MAGENTA_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/magenta_stained_glass_shaft"));

    public static final PartialModel PINK_STAINED_GLASS_SHAFT = PartialModel.of(More_transmission.modLoc("block/pink_stained_glass_shaft"));

    public static final PartialModel CLAY_SHAFT = PartialModel.of(More_transmission.modLoc("block/clay_shaft"));

    public static final PartialModel GRAVEL_SHAFT = PartialModel.of(More_transmission.modLoc("block/gravel_shaft"));

    public static final PartialModel ICE_SHAFT = PartialModel.of(More_transmission.modLoc("block/ice_shaft"));

    public static final PartialModel SNOW_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/snow_block_shaft"));

    public static final PartialModel MOSS_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/moss_block_shaft"));

    public static final PartialModel CALCITE_SHAFT = PartialModel.of(More_transmission.modLoc("block/calcite_shaft"));

    public static final PartialModel MAGMA_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/magma_block_shaft"));

    public static final PartialModel OBSIDIAN_SHAFT = PartialModel.of(More_transmission.modLoc("block/obsidian_shaft"));

    public static final PartialModel SOUL_SAND_SHAFT = PartialModel.of(More_transmission.modLoc("block/soul_sand_shaft"));

    public static final PartialModel SAND_SHAFT = PartialModel.of(More_transmission.modLoc("block/sand_shaft"));

    public static final PartialModel RED_SAND_SHAFT = PartialModel.of(More_transmission.modLoc("block/red_sand_shaft"));

    public static final PartialModel DRIPSTONE_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/dripstone_block_shaft"));

    public static final PartialModel BONE_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/bone_block_shaft"));

    public static final PartialModel GLOWSTONE_SHAFT = PartialModel.of(More_transmission.modLoc("block/glowstone_shaft"));

    public static final PartialModel SPONGE_SHAFT = PartialModel.of(More_transmission.modLoc("block/sponge_shaft"));

    public static final PartialModel HAY_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/hay_block_shaft"));

    public static final PartialModel HONEYCOMB_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/honeycomb_block_shaft"));

    public static final PartialModel SLIME_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/slime_block_shaft"));

    public static final PartialModel HONEY_BLOCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/honey_block_shaft"));

    public static final PartialModel SCULK_SHAFT = PartialModel.of(More_transmission.modLoc("block/sculk_shaft"));

    public static final PartialModel BEDROCK_SHAFT = PartialModel.of(More_transmission.modLoc("block/bedrock_shaft"));

    public static final PartialModel TNT_SHAFT = PartialModel.of(More_transmission.modLoc("block/tnt_shaft"));

    public static void init() {
    }
}
