package com.shengzi.more_transmission;

//import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlockEntityTypes;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
//import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(More_transmission.MODID)
public class More_transmission {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "more_transmission";
    // Directly reference a slf4j logger
//    private static final Logger LOGGER = LogUtils.getLogger();

    public static  final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    /** 本模组创造物品栏的名字——key 是 {@code more_transmission:main}，标题走 {@code itemGroup.more_transmission}。 */
    public static final String CREATIVE_TAB_NAME = "main";

    /**
     * 上面那个创造物品栏的 key，给 {@code ItemBuilder#removeTab} 用：
     * 少数方块（如封套传动杆）只该在游戏里靠机壳包出来，不该出现在创造物品栏里。
     * Registrate 默认会把它塞进本模组的栏，所以那些方块要显式 removeTab 掉。
     */
    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MODID, CREATIVE_TAB_NAME));

    static {
        // 注册本模组自带的创造物品栏并把 Registrate 默认物品栏指向它：
        // 之后凡是通过 REGISTRATE .item() 注册的物品，不额外写 .tab(...) 就只会进这个 tab，
        // 不会自动落到任何原版 tab。
        REGISTRATE.defaultCreativeTab(CREATIVE_TAB_NAME, tab -> tab
                .title(Component.translatable("itemGroup." + MODID))
                .icon(ModBlocks.DIRT_SHAFT::asStack))
                .register();

        REGISTRATE.addRawLang("itemGroup." + MODID, "More Transmission");

        // Max Speed 提示用的英文文案（占位符 %s 会被替换成数字）
        REGISTRATE.addRawLang("more_transmission.tooltip.max_speed", "Max Speed: %s RPM");

        // TNT 轴"转太快会爆炸"提示（英文；中文在 zh_cn.json）
        REGISTRATE.addRawLang("more_transmission.tooltip.tnt_explode", "Spinning too fast will explode");

        // 红石轴"就像红石块一样"提示（英文；中文在 zh_cn.json）
        REGISTRATE.addRawLang("more_transmission.tooltip.redstone_like", "Behaves like a redstone block");

        // 红石灯轴"转起来点亮并发红石信号"提示（英文；中文在 zh_cn.json）
        REGISTRATE.addRawLang("more_transmission.tooltip.lamp_redstone", "Glows and emits a redstone signal of 15 while spinning");

        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));

    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public More_transmission(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);
        REGISTRATE.registerEventListeners(modEventBus);

        ModBlocks.register();

        ModBlockEntities.register();


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (More_transmission) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // 给 BRACKETED_KINETIC（泥土/石轴共用的 BE 类型）的每个实例挂 OverspendCrumbleBehaviour：
        // Create 在 SmartBlockEntity 首次加载/放置时会广播 BlockEntityBehaviourEvent，
        // 借此在不动 BE 类的前提下给泥土轴注入"超速自毁"逻辑（石轴不受影响，见 behaviour 内部判断）。
        // Create 原版轴的 BE 类型也一起挂，让 create:shaft 也能按配置碎裂。
        NeoForge.EVENT_BUS.addListener((BlockEntityBehaviourEvent event) -> {
            event.forType(ModBlockEntities.BRACKETED_KINETIC.get(), be ->
                event.attach(new OverspeedCrumbleBehaviour(be)));
            event.forType(AllBlockEntityTypes.BRACKETED_KINETIC.get(), be ->
                event.attach(new OverspeedCrumbleBehaviour(be)));
            // 封套轴也要参与超速碎裂（行为内部会按「里面包的那根轴」查配置，
            // 否则给轴套个 Casing 就成了免疫超速的漏洞）。
            event.forType(ModBlockEntities.ENCASED_SHAFT.get(), be ->
                event.attach(new OverspeedCrumbleBehaviour(be)));

            // 红石灯传动杆的「转起来就点亮」。这里对所有材质轴都挂上——不能改成按方块类型条件挂：
            // 在同一位置把普通轴换成红石灯轴时两者 BE 类型相同，vanilla 会复用旧 BE、本事件不会重跑，
            // 灯就永远不亮。行为内部第一行就按方块类型早退，开销可以忽略。
            event.forType(ModBlockEntities.BRACKETED_KINETIC.get(), be ->
                event.attach(new LampShaftBehaviour(be)));

            // 传送带每格记住「当初顶掉的是哪根材质轴」，供拆带/切片时还原（见 BeltShaftBehaviour）。
            event.forType(AllBlockEntityTypes.BELT.get(), be ->
                event.attach(new BeltShaftBehaviour(be)));
        });

        // 在任意物品提示框上追加「Max Speed」——这样 create:shaft 等非本模组注册的轴也能显示。
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) ->
            new ShaftMaxSpeedTooltip().modify(event));

        // 传送带滑轮的 visual 换成自己的子类：Create 那个模型里那根轴贴的是原版轴贴图，
        // 这里叠一根材质轴自己的杆把它盖掉（细节见 MaterialBeltVisual）。
        //
        // 时机很讲究：**不能**放在 FMLClientSetupEvent 里。Create 注册传送带 visual 用的也是这个事件
        // （见 CreateBlockEntityBuilder.registerVisualizer），同一事件上两个监听器的先后没有保证，
        // 而我必须排在他后面覆盖才能生效（之前就栽在这里：注册赢不了，等于没改）。
        // 改到客户端世界加载时注册，那时 Create 早就注册完了。
        NeoForge.EVENT_BUS.addListener((LevelEvent.Load event) -> {
            if (event.getLevel().isClientSide())
                installBeltVisualizer();
        });

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // 注册配置。这里刻意用 SERVER 而不是 COMMON：
        // SERVER 配置会由服务端同步给客户端（见 NeoForge 的 ConfigSync，它只同步 SERVER 类型），
        // 这样客户端 tooltip 上显示的才是服务端真正在用的值；用 COMMON 的话两边各读各的本地文件，
        // 多人游戏里客户端可能显示着"没限速"而服务端其实还在碎轴。
        // 代价是 SERVER 配置要等服务器起来才加载，所以标题界面的配置按钮会显示为不可编辑。
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    public static ResourceLocation modLoc(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /**
     * 把 {@code create:belt} 的 visual 换成 {@link MaterialBeltVisual}。
     *
     * <p>「这一格是否仍要走原版 BeltRenderer」的判定**直接转发原来那个 visualizer 的**——
     * 不去猜 Flywheel 那个 `skipVanillaRender` 谓词的方向，行为就一定和之前完全一致。
     * 重复调用是安全的（就是再 set 一次）。
     */
    private static void installBeltVisualizer() {
        var original = VisualizerRegistry.getVisualizer(AllBlockEntityTypes.BELT.get());
        VisualizerRegistry.setVisualizer(AllBlockEntityTypes.BELT.get(),
            new SimpleBlockEntityVisualizer<BeltBlockEntity>(MaterialBeltVisual::new,
                original == null ? be -> false : original::skipVanillaRender));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 强制触发 MyPartialModels 的静态初始化：PartialModel.of() 必须早于模型烘焙
            // （Flywheel 的 ModelEvent.RegisterAdditional 才会把模型加入烘焙队列），
            // 否则 populateOnInit 已置位、查不到 standalone 模型 → 世界里显示黑紫块。
            MorePartialModels.init();

            // 玻璃类传动轴的物品（手持/背包图标）也要按透明渲染层画。
            // （世界内那根转动的杆已由 MoreShaftVisual 的 translucent() 处理透明。）
            registerGlassItemRenderLayers();

            // 挂上 NeoForge 自带的配置界面：Mods 列表 → More Transmission → Config。
            // 玩家因此在游戏里就能直接开关「最大转速」，不用去翻配置文件。
            // 必须在这个客户端方法里注册——ConfigurationScreen 是纯客户端类，服务端加载会炸。
            // 注册只是往 ModContainer 的 extensionPoints map 里放一项，Mods 列表打开时才去取，所以此刻注册来得及。
            ModList.get()
                .getModContainerById(MODID)
                .ifPresent(container -> container.registerExtensionPoint(IConfigScreenFactory.class,
                    ConfigurationScreen::new));

        }

        /** 给 18 种玻璃轴注册 translucent 物品/方块渲染层。 */
        @SuppressWarnings("deprecation")
        private static void registerGlassItemRenderLayers() {
            Block[] glass = {
                ModBlocks.GLASS_SHAFT.get(),
                ModBlocks.TINTED_GLASS_SHAFT.get(),
                ModBlocks.WHITE_STAINED_GLASS_SHAFT.get(),
                ModBlocks.LIGHT_GRAY_STAINED_GLASS_SHAFT.get(),
                ModBlocks.GRAY_STAINED_GLASS_SHAFT.get(),
                ModBlocks.BLACK_STAINED_GLASS_SHAFT.get(),
                ModBlocks.BROWN_STAINED_GLASS_SHAFT.get(),
                ModBlocks.RED_STAINED_GLASS_SHAFT.get(),
                ModBlocks.ORANGE_STAINED_GLASS_SHAFT.get(),
                ModBlocks.YELLOW_STAINED_GLASS_SHAFT.get(),
                ModBlocks.LIME_STAINED_GLASS_SHAFT.get(),
                ModBlocks.GREEN_STAINED_GLASS_SHAFT.get(),
                ModBlocks.CYAN_STAINED_GLASS_SHAFT.get(),
                ModBlocks.LIGHT_BLUE_STAINED_GLASS_SHAFT.get(),
                ModBlocks.BLUE_STAINED_GLASS_SHAFT.get(),
                ModBlocks.PURPLE_STAINED_GLASS_SHAFT.get(),
                ModBlocks.MAGENTA_STAINED_GLASS_SHAFT.get(),
                ModBlocks.PINK_STAINED_GLASS_SHAFT.get()
            };
            for (Block block : glass)
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());

            registerGrateItemRenderLayers();
        }

        /**
         * 给 8 种铜格栅轴注册 cutout 物品渲染层。
         *
         * <p>格栅贴图是镂空的（像素非全透明即不透明），原版也把这 8 个方块放在 {@code RenderType.cutout()}
         * （见 {@code ItemBlockRenderTypes}）。不设的话物品图标里镂空处会按实心渲染成黑斑。
         * 世界内那根转动的杆由 {@code MoreShaftVisual.cutout()} 单独处理。
         */
        @SuppressWarnings("deprecation")
        private static void registerGrateItemRenderLayers() {
            Block[] grates = {
                ModBlocks.COPPER_GRATE_SHAFT.get(),
                ModBlocks.EXPOSED_COPPER_GRATE_SHAFT.get(),
                ModBlocks.WEATHERED_COPPER_GRATE_SHAFT.get(),
                ModBlocks.OXIDIZED_COPPER_GRATE_SHAFT.get(),
                ModBlocks.WAXED_COPPER_GRATE_SHAFT.get(),
                ModBlocks.WAXED_EXPOSED_COPPER_GRATE_SHAFT.get(),
                ModBlocks.WAXED_WEATHERED_COPPER_GRATE_SHAFT.get(),
                ModBlocks.WAXED_OXIDIZED_COPPER_GRATE_SHAFT.get()
            };
            for (Block block : grates)
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
        }
    }
}
