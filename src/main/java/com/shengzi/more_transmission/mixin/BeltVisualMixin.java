package com.shengzi.more_transmission.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.shengzi.more_transmission.MorePartialModels;
import com.simibubi.create.content.kinetics.belt.BeltVisual;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/**
 * 把 Create 滑轮里那根原版轴杆抽掉，改由 {@link com.shengzi.more_transmission.MaterialBeltVisual} 自己画。
 *
 * <p>{@code create:block/belt_pulley} 里那个 {@code Axis} 元素是一根 4×4×16 的实心杆，贴
 * {@code create:block/axis}——和一根轴的几何、UV 完全一样。本模组叠上去的材质轴杆本来是靠
 * 等比放大 1.05 把它整个包住来"盖掉"的，但盖得住不透明材质、盖不住透明材质：铜格栅的镂空、
 * 玻璃的半透明打出来的洞，透过去看到的正是它。所以这里让 Create 改用
 * {@link MorePartialModels#BELT_PULLEY_NO_AXIS}（同一份模型、只少那根杆），那根杆统一由我们画。
 *
 * <p>为什么非 mixin 不可：滑轮实例是在 {@code BeltVisual} 构造器里用 {@code getPulleyModel()}
 * 建出来的，而这个方法是 **private**、字段 {@code pulley} 又是 {@code protected final}，
 * 子类既覆写不了也换不掉（{@code update}/{@code updateLight}/{@code _delete} 都还会去碰它）。
 *
 * <p>只换"模型来源"这一处、不重写整个方法：Create 那套朝向变换
 * （{@code getOrientation()} 加 {@code rotateXDegrees}/{@code rotateYDegrees}）必须原样保留，
 * 否则滑轮的**木体**会被转错方向。
 */
@Mixin(BeltVisual.class)
public abstract class BeltVisualMixin {

	/**
	 * 把 {@code getPulleyModel()} 里那次 {@code AllPartialModels.BELT_PULLEY} 的读取换成没有轴杆的那份。
	 *
	 * <p>不写 {@code opcode} 也能匹配（该方法里 {@code BELT_PULLEY} 只被引用这一次），
	 * 而 {@code mixins.json} 里开着 {@code defaultRequire: 1}，匹配数不对会在类加载时直接报错。
	 */
	@Redirect(
		method = "getPulleyModel",
		at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/AllPartialModels;BELT_PULLEY:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"))
	private PartialModel more_transmission$pulleyWithoutAxis() {
		return MorePartialModels.BELT_PULLEY_NO_AXIS;
	}
}
