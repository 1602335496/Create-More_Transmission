# More Transmission（更多传动）

[中文](#中文) ｜ [English](#english)

---

# 中文

为 **机械动力（Create）** 添加 **158 种不同材质的传动轴**：泥土、木头、石头、矿物块、羊毛、玻璃……总有一种质感适合你的机械装置。

模组以 Create 原版传动轴为基类扩展，因此这些轴**可以像普通 Create 轴一样使用**：传动、连接齿轮、套上齿轮箱/封套、挂支架、含水、被活塞推动……全部照旧；同时每种材质都带有自己的外观、物性，以及一个**默认关闭**的可选「最大转速」限制。

> 支持版本：**Minecraft 1.21.1 + NeoForge 21.1.250 + Create 6.0.10 及以上**
> 许可证：MIT ｜ 作者：sheng_zi

## ✨ 特色

- **158 种材质传动轴**：从木头、石头，到矿物块、羊毛、玻璃，几乎把原版方块都做成了轴。
- **材质化物性**：每根轴的硬度、音效、挖掘工具、是否掉落，都**继承对应原版方块**。
  - 木质轴 → 用斧头；松软材质（泥土/沙…）→ 用铲子；石质/矿物 → 用镐。
  - 羊毛轴 → 和羊毛一样：不需要镐，**剪刀 5× / 剑 1.5× 加速**。
  - 玻璃轴 → 和玻璃一样：任意工具都能破坏，但**普通破坏不掉落**，只有**精准采集**才能回收。
- **可选的最大转速限制（默认关闭）**：打开开关后，每根轴都有转速上限，超过上限会**碎裂**（TNT 轴则是**爆炸**），数值可在配置文件中逐根调整。
  默认 `enable_shaft_max_speed = false`，也就是**完全不限制转速**，和原版 Create 手感一致；想要硬核设定的玩家自己打开即可。
- **原地显示上限**：开关打开时，物品悬浮提示里会显示 `Max Speed`，一眼看出这轴能转多快。
- **特殊机制轴**：
  - 🧨 **TNT 传动轴**：可被火/打火石/火焰弹点燃，一点即爆；转速超限同样会爆炸。
  - 🔴 **红石传动轴**：像红石块一样持续向相邻发出 **15 级红石信号**。
- **可被机壳包裹（封套）**：和原版轴一样，拿着**安山机壳**或**黄铜机壳**右键任意材质轴即可包起来。
  与原版不同的是**材质会保留**：封套后里面转的还是原来那根轴，扳手拆开、破坏掉落也都能拿回原材质的那根轴。
- **与 Create / 其它模组互通**：提供转换配方与物品标签，让新轴能参与机械动力的合成（见下）。
- **中英双语**：内置英文与简体中文语言文件。

## 🧱 材质与默认最大转速

| 默认转速 (RPM) | 材质举例 | 数量 |
|---|---|---|
| **16** | 泥土、沙、红沙、沙砾、黏土、灵魂沙、雪块、冰、苔藓、海绵、干草、黏液、蜂蜜、蜜脾、幽匿、**TNT**、16 色羊毛、玻璃与 16 色染色玻璃 | 50 |
| **32** | 全部木质/植物：原木·去皮原木·木板（橡木/云杉/白桦/丛林/金合欢/深色橡木/红树/樱花）、竹块/竹板、绯红·诡异菌柄 | 33 |
| **48** | 砂岩/平滑砂岩、红砂岩、砖块、泥坯、泥砖、下界岩、海晶灯、荧石 | 10 |
| **64** | 石头、圆石、苔石、平滑石头、石砖、花岗岩/闪长岩/安山岩及磨制、凝灰岩系、海晶石/暗海晶石、末地石系、方解石、骨块 | 21 |
| **96** | 深板岩系（圆石/磨制/深板岩砖） | 4 |
| **112** | 下界砖块、红色下界砖块 | 2 |
| **128** | 玄武岩系、黑石系、镶金黑石、紫珀块、滴水石 | 8 |
| **160** | 铜块系共 16 根（铜块/切制铜块 + 斑驳·锈蚀·氧化 + 各自涂蜡）、煤炭块、岩浆块 | 18 |
| **192** | 铁块、金块、青金石块、石英块、石英柱、绿宝石块、紫水晶块、红石块 | 8 |
| **224** | 钻石块 | 1 |
| **256** | 黑曜石、下界合金块、基岩 | 3 |

> 合计 **158** 根。上表是**默认值**，需要先在配置里打开 `enable_shaft_max_speed` 才会生效（数值本身也可逐根修改）。

## ⚙️ 配置文件

首次进入存档时会自动生成：

```
config/more_transmission-server.toml
```

**也可以直接在游戏里改，不用翻配置文件**：暂停菜单（或标题界面）→ **Mods** → **More Transmission** → **Config**，
勾选即可生效——不用重启游戏，也不用重进存档。

> 这份配置是 `SERVER` 类型：由服务端同步给客户端，保证多人游戏里「提示显示的数值」和「服务端真正在碎轴的数值」一致。
> 代价是它要等服务器起来才加载，所以**标题界面上那个 Config 按钮是灰的**，进存档后才能点。

### 转速上限总开关（默认关闭）

```toml
#是否启用「最大转速」限制。默认 false = 不限制，所有传动轴想转多快就转多快。
enable_shaft_max_speed = false
```

默认**不限制转速**，和原版 Create 一样没有转速惩罚：

- 不会超速碎裂；
- TNT 轴不会因为转速爆炸（但**仍然**可以被火 / 打火石 / 火焰弹点燃）；
- 物品提示里不显示 `Max Speed`。

改成 `true` 之后，下面分组里逐根配置的上限才会生效。

### 逐根轴的转速上限（仅在开关打开时生效）

```toml
[shaft_max_speed]
    #Max speed for dirt_shaft (default 16)
    dirt_shaft = 16
    #Max speed for oak_log_shaft (default 32)
    oak_log_shaft = 32
    ...
    #Max speed for bedrock_shaft (default 256)
    bedrock_shaft = 256
```

- 下限 `0`（一有转速就碎裂），**上限不设**——Create 的转速能远超 256（齿轮箱叠加、转速控制器，或改 `create-server.toml` 里的 `kinetics.maxRotationSpeed`），所以这里想填多少填多少；
- 修改后保存并**重启游戏**（或用配置界面 Reload）生效；
- 转速**超过**该值即触发碎裂/爆炸。

## 🛠 配方

每根轴都有三条基础配方（以泥土轴为例）：

| 类型 | 配方 |
|---|---|
| 工作台（有序） | 同一列竖放 **2 个对应方块** → **8 根轴** |
| 切石机 | **1 个对应方块** → **8 根轴** |
| 工作台（无序·逆向） | **9 根轴** → **1 个对应方块** |

### 与机械动力互通（转换配方）

因为 Create 机器配方大多直接引用 `create:shaft` / 齿轮物品，模组提供了转换配方让你用任意材质的轴进入 Create 的合成链：

| 配方（无序） | 产出 |
|---|---|
| 任意本模组传动轴 + 1 × Andesite Alloy | `create:shaft` |
| 任意本模组传动轴 + 1 × 任意木板 | `create:cogwheel` |
| 任意本模组传动轴 + 2 × 任意木板 | `create:large_cogwheel` |

其中「任意本模组传动轴」使用物品标签 `more_transmission:shafts`（含全部 158 根），方便其它模组/数据包引用。

## 📦 安装

1. 安装 **Minecraft 1.21.1** + **NeoForge 21.1.250+**（例如用 PCL/HMCL 等启动器）；
2. 安装 **机械动力 Create 6.0.10 或以上**（Create 会自带 Flywheel / Ponder / Registrate 依赖）；
3. 把 `more_transmission-neo_1.21.1-1.0.0.jar` 放进 `.minecraft/mods/`；
4. 启动游戏即可，创造物品栏中会出现 **「更多传动 / More Transmission」** 分类。

> JEI 为可选（仅用于查看配方，不装也不影响使用）。

## 🧑‍💻 从源码构建（开发）

```bash
# 编译
./gradlew build
# 产物：build/libs/more_transmission-neo_1.21.1-1.0.0.jar

# 本地开发运行
./gradlew runClient

# 重新生成数据（方块状态 / 物品模型 / 掉落表 / 标签 / 语言 / 配方）
./gradlew runData
```

数据生成产物位于 `src/generated/resources/`，已随仓库提交，克隆后可直接构建。

## 🌏 本地化

- `en_us`：英文（默认）
- `zh_cn`：简体中文（`src/main/resources/assets/more_transmission/lang/zh_cn.json`）

欢迎提交其它语言的翻译（PR 即可）。

## 🔧 与 Create 的兼容说明

- 所有轴继承 Create 的 `ShaftBlock` / `BracketedKineticBlockEntity`，动能网络、旋转渲染、支架（bracket）、含水、封套（casing）等行为均由 Create 原生逻辑驱动；
- 「最大转速」是本模组额外附加的行为，通过 Create 的 `BlockEntityBehaviourEvent` 注入，不改动 Create 代码；**默认关闭**（`enable_shaft_max_speed = false`），关闭时整条逻辑直接放行，等于不存在；
- 提供 `more_transmission:shafts` 物品标签与转换配方，便于与其它模组/数据包对接。

## 📜 许可

本项目以 **MIT** 许可证开源。感谢 **机械动力（Create）** 及其团队提供了优秀的开源基础与 API。

## 🐛 反馈

遇到问题或有建议，欢迎提 [Issue](https://github.com/1602335496/More_Transmission/issues)。

---

# English

Adds **158 material shafts** to **Create**: dirt, wood, stone, mineral blocks, wool, glass… There is a texture for every machine.

Every shaft extends Create's vanilla shaft, so they **behave exactly like a normal Create shaft**: they transmit rotation, connect to cogwheels, accept casings, hold brackets, support waterlogging and can be moved by pistons — plus each material brings its own look, its own block properties, and an **optional, off-by-default** maximum speed limit.

> Supported: **Minecraft 1.21.1 + NeoForge 21.1.250 + Create 6.0.10 or newer**
> License: MIT ｜ Author: sheng_zi

## ✨ Features

- **158 material shafts** — from wood and stone to mineral blocks, wool and glass.
- **Material-derived properties**: hardness, sound, mining tool and drops are **inherited from the matching vanilla block**.
  - Wooden shafts → axe; soft blocks (dirt/sand…) → shovel; stone/mineral → pickaxe.
  - Wool shafts behave like wool: no pickaxe needed, **shears 5× / sword 1.5× faster**.
  - Glass shafts behave like glass: breakable with anything, but **only drop with Silk Touch**.
- **Optional maximum speed limit — off by default**: once enabled, each shaft has an RPM limit and exceeding it **shatters** the shaft (the **TNT** shaft **explodes** instead). Every value is editable per shaft in the config file.
  It defaults to `enable_shaft_max_speed = false`, i.e. **no speed limit at all** — the same feel as vanilla Create. Players who want the hardcore rules can turn it on.
- **Visible limit**: when the switch is on, the item tooltip shows `Max Speed` at a glance.
- **Special shafts**:
  - 🧨 **TNT Shaft** — ignites from fire / flint & steel / fire charge and detonates instantly; also explodes when overspun.
  - 🔴 **Redstone Shaft** — emits a constant **redstone signal of 15**, just like a block of redstone.
- **Encasable**: right-click any material shaft with an **Andesite** or **Brass Casing**, just like a vanilla shaft.
  Unlike vanilla, the **material is kept**: the shaft inside still spins as your material shaft, and unwrenching or breaking it gives that material shaft back.
- **Interoperability with Create / other mods** via conversion recipes and an item tag (see below).
- **Bilingual**: built-in English and Simplified Chinese language files.

## 🧱 Materials & Default Max Speed

| Default speed (RPM) | Examples | Count |
|---|---|---|
| **16** | dirt, sand, red sand, gravel, clay, soul sand, snow block, ice, moss, sponge, hay, slime, honey, honeycomb, sculk, **TNT**, 16 wool colors, glass and 16 stained glass | 50 |
| **32** | all wood/plant: logs · stripped logs · planks (oak/spruce/birch/jungle/acacia/dark oak/mangrove/cherry), bamboo block/planks, crimson & warped stems | 33 |
| **48** | sandstone/smooth sandstone, red sandstone, bricks, packed mud, mud bricks, netherrack, sea lantern, glowstone | 10 |
| **64** | stone, cobblestone, mossy cobblestone, smooth stone, stone bricks, granite/diorite/andesite and polished, tuff set, prismarine/dark prismarine, end stone set, calcite, bone block | 21 |
| **96** | deepslate set (cobbled/polished/bricks) | 4 |
| **112** | nether bricks, red nether bricks | 2 |
| **128** | basalt set, blackstone set, gilded blackstone, purpur block, dripstone block | 8 |
| **160** | copper family, 16 in total (copper / cut copper + exposed, weathered, oxidized + each waxed), coal block, magma block | 18 |
| **192** | iron block, gold block, lapis block, quartz block, quartz pillar, emerald block, amethyst block, redstone block | 8 |
| **224** | diamond block | 1 |
| **256** | obsidian, netherite block, bedrock | 3 |

> **158** shafts in total. The table lists **defaults**; they only apply once `enable_shaft_max_speed` is turned on (and each value is editable).

## ⚙️ Configuration

Generated automatically the first time you enter a world:

```
config/more_transmission-server.toml
```

**You can also change it in-game without touching the file**: pause menu (or title screen) → **Mods** → **More Transmission** → **Config**.
Tick the box and it applies immediately — no game restart, no need to re-enter the world.

> This is a `SERVER`-type config: the server syncs it to clients so that the tooltips show exactly the values the server
> actually enforces. The trade-off is that it is only loaded once a server is running, so **the Config button on the title
> screen is greyed out** — enter a world first.

### Master switch for the speed limit — off by default

```toml
#是否启用「最大转速」限制。默认 false = 不限制，所有传动轴想转多快就转多快。
enable_shaft_max_speed = false
```

By default there is **no speed limit**, just like vanilla Create:

- nothing ever shatters from overspeeding;
- the TNT shaft never explodes from spinning (it can still be lit by fire / flint & steel / fire charge);
- the `Max Speed` tooltip line is not shown.

Set it to `true` and the per-shaft limits below become active.

### Per-shaft limits (only active when the switch is on)

```toml
[shaft_max_speed]
    #Max speed for dirt_shaft (default 16)
    dirt_shaft = 16
    #Max speed for oak_log_shaft (default 32)
    oak_log_shaft = 32
    ...
    #Max speed for bedrock_shaft (default 256)
    bedrock_shaft = 256
```

- Lower bound `0` (shatters as soon as it spins), **no upper bound** — Create speeds can go far beyond 256
  (stacked gearboxes, rotation speed controllers, or `kinetics.maxRotationSpeed` in `create-server.toml`), so any value is accepted;
- Save and **restart the game** (or use Reload in the config screen) to apply;
- Spinning **faster than** the value triggers shattering / explosion.

## 🛠 Recipes

Every shaft has three base recipes (dirt shaft example):

| Type | Recipe |
|---|---|
| Crafting (shaped) | **2 matching blocks** stacked in a column → **8 shafts** |
| Stonecutter | **1 matching block** → **8 shafts** |
| Crafting (shapeless, reverse) | **9 shafts** → **1 matching block** |

### Create interoperability (conversion recipes)

Create's machine recipes mostly reference `create:shaft` / cogwheel items directly, so conversion recipes let any material shaft enter Create's crafting chain:

| Recipe (shapeless) | Result |
|---|---|
| any shaft from this mod + 1 × Andesite Alloy | `create:shaft` |
| any shaft from this mod + 1 × any planks | `create:cogwheel` |
| any shaft from this mod + 2 × any planks | `create:large_cogwheel` |

"any shaft from this mod" is the item tag `more_transmission:shafts` (all 158), easy to reference from other mods / datapacks.

## 📦 Installation

1. Install **Minecraft 1.21.1** + **NeoForge 21.1.250+**;
2. Install **Create 6.0.10 or newer** (Create bundles Flywheel / Ponder / Registrate);
3. Drop `more_transmission-neo_1.21.1-1.0.0.jar` into `.minecraft/mods/`;
4. Launch the game — a **"More Transmission"** tab appears in the creative inventory.

> JEI is optional (only for recipe viewing).

## 🧑‍💻 Building from source

```bash
# Build
./gradlew build
# Output: build/libs/more_transmission-neo_1.21.1-1.0.0.jar

# Run a dev client
./gradlew runClient

# Regenerate data (blockstates / item models / loot tables / tags / lang / recipes)
./gradlew runData
```

Generated resources live in `src/generated/resources/` and are committed, so the repo builds out of the box.

## 🌏 Localization

- `en_us`: English (default)
- `zh_cn`: Simplified Chinese (`src/main/resources/assets/more_transmission/lang/zh_cn.json`)

Pull requests for more languages are welcome.

## 🔧 Create compatibility notes

- All shafts extend Create's `ShaftBlock` / `BracketedKineticBlockEntity`; kinetic networks, rotating rendering, brackets, waterlogging and casings are all driven by Create's own logic;
- **Encasing** works exactly like vanilla (`Andesite` / `Brass Casing` on any material shaft), except the **material is kept**: the shaft inside an encased shaft is still your material shaft, and unwrenching it (or breaking it) gives that material back;
- The "max speed" behaviour is injected via Create's `BlockEntityBehaviourEvent` — **no Create code is modified**; it is **off by default** (`enable_shaft_max_speed = false`) and short-circuits completely when disabled;
  - it also applies to encased shafts, and is looked up by the **inner shaft's** material — encasing a shaft does not shield it from its RPM limit;
- An item tag (`more_transmission:shafts`) and conversion recipes are provided for integration with other mods / datapacks.

## 📜 License

Released under the **MIT** license. Thanks to **Create** and its team for the excellent open-source base and API.

## 🐛 Feedback

Questions or suggestions? Open an [Issue](https://github.com/1602335496/More_Transmission/issues).
