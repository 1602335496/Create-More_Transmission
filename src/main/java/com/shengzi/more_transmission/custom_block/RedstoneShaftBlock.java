package com.shengzi.more_transmission.custom_block;

import com.shengzi.more_transmission.MoreShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 红石传动杆：像原版红石块(PoweredBlock)一样持续向相邻发出 15 级红石信号，
 * 本身也是红石信号源（红石线/受电方块贴上去就会被激活）。
 */
public class RedstoneShaftBlock extends MoreShaftBlock {

	public RedstoneShaftBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return 15;
	}
}
