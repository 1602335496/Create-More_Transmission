package com.shengzi.more_transmission;

import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

/**
 * 封套轴的 Flywheel visual：外壳（Casing）由方块自身的静态模型画，这里只负责里面那根转动的杆。
 *
 * 和 {@link MoreShaftVisual} 的区别是「用哪个模型」要问 BE 里面包的是哪种材质，
 * 于是委托给 {@link MoreShaftVisual#modelFor}——包着钻石轴就转钻石杆，包着玻璃轴也会走 translucent 渲染层。
 *
 * 之所以不像 {@code SingleAxisRotatingVisual} 那样在构造时把模型定死，而是每帧对一次：
 * 内轴材质是跟着方块实体数据包（{@link MoreEncasedShaftBlockEntity#setInnerShaft} 里的 notifyUpdate）
 * 同步到客户端的，而 Flywheel 建 visual 的时机和这个包到达的时机没有先后保证。
 * 万一 visual 先建好，定死就会一直转着原版轴的杆，只能靠重载区块才恢复正常。
 */
public class MoreEncasedShaftVisual extends KineticBlockEntityVisual<MoreEncasedShaftBlockEntity>
	implements SimpleTickableVisual {

	/** Flywheel visual 工厂，注册给 BE 类型后由 Flywheel 在方块进入视野时调用。 */
	public static MoreEncasedShaftVisual create(VisualizationContext context, MoreEncasedShaftBlockEntity blockEntity,
		float partialTick) {
		return new MoreEncasedShaftVisual(context, blockEntity, partialTick);
	}

	private RotatingInstance rotatingModel;

	/** 当前这根 instance 用的是哪种材质——换来和 {@link MoreEncasedShaftBlockEntity#getInnerShaft()} 比对。 */
	private Block renderedShaft;

	private MoreEncasedShaftVisual(VisualizationContext context, MoreEncasedShaftBlockEntity blockEntity,
		float partialTick) {
		super(context, blockEntity, partialTick);
		renderedShaft = blockEntity.getInnerShaft();
		rotatingModel = newInstance(renderedShaft);
	}

	/** 按指定材质建一个可旋转 instance（模型由 Flywheel 按 instancer 缓存，换材质就是换一个 instancer）。 */
	private RotatingInstance newInstance(Block shaft) {
		RotatingInstance instance = instancerProvider()
			.instancer(AllInstanceTypes.ROTATING, MoreShaftVisual.modelFor(shaft))
			.createInstance()
			.rotateToFace(Direction.UP, rotationAxis())
			.setup(blockEntity)
			.setPosition(getVisualPosition());
		instance.setChanged();
		return instance;
	}

	@Override
	public void update(float partialTick) {
		Block shaft = blockEntity.getInnerShaft();
		if (shaft != renderedShaft) {
			rotatingModel.delete();
			renderedShaft = shaft;
			rotatingModel = newInstance(shaft);
			// 必须当场补一次光照：Flywheel 的 updateLight 只在「visual 构造后」与「所在区块发生光照更新时」
			// 各调一次，不是每帧都调（见 LightUpdatedVisual 的 javadoc）。而这里的换实例发生在构造之后，
			// 漏掉这行新实例的光就是 0 → 整根杆全黑，且会一直黑到附近碰巧发生一次光照更新才恢复。
			// 触发本次 update 的正是内轴材质同步包（Create 在 KineticBlockEntity.read 里对 clientPacket
			// 调 queueUpdate），所以修复前表现为「刚包好的那根是黑的，再包一根前一根才变正常」。
			relight(rotatingModel);
		}
		rotatingModel.setup(blockEntity)
			.setChanged();
	}

	@Override
	public void tick(Context context) {
		applyOverstressEffect(blockEntity, rotatingModel);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(rotatingModel);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}
}
