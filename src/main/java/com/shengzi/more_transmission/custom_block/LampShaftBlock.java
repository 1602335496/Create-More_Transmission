package com.shengzi.more_transmission.custom_block;

import com.shengzi.more_transmission.MoreShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 「灯轴」：转动时点亮、并像红石块一样输出红石信号的一类轴。
 *
 * <p>目前有 9 根：**红石灯传动杆** + **8 根铜灯传动杆**（铜灯四态 + 各自涂蜡）。它们的 Java 行为完全一致，
 * 差别只在注册时的材质、贴图和名字，所以共用这一个类；具体登记见 {@code ModBlocks.litShaft}。
 *
 * <p><b>亮度不用自己算</b>：注册时 {@code initialProperties(() -> material)} 走的是 {@code Properties.ofFullCopy}，
 * 而原版这两个方块的亮度就写在物性里，且用的是同一个 {@link BlockStateProperties#LIT}：
 * <ul>
 *   <li>红石灯：{@code lightLevel(state -> LIT ? 15 : 0)}</li>
 *   <li>铜灯：{@code litBlockEmission(n)}，n 随氧化程度递减——铜灯 15 / 斑驳 12 / 锈蚀 8 / 氧化 <b>4</b></li>
 * </ul>
 * {@code Properties.ofLegacyCopy} 会把这份 {@code lightEmission} 函数一并复制过来，于是**四档亮度自动正确**，
 * 本类无需覆写 {@code getLightEmission}。前提是属性必须复用原版那个 {@link #LIT} 对象，不能自己新建。
 *
 * <p>红石信号也挂在同一个 {@link #LIT} 上（见 {@link #getSignal}）：**状态、发光、红石信号三者共用一份判断**，
 * 所以永远同步，不需要各写一套。
 *
 * <p>什么时候亮由 {@link com.shengzi.more_transmission.LampShaftBehaviour} 按转速决定；
 * 亮/灭采用两个不同的方块模型，这样客户端换状态时 Flywheel 会重建 visual、贴图跟着换。
 */
public class LampShaftBlock extends MoreShaftBlock {

	/**
	 * 是否点亮。刻意复用原版的 {@link BlockStateProperties#LIT}——上面提到的两份亮度函数按的都是这个属性对象，
	 * 换成自己新建的 BooleanProperty 会让它们取不到值。
	 */
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public LampShaftBlock(Properties properties) {
		super(properties);
		// LIT 默认就是 false（未点亮），与其它轴一致，所以不必再 registerDefaultState。
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIT);
	}

	/**
	 * 与 {@link RedstoneShaftBlock} 一样把红石信号发出去，区别只是这里挂在 {@link #LIT} 上：
	 * 转动 → 点亮 → 发出 **15 级红石信号**，停下就一起断掉。
	 *
	 * <p>写法沿用原版红石火把那套——{@code isSignalSource} 恒为 true、由 {@code getSignal} 按状态给值。
	 * 不写成 {@code return state.getValue(LIT)} 是为了避开「方块从非信号源变成信号源」这个边角：
	 * {@code Level#getSignal} 本来就会用 {@code isSignalSource()} 去兜 {@code getSignal} 的结果，
	 * 所以恒 true 更省心，代价只是未点亮时多被问一次。
	 *
	 * <p>信号变化靠 {@link com.shengzi.more_transmission.LampShaftBehaviour} 那次 `setBlock`
	 * （带 {@code UPDATE_NEIGHBORS}）通知邻居，所以不需要在这里额外调度什么。
	 */
	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(LIT) ? 15 : 0;
	}
}
