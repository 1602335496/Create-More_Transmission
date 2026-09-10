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
 * 数值实时读 {@link Config}（即玩家配置文件里设的值），所以改配置后重启即可看到更新；
 * 非本模组可碎方块(Config 无条目)自动跳过、不显示。
 */
public class ShaftMaxSpeedTooltip implements TooltipModifier {

	@Override
	public void modify(ItemTooltipEvent context) {
		ItemStack stack = context.getItemStack();
		if (!(stack.getItem() instanceof BlockItem blockItem))
			return;

		String blockPath = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).getPath();
		int maxSpeed = Config.maxSpeed(blockPath);
		if (maxSpeed < 0)
			return;

		context.getToolTip().add(Component.translatable("more_transmission.tooltip.max_speed", maxSpeed)
			.withStyle(ChatFormatting.GOLD));

		// TNT 轴额外提示：转太快会爆炸
		if (blockPath.equals("tnt_shaft"))
			context.getToolTip().add(Component.translatable("more_transmission.tooltip.tnt_explode")
				.withStyle(ChatFormatting.RED));

		// 红石轴额外提示：像红石块一样持续发信号
		if (blockPath.equals("redstone_block_shaft"))
			context.getToolTip().add(Component.translatable("more_transmission.tooltip.redstone_like")
				.withStyle(ChatFormatting.DARK_RED));
	}
}
