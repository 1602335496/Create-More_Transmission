package com.shengzi.more_transmission;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「保留材质的封套传动杆」的方块实体。
 *
 * 原版 Create 的 create:andesite_encased_shaft 内部固定包着 create:shaft，材质没得选；
 * 本模组的封套轴要在里面装任意一种 {@link MoreShaftBlock}，所以额外用一个字段把「内轴方块」记下来：
 * <ul>
 *   <li>渲染：{@link MoreEncasedShaftVisual} 按它选该材质的旋转模型；</li>
 *   <li>拆除：{@link MoreEncasedShaftBlock#onSneakWrenched} 按它还原成对应的材质轴；</li>
 *   <li>掉落/取方块：{@link MoreEncasedShaftBlock#getDrops}、{@link MoreEncasedShaftBlock#getCloneItemStack} 同理。</li>
 * </ul>
 *
 * 转速、应力传播、超速表现等全部继承 {@link KineticBlockEntity}，与原版封套轴完全一致。
 */
public class MoreEncasedShaftBlockEntity extends KineticBlockEntity {

	/** NBT/网络包里的键名，存内轴方块的注册名（如 more_transmission:diamond_block_shaft）。 */
	private static final String INNER_SHAFT = "InnerShaft";

	/** 内轴方块。默认取原版轴，读档时认不出来（旧存档 / 该方块被移除）也会留在它上面兜底。 */
	private Block innerShaft = AllBlocks.SHAFT.get();

	public MoreEncasedShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Block getInnerShaft() {
		return innerShaft;
	}

	/** 记下内轴材质并同步给客户端——Flywheel 渲染时要用，所以不能只 setChanged 不发包。 */
	public void setInnerShaft(Block block) {
		if (block == null || block == innerShaft)
			return;
		innerShaft = block;
		notifyUpdate();
	}

	/**
	 * 走 Create 的 write/read 而不是原版 saveAdditional/loadAdditional：
	 * SyncedBlockEntity 的 getUpdateTag/onDataPacket 也是转到这里，
	 * 所以写在这儿的字段会同时进存档和客户端同步包，两端都能拿到材质。
	 */
	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
		compound.putString(INNER_SHAFT, BuiltInRegistries.BLOCK.getKey(innerShaft)
			.toString());
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		if (!compound.contains(INNER_SHAFT))
			return;
		ResourceLocation id = ResourceLocation.tryParse(compound.getString(INNER_SHAFT));
		if (id == null)
			return;
		// 只认本模组的材质轴：万一存档里的方块已经不存在（注册表返回 air）或换成了别的模组的轴，
		// 就保持原版轴不变，避免渲染和拆除时拿到一个不能当轴用的方块。
		if (BuiltInRegistries.BLOCK.get(id) instanceof MoreShaftBlock shaft)
			innerShaft = shaft;
	}
}
