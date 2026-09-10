package com.shengzi.more_transmission;

//import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
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

    static {
        // 注册本模组自带的创造物品栏并把 Registrate 默认物品栏指向它：
        // 之后凡是通过 REGISTRATE .item() 注册的物品，不额外写 .tab(...) 就只会进这个 tab，
        // 不会自动落到任何原版 tab。
        REGISTRATE.defaultCreativeTab("main", tab -> tab
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
        });

        // 在任意物品提示框上追加「Max Speed」——这样 create:shaft 等非本模组注册的轴也能显示。
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) ->
            new ShaftMaxSpeedTooltip().modify(event));

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation modLoc(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
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
        }
    }
}
