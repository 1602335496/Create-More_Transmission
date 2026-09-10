# Create 模组 API 参考（供下游模组 / Addon 使用）

> 这份文档是给**你自己模组项目**用的参考地图。把它放到你模组项目的**根目录**，
> Claude Code / Cursor 会自动加载它，之后在你项目里写代码时 AI 就能照着这份地图去
> Create 源码里找正确的类和方法。
>
> 针对版本：**Create 6.0.11 / Minecraft 1.21.1 / NeoForge**

---

## 0. 必读：Create 源码在哪里

本机 Create 源码根目录（绝对路径，直接告诉 AI 去这里读参考实现）：

```
C:\Users\16023\Desktop\cursor_name\Create-mc1.21.1-dev\Create-mc1.21.1-dev
```

源码里 Java 代码在 `src/main/java/com/simibubi/create/`。当 AI 需要看某个功能到底怎么实现、
某个方法签名是什么时，**直接让它去这个路径下 `grep` / `read` 对应文件**，而不是猜。

---

## 1. 依赖方式（Gradle）

Create 官方 Maven 仓库：`https://maven.createmod.net`

```gradle
repositories {
    maven { url = "https://maven.createmod.net" }
    // 其它 Create 生态依赖也在同一仓库：Ponder、Flywheel
    maven { url = "https://maven.ithundxr.dev/snapshots" } // Registrate
}

dependencies {
    // 官方坐标 group=com.simibubi.create，artifact 由项目名 create-1.21.1 派生
    implementation("com.simibubi.create:create-1.21.1:6.0.11")
}
```

> 注意：artifact 名和版本号以实际 Maven 列表为准（这里是根据 `build.gradle` / `settings.gradle`
> 推断：`group = "com.simibubi.create"`、`rootProject.name = "create-1.21.1"`、`mod_version = 6.0.11`）。

Create 通过 `jarJar(api(...))` 把下面这些**一起打包并暴露给下游**，依赖 Create 即可拿到：

- `net.createmod.ponder:ponder-neoforge`（Ponder 教学场景库）
- `dev.engine-room.flywheel:flywheel-neoforge`（渲染后端）
- `com.tterrag.registrate:Registrate`（注册工具库）

如果你的模组要自己写 Ponder 场景或直接用 Flywheel，也可以单独依赖上面这些。

---

## 2. 顶层包结构

| 包 | 作用 | 是否面向下游 |
|---|---|---|
| `com.simibubi.create.api` | **公共 API**，下游模组主要对接这一层 | ✅ 是 |
| `com.simibubi.create.content` | 各种实际内容（机器、方块、实体、火车…） | 参考实现用 |
| `com.simibubi.create.foundation` | 内部框架（BE 基类、行为系统、GUI 等） | 参考实现用 |
| `com.simibubi.create.impl` | `api` 接口的内部实现 | ❌ 不要直接依赖 |
| `com.simibubi.create.infrastructure` | 配置、网络、数据包等基础设施 | 偶尔参考 |
| `com.simibubi.create.compat` | 对其它模组的兼容层 | 参考用 |

**经验法则**：写代码时优先 import `api` 包；要找"某功能怎么做"，去 `content` 包找同名机器抄。

---

## 3. `api` 包地图（每个子包是干嘛的）

| 子包 | 用途 | 关键类 / 接口 |
|---|---|---|
| `api.stress` | **应力（stress）系统** | `BlockStressValues` |
| `api.registry` | Create 自定义注册表 + 数据映射 | `CreateRegistries`、`CreateBuiltInRegistries`、`CreateDataMaps`、`SimpleRegistry` |
| `api.data.recipe` | **配方数据生成（datagen）** | `ProcessingRecipeGen`、`StandardProcessingRecipeGen` + 各 `*RecipeGen` |
| `api.data.datamaps` | 数据映射值类型 | `BlazeBurnerFuel` |
| `api.equipment.goggles` | **护目镜 / 悬浮 tooltip** | `IHaveGoggleInformation`、`IHaveHoveringInformation` |
| `api.behaviour.movement` | 装置上的"行为/Actor" | `MovementBehaviour` |
| `api.behaviour.display` | 显示板（Display Link）源/目标 | `DisplaySource`、`DisplayTarget` |
| `api.behaviour.interaction` | 装置移动中的交互行为 | `MovingInteractionBehaviour`、`ConductorBlockInteractionBehavior` |
| `api.behaviour.spouting` | 注液器（Spout）行为 | `BlockSpoutingBehaviour`、`CauldronSpoutingBehavior` |
| `api.contraption` | 装置移动规则 | `BlockMovementChecks`、`ContraptionMovementSetting`、`ContraptionType` |
| `api.contraption.storage` | 装置上的物品/流体存储 | `MountedItemStorage`、`MountedFluidStorage` 及其 Type |
| `api.contraption.dispenser` | 装置上的发射器行为 | `MountedDispenseBehavior` |
| `api.contraption.transformable` | 方块"变形"（如伪装板 copycat） | `TransformableBlock`、`TransformableBlockEntity` |
| `api.equipment.potatoCannon` | 马铃薯炮弹药 | `PotatoCannonProjectileType`、命中行为、渲染模式 |
| `api.event` | Create 派发的事件 | `BlockEntityBehaviourEvent`、`PipeCollisionEvent`、`TrackGraphMergeEvent` |
| `api.effect` | 开放管道药水效果 | `OpenPipeEffectHandler` |
| `api.boiler` | 锅炉加热 | `BoilerHeater` |
| `api.connectivity` | 多方块连接 | `ConnectivityHandler` |
| `api.schematic` | 蓝图/原理图（NBT、物品需求、状态过滤） | `SchematicRequirementRegistries`、`SchematicStateFilterRegistry` |
| `api.packager` | 打包器（新物流） | `InventoryIdentifier`、`UnpackingHandler` |
| `api.registrate` | Registrate 集成 | `CreateRegistrateRegistrationCallback` |
| `api.registry.registrate` | 简易 Registrate builder | `SimpleBuilder` |

---

## 4. 常用扩展点（含可复制的代码示例）

### 4.1 应力值 `BlockStressValues`（`api.stress`）

给方块登记"消耗/提供多少应力"。**下游模组用这个公开 API，不要用 Create 内部的 `CStress.setImpact/setCapacity`**（那个只允许 Create 自己的方块）。

```java
import com.simibubi.create.api.stress.BlockStressValues;

// 消耗型机器：在 1 RPM 下消耗 4 应力（速度翻倍消耗也翻倍）
BlockStressValues.IMPACTS.register(MyBlocks.MY_MACHINE.get(), () -> 4.0);

// 发电机：提供 1024 应力容量
BlockStressValues.CAPACITIES.register(MyBlocks.MY_GENERATOR.get(), () -> 1024.0);

// 用 Registrate 注册时，直接用 onRegister 简写
// （setGeneratorSpeed 返回 NonNullConsumer<Block>）
MyBlocks.CREATIVE_MOTOR = REGISTRATE.block("creative_motor", ...)
    .onRegister(BlockStressValues.setGeneratorSpeed(256, true)) // 256 RPM，允许降速
    .register();
```

### 4.2 护目镜 / 悬浮信息 `IHaveGoggleInformation`、`IHaveHoveringInformation`（`api.equipment.goggles`）

在你的 **BlockEntity** 上实现接口即可，无需注册。

```java
public class MyBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    // 戴护目镜时看这个方块，往 tooltip 里追加文字
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("当前转速: " + speed + " RPM"));
        return true; // 返回 true 表示要显示
    }
}
```

- `IHaveGoggleInformation` → 护目镜 overlay
- `IHaveHoveringInformation` → 手持扳手/悬浮时 overlay
- `IHaveCustomOverlayIcon` / `IProxyHoveringInformation` → 自定义图标 / 代理悬浮（少见）

### 4.3 配方数据生成 `StandardProcessingRecipeGen`（`api.data.recipe`）

给你的自定义加工配方（粉碎、搅拌、压制等）做 datagen。

```java
public class MyMixingRecipeGen extends StandardProcessingRecipeGen<MyMixingRecipe> {
    public MyMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, "mymod"); // 第三个参数是默认命名空间
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return MyRecipeTypes.MIXING; // 你的配方类型
    }

    GeneratedRecipe genSome() {
        return create("my_recipe",
            b -> b.require(Ingredient.of(Items.IRON_INGOT))
                .require(FluidIngredients.water(250))
                .output(new ItemStack(Items.GOLD_INGOT))
                .duration(100));
    }
}
```

要点：
- 普通加工配方（用基础 `ProcessingRecipeParams`）→ 继承 `StandardProcessingRecipeGen`
- 自定义参数（像 `ItemApplicationRecipe` 那种）→ 继承 `ProcessingRecipeGen` 并覆写 `getRecipeType()` / `getBuilder()`
- 现成的每种机器都有对应的 gen，直接抄：`MixingRecipeGen`、`CrushingRecipeGen`、`PressingRecipeGen`、`MillingRecipeGen`、`DeployingRecipeGen`、`FillingRecipeGen`、`EmptyingRecipeGen`、`HauntingRecipeGen`、`PolishingRecipeGen`、`CompactingRecipeGen`、`SequencedAssemblyRecipeGen`、`MechanicalCraftingRecipeGen` 等。

### 4.4 装置移动行为 `MovementBehaviour`（`api.behaviour.movement`）

让方块被搬进装置（活塞/轴承等）后有自己的行为，也叫 "Actor"。

```java
public class MyMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        // 每个 tick 在装置移动时调用
    }
}

// 注册到方块（或配合 Registrate）
public static final SimpleRegistry<Block, MovementBehaviour> REG = MovementBehaviour.REGISTRY;
MovementBehaviour.REGISTRY.register(MyBlocks.MY_BLOCK.get(), new MyMovementBehaviour());

// Registrate 简写（movementBehaviour 返回 NonNullConsumer）
// .onRegister(MovementBehaviour.movementBehaviour(new MyMovementBehaviour()))
```

常用可覆写方法：`tick`、`startMoving`、`stopMoving`、`visitNewPosition`、`onSpeedChanged`、`collectOrDropItem`（旧名 `dropItem` 已弃用）、`renderInContraption`（客户端渲染）、`createVisual`（Flywheel 可视化）。

### 4.5 显示板源 `DisplaySource` / 目标 `DisplayTarget`（`api.behaviour.display`）

给显示板（Display Link / 翻牌显示器）提供数据源。

```java
public class MyDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        return List.of(Component.literal("Hello"));
    }
}
// 注册：需要在 Create 的 DISPLAY_SOURCE 注册表登记，并通过 BY_BLOCK / BY_BLOCK_ENTITY 关联
```

注册方式参考 `DisplaySource.displaySource(...)`（返回 Registrate 的 builder transformer）。

### 4.6 自定义注册表 `SimpleRegistry`（`api.registry`）

Create 用 `SimpleRegistry` 做轻量 key→value 映射（支持 tag 懒加载 provider）。

```java
// 单值映射
SimpleRegistry<Block, MyData> REG = SimpleRegistry.create();
REG.register(someBlock, someData);
REG.registerProvider(SimpleRegistry.Provider.forBlockTag(TagKey.create(Registries.BLOCK, myTag), someDefault));

// 多值映射（一个 key 对应 List<V>）
SimpleRegistry.Multi<Block, MyBehaviour> MULTI = SimpleRegistry.Multi.create();
MULTI.add(someBlock, behaviour);
```

### 4.7 数据映射 `CreateDataMaps`（`api.registry` + `api.data.datamaps`）

往 Create 的 datamap 里加条目（例如给烈焰人燃烧器加燃料）。用 NeoForge 的 `DataMapType` 机制，在你自己 mod 的 datapack 里放 JSON：

```jsonc
// 路径：data/<你的命名空间>/data_maps/item/superheated_blaze_burner_fuels.json
// 或 data/mymod/data_maps/item/regular_blaze_burner_fuels.json
{
  "values": {
    "mymod:my_fuel": { "burn_time": 2000 }
  }
}
```

对应类型见 `CreateDataMaps.REGULAR_BLAZE_BURNER_FUELS` / `SUPERHEATED_BLAZE_BURNER_FUELS`（`DataMapType<Item, BlazeBurnerFuel>`，`BlazeBurnerFuel` 只有 `burn_time` 一个字段）。

### 4.8 装置移动规则 `BlockMovementChecks`（`api.contraption`）

决定"哪些方块能不能被搬动 / 是否脆弱 / 是否相互附着"。

```java
BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) -> {
    // 返回 CheckResult.SUCCESS / FAIL / PASS（PASS = 不表态，交给别的检查）
    return state.is(MyTags.IMMOVABLE) ? CheckResult.FAIL : CheckResult.PASS;
});
```

`CheckResult`：`SUCCESS`、`FAIL`、`PASS`。`PASS` 表示这个检查不表态，继续问其它已注册检查。

### 4.9 给 Create 的方块实体挂行为 `BlockEntityBehaviourEvent`（`api.event`）

给现有的 SmartBlockEntity 注入自定义 `BlockEntityBehaviour`。

```java
neoForgeEventBus.addListener((BlockEntityBehaviourEvent event) -> {
    event.forType(AllBlockEntityTypes.FUNNEL.get(), be -> {
        event.attach(new MyFunnelBehaviour(be));
    });
});
```

### 4.10 马铃薯炮弹药 `PotatoCannonProjectileType`（`api.equipment.potatoCannon`）

通过 `CreateRegistries.POTATO_PROJECTILE_TYPE` 注册弹药类型，可自定义实体命中行为、方块命中行为、渲染模式。

---

## 5. 动能系统（加一个"会转/会耗应力"的机器）

动能方块是 Create 最核心的扩展方向。关键类都在 `content.kinetics.base`：

| 类 | 作用 |
|---|---|
| `KineticBlock` | 动能方块基类（轴方向、相邻传动） |
| `KineticBlockEntity` | **动能方块实体基类**（转速、应力传播、`updateFromNetwork` 等） |
| `GeneratingKineticBlockEntity` | 发电机基类（提供应力容量） |
| `BlockBreakingKineticBlockEntity` | 会破坏方块的动能机器（钻头/锯子）基类 |
| `DirectionalKineticBlock` / `HorizontalKineticBlock` / `HorizontalAxisKineticBlock` | 按朝向/轴取向的动能方块变体 |
| `RotationPropagator` | 转速传播核心 |

**加一个动能机器的大致路径**（看现有机器抄最快，见下节索引）：
1. `Block` extends `KineticBlock`（或某个取向变体）
2. `BlockEntity` extends `KineticBlockEntity`
3. 用 `BlockStressValues.IMPACTS.register(...)` 登记应力消耗（见 4.1）
4. 在 `content.kinetics.<某机器>` 下找相似机器，照着它的 Block/BlockEntity/Renderer 抄

参考机器（都在 `src/main/java/com/simibubi/create/content/kinetics/`）：
`millstone`（石磨，最简单的耗应力机器）、`press`、`mixer`、`crusher`、`saw`、`deployer`、`drill`、`fan`、`waterwheel`/`steamEngine`/`motor`（发电机）。

---

## 6. 参考实现索引（去哪里找"XX 怎么做"）

### 注册入口（顶层类，路径 `src/main/java/com/simibubi/create/`）
`AllBlocks`、`AllItems`、`AllBlockEntityTypes`、`AllEntityTypes`、`AllFluids`、`AllRecipeTypes`、
`AllMenuTypes`、`AllCreativeModeTabs`、`AllMovementBehaviours`、`AllDisplaySources`、`AllDisplayTargets`、
`AllContraptionTypes`、`AllContraptionMovementSettings`、`AllMountedStorageTypes`、`AllTags`、
`Create.java`（主类，`Create.ID = "create"`）、`CreateClient.java`。

### 内容子包（`content/`）
- `content.kinetics` — 所有动能机器（见第 5 节列表）
- `content.processing` — 加工（basin 盆、burner 燃烧器、recipe 配方框架、sequenced 序列组装）
- `content.logistics` — 物流（funnel 漏斗、chute 溜槽、depot 置物台、crate/vault 箱子、tunnel 传送带、filter 过滤器、packager 打包器）
- `content.redstone` — 红石（displayLink、link 红石链、nixieTube 数码管、contact 接触器、rail 信号）
- `content.fluids` — 流体（pump 泵、spout 注液器、tank 储罐、drain 排放口、pipes 管道、hosePulley 软管滑轮）
- `content.contraptions` — 装置（piston 活塞、bearing 轴承、pulley 滑轮、gantry 龙门、elevator 电梯、actors 装置行为）
- `content.trains` — 火车（track 轨道、bogey 转向架、station 车站、signal 信号、schedule 时刻表、graph 铁路图）
- `content.schematics` — 蓝图
- `content.decoration` — 装饰（copycat 伪装板、girder 梁、placard 告示牌、bracket 支架）
- `content.equipment` — 装备（goggles 护目镜、wrench 扳手、potatoCannon 马铃薯炮、toolbox 工具箱）

---

## 7. 给 AI（Claude Code）的使用说明

写代码时遵循以下流程：

1. **先查这份文档**确定要用的 API 在 `api` 包的哪个子包、关键类叫什么。
2. **不确定签名/行为时**，直接到下面路径 grep/read 对应源码，不要凭记忆猜：
   `C:\Users\16023\Desktop\cursor_name\Create-mc1.21.1-dev\Create-mc1.21.1-dev\src\main\java\com\simibubi\create\`
   - 公共 API 看 `api/` 子包
   - 具体实现抄 `content/` 下同名机器
   - 注册方式看顶层 `All*` 类
3. **优先 import `api` 包**，避免依赖 `impl` / `foundation` 内部类（`foundation` 会随版本变动）。
4. 涉及到**应力/转速**先看 `BlockStressValues`；**tooltip** 看 goggles 接口；**配方 datagen** 看 `api.data.recipe`；**装置行为**看 `MovementBehaviour`。
