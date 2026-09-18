package com.shengzi.more_transmission;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * 传送带滑轮里那根轴，换成"当初被顶掉的那根材质轴"的模型。
 *
 * <p>Create 的 {@code BeltVisual} 画滑轮时用的是写死的 {@code AllPartialModels.BELT_PULLEY}，
 * 而那个模型里的 {@code Axis} 元素贴的是 {@code create:block/axis} / {@code axis_top}——
 * 也就是**原版轴的贴图**。所以哪怕这一格原本是本模组的钻石轴，滑轮里露出来的杆也是原版样式。材质能在
 * 数据上保留（见 {@link BeltShaftBehaviour}），但视觉上还得自己补一根杆。
 *
 * <p>做法是**继承 Create 的 {@code BeltVisual} 再叠一个旋转实例**：Create 那套传送带 / 滑轮木体照常由父类画，
 * 那根杆由 {@link #rodModel} 按材质画——没记材质（这一格原本就不是本模组的轴）就画原版轴那根，
 * 两种情况的几何都跟原来那根一模一样。
 *
 * <p>Create 自带的那个 {@code Axis} 元素是**抽掉**的，不是盖住的：{@code BeltVisualMixin} 让 Create 改用
 * {@link MorePartialModels#BELT_PULLEY_NO_AXIS}。一开始的办法是把我们这根等比放大 1.05 去盖住它——
 * 盖得住不透明材质，却盖不住透明材质：铜格栅的镂空、玻璃的半透明打出来的洞，透过去看到的正是它，
 * 于是"洞"永远透不到背景。抽掉之后洞才真的通，放大那 5%（连同它带来的端面 z-fighting）也就不需要了。
 *
 * <p>注册方式见 {@code More_transmission.ClientModEvents}：用 Flywheel 的
 * {@code VisualizerRegistry.setVisualizer} 把 {@code AllBlockEntityTypes.BELT} 的 visual 换成这个子类。
 *
 * <p>注意兜底渲染器（Flywheel 可视化关闭时的 {@code BeltRenderer}）没走这条路，那种情况下仍是原版样式。
 */
public class MaterialBeltVisual extends BeltVisual {

	/** 那根杆的模型按材质缓存（key 为 null 表示"画原版轴那根"），避免每格传送带都重新烘焙一次。 */
	private static final Map<Block, Model> BELT_ROD_MODELS = new HashMap<>();

	private RotatingInstance pulleyShaft;
	/** 当前这根画的是哪种材质；null = 这一格原本不是本模组的轴，画的是原版轴那根。 */
	private Block renderedShaft;

	public MaterialBeltVisual(VisualizationContext context, BeltBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		renderedShaft = BeltShaftBehaviour.replacedShaftOf(blockEntity);
		pulleyShaft = newPulleyShaft(renderedShaft);
	}

	private RotatingInstance newPulleyShaft(Block material) {
		if (!blockEntity.hasPulley())
			return null;

		// 与 Create 的 getOrientation() 同款：滑轮那根轴垂直于传送带走向；横向传送带则朝上
		Direction dir = blockState.getValue(BeltBlock.HORIZONTAL_FACING)
			.getClockWise();
		if (blockState.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS)
			dir = Direction.UP;

		RotatingInstance instance = instancerProvider()
			.instancer(AllInstanceTypes.ROTATING, rodModel(material))
			.createInstance()
			.rotateToFace(Direction.UP, dir.getAxis())
			.setup(blockEntity)
			.setPosition(getVisualPosition());
		instance.setChanged();
		return instance;
	}

	/**
	 * 那根杆的模型。{@code material} 为 null（这一格原本就不是本模组的轴）时用原版轴那根——
	 * {@code create:block/shaft} 与 Create 滑轮模型里被抽掉的那个 {@code Axis} 元素逐字段相同，
	 * 所以画出来和原版一模一样。
	 *
	 * <p>模型与朝向无关（转哪个轴靠 instance 的 {@code rotateToFace}），也不需要任何变换——
	 * Create 那根原版杆已经由 {@code BeltVisualMixin} 抽掉了，没有东西要盖。所以这里只需按材质缓存。
	 */
	private static Model rodModel(Block material) {
		return BELT_ROD_MODELS.computeIfAbsent(material, m -> {
			if (m == null)
				return Models.partial(AllPartialModels.SHAFT);

			// 渲染层不能省：玻璃类轴的贴图带半透明像素、铜格栅带镂空，照默认的 solid 烘焙会让这些
			// 像素按实心画出来（就是"透明像素变黑"）。世界内那根转动的杆在 MoreShaftVisual 里
			// 用 translucent()/cutout() 单独处理过，传送带这根本来漏了这件事。
			return MoreShaftVisual.bake(partialFor(m), MoreShaftVisual.renderTypesFor(m), null);
		});
	}

	/**
	 * 由材质方块反推它的旋转 partial——本模组的约定就是 {@code block/<轴注册名>}，
	 * 而这些 partial 开机时已经由 {@code MorePartialModels} 注册过，
	 * {@link PartialModel#of} 又是带缓存的，所以这里拿到的是同一个已加载的实例。
	 */
	private static PartialModel partialFor(Block material) {
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(material);
		return PartialModel.of(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()));
	}

	@Override
	public void update(float pt) {
		super.update(pt);

		// 材质是随方块实体同步包过来的，可能晚于 visual 建立，所以要每帧对一次
		Block material = BeltShaftBehaviour.replacedShaftOf(blockEntity);
		if (material != renderedShaft) {
			if (pulleyShaft != null)
				pulleyShaft.delete();
			renderedShaft = material;
			pulleyShaft = newPulleyShaft(material);
			if (pulleyShaft != null)
				relight(pulleyShaft);
		}

		if (pulleyShaft != null)
			pulleyShaft.setup(blockEntity)
				.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
		if (pulleyShaft != null)
			relight(pulleyShaft);
	}

	@Override
	protected void _delete() {
		super._delete();
		if (pulleyShaft != null)
			pulleyShaft.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		if (pulleyShaft != null)
			consumer.accept(pulleyShaft);
	}
}
