# More Transmission（更多传动）

[中文](#中文) ｜ [English](#english)

---

# 中文

为 **机械动力（Create）** 添加 **144 种不同材质的传动轴**：泥土、木头、石头、矿物块、羊毛、玻璃……总有一种质感适合你的机械装置。

模组以 Create 原版传动轴为基类扩展，因此这些轴**可以像普通 Create 轴一样使用**：传动、连接齿轮、套上齿轮箱/封套、挂支架、含水、被活塞推动……全部照旧；同时每种材质都带有自己的外观、物性，以及可配置的「最大转速」。

> 支持版本：**Minecraft 1.21.1 + NeoForge 21.1.250 + Create 6.0.10 及以上**
> 许可证：MIT ｜ 作者：sheng_zi

## ✨ 特色

- **144 种材质传动轴**：从木头、石头，到矿物块、羊毛、玻璃，几乎把原版方块都做成了轴。
- **材质化物性**：每根轴的硬度、音效、挖掘工具、是否掉落，都**继承对应原版方块**。
  - 木质轴 → 用斧头；松软材质（泥土/沙…）→ 用铲子；石质/矿物 → 用镐。
  - 羊毛轴 → 和羊毛一样：不需要镐，**剪刀 5× / 剑 1.5× 加速**。
  - 玻璃轴 → 和玻璃一样：任意工具都能破坏，但**普通破坏不掉落**，只有**精准采集**才能回收。
- **可配置的最大转速**：每根轴都有转速上限，超过上限会**碎裂**（TNT 轴则是**爆炸**）。数值可在配置文件中逐根调整。
- **原地显示上限**：物品悬浮提示里会显示 `Max Speed`，一眼看出这轴能转多快。
- **特殊机制轴**：
  - 🧨 **TNT 传动轴**：可被火/打火石/火焰弹点燃，一点即爆；转速超限同样会爆炸。
  - 🔴 **红石传动轴**：像红石块一样持续向相邻发出 **15 级红石信号**。
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
| **160** | 铜块、切制铜块、煤炭块、岩浆块 | 4 |
| **192** | 铁块、金块、青金石块、石英块、石英柱、绿宝石块、紫水晶块、红石块 | 8 |
| **224** | 钻石块 | 1 |
| **256** | 黑曜石、下界合金块、基岩 | 3 |

> 合计 **144** 根。上表是**默认值**，全部可在配置文件中修改。

## ⚙️ 配置文件

首次启动游戏时会自动生成：

```
config/more_transmission-common.toml
```

其中 `[shaft_max_speed]` 分组为每根轴提供一条 0~256 的配置（默认即上表数值）：

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

- 值域 `0~256`，`0` 表示一有转速就碎裂；
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

其中「任意本模组传动轴」使用物品标签 `more_transmission:shafts`（含全部 144 根），方便其它模组/数据包引用。

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
- 「最大转速」是本模组额外附加的行为，通过 Create 的 `BlockEntityBehaviourEvent` 注入，不改动 Create 代码；
- 提供 `more_transmission:shafts` 物品标签与转换配方，便于与其它模组/数据包对接。

## 📜 许可

本项目以 **MIT** 许可证开源。感谢 **机械动力（Create）** 及其团队提供了优秀的开源基础与 API。

## 🐛 反馈

遇到问题或有建议，欢迎提 [Issue](https://github.com/1602335496/More_Transmission/issues)。

---

# English

Adds **144 material shafts** to **Create**: dirt, wood, stone, mineral blocks, wool, glass… There is a texture for every machine.

Every shaft extends Create's vanilla shaft, so they **behave exactly like a normal Create shaft**: they transmit rotation, connect to cogwheels, accept casings, hold brackets, support waterlogging and can be moved by pistons — plus each material brings its own look, its own block properties, and a configurable **maximum speed**.

> Supported: **Minecraft 1.21.1 + NeoForge 21.1.250 + Create 6.0.10 or newer**
> License: MIT ｜ Author: sheng_zi

## ✨ Features

- **144 material shafts** — from wood and stone to mineral blocks, wool and glass.
- **Material-derived properties**: hardness, sound, mining tool and drops are **inherited from the matching vanilla block**.
  - Wooden shafts → axe; soft blocks (dirt/sand…) → shovel; stone/mineral → pickaxe.
  - Wool shafts behave like wool: no pickaxe needed, **shears 5× / sword 1.5× faster**.
  - Glass shafts behave like glass: breakable with anything, but **only drop with Silk Touch**.
- **Configurable maximum speed**: each shaft has an RPM limit; exceeding it **shatters** the shaft (the **TNT** shaft **explodes** instead). Every value can be edited per shaft in the config file.
- **Visible limit**: the item tooltip shows `Max Speed` at a glance.
- **Special shafts**:
  - 🧨 **TNT Shaft** — ignites from fire / flint & steel / fire charge and detonates instantly; also explodes when overspun.
  - 🔴 **Redstone Shaft** — emits a constant **redstone signal of 15**, just like a block of redstone.
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
| **160** | copper block, cut copper, coal block, magma block | 4 |
| **192** | iron block, gold block, lapis block, quartz block, quartz pillar, emerald block, amethyst block, redstone block | 8 |
| **224** | diamond block | 1 |
| **256** | obsidian, netherite block, bedrock | 3 |

> **144** shafts in total. The table lists **defaults**; all of them are editable in the config file.

## ⚙️ Configuration

Generated automatically on first launch:

```
config/more_transmission-common.toml
```

The `[shaft_max_speed]` section contains one entry (0–256) per shaft, defaulting to the table above:

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

- Range `0–256`; `0` means it shatters as soon as it spins;
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

"any shaft from this mod" is the item tag `more_transmission:shafts` (all 144), easy to reference from other mods / datapacks.

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
- The "max speed" behaviour is injected via Create's `BlockEntityBehaviourEvent` — **no Create code is modified**;
- An item tag (`more_transmission:shafts`) and conversion recipes are provided for integration with other mods / datapacks.

## 📜 License

Released under the **MIT** license. Thanks to **Create** and its team for the excellent open-source base and API.

## 🐛 Feedback

Questions or suggestions? Open an [Issue](https://github.com/1602335496/More_Transmission/issues).
