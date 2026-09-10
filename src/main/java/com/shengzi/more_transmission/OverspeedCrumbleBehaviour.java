package com.shengzi.more_transmission;

import com.shengzi.more_transmission.custom_block.TntShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;

/**
 * 「转速过高会碎」行为：挂在泥土/石等共用轴 BE 类型（BRACKETED_KINETIC）上。
 *
 * 每根轴的最大转速由 {@link Config} 提供（玩家可在配置文件 shaft_max_speed 里改）。
 * 每个游戏刻读一次当前方块转速；转速的绝对值一旦超过上限，就用 destroyBlock 把方块
 * 连同自身拆掉（BE 的 remove() 会自动从动能网络脱钩）。
 *
 * Config 里没有条目（如未来新增、未配置的方块）一律放行不碎裂。
 */
public class OverspeedCrumbleBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<OverspeedCrumbleBehaviour> TYPE =
		new BehaviourType<>("overspeed_crumble");

	public OverspeedCrumbleBehaviour(SmartBlockEntity be) {
		super(be);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public void tick() {
		if (blockEntity.isRemoved())
			return;

		Level level = blockEntity.getLevel();
		if (level == null || level.isClientSide)
			return;

		// 从配置读取该方块的最大转速；未配置(没有对应条目)则原样放行
		String blockPath = BuiltInRegistries.BLOCK.getKey(level.getBlockState(getPos()).getBlock()).getPath();
		int maxSpeed = Config.maxSpeed(blockPath);
		if (maxSpeed < 0)
			return;

		// 转速取绝对值：正转反转都算，超过上限即散架
		float speed = blockEntity instanceof KineticBlockEntity kbe ? Math.abs(kbe.getSpeed()) : 0;
		if (speed > maxSpeed) {
			if (blockEntity.getBlockState().getBlock() instanceof TntShaftBlock)
				TntShaftBlock.explodePos(level, getPos()); // TNT 轴超速 → 爆炸
			else
				level.destroyBlock(getPos(), true); // true = 像被挖掉一样掉落自身
		}
	}
}
