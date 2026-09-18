package com.shengzi.more_transmission.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.shengzi.more_transmission.BeltShaftBehaviour;
import com.shengzi.more_transmission.MoreShaftBlock;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * 传送带那几处「只认原版轴」的地方，补上本模组的材质轴：拆带/拆滑轮时把还原回去的原版轴换回材质轴，
 * 以及「往中段塞轴」那一步也认材质轴物品。
 *
 * <p><b>一、还原本方块 / 掉落 / 返还物品。</b>Create 有三处会凭空造一根 {@code create:shaft} 出来
 * （它只记得「这格原来是轴」，不记得是哪种）：
 * <ul>
 *   <li>{@code onRemove}：拆掉传送带时，链上每格原本是轴的位置都还原成原版轴方块（本类的两个注入点）；</li>
 *   <li>{@code getDrops}：被破坏那一格的掉落物里补一根原版轴；</li>
 *   <li>{@code onWrenched}：用扳手把滑轮拆成平带时，返还一根原版轴。</li>
 * </ul>
 *
 * <p>难点是 {@code onRemove} 里 **BE 先被移除、然后才拼出轴的方块状态**，所以不能等到那时再读记录。
 * 做法是在 {@code removeBlockEntity} 那一步先把记录读进静态暂存（此刻 BE 还活着），
 * 紧接着的状态构造再把暂存消费掉。同一个循环迭代内成对出现，不会串到下一格。
 *
 * <p><b>二、往中段塞轴。</b>{@code useItemOn} 里那个判定写死成 {@code AllBlocks.SHAFT.isIn(stack)}
 * （Registrate 的 {@code isIn} 就是 {@code asItem() == item}，认的是物品本身，不是 tag、也没有钩子），
 * 所以材质轴物品塞不进去——见本类末尾那两个注入点。
 *
 * <p>本模组的记录来源见 {@link BeltShaftBehaviour}。
 */
@Mixin(BeltBlock.class)
public abstract class BeltBlockMixin {

	/** 一格之内把「原本的材质轴」从即将销毁的 BE 带到状态构造那一步。 */
	private static Block more_transmission$stash;

	/**
	 * 一格之内把「刚往中段塞进去的那根轴」从判定那一步带到转换那一步。
	 * 取值见 {@link #more_transmission$acceptMaterialShaft}：材质轴方块 / {@code create:shaft}（哨兵）/ null（不是塞轴）。
	 */
	private static Block more_transmission$insertedShaft;

	// ---------- 拆掉传送带时还原本方块 ----------

	@Redirect(
		method = "onRemove",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
	private static void more_transmission$stashBeforeRemoval(Level world, BlockPos pos) {
		// 这里恢复的是**相邻**链格（onRemove 自己那格由 getDrops 管）。用带待认领表回落的取法：
		// 整条带子如果在第一次 tick 之前就被拆了，这些格的 BE 都还没认领到记录。
		more_transmission$stash = BeltShaftBehaviour.replacedShaftAt(world, pos);
		world.removeBlockEntity(pos);
	}

	@Redirect(
		method = "onRemove",
		at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;getDefaultState()Lnet/minecraft/world/level/block/state/BlockState;"))
	private static BlockState more_transmission$restoreMaterialShaft(BlockEntry<?> shaftEntry) {
		Block material = more_transmission$stash;
		more_transmission$stash = null;

		// 原本不是材质轴就照常给原版轴；是材质轴就换成它自己的默认状态
		// （紧接着 Create 会 .setValue(AXIS, ...)，本模组的轴同样有 AXIS，所以那份轴向能接上）
		if (material != null)
			return material.defaultBlockState();

		return shaftEntry.getDefaultState();
	}

	// ---------- 破坏那一格的掉落 ----------

	@Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
	private void more_transmission$materialDrop(BlockState state, LootParams.Builder builder,
		CallbackInfoReturnable<List<ItemStack>> cir) {
		BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		// 带待认领表回落：传送带在第一次 tick 认领之前就被拆掉时（见 BeltShaftBehaviour#replacedShaftAt），
		// 记录只在待认领表里，只查 BE 会退化成原版轴
		Block material = blockEntity == null || blockEntity.getLevel() == null ? null
			: BeltShaftBehaviour.replacedShaftAt(blockEntity.getLevel(), blockEntity.getBlockPos());
		if (material == null)
			return;

		List<ItemStack> drops = cir.getReturnValue();
		List<ItemStack> result = new ArrayList<>(drops.size());
		boolean swapped = false;
		for (ItemStack stack : drops) {
			// 只换一次：Create 只会补一根轴
			if (!swapped && AllBlocks.SHAFT.isIn(stack)) {
				result.add(new ItemStack(material, stack.getCount()));
				swapped = true;
			} else {
				result.add(stack);
			}
		}

		if (swapped)
			cir.setReturnValue(result);
	}

	// ---------- 扳手把滑轮拆成平带时返还的轴 ----------

	@Redirect(
		method = "onWrenched",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;switchToBlockState(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
	private static void more_transmission$stashBeforeUnpulley(Level world, BlockPos pos, BlockState newState) {
		// 这里方块只是从 PULLEY 变成 MIDDLE，BE 还在，可以直接读
		more_transmission$stash = BeltShaftBehaviour.replacedShaftOf(world.getBlockEntity(pos));
		KineticBlockEntity.switchToBlockState(world, pos, newState);
	}

	@Redirect(
		method = "onWrenched",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V"))
	private static void more_transmission$giveMaterialShaft(Inventory inventory, ItemStack stack) {
		Block material = more_transmission$stash;
		more_transmission$stash = null;

		if (material != null && AllBlocks.SHAFT.isIn(stack))
			stack = new ItemStack(material, stack.getCount());

		inventory.placeItemBackInInventory(stack);
	}

	// ---------- 右键往传送带中段塞轴 ----------

	/**
	 * 让「往中段塞轴」这一步也认下本模组的材质轴物品。
	 *
	 * <p>{@code BeltBlock.useItemOn} 里的判定是 {@code AllBlocks.SHAFT.isIn(stack)}，而 Registrate 的
	 * {@code ItemProviderEntry#isIn} 实现就是 {@code is(stack.getItem())} → {@code asItem() == item}：
	 * 写死认 {@code create:shaft} 那个**物品本身**，既不是 tag 也不是 datamap（本模组那条
	 * {@code more_transmission:shafts} 物品标签只服务于自己的转换配方，这里帮不上忙）。这和
	 * {@code ShaftBlock.isShaft} 是同一类问题，只是发生在**物品**一侧。
	 *
	 * <p>同一个方法里 {@code AllBlocks.BRASS_CASING.isIn(stack)} 也会命中这一条 redirect，
	 * 所以**按 receiver 判断、其余原样转交**。不要用 ordinal 去挑那一次调用——Create 一改顺序就会
	 * 静默指到别的判定上（比如把材质轴当成黄铜机壳）。
	 *
	 * <p>顺便把「这次要塞的是什么」暂存给下面那个转换点：材质轴记它自己，原版轴拿
	 * {@code create:shaft} 当哨兵（含义是"把这一格旧的材质记录清掉"，理由见下面那个注入点），
	 * 手里根本不是轴则置 null、不留残留。
	 */
	@Redirect(
		method = "useItemOn",
		at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"))
	private static boolean more_transmission$acceptMaterialShaft(BlockEntry<?> entry, ItemStack stack) {
		boolean isVanillaShaft = entry.isIn(stack);
		if (entry != AllBlocks.SHAFT)
			return isVanillaShaft;

		Block held = stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : null;
		boolean isMaterialShaft = held instanceof MoreShaftBlock;

		if (isMaterialShaft)
			more_transmission$insertedShaft = held;
		else if (isVanillaShaft)
			more_transmission$insertedShaft = AllBlocks.SHAFT.get();
		else
			more_transmission$insertedShaft = null;

		return isVanillaShaft || isMaterialShaft;
	}

	/**
	 * 中段变成滑轮（MIDDLE → PULLEY）之后，把这一格记着的材质轴更新成刚塞进去的那根。
	 *
	 * <p>必须在 {@code switchToBlockState} **之后**再去取 BE：转换本身会动方块实体，先记可能记到旧的上面。
	 * 用 {@link BeltShaftBehaviour#setReplacedShaft} 而不是 {@code remember}——那条待认领表只在 BE 第一次
	 * 挂行为时（{@code initialize()}）被认领，而这里的传送带早就 tick 过了。
	 *
	 * <p>塞进去的是**原版轴**时传 null 清掉记录：这一格若还留着先前那根材质轴的记录（例如它原来的滑轮
	 * 被扳手拆掉过，记录没清），不清的话滑轮会显示、并返还那根早就不在的材质轴——等于拿原版轴白换一根
	 * 材质轴。清掉之后「这一格记着的材质」才始终等于它实际插着的那根轴。
	 *
	 * <p>暂存的哨兵值（{@code create:shaft}）表示"塞的是原版轴"；{@code null} 表示这次不是塞轴，什么都不做。
	 * 该方法在 {@code useItemOn} 里只有这一处调用，所以暂存在同一次调用内成对使用。
	 */
	@Redirect(
		method = "useItemOn",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;switchToBlockState(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
	private static void more_transmission$recordInsertedShaft(Level level, BlockPos pos, BlockState newState) {
		Block inserted = more_transmission$insertedShaft;
		more_transmission$insertedShaft = null;

		KineticBlockEntity.switchToBlockState(level, pos, newState);

		if (inserted == null)
			return;

		BeltShaftBehaviour.setReplacedShaft(level, pos, inserted == AllBlocks.SHAFT.get() ? null : inserted);
	}

}
