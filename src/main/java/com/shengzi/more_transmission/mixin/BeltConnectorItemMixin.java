package com.shengzi.more_transmission.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.shengzi.more_transmission.BeltShaftBehaviour;
import com.shengzi.more_transmission.MoreShaftBlock;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 贴传送带时，把「这一格原本的材质轴」记下来。
 *
 * <p>{@code BeltConnectorItem.createBelts} 放置每一格时的顺序是：
 * <pre>
 * if (!existingBlock.canBeReplaced())
 *     world.destroyBlock(pos, false);      // ← 轴不是可替换方块，在这里被直接销毁（不掉落）
 * KineticBlockEntity.switchToBlockState(world, pos, 传送带状态);
 * </pre>
 * 所以「轴被顶掉」的真正瞬间是 {@code destroyBlock} 那一行——此刻方块还是轴，再晚一步就没了。
 * 这里就把那时的方块交给 {@link BeltShaftBehaviour#remember}，等传送带 BE 第一次 tick 挂上行为时认领。
 */
@Mixin(BeltConnectorItem.class)
public abstract class BeltConnectorItemMixin {

	@Redirect(
		method = "createBelts",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
	private static boolean more_transmission$rememberReplacedShaft(Level world, BlockPos pos, boolean drop) {
		BlockState replaced = world.getBlockState(pos);

		boolean result = world.destroyBlock(pos, drop);

		if (replaced.getBlock() instanceof MoreShaftBlock shaft)
			BeltShaftBehaviour.remember(world, pos, shaft);

		return result;
	}
}
