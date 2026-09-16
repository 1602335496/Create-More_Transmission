package com.shengzi.more_transmission;

import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 「保留材质的封套传动杆」：把本模组的材质轴用 Andesite / Brass Casing 包起来后的样子。
 *
 * 结构照抄 Create 的 {@code EncasedShaftBlock}（同样是 {@link AbstractEncasedShaftBlock} + {@link EncasedBlock}），
 * 唯一区别是里面包的轴不再写死成 create:shaft，而是记在 {@link MoreEncasedShaftBlockEntity#getInnerShaft()} 里：
 * <ul>
 *   <li>封套瞬间：{@link #handleEncasing} 把当前这根材质轴记进 BE；</li>
 *   <li>扳手拆开：{@link #onSneakWrenched} 还原成记录的那种材质轴；</li>
 *   <li>破坏掉落 / 创造模式取方块：也按记录的内轴走，和原版「拆开拿到里面那根轴」的手感一致。</li>
 * </ul>
 *
 * 封套的触发入口不用自己写：{@link MoreShaftBlock} 继承自 Create 的 ShaftBlock，
 * 而 ShaftBlock 已经实现了 EncasableBlock 并在右键时调用 tryEncase；只要在
 * {@code EncasingRegistry} 里把「材质轴 -> 本方块」登记上，右键 Casing 就会自动生效。
 */
public class MoreEncasedShaftBlock extends AbstractEncasedShaftBlock
	implements IBE<MoreEncasedShaftBlockEntity>, SpecialBlockItemRequirement, EncasedBlock {

	private final Supplier<Block> casing;

	public MoreEncasedShaftBlock(Properties properties, Supplier<Block> casing) {
		super(properties);
		this.casing = casing;
	}

	@Override
	public Block getCasing() {
		return casing.get();
	}

	/** 右键 Casing 封套成功后：换成封套态，并把「原来那根是哪种材质」记进新的 BE。 */
	@Override
	public void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player,
		InteractionHand hand, BlockHitResult ray) {
		Block innerShaft = state.getBlock();

		KineticBlockEntity.switchToBlockState(level, pos, defaultBlockState()
			.setValue(RotatedPillarKineticBlock.AXIS, state.getValue(RotatedPillarKineticBlock.AXIS)));

		// switchToBlockState 内部走的是 setBlock，旧 BE 会被就地换成本模组的新 BE，
		// 所以必须等换完再从 level 里取 BE 写材质（换之前拿到的是旧轴的 BE）。
		if (level.getBlockEntity(pos) instanceof MoreEncasedShaftBlockEntity blockEntity)
			blockEntity.setInnerShaft(innerShaft);
	}

	/** 扳手潜行右键：拆掉 Casing，还原成里面那根材质轴（与原版一样不返还 Casing 方块）。 */
	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		BlockPos pos = context.getClickedPos();
		Block innerShaft = getInnerShaft(level.getBlockEntity(pos));

		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
		KineticBlockEntity.switchToBlockState(level, pos, innerShaft.defaultBlockState()
			.setValue(RotatedPillarKineticBlock.AXIS, state.getValue(RotatedPillarKineticBlock.AXIS)));
		return InteractionResult.SUCCESS;
	}

	/**
	 * 破坏掉落物 = 里面那根材质轴（原版封套轴破坏固定掉 create:shaft，这里只是把 shaft 换成对应材质）。
	 *
	 * 注意：本方块在 Registrate 那边仍会生成一张默认的 dropSelf 掉落表，但那条路径不会被执行——
	 * {@link Block#dropResources} 最终调用的就是这个被覆写的方法。
	 */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		return List.of(new ItemStack(getInnerShaft(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY))));
	}

	/** 创造模式取方块：点到轴端那一面给内轴，点到侧面给 Casing（和原版一致）。 */
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		if (target instanceof BlockHitResult hitResult)
			return hitResult.getDirection()
				.getAxis() == getRotationAxis(state) ? new ItemStack(getInnerShaft(level.getBlockEntity(pos)))
					: getCasing().asItem()
						.getDefaultInstance();
		return super.getCloneItemStack(state, target, level, pos, player);
	}

	/** 蓝图/原理图打印时需要的材料：按内轴材质算（与原版按 create:shaft 算是同一个道理）。 */
	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
		return ItemRequirement.of(getInnerShaft(be)
			.defaultBlockState(), be);
	}

	@Override
	public Class<MoreEncasedShaftBlockEntity> getBlockEntityClass() {
		return MoreEncasedShaftBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends MoreEncasedShaftBlockEntity> getBlockEntityType() {
		return ModBlockEntities.ENCASED_SHAFT.get();
	}

	/** 从 BE 读内轴材质；拿不到（刚放下还没建 BE、或被移除）就按原版轴处理，保证永远返回一个能用的轴方块。 */
	private static Block getInnerShaft(BlockEntity blockEntity) {
		return blockEntity instanceof MoreEncasedShaftBlockEntity encased ? encased.getInnerShaft()
			: AllBlocks.SHAFT.get();
	}
}
