package com.shengzi.more_transmission;

import java.util.function.Supplier;

import com.shengzi.more_transmission.custom_block.LampShaftBlock;
import com.shengzi.more_transmission.custom_block.RedstoneShaftBlock;
import com.shengzi.more_transmission.custom_block.TntShaftBlock;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.neoforge.client.model.generators.ModelFile;

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

    // ---- 铜的氧化 / 涂蜡变种（14 根），默认最大转速与铜块传动杆一致 = 160 ----
    // 命名沿用「注册名 = 原版方块 id + _shaft」，所以下面 shaft() 助手能自动找到对应材质方块，
    // 物性、挖掘标签、三条配方都照常生成。
    // 注意涂蜡变种（waxed_*）在原版里没有自己的贴图——blockstates/waxed_copper_block 直接指向未涂蜡模型，
    // 所以它们的模型 JSON 引用的是未涂蜡那张贴图（见 models/block/waxed_*_shaft.json）。
    // 顺序与 Config.IDS / MorePartialModels / MoreShaftVisual.PARTIALS / ModBlockEntities 保持一致。
    public static final BlockEntry<MoreShaftBlock> EXPOSED_COPPER_SHAFT = shaft("exposed_copper_shaft", "Exposed Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WEATHERED_COPPER_SHAFT = shaft("weathered_copper_shaft", "Weathered Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> OXIDIZED_COPPER_SHAFT = shaft("oxidized_copper_shaft", "Oxidized Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> EXPOSED_CUT_COPPER_SHAFT = shaft("exposed_cut_copper_shaft", "Exposed Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WEATHERED_CUT_COPPER_SHAFT = shaft("weathered_cut_copper_shaft", "Weathered Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> OXIDIZED_CUT_COPPER_SHAFT = shaft("oxidized_cut_copper_shaft", "Oxidized Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_COPPER_BLOCK_SHAFT = shaft("waxed_copper_block_shaft", "Waxed Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_EXPOSED_COPPER_SHAFT = shaft("waxed_exposed_copper_shaft", "Waxed Exposed Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_WEATHERED_COPPER_SHAFT = shaft("waxed_weathered_copper_shaft", "Waxed Weathered Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_OXIDIZED_COPPER_SHAFT = shaft("waxed_oxidized_copper_shaft", "Waxed Oxidized Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_CUT_COPPER_SHAFT = shaft("waxed_cut_copper_shaft", "Waxed Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_EXPOSED_CUT_COPPER_SHAFT = shaft("waxed_exposed_cut_copper_shaft", "Waxed Exposed Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_WEATHERED_CUT_COPPER_SHAFT = shaft("waxed_weathered_cut_copper_shaft", "Waxed Weathered Cut Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_OXIDIZED_CUT_COPPER_SHAFT = shaft("waxed_oxidized_cut_copper_shaft", "Waxed Oxidized Cut Copper Shaft");

    // 雕纹铜块与铜格栅各 8 种，接在铜系后面；默认最大转速与铜块传动杆相同（160）。
    // 雕纹铜块贴图不透明，按普通轴处理即可；铜格栅用原版那张镂空贴图，
    // 所以 partial 要在 MoreShaftVisual 里用 cutout() 包一层，物品渲染层也要跟着设。
    // 「每个面在贴图上取哪一块」是为细杆专门挑的，写在各自的 models/block/*_copper_grate_shaft.json 里。
    public static final BlockEntry<MoreShaftBlock> CHISELED_COPPER_SHAFT = shaft("chiseled_copper_shaft", "Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> EXPOSED_CHISELED_COPPER_SHAFT = shaft("exposed_chiseled_copper_shaft", "Exposed Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WEATHERED_CHISELED_COPPER_SHAFT = shaft("weathered_chiseled_copper_shaft", "Weathered Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> OXIDIZED_CHISELED_COPPER_SHAFT = shaft("oxidized_chiseled_copper_shaft", "Oxidized Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_CHISELED_COPPER_SHAFT = shaft("waxed_chiseled_copper_shaft", "Waxed Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_EXPOSED_CHISELED_COPPER_SHAFT = shaft("waxed_exposed_chiseled_copper_shaft", "Waxed Exposed Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_WEATHERED_CHISELED_COPPER_SHAFT = shaft("waxed_weathered_chiseled_copper_shaft", "Waxed Weathered Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_OXIDIZED_CHISELED_COPPER_SHAFT = shaft("waxed_oxidized_chiseled_copper_shaft", "Waxed Oxidized Chiseled Copper Shaft");

    public static final BlockEntry<MoreShaftBlock> COPPER_GRATE_SHAFT = shaft("copper_grate_shaft", "Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> EXPOSED_COPPER_GRATE_SHAFT = shaft("exposed_copper_grate_shaft", "Exposed Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> WEATHERED_COPPER_GRATE_SHAFT = shaft("weathered_copper_grate_shaft", "Weathered Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> OXIDIZED_COPPER_GRATE_SHAFT = shaft("oxidized_copper_grate_shaft", "Oxidized Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_COPPER_GRATE_SHAFT = shaft("waxed_copper_grate_shaft", "Waxed Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_EXPOSED_COPPER_GRATE_SHAFT = shaft("waxed_exposed_copper_grate_shaft", "Waxed Exposed Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_WEATHERED_COPPER_GRATE_SHAFT = shaft("waxed_weathered_copper_grate_shaft", "Waxed Weathered Copper Grate Shaft");

    public static final BlockEntry<MoreShaftBlock> WAXED_OXIDIZED_COPPER_GRATE_SHAFT = shaft("waxed_oxidized_copper_grate_shaft", "Waxed Oxidized Copper Grate Shaft");

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

    // ---- 「灯轴」：转动时点亮、并像红石块一样输出红石信号（红石灯轴 + 8 根铜灯轴）----

    /**
     * 灯轴的注册脚手架。id 去掉 _shaft 就是对应的原版方块（红石灯 / 铜灯系列），
     * 物性、挖掘标签、掉落、三条配方都由它派生——和 {@link #shaft} 一样，区别只有两点：
     *
     * <ul>
     *   <li>方块类是 {@link LampShaftBlock}：多一个 LIT 状态，转动时点亮并发红石信号；</li>
     *   <li>blockstate 按 LIT 在两个模型之间切——{@code block/<id>} 与 {@code block/<id>_lit}。</li>
     * </ul>
     *
     * 亮度不用自己算：原版红石灯与铜灯的亮度都写在物性里（铜灯还按氧化程度分 15/12/8/4 四档），
     * {@code initialProperties} 会把那份 {@code lightLevel} 函数一起复制过来。
     */
    private static BlockEntry<LampShaftBlock> litShaft(String id, String display) {
        Block material = materialOf(id);
        return REGISTRATE
                .block(id, LampShaftBlock::new)
                .initialProperties(() -> material)
                .properties(p -> p.noOcclusion())
                .transform(TagGen.pickaxeOnly())
                // 按 LIT 选模型：lit=false → block/<id>，lit=true → block/<id>_lit。
                // 用 Create 的 axisBlock 只为拿到那套轴向旋转；模型名交给这个函数决定。
                .blockstate((c, p) -> BlockStateGen.axisBlock(c, p, state -> p.models()
                        .getExistingFile(More_transmission.modLoc("block/" + c.getName()
                                + (state.getValue(LampShaftBlock.LIT) ? "_lit" : ""))), false))
                // 这个 BakedModel 包装器负责支架渲染，并且在世界内把静态模型的面全部吃掉
                // （getQuads 返回空），真正在转的那根杆由 Flywheel 画 —— 其它材质轴同理
                .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                .loot((t, block) -> t.dropSelf(block))
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

    public static final BlockEntry<LampShaftBlock> REDSTONE_LAMP_SHAFT = litShaft("redstone_lamp_shaft", "Redstone Lamp Shaft");

    // 铜灯系列 8 根：亮度随氧化程度递减（15/12/8/4），由各自的物性自带；涂蜡版复用未涂蜡的贴图。
    public static final BlockEntry<LampShaftBlock> COPPER_BULB_SHAFT = litShaft("copper_bulb_shaft", "Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> EXPOSED_COPPER_BULB_SHAFT = litShaft("exposed_copper_bulb_shaft", "Exposed Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> WEATHERED_COPPER_BULB_SHAFT = litShaft("weathered_copper_bulb_shaft", "Weathered Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> OXIDIZED_COPPER_BULB_SHAFT = litShaft("oxidized_copper_bulb_shaft", "Oxidized Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> WAXED_COPPER_BULB_SHAFT = litShaft("waxed_copper_bulb_shaft", "Waxed Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> WAXED_EXPOSED_COPPER_BULB_SHAFT = litShaft("waxed_exposed_copper_bulb_shaft", "Waxed Exposed Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> WAXED_WEATHERED_COPPER_BULB_SHAFT = litShaft("waxed_weathered_copper_bulb_shaft", "Waxed Weathered Copper Bulb Shaft");

    public static final BlockEntry<LampShaftBlock> WAXED_OXIDIZED_COPPER_BULB_SHAFT = litShaft("waxed_oxidized_copper_bulb_shaft", "Waxed Oxidized Copper Bulb Shaft");
    // ---- 封套传动杆：把上面任意一种材质轴用 Andesite / Brass Casing 包起来 ----
    //
    // 原版 Create 的 create:andesite_encased_shaft 内部固定包着 create:shaft，材质没得选；
    // 这里做的是「保留材质」的版本：里面包的是哪一种 MoreShaftBlock 记在 MoreEncasedShaftBlockEntity 里，
    // 于是封套后转的还是那个材质、扳手拆开还原成那个材质、破坏也掉那个材质。
    //
    // 不需要新增任何模型/贴图文件：模型直接复用 Create 的封套轴模型（区别只在 casing 贴图上），
    // 见下面 encasedShaft() 里 blockstate 与物品模型对 create:block/encased_shaft/* 的引用。
    public static final BlockEntry<MoreEncasedShaftBlock> ANDESITE_ENCASED_SHAFT =
            encasedShaft("andesite", "Andesite Encased Shaft", MapColor.PODZOL, AllBlocks.ANDESITE_CASING::get,
                    AllSpriteShifts.ANDESITE_CASING);

    public static final BlockEntry<MoreEncasedShaftBlock> BRASS_ENCASED_SHAFT =
            encasedShaft("brass", "Brass Encased Shaft", MapColor.TERRACOTTA_BROWN, AllBlocks.BRASS_CASING::get,
                    AllSpriteShifts.BRASS_CASING);

    /**
     * 封套传动杆的注册脚手架。casingId 同时是 Create 那边的模型/贴图目录名
     * （create:block/encased_shaft/block_&lt;casingId&gt;、item_&lt;casingId&gt;），
     * 所以新增一种 Casing 只要在 Create 里有对应模型，这里加一行即可。
     */
    private static BlockEntry<MoreEncasedShaftBlock> encasedShaft(String casingId, String display, MapColor mapColor,
            Supplier<Block> casing, CTSpriteShiftEntry casingShift) {
        return REGISTRATE
                .block(casingId + "_encased_shaft", p -> new MoreEncasedShaftBlock(p, casing))
                // 物性与原版封套轴一致：石头硬度/音效，无应力消耗（轴类本来就不吃应力）
                .initialProperties(SharedProperties::stone)
                .properties(p -> p.mapColor(mapColor).noOcclusion())
                .transform(TagGen.axeOrPickaxe())
                // blockstate 直接指向 Create 的封套轴模型（生成结果与 create:andesite_encased_shaft 一模一样）。
                // 因为是别的命名空间的文件，datagen 的 ExistingFileHelper 查不到，必须用 UncheckedModelFile
                // 跳过存在性校验——Create 是硬依赖，模型一定在。
                .blockstate((c, p) -> BlockStateGen.axisBlock(c, p,
                        $ -> new ModelFile.UncheckedModelFile(createModel("block/encased_shaft/block_" + casingId)),
                        true))
                // 外壳贴图与 Casing 方块之间做 CT 连接（和原版封套轴一样，贴在一起会连成一片）
                .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(casingShift)))
                .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, casingShift,
                        (s, f) -> f.getAxis() != s.getValue(MoreEncasedShaftBlock.AXIS))))
                // 关键一步：登记「本模组所有材质轴 -> 这个封套方块」的封套变体，
                // 不做这一步右键 Casing 会静默失败（EncasingRegistry 查不到变体）。
                // 必须等方块注册全部结束才能登记，故用 onRegisterAfter。
                .onRegisterAfter(Registries.BLOCK, ModBlocks::addEncasingVariants)
                .lang(display)
                .item()
                // 封套传动杆只能在游戏里用机壳包出来，不进创造物品栏。
                // Registrate 会把 .item() 的物品默认塞进本模组那栏（见 More_transmission 静态块），
                // 所以这里显式把这一栏的注册摘掉。BlockItem 本身要留着：放置、蓝图打印、/give 还得靠它。
                .removeTab(More_transmission.CREATIVE_TAB)
                // 物品模型同样借用 Create 的 item_<casing>（那版特意把轴芯画出来，比直接拿方块模型当图标好看）
                .model((c, p) -> p.getBuilder(c.getName())
                        .parent(new ModelFile.UncheckedModelFile(createModel("block/encased_shaft/item_" + casingId))))
                .build()
                .register();
    }

    /**
     * 把刚注册好的封套轴登记成「本模组所有材质轴」的封套变体（可同时被多种 Casing 封套）。
     *
     * 这里遍历方块注册表按类型筛选，而不是手写 183 个常量：以后新增材质轴只要 extends MoreShaftBlock
     * 就会自动支持封套，不会出现「新加的轴又不能包 Casing」这种漏登记。
     */
    private static void addEncasingVariants(Block encased) {
        if (!(encased instanceof MoreEncasedShaftBlock encasedShaft))
            return;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!More_transmission.MODID.equals(BuiltInRegistries.BLOCK.getKey(block)
                    .getNamespace()))
                continue;
            if (block instanceof MoreShaftBlock shaft)
                EncasingRegistry.addVariant(shaft, encasedShaft);
        }
    }

    /** 指向 Create 命名空间下的模型文件（封套轴的模型/贴图全部复用 Create 的，本模组不新增）。 */
    private static ResourceLocation createModel(String path) {
        return ResourceLocation.fromNamespaceAndPath("create", path);
    }

    public static void register() {
    }

}
