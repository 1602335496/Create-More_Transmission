package com.shengzi.more_transmission;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 挂在传送带方块实体上，记住「这一格传送带当初顶掉的是哪根材质轴」。
 *
 * <p>为什么需要记：Create 给传送带贴上来时会把原本的轴**直接销毁**（{@code BeltConnectorItem.createBelts}
 * 里先 {@code destroyBlock(pos, false)} 再换上传送带），而 {@code BeltBlockEntity} 自己只用一个布尔式的
 * {@code hasPulley()}（其实看的是状态里的 {@code PART != MIDDLE}）表示「这格原来是轴」，
 * **没记住是哪一种轴**。所以等传送带被破坏/拆掉时，Create 只能还原成 {@code create:shaft}。
 *
 * <p>本行为把那一格原来的方块存进自己的 NBT，并在还原点由 mixin 读出来替换掉原版轴。
 *
 * <p>值的产生有两条路：放置瞬间由 {@code BeltConnectorItemMixin} 记进 {@link #PENDING} 待认领表，
 * 传送带 BE 第一次 tick（Create 的 {@code SmartBlockEntity.initialize()} 挂行为时）把它认领并写进 NBT；
 * 之后从存档读回来就直接走 NBT 了。待认领表只在「被顶掉 → BE 挂上行为」这一个 tick 之间有效。
 * （右键往中段塞轴那条路由 {@link #setReplacedShaft} 直接改值，不走待认领表。）
 */
public class BeltShaftBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<BeltShaftBehaviour> TYPE = new BehaviourType<>("material_shaft");

	private static final String NBT_KEY = "MoreTransmissionShaft";

	/** 待认领表：轴被传送带顶掉时先把方块记这儿，传送带 BE 挂上行为时认领。按 Level 分开存。 */
	private static final Map<Level, Map<BlockPos, Block>> PENDING = new WeakHashMap<>();

	/** 这一格传送带顶掉的那根材质轴；原本不是材质轴则为 null。 */
	private Block replacedShaft;

	public BeltShaftBehaviour(SmartBlockEntity be) {
		super(be);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public void initialize() {
		// 从存档读回来的 BE 已经在 NBT 里带着值了，所以只在还没有值时才去认领待认领表
		if (replacedShaft != null)
			return;

		Level level = blockEntity.getLevel();
		if (level == null)
			return;

		Map<BlockPos, Block> byPos = PENDING.get(level);
		if (byPos == null)
			return;

		Block claimed = byPos.remove(blockEntity.getBlockPos());
		if (claimed != null) {
			replacedShaft = claimed;
			blockEntity.setChanged();
			// 必须主动同步一次：客户端要靠这个值才知道滑轮里该画哪种材质。
			// 不能指望 Create 自己会发——传送带 BE 第一次 tick 时 attachKinetics() 里的 sendData()
			// 跑在 SmartBlockEntity.initialize()（也就是本方法）**之前**，那一包里还没有这个字段。
			blockEntity.sendData();
		}
	}

	/** 由 {@code BeltConnectorItemMixin} 在传送带顶掉一根材质轴的那一刻调用。 */
	public static void remember(Level level, BlockPos pos, Block block) {
		if (level == null || level.isClientSide)
			return;

		Map<BlockPos, Block> byPos = PENDING.computeIfAbsent(level, l -> new HashMap<>());
		// 正常路径下条目会在下一 tick 被认领掉；清一下是防止放置中途失败导致条目堆积
		if (byPos.size() > 64)
			byPos.clear();
		byPos.put(pos.immutable(), block);
	}

	/**
	 * 把某一格传送带记着的材质轴直接改成给定值（{@code material} 为 null 表示清掉）。
	 *
	 * <p>给「右键往传送带中段塞轴」那条路用（见 {@code BeltBlockMixin}）：那里 BE 早就挂上行为、
	 * 也早就 tick 过了，所以走不了 {@link #remember} 那张待认领表（它只在 {@code initialize()} 时被认领），
	 * 得直接找到行为改值。
	 */
	public static void setReplacedShaft(Level level, BlockPos pos, Block material) {
		if (level == null || level.isClientSide)
			return;
		if (!(level.getBlockEntity(pos) instanceof SmartBlockEntity smart))
			return;

		BeltShaftBehaviour behaviour = smart.getBehaviour(TYPE);
		if (behaviour == null || behaviour.replacedShaft == material)
			return;

		behaviour.replacedShaft = material;
		smart.setChanged();
		// 客户端要靠这个值决定滑轮里该画哪种材质；清空同样得同步过去，否则那边会一直留着旧材质
		smart.sendData();
	}

	/**
	 * 取某一格记着的材质轴：先问 BE 的 NBT，**BE 还没认领到**就回落到待认领表，并顺手把那条消费掉。
	 *
	 * <p>为什么需要这条回落：{@code BeltBlockEntity.tick()} 里
	 * {@code initBelt}（链校验失败会当场**带掉落**拆掉传送带）跑在 {@code super.tick()} **之前**，
	 * 而认领发生在 {@code super.tick() → SmartBlockEntity.initialize()} 里，行为也是在那时才由
	 * {@code BlockEntityBehaviourEvent} 挂上的。所以传送带只要没撑到第一次 tick 就没了
	 * （典型场景：两个相邻轴上各挂一个小齿轮、齿轮互相啮合，贴带后 {@code initBelt} 判定链无效当场拆掉它），
	 * 认领就永远不会发生——掉落与还原于是都退化成原版轴，玩家的材质就这么没了。
	 *
	 * <p>消费掉是必须的：同一个位置之后可能再放一条传送带，留着这条旧记录会张冠李戴。
	 * 两个调用点位置不重叠——{@code getDrops} 管被拆的那一格本身，{@code onRemove} 管相邻的链格——
	 * 所以各取各的，不会互相抢。
	 */
	public static Block replacedShaftAt(Level level, BlockPos pos) {
		Block recorded = replacedShaftOf(level.getBlockEntity(pos));
		if (recorded != null)
			return recorded;

		Map<BlockPos, Block> byPos = PENDING.get(level);
		return byPos == null ? null : byPos.remove(pos);
	}

	/** 这一格原本的那根材质轴，没有记录则返回 null。 */
	public Block getReplacedShaft() {
		return replacedShaft;
	}

	@Override
	public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		// 永远写：空串代表「没有记录」。只写非 null 值的话，后来被清掉的记录传不到客户端
		tag.putString(NBT_KEY, replacedShaft == null ? "" : BuiltInRegistries.BLOCK.getKey(replacedShaft)
			.toString());
	}

	@Override
	public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		// 连键都没有的（老存档、或压根不是本模组记过的传送带）保持原值，别误清
		if (!tag.contains(NBT_KEY))
			return;

		// 键在但解析不出材质轴——空串，或存档里那个方块已经没了——按「没有记录」处理
		ResourceLocation id = ResourceLocation.tryParse(tag.getString(NBT_KEY));
		replacedShaft = id != null && BuiltInRegistries.BLOCK.get(id) instanceof MoreShaftBlock shaft ? shaft : null;
	}

	/**
	 * 取某个方块实体上记着的材质轴；不是本模组记过的传送带一格、或原本就不是材质轴，都返回 null。
	 * 给还原点的 mixin 用。
	 */
	public static Block replacedShaftOf(BlockEntity blockEntity) {
		if (!(blockEntity instanceof SmartBlockEntity smart))
			return null;
		BeltShaftBehaviour behaviour = smart.getBehaviour(TYPE);
		return behaviour == null ? null : behaviour.getReplacedShaft();
	}
}
