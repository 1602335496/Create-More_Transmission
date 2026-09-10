package com.shengzi.more_transmission;

import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 本模组「各种材质的轴」共用的方块基类（泥土轴、石轴、铁轴……都是它的实例）。
 *
 * 继承 Create 的 {@link ShaftBlock} 以免费获得原版轴的全部行为：
 * 轴方向状态、含水、BracketedKineticBlockEntity 旋转渲染、扳手拆除支架等。
 *
 * 目前各材质间没有行为差异，区别只在材质物性/贴图/音效，故暂时不需要覆写任何东西。
 * 以后若想给某类材质加共享逻辑（例如把 ShaftBlock 的齿轮/封套右键交互剥离掉），
 * 可仿照 Create 的 PoweredShaftBlock 改继承 AbstractShaftBlock。
 */
public class MoreShaftBlock extends ShaftBlock {

	public MoreShaftBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/**
	 * 让本方块使用自注册的 BE 类型 more_transmission:bracketed_kinetic（类仍是 Create 的
	 * {@link BracketedKineticBlockEntity}）。IBE#getBlockEntityClass() 无需覆写——
	 * 父类返回的 KineticBlockEntity.class 是 BracketedKineticBlockEntity 的父类，
	 * instanceof / ticker 判断照常成立。
	 */
	@Override
	public BlockEntityType<? extends BracketedKineticBlockEntity> getBlockEntityType() {
		return ModBlockEntities.BRACKETED_KINETIC.get();
	}

}
