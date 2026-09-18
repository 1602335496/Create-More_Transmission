package com.shengzi.more_transmission;

import com.shengzi.more_transmission.custom_block.LampShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「转起来就点亮」行为：让红石灯传动杆的 {@code LIT} 状态跟随转速。
 *
 * <p>每刻比对一次「是否在转」和「是否点亮」，不一致就 {@code setBlock} 改过去。改完客户端会收到方块更新，
 * Flywheel 随即重建该 visual（它的 mixin 挂在 {@code LevelRenderer#setBlockDirty} 尾部，任何状态变化都会
 * remove+add），所以贴图切换不必在 visual 里做动态换模型；亮度则由
 * {@link LampShaftBlock} 从原版红石灯继承来的 {@code lightLevel} 函数给出。
 *
 * <p>本行为对**所有**材质轴都会挂上：不能改成按方块类型条件挂——在同一个位置把普通轴换成红石灯轴时，
 * 两者 BE 类型相同，vanilla 会直接复用旧 BE、behaviour 事件不会重跑，那样灯就永远不亮。
 * 代价只是每刻一次 {@code instanceof}，所以第一行就早退。
 */
public class LampShaftBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<LampShaftBehaviour> TYPE =
		new BehaviourType<>("lamp_shaft");

	/**
	 * 判断改变前要先连续成立这么多刻。见 {@link #isSpinning()}：放置瞬间的转速抖动大多只有一两刻，
	 * 卡一下就不会跟着闪；真在转的轴最多晚 0.15 秒点亮，看不出来。
	 */
	private static final int CONFIRM_TICKS = 3;

	/** 「当前判断已经连续成立多少刻」。 */
	private int stableTicks;

	public LampShaftBehaviour(SmartBlockEntity be) {
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

		BlockState state = blockEntity.getBlockState();
		if (!(state.getBlock() instanceof LampShaftBlock)) {
			stableTicks = 0;
			return;
		}

		boolean spinning = isSpinning();
		if (spinning == state.getValue(LampShaftBlock.LIT)) {
			stableTicks = 0;
			return;
		}
		if (++stableTicks < CONFIRM_TICKS)
			return;

		stableTicks = 0;
		level.setBlock(getPos(), state.setValue(LampShaftBlock.LIT, spinning),
			Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
	}

	/**
	 * 是否算「正在转」。
	 *
	 * <p>关键是不能只看 {@code getSpeed() != 0}：放置瞬间方块可能先拿到一个速度、而 {@code source}
	 * 还没接上（见 {@code RotationPropagator} 的传播流程）。这种「无源的速度」Create 自己会在
	 * {@code validateKinetics()} 里清掉——它只在 {@code !hasSource()} 且生成速度为 0 时把 speed 置 0，
	 * 而且**不调 sendData()**，间隔是 {@code kineticValidationFrequency}（默认 60 刻 = 3 秒）。
	 * 只按 getSpeed() 判断的话，灯会在放置后亮起、约 3 秒后被那一下静默清零弄灭，
	 * 表现就是「放置时亮一下再熄灭」。加上 {@code hasSource()} 正好把这整类情况排除掉——
	 * 对普通轴来说速度非 0 本来就该有 source，没有就是还没稳定。
	 */
	private boolean isSpinning() {
		if (!(blockEntity instanceof KineticBlockEntity kinetic))
			return false;
		return kinetic.getSpeed() != 0 && kinetic.hasSource();
	}
}
