package com.shengzi.more_transmission.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.shengzi.more_transmission.MoreShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.world.level.block.state.BlockState;

/**
 * 让 Create 的「这算不算一根轴」判定认下本模组的材质轴。
 *
 * <p>Create 的 {@code ShaftBlock.isShaft} 实现是 {@code AllBlocks.SHAFT.has(state)}——
 * 也就是「方块**正好是** {@code create:shaft}」。它既不是 tag 也不是 datamap，API 包里也没有对应钩子，
 * 所以下游加多少轴都进不去。而这个判定有 4 个调用点，全在传送带那一套里：
 * {@code BeltConnectorItem.validateAxis}（起终点）、{@code canConnect}（中段能否穿过）、
 * {@code createBelts}（中段算不算滑轮）、{@code BeltConnectorHandler}（拖拽时的预览线）。
 * 结果就是本模组的轴无法用传送带连接——既不能当端点，也不能被传送带绕成滑轮。
 *
 * <p>与其在方块里重写一遍传送带交互（只能修端点、还要抄 Create 的内部逻辑），不如直接补上这个判定，
 * 一处修全部调用点。**刻意只认 {@link MoreShaftBlock}**：Create 自己的封套轴、动力轴同样过不了
 * {@code isShaft}，保持和原版一致的行为。
 *
 * <p>这是本模组唯一一处 mixin，也是唯一一处会碰到 Create 类字节码的地方（详见 README 的兼容说明）。
 */
@Mixin(ShaftBlock.class)
public abstract class ShaftBlockMixin {

	/**
	 * 在 {@code isShaft} 开头短路：是本模组的材质轴就直接返回 true，
	 * 其余方块照常走原实现（不 cancel）。
	 */
	@Inject(method = "isShaft", at = @At("HEAD"), cancellable = true)
	private static void more_transmission$treatMaterialShaftsAsShafts(BlockState state,
		CallbackInfoReturnable<Boolean> cir) {
		if (state.getBlock() instanceof MoreShaftBlock)
			cir.setReturnValue(true);
	}
}
