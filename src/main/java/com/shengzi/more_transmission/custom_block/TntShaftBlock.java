package com.shengzi.more_transmission.custom_block;

import javax.annotation.Nullable;

import com.shengzi.more_transmission.MoreShaftBlock;
import com.shengzi.more_transmission.OverspeedCrumbleBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * TNT 传动杆：可被火/打火石/火焰弹点燃，一点燃就立即爆炸（不做引信延迟）。
 * 超过最大转速时，{@link OverspeedCrumbleBehaviour} 也会调用 {@link #explodePos} 让它爆炸。
 * 爆炸威力与机制等同原版 TNT（半径 4.0，ExplosionInteraction.TNT）。
 */
public class TntShaftBlock extends MoreShaftBlock {

	public TntShaftBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 在指定位置执行一次 TNT 当量的立即爆炸，并移除该方块（不掉落物品）。 */
	public static void explodePos(Level level, BlockPos pos) {
		if (level.isClientSide)
			return;
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0F,
			Level.ExplosionInteraction.TNT);
	}

	@Override
	public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
		@Nullable LivingEntity igniter) {
		explodePos(level, pos);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, InteractionHand hand, BlockHitResult hitResult) {
		// 打火石/火焰弹右键 → 立即爆炸；否则交给普通轴行为（如扳手/封套右键交互）
		if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE))
			return super.useItemOn(stack, state, level, pos, player, hand, hitResult);

		explodePos(level, pos);
		if (stack.is(Items.FLINT_AND_STEEL))
			stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
		else
			stack.consume(1, player);
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}
}
