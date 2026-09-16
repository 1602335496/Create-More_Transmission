package com.shengzi.more_transmission;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 封套轴的「兜底」方块实体渲染器，对应 {@link MoreEncasedShaftVisual}。
 *
 * 正常情况下轮不到它：{@code KineticBlockEntityRenderer.renderSafe} 在 Flywheel 可视化可用时会直接返回，
 * 转动由 {@link MoreEncasedShaftVisual} 负责。但 Flywheel 可视化被关掉时就会走到这里，
 * 所以它按内轴材质取方块模型来转，避免这种情况下封套轴里面的杆变成原版贴图（或干脆不转）。
 */
public class MoreEncasedShaftRenderer extends KineticBlockEntityRenderer<MoreEncasedShaftBlockEntity> {

	public MoreEncasedShaftRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected BlockState getRenderedBlockState(MoreEncasedShaftBlockEntity blockEntity) {
		// 用内轴方块自己的模型当旋转模型（Create 的 ShaftRenderer 这里是写死的 create:shaft）。
		return blockEntity.getInnerShaft()
			.defaultBlockState()
			.setValue(MoreEncasedShaftBlock.AXIS, getRotationAxisOf(blockEntity));
	}
}
