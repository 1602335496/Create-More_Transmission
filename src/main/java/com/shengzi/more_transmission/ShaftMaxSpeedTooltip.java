package com.shengzi.more_transmission;

import com.simibubi.create.foundation.item.TooltipModifier;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 在物品提示框里追加一行「该传动轴的最大转速(RPM)」。
 *
 * 数值实时读 {@link Config}（即玩家配置文件里设的值），所以改配置后重启即可看到更新。
 * {@link Config#maxSpeed} 在「总开关关闭（默认）」和「该方块没有配置条目」两种情况下都返回 -1，
 * 所以这里用 {@code maxSpeed >= 0} 一次就把两种情况都挡掉了——默认配置下根本不会显示这一行。
 */
public class ShaftMaxSpeedTooltip implements TooltipModifier {

	@Override
	public void modify(ItemTooltipEvent context) {
		ItemStack stack = context.getItemStack();
		if (!(stack.getItem() instanceof BlockItem blockItem))
			return;

		String blockPath = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock())
			.getPath();

		// 与「最大转速」有关的提示：总开关关着（默认）时 maxSpeed 为 -1，整块都跳过。
		int maxSpeed = Config.maxSpeed(blockPath);
		if (maxSpeed >= 0) {
			context.getToolTip()
				.add(Component.translatable("more_transmission.tooltip.max_speed", maxSpeed)
					.withStyle(ChatFormatting.GOLD));

			// TNT 轴额外提示：转太快会爆炸——也是超速限制的一部分，所以跟着开关一起出现/消失
			if (blockPath.equals("tnt_shaft"))
				context.getToolTip()
					.add(Component.translatable("more_transmission.tooltip.tnt_explode")
						.withStyle(ChatFormatting.RED));
		}

		// 红石轴的提示跟转速无关（讲的是它一直在发红石信号），任何时候都显示。
		if (blockPath.equals("redstone_block_shaft"))
			context.getToolTip()
				.add(Component.translatable("more_transmission.tooltip.redstone_like")
					.withStyle(ChatFormatting.DARK_RED));
	}
}
