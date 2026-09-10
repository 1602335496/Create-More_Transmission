package com.shengzi.more_transmission;

import com.shengzi.more_transmission.custom_block.RedstoneShaftBlock;
import com.shengzi.more_transmission.custom_block.TntShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.shengzi.more_transmission.More_transmission.REGISTRATE;


public class ModBlocks {

    public static final BlockEntry<MoreShaftBlock> DIRT_SHAFT = REGISTRATE

            .block("dirt_shaft", MoreShaftBlock::new)

            .initialProperties(() -> Blocks.DIRT)//基础物性继承泥土本体（泥土不要求正确工具）

            .properties(p -> p.noOcclusion())//细柱不是实心立方，避免相邻方块把贴着它的面误剔除(轴只有中间细柱，实心立方体会遮挡)

            .transform(shovelOnly())//挖掘标签：铲类可挖(对应泥土的 mineable/shovel)

            .blockstate(BlockStateGen.axisBlockProvider(false))//按 axis=x/y/z 生成 blockstate，false=非 customItem

            //给方块注册 Create 的旋转 BakedModel：负责按 BE 的旋转角渲染(原版轴的 spinning 渲染就靠它)
            .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))

            //合成配方：同一列竖放两块泥土 → 8 个泥土传动杆
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 8)
                        .define('X', Items.DIRT)
                        .pattern("X")
                        .pattern("X")
                        .unlockedBy("has_dirt", RegistrateRecipeProvider.has(Items.DIRT))
                        .save(p);
                // 切石机：1 泥土 → 8 泥土传动杆
                p.stonecutting(DataIngredient.items(Items.DIRT), RecipeCategory.BUILDING_BLOCKS, c::get, 8);
                // 逆向：9 泥土传动杆 → 1 泥土
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Items.DIRT)
                        .requires(c.get(), 9)
                        .unlockedBy("has_dirt_shaft", RegistrateRecipeProvider.has(c.get()))
                        .save(p, More_transmission.modLoc("dirt_from_9_dirt_shaft"));
            })

            .lang("Dirt Shaft")//显示名

            //自动注册 BlockItem；无 .tab(...) 时默认进本模组自带创造物品栏（见 More_transmission 静态块）
            .item()
            .build()

            .register();

    public static final BlockEntry<MoreShaftBlock> STONE_SHAFT = REGISTRATE

            .block("stone_shaft", MoreShaftBlock::new)

            .initialProperties(() -> Blocks.STONE)

            .properties(p -> p.mapColor(MapColor.STONE))
            .properties(p -> p.noOcclusion())

            .transform(TagGen.pickaxeOnly())

            .blockstate(BlockStateGen.axisBlockProvider(false))

            .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))

            //合成配方：同一列竖放两块石头 → 8 个石头传动杆
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 8)
                        .define('X', Blocks.STONE)
                        .pattern("X")
                        .pattern("X")
                        .unlockedBy("has_stone", RegistrateRecipeProvider.has(Blocks.STONE))
                        .save(p);
                // 切石机：1 石头 → 8 石头传动杆
                p.stonecutting(DataIngredient.items(Blocks.STONE), RecipeCategory.BUILDING_BLOCKS, c::get, 8);
                // 逆向：9 石头传动杆 → 1 石头
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE)
                        .requires(c.get(), 9)
                        .unlockedBy("has_stone_shaft", RegistrateRecipeProvider.has(c.get()))
                        .save(p, More_transmission.modLoc("stone_from_9_stone_shaft"));
            })

            .lang("Stone Shaft")

            .item()
            .build()

            .register();

    // ---- 序号 1~9：橡木/云杉/白桦 三组原木·去皮原木·木板传动杆（最大转速均为 32）----
    public static final BlockEntry<MoreShaftBlock> OAK_LOG_SHAFT = shaft("oak_log_shaft", "Oak Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_OAK_LOG_SHAFT = shaft("stripped_oak_log_shaft", "Stripped Oak Log Shaft");

    public static final BlockEntry<MoreShaftBlock> OAK_PLANKS_SHAFT = shaft("oak_planks_shaft", "Oak Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> SPRUCE_LOG_SHAFT = shaft("spruce_log_shaft", "Spruce Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_SPRUCE_LOG_SHAFT = shaft("stripped_spruce_log_shaft", "Stripped Spruce Log Shaft");

    public static final BlockEntry<MoreShaftBlock> SPRUCE_PLANKS_SHAFT = shaft("spruce_planks_shaft", "Spruce Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> BIRCH_LOG_SHAFT = shaft("birch_log_shaft", "Birch Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_BIRCH_LOG_SHAFT = shaft("stripped_birch_log_shaft", "Stripped Birch Log Shaft");

    public static final BlockEntry<MoreShaftBlock> BIRCH_PLANKS_SHAFT = shaft("birch_planks_shaft", "Birch Planks Shaft");

    /**
     * 各材质传动杆的注册脚手架：基础物性完全继承「注册名去掉 _shaft」对应的原版方块
     * （硬度/音效/地图色/是否要求正确工具都照搬 ofFullCopy），挖掘标签按该材质工具类别加。
     * 手工合成 / 切石机 / 逆向 9 合 1 三条配方也统一在此生成。
     */
    private static BlockEntry<MoreShaftBlock> shaft(String id, String display) {
        Block material = materialOf(id);
        String mat = id.substring(0, id.length() - "_shaft".length());
        return REGISTRATE
                .block(id, MoreShaftBlock::new)
                .initialProperties(() -> material)
                .properties(p -> p.noOcclusion())
                .transform(mineableOf(id))
                .blockstate(BlockStateGen.axisBlockProvider(false))
                .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                // 掉落：玻璃类仿原版玻璃——普通破坏不掉，Silk Touch 才掉自身；其余轴默认掉落自身
                .loot((t, block) -> {
                    if (isGlassMaterial(mat))
                        t.dropWhenSilkTouch(block);
                    else
                        t.dropSelf(block);
                })
                // 配方：同列两块「该材质方块」→8 根；切石机 1 块→8 根；逆向 9 根→1 块
                .recipe((c, p) -> {
                    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 8)
                        .define('X', material)
                        .pattern("X")
                        .pattern("X")
                        .unlockedBy("has_" + id, RegistrateRecipeProvider.has(material))
                        .save(p);
                    p.stonecutting(DataIngredient.items(material), RecipeCategory.BUILDING_BLOCKS, c::get, 8);
                    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, material)
                        .requires(c.get(), 9)
                        .unlockedBy("has_" + id, RegistrateRecipeProvider.has(c.get()))
                        .save(p, More_transmission.modLoc(BuiltInRegistries.BLOCK.getKey(material).getPath() + "_from_9_" + id));
                })
                .lang(display)
                .item()
                .build()
                .register();
    }

    /** 由轴注册名反查其材质方块（= 注册名去掉 _shaft 的原版方块）。 */
    private static Block materialOf(String id) {
        return BuiltInRegistries.BLOCK
            .get(ResourceLocation.withDefaultNamespace(id.substring(0, id.length() - "_shaft".length())));
    }

    /** 木质/植物类（原木/木板/菌柄/竹块）→ 斧头类挖掘。 */
    private static boolean isWoodMaterial(String mat) {
        return mat.endsWith("_log") || mat.endsWith("_planks") || mat.endsWith("_stem")
            || mat.equals("bamboo_block") || mat.equals("stripped_bamboo_block");
    }

    /** 松软土石（泥土/沙/沙砾/黏土/灵魂沙/雪块）→ 铲类挖掘。 */
    private static boolean isSoftMaterial(String mat) {
        return mat.equals("dirt") || mat.equals("sand") || mat.equals("red_sand") || mat.equals("gravel")
            || mat.equals("clay") || mat.equals("soul_sand") || mat.equals("snow_block");
    }

    /** 羊毛 → 不走 mineable；仿原版羊毛挂 #wool + #sword_efficient（剪刀 5× / 剑 1.5× 加速）。 */
    private static boolean isWoolMaterial(String mat) {
        return mat.endsWith("_wool");
    }

    /** 玻璃（含遮光/染色玻璃）→ 不走 mineable：仿原版玻璃，任意工具都能破坏但不掉落。 */
    private static boolean isGlassMaterial(String mat) {
        return mat.endsWith("glass");
    }

    /** TNT → 不走 mineable：仿原版 TNT（instabreak 瞬破、任意工具都行）。 */
    private static boolean isTntMaterial(String mat) {
        return mat.equals("tnt");
    }

    /** 铲类挖掘标签（等价 mineable/shovel）。 */
    private static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> shovelOnly() {
        return b -> b.tag(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    /**
     * 按材质挑挖掘标签：玻璃→不加 mineable、木质→axe、松软→shovel、羊毛→wool+sword_efficient（不用镐）、其余→pickaxe。
     * 是否真的"必须用正确工具才能掉落"由 {@code initialProperties} 复制的材质方块属性决定
     * （石头等原版需要镐的才有此要求，原木/玻璃/羊毛/泥土等原版没有的也不会强加）。
     */
    private static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> mineableOf(String id) {
        String mat = id.substring(0, id.length() - "_shaft".length());
        if (isGlassMaterial(mat) || isTntMaterial(mat))
            return b -> b;
        if (isWoolMaterial(mat))
            return b -> b.tag(BlockTags.WOOL).tag(BlockTags.SWORD_EFFICIENT);
        if (isWoodMaterial(mat))
            return TagGen.axeOnly();
        if (isSoftMaterial(mat))
            return shovelOnly();
        return TagGen.pickaxeOnly();
    }


    public static final BlockEntry<MoreShaftBlock> JUNGLE_LOG_SHAFT = shaft("jungle_log_shaft", "Jungle Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_JUNGLE_LOG_SHAFT = shaft("stripped_jungle_log_shaft", "Stripped Jungle Log Shaft");

    public static final BlockEntry<MoreShaftBlock> JUNGLE_PLANKS_SHAFT = shaft("jungle_planks_shaft", "Jungle Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> ACACIA_LOG_SHAFT = shaft("acacia_log_shaft", "Acacia Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_ACACIA_LOG_SHAFT = shaft("stripped_acacia_log_shaft", "Stripped Acacia Log Shaft");

    public static final BlockEntry<MoreShaftBlock> ACACIA_PLANKS_SHAFT = shaft("acacia_planks_shaft", "Acacia Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> DARK_OAK_LOG_SHAFT = shaft("dark_oak_log_shaft", "Dark Oak Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_DARK_OAK_LOG_SHAFT = shaft("stripped_dark_oak_log_shaft", "Stripped Dark Oak Log Shaft");

    public static final BlockEntry<MoreShaftBlock> DARK_OAK_PLANKS_SHAFT = shaft("dark_oak_planks_shaft", "Dark Oak Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> MANGROVE_LOG_SHAFT = shaft("mangrove_log_shaft", "Mangrove Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_MANGROVE_LOG_SHAFT = shaft("stripped_mangrove_log_shaft", "Stripped Mangrove Log Shaft");

    public static final BlockEntry<MoreShaftBlock> MANGROVE_PLANKS_SHAFT = shaft("mangrove_planks_shaft", "Mangrove Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> CHERRY_LOG_SHAFT = shaft("cherry_log_shaft", "Cherry Log Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_CHERRY_LOG_SHAFT = shaft("stripped_cherry_log_shaft", "Stripped Cherry Log Shaft");

    public static final BlockEntry<MoreShaftBlock> CHERRY_PLANKS_SHAFT = shaft("cherry_planks_shaft", "Cherry Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> BAMBOO_BLOCK_SHAFT = shaft("bamboo_block_shaft", "Bamboo Block Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_BAMBOO_BLOCK_SHAFT = shaft("stripped_bamboo_block_shaft", "Stripped Bamboo Block Shaft");

    public static final BlockEntry<MoreShaftBlock> BAMBOO_PLANKS_SHAFT = shaft("bamboo_planks_shaft", "Bamboo Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> CRIMSON_STEM_SHAFT = shaft("crimson_stem_shaft", "Crimson Stem Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_CRIMSON_STEM_SHAFT = shaft("stripped_crimson_stem_shaft", "Stripped Crimson Stem Shaft");

    public static final BlockEntry<MoreShaftBlock> CRIMSON_PLANKS_SHAFT = shaft("crimson_planks_shaft", "Crimson Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> WARPED_STEM_SHAFT = shaft("warped_stem_shaft", "Warped Stem Shaft");

    public static final BlockEntry<MoreShaftBlock> STRIPPED_WARPED_STEM_SHAFT = shaft("stripped_warped_stem_shaft", "Stripped Warped Stem Shaft");

    public static final BlockEntry<MoreShaftBlock> WARPED_PLANKS_SHAFT = shaft("warped_planks_shaft", "Warped Planks Shaft");

    public static final BlockEntry<MoreShaftBlock> COBBLESTONE_SHAFT = shaft("cobblestone_shaft", "Cobblestone Shaft");

    public static final BlockEntry<MoreShaftBlock> MOSSY_COBBLESTONE_SHAFT = shaft("mossy_cobblestone_shaft", "Mossy Cobblestone Shaft");

    public static final BlockEntry<MoreShaftBlock> SMOOTH_STONE_SHAFT = shaft("smooth_stone_shaft", "Smooth Stone Shaft");

    public static final BlockEntry<MoreShaftBlock> STONE_BRICKS_SHAFT = shaft("stone_bricks_shaft", "Stone Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> MOSSY_STONE_BRICKS_SHAFT = shaft("mossy_stone_bricks_shaft", "Mossy Stone Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> GRANITE_SHAFT = shaft("granite_shaft", "Granite Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_GRANITE_SHAFT = shaft("polished_granite_shaft", "Polished Granite Shaft");

    public static final BlockEntry<MoreShaftBlock> DIORITE_SHAFT = shaft("diorite_shaft", "Diorite Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_DIORITE_SHAFT = shaft("polished_diorite_shaft", "Polished Diorite Shaft");

    public static final BlockEntry<MoreShaftBlock> ANDESITE_SHAFT = shaft("andesite_shaft", "Andesite Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_ANDESITE_SHAFT = shaft("polished_andesite_shaft", "Polished Andesite Shaft");

    public static final BlockEntry<MoreShaftBlock> DEEPSLATE_SHAFT = shaft("deepslate_shaft", "Deepslate Shaft");

    public static final BlockEntry<MoreShaftBlock> COBBLED_DEEPSLATE_SHAFT = shaft("cobbled_deepslate_shaft", "Cobbled Deepslate Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_DEEPSLATE_SHAFT = shaft("polished_deepslate_shaft", "Polished Deepslate Shaft");

    public static final BlockEntry<MoreShaftBlock> DEEPSLATE_BRICKS_SHAFT = shaft("deepslate_bricks_shaft", "Deepslate Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> TUFF_SHAFT = shaft("tuff_shaft", "Tuff Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_TUFF_SHAFT = shaft("polished_tuff_shaft", "Polished Tuff Shaft");

    public static final BlockEntry<MoreShaftBlock> TUFF_BRICKS_SHAFT = shaft("tuff_bricks_shaft", "Tuff Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> BRICKS_SHAFT = shaft("bricks_shaft", "Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> PACKED_MUD_SHAFT = shaft("packed_mud_shaft", "Packed Mud Shaft");

    public static final BlockEntry<MoreShaftBlock> MUD_BRICKS_SHAFT = shaft("mud_bricks_shaft", "Mud Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> SANDSTONE_SHAFT = shaft("sandstone_shaft", "Sandstone Shaft");

    public static final BlockEntry<MoreShaftBlock> SMOOTH_SANDSTONE_SHAFT = shaft("smooth_sandstone_shaft", "Smooth Sandstone Shaft");

    public static final BlockEntry<MoreShaftBlock> RED_SANDSTONE_SHAFT = shaft("red_sandstone_shaft", "Red Sandstone Shaft");

    public static final BlockEntry<MoreShaftBlock> SMOOTH_RED_SANDSTONE_SHAFT = shaft("smooth_red_sandstone_shaft", "Smooth Red Sandstone Shaft");

    public static final BlockEntry<MoreShaftBlock> SEA_LANTERN_SHAFT = shaft("sea_lantern_shaft", "Sea Lantern Shaft");

    public static final BlockEntry<MoreShaftBlock> PRISMARINE_SHAFT = shaft("prismarine_shaft", "Prismarine Shaft");

    public static final BlockEntry<MoreShaftBlock> DARK_PRISMARINE_SHAFT = shaft("dark_prismarine_shaft", "Dark Prismarine Shaft");

    public static final BlockEntry<MoreShaftBlock> NETHERRACK_SHAFT = shaft("netherrack_shaft", "Netherrack Shaft");

    public static final BlockEntry<MoreShaftBlock> NETHER_BRICKS_SHAFT = shaft("nether_bricks_shaft", "Nether Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> RED_NETHER_BRICKS_SHAFT = shaft("red_nether_bricks_shaft", "Red Nether Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> BASALT_SHAFT = shaft("basalt_shaft", "Basalt Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_BASALT_SHAFT = shaft("polished_basalt_shaft", "Polished Basalt Shaft");

    public static final BlockEntry<MoreShaftBlock> BLACKSTONE_SHAFT = shaft("blackstone_shaft", "Blackstone Shaft");

    public static final BlockEntry<MoreShaftBlock> GILDED_BLACKSTONE_SHAFT = shaft("gilded_blackstone_shaft", "Gilded Blackstone Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_BLACKSTONE_SHAFT = shaft("polished_blackstone_shaft", "Polished Blackstone Shaft");

    public static final BlockEntry<MoreShaftBlock> POLISHED_BLACKSTONE_BRICKS_SHAFT = shaft("polished_blackstone_bricks_shaft", "Polished Blackstone Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> END_STONE_SHAFT = shaft("end_stone_shaft", "End Stone Shaft");

    public static final BlockEntry<MoreShaftBlock> END_STONE_BRICKS_SHAFT = shaft("end_stone_bricks_shaft", "End Stone Bricks Shaft");

    public static final BlockEntry<MoreShaftBlock> PURPUR_BLOCK_SHAFT = shaft("purpur_block_shaft", "Purpur Block Shaft");

    public static final BlockEntry<MoreShaftBlock> COAL_BLOCK_SHAFT = shaft("coal_block_shaft", "Coal Block Shaft");

    public static final BlockEntry<MoreShaftBlock> IRON_BLOCK_SHAFT = shaft("iron_block_shaft", "Iron Block Shaft");

    public static final BlockEntry<MoreShaftBlock> GOLD_BLOCK_SHAFT = shaft("gold_block_shaft", "Gold Block Shaft");

    public static final BlockEntry<RedstoneShaftBlock> REDSTONE_BLOCK_SHAFT = REGISTRATE

            .block("redstone_block_shaft", RedstoneShaftBlock::new)

            .initialProperties(() -> Blocks.REDSTONE_BLOCK)

            .properties(p -> p.noOcclusion())

            .transform(TagGen.pickaxeOnly())

            .blockstate(BlockStateGen.axisBlockProvider(false))

            .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))

            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 8)
                        .define('X', Blocks.REDSTONE_BLOCK)
                        .pattern("X")
                        .pattern("X")
                        .unlockedBy("has_redstone_block_shaft", RegistrateRecipeProvider.has(Blocks.REDSTONE_BLOCK))
                        .save(p);
                p.stonecutting(DataIngredient.items(Blocks.REDSTONE_BLOCK), RecipeCategory.BUILDING_BLOCKS, c::get, 8);
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.REDSTONE_BLOCK)
                        .requires(c.get(), 9)
                        .unlockedBy("has_redstone_block_shaft", RegistrateRecipeProvider.has(c.get()))
                        .save(p, More_transmission.modLoc("redstone_block_from_9_redstone_block_shaft"));
            })

            .lang("Redstone Block Shaft")

            .item()
            .build()

            .register();

    public static final BlockEntry<MoreShaftBlock> EMERALD_BLOCK_SHAFT = shaft("emerald_block_shaft", "Emerald Block Shaft");

    public static final BlockEntry<MoreShaftBlock> LAPIS_BLOCK_SHAFT = shaft("lapis_block_shaft", "Lapis Block Shaft");

    public static final BlockEntry<MoreShaftBlock> DIAMOND_BLOCK_SHAFT = shaft("diamond_block_shaft", "Diamond Block Shaft");

    public static final BlockEntry<MoreShaftBlock> NETHERITE_BLOCK_SHAFT = shaft("netherite_block_shaft", "Netherite Block Shaft");

    public static final BlockEntry<MoreShaftBlock> QUARTZ_BLOCK_SHAFT = shaft("quartz_block_shaft", "Quartz Block Shaft");

    public static final BlockEntry<MoreShaftBlock> QUARTZ_PILLAR_SHAFT = shaft("quartz_pillar_shaft", "Quartz Pillar Shaft");

    public static final BlockEntry<MoreShaftBlock> AMETHYST_BLOCK_SHAFT = shaft("amethyst_block_shaft", "Amethyst Block Shaft");

    public static final BlockEntry<MoreShaftBlock> COPPER_BLOCK_SHAFT = shaft("copper_block_shaft", "Copper Block Shaft");

    public static final BlockEntry<MoreShaftBlock> CUT_COPPER_SHAFT = shaft("cut_copper_shaft", "Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WHITE_WOOL_SHAFT = shaft("white_wool_shaft", "White Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> LIGHT_GRAY_WOOL_SHAFT = shaft("light_gray_wool_shaft", "Light Gray Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> GRAY_WOOL_SHAFT = shaft("gray_wool_shaft", "Gray Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> BLACK_WOOL_SHAFT = shaft("black_wool_shaft", "Black Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> BROWN_WOOL_SHAFT = shaft("brown_wool_shaft", "Brown Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> RED_WOOL_SHAFT = shaft("red_wool_shaft", "Red Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> ORANGE_WOOL_SHAFT = shaft("orange_wool_shaft", "Orange Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> YELLOW_WOOL_SHAFT = shaft("yellow_wool_shaft", "Yellow Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> LIME_WOOL_SHAFT = shaft("lime_wool_shaft", "Lime Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> GREEN_WOOL_SHAFT = shaft("green_wool_shaft", "Green Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> CYAN_WOOL_SHAFT = shaft("cyan_wool_shaft", "Cyan Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> LIGHT_BLUE_WOOL_SHAFT = shaft("light_blue_wool_shaft", "Light Blue Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> BLUE_WOOL_SHAFT = shaft("blue_wool_shaft", "Blue Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> PURPLE_WOOL_SHAFT = shaft("purple_wool_shaft", "Purple Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> MAGENTA_WOOL_SHAFT = shaft("magenta_wool_shaft", "Magenta Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> PINK_WOOL_SHAFT = shaft("pink_wool_shaft", "Pink Wool Shaft");

    public static final BlockEntry<MoreShaftBlock> GLASS_SHAFT = shaft("glass_shaft", "Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> TINTED_GLASS_SHAFT = shaft("tinted_glass_shaft", "Tinted Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> WHITE_STAINED_GLASS_SHAFT = shaft("white_stained_glass_shaft", "White Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> LIGHT_GRAY_STAINED_GLASS_SHAFT = shaft("light_gray_stained_glass_shaft", "Light Gray Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> GRAY_STAINED_GLASS_SHAFT = shaft("gray_stained_glass_shaft", "Gray Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> BLACK_STAINED_GLASS_SHAFT = shaft("black_stained_glass_shaft", "Black Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> BROWN_STAINED_GLASS_SHAFT = shaft("brown_stained_glass_shaft", "Brown Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> RED_STAINED_GLASS_SHAFT = shaft("red_stained_glass_shaft", "Red Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> ORANGE_STAINED_GLASS_SHAFT = shaft("orange_stained_glass_shaft", "Orange Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> YELLOW_STAINED_GLASS_SHAFT = shaft("yellow_stained_glass_shaft", "Yellow Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> LIME_STAINED_GLASS_SHAFT = shaft("lime_stained_glass_shaft", "Lime Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> GREEN_STAINED_GLASS_SHAFT = shaft("green_stained_glass_shaft", "Green Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> CYAN_STAINED_GLASS_SHAFT = shaft("cyan_stained_glass_shaft", "Cyan Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> LIGHT_BLUE_STAINED_GLASS_SHAFT = shaft("light_blue_stained_glass_shaft", "Light Blue Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> BLUE_STAINED_GLASS_SHAFT = shaft("blue_stained_glass_shaft", "Blue Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> PURPLE_STAINED_GLASS_SHAFT = shaft("purple_stained_glass_shaft", "Purple Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> MAGENTA_STAINED_GLASS_SHAFT = shaft("magenta_stained_glass_shaft", "Magenta Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> PINK_STAINED_GLASS_SHAFT = shaft("pink_stained_glass_shaft", "Pink Stained Glass Shaft");

    public static final BlockEntry<MoreShaftBlock> CLAY_SHAFT = shaft("clay_shaft", "Clay Shaft");

    public static final BlockEntry<MoreShaftBlock> GRAVEL_SHAFT = shaft("gravel_shaft", "Gravel Shaft");

    public static final BlockEntry<MoreShaftBlock> ICE_SHAFT = shaft("ice_shaft", "Ice Shaft");

    public static final BlockEntry<MoreShaftBlock> SNOW_BLOCK_SHAFT = shaft("snow_block_shaft", "Snow Block Shaft");

    public static final BlockEntry<MoreShaftBlock> MOSS_BLOCK_SHAFT = shaft("moss_block_shaft", "Moss Block Shaft");

    public static final BlockEntry<MoreShaftBlock> CALCITE_SHAFT = shaft("calcite_shaft", "Calcite Shaft");

    public static final BlockEntry<MoreShaftBlock> MAGMA_BLOCK_SHAFT = shaft("magma_block_shaft", "Magma Block Shaft");

    public static final BlockEntry<MoreShaftBlock> OBSIDIAN_SHAFT = shaft("obsidian_shaft", "Obsidian Shaft");

    public static final BlockEntry<MoreShaftBlock> SOUL_SAND_SHAFT = shaft("soul_sand_shaft", "Soul Sand Shaft");

    public static final BlockEntry<MoreShaftBlock> SAND_SHAFT = shaft("sand_shaft", "Sand Shaft");

    public static final BlockEntry<MoreShaftBlock> RED_SAND_SHAFT = shaft("red_sand_shaft", "Red Sand Shaft");

    public static final BlockEntry<MoreShaftBlock> DRIPSTONE_BLOCK_SHAFT = shaft("dripstone_block_shaft", "Dripstone Block Shaft");

    public static final BlockEntry<MoreShaftBlock> BONE_BLOCK_SHAFT = shaft("bone_block_shaft", "Bone Block Shaft");

    public static final BlockEntry<MoreShaftBlock> GLOWSTONE_SHAFT = shaft("glowstone_shaft", "Glowstone Shaft");

    public static final BlockEntry<MoreShaftBlock> SPONGE_SHAFT = shaft("sponge_shaft", "Sponge Shaft");

    public static final BlockEntry<MoreShaftBlock> HAY_BLOCK_SHAFT = shaft("hay_block_shaft", "Hay Block Shaft");

    public static final BlockEntry<MoreShaftBlock> HONEYCOMB_BLOCK_SHAFT = shaft("honeycomb_block_shaft", "Honeycomb Block Shaft");

    public static final BlockEntry<MoreShaftBlock> SLIME_BLOCK_SHAFT = shaft("slime_block_shaft", "Slime Block Shaft");

    public static final BlockEntry<MoreShaftBlock> HONEY_BLOCK_SHAFT = shaft("honey_block_shaft", "Honey Block Shaft");

    public static final BlockEntry<MoreShaftBlock> SCULK_SHAFT = shaft("sculk_shaft", "Sculk Shaft");

    public static final BlockEntry<MoreShaftBlock> BEDROCK_SHAFT = shaft("bedrock_shaft", "Bedrock Shaft");

    public static final BlockEntry<TntShaftBlock> TNT_SHAFT = REGISTRATE

            .block("tnt_shaft", TntShaftBlock::new)

            .initialProperties(() -> Blocks.TNT)

            .properties(BlockBehaviour.Properties::noOcclusion)

            .blockstate(BlockStateGen.axisBlockProvider(false))

            .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))

            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 8)
                        .define('X', Blocks.TNT)
                        .pattern("X")
                        .pattern("X")
                        .unlockedBy("has_tnt_shaft", RegistrateRecipeProvider.has(Blocks.TNT))
                        .save(p);
                p.stonecutting(DataIngredient.items(Blocks.TNT), RecipeCategory.BUILDING_BLOCKS, c::get, 8);
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Blocks.TNT)
                        .requires(c.get(), 9)
                        .unlockedBy("has_tnt_shaft", RegistrateRecipeProvider.has(c.get()))
                        .save(p, More_transmission.modLoc("tnt_from_9_tnt_shaft"));
            })

            .lang("TNT Shaft")

            .item()
            .build()

            .register();

    public static void register() {
    }

}
