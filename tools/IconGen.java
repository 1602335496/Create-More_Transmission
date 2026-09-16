import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * More Transmission 模组图标生成器（无第三方依赖，只用 JDK 的 Java2D）。
 *
 * 设计语言参考机械动力（Create）官方 icon：圆形徽章 + 蓝色工程网格 + 等距(isometric)
 * 立体传动组件 + 白色贴纸描边。本模组的不同点落在「材质」上——传动杆由多段不同材质的
 * 六棱柱拼成，一眼看出「很多种材质的轴」。
 *
 * 用法：
 *     java tools/IconGen.java <输出目录>
 * 输出 variant-a / variant-b / variant-c，各含 256×256、512×512、128×128 三种尺寸。
 * 改下面的参数（轴倾角、材质分段、配色、缩放）即可微调，重跑即可再生成。
 */
public class IconGen {

	// ---------- 画布与投影 ----------
	static final int SS = 1024;                    // 超高采样画布，最后再缩到目标尺寸
	static final double COS30 = Math.cos(Math.toRadians(30));
	static final double SIN30 = 0.5;
	static final double VIEW = 1 / Math.sqrt(3);   // 等距视角 (1,1,1)

	static double scale = 132;                     // 世界单位 → 像素
	static double cx = SS / 2.0;
	static double cy = SS / 2.0;

	/** 等距正投影：世界坐标 (x 右, y 上, z 朝观察者) → 屏幕像素。 */
	static double[] proj(double[] p) {
		return new double[] {
			cx + (p[0] - p[2]) * COS30 * scale,
			cy + ((p[0] + p[2]) * SIN30 - p[1]) * scale
		};
	}

	// ---------- 向量小工具 ----------
	static double[] v(double x, double y, double z) { return new double[] {x, y, z}; }
	static double[] sub(double[] a, double[] b) { return v(a[0] - b[0], a[1] - b[1], a[2] - b[2]); }
	static double[] add(double[] a, double[] b) { return v(a[0] + b[0], a[1] + b[1], a[2] + b[2]); }
	static double[] mul(double[] a, double s) { return v(a[0] * s, a[1] * s, a[2] * s); }
	static double dot(double[] a, double[] b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }
	static double[] cross(double[] a, double[] b) {
		return v(a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]);
	}
	static double[] norm(double[] a) {
		double l = Math.sqrt(dot(a, a));
		return l < 1e-9 ? v(0, 1, 0) : mul(a, 1 / l);
	}

	// ---------- 光照 ----------
	static final double[] LIGHT = norm(v(-0.42, 0.84, 0.52));

	/** 用面法线算卡通式明暗：法线朝向光源则更亮。 */
	static Color shade(Color base, double[] n) {
		double d = Math.max(0, dot(norm(n), LIGHT));
		return scale(base, 0.60 + 0.66 * d);
	}

	static Color scale(Color c, double f) {
		return new Color(clamp((int) Math.round(c.getRed() * f)),
			clamp((int) Math.round(c.getGreen() * f)),
			clamp((int) Math.round(c.getBlue() * f)), c.getAlpha());
	}

	static int clamp(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }

	// ---------- 面 ----------
	static class Face implements Comparable<Face> {
		final double[] xs;
		final double[] ys;
		final double depth;
		final Color color;

		Face(double[] xs, double[] ys, double depth, Color color) {
			this.xs = xs;
			this.ys = ys;
			this.depth = depth;
			this.color = color;
		}

		@Override
		public int compareTo(Face o) { return Double.compare(depth, o.depth); }

		Path2D.Double path() {
			Path2D.Double p = new Path2D.Double();
			p.moveTo(xs[0], ys[0]);
			for (int i = 1; i < xs.length; i++)
				p.lineTo(xs[i], ys[i]);
			p.closePath();
			return p;
		}
	}

	static final List<Face> FACES = new ArrayList<>();

	/** 收集一个多边形面（世界坐标），颜色按法线自动上明暗。 */
	static void face(double[][] pts, double[] normal, Color base) {
		int n = pts.length;
		double[] xs = new double[n];
		double[] ys = new double[n];
		double d = 0;
		for (int i = 0; i < n; i++) {
			double[] s = proj(pts[i]);
			xs[i] = s[0];
			ys[i] = s[1];
			d += dot(pts[i], v(VIEW, VIEW, VIEW));
		}
		FACES.add(new Face(xs, ys, d / n, shade(base, normal)));
	}

	// ---------- 几何体 ----------

	/** 与 axis 正交的一组基 (u, w)，用于摆放横截面。 */
	static double[][] basis(double[] axis) {
		double[] up = Math.abs(dot(axis, v(0, 1, 0))) > 0.92 ? v(1, 0, 0) : v(0, 1, 0);
		double[] u = norm(cross(up, axis));
		return new double[][] {u, cross(axis, u)};
	}

	/** 六棱柱的一节；rot 是横截面相位，cap0/cap1 控制两端端面。 */
	static void prism(double[] c0, double[] c1, double r, double rot, Color base,
		boolean cap0, boolean cap1) {
		double[] axis = norm(sub(c1, c0));
		double[][] bw = basis(axis);
		double[] u = bw[0], w = bw[1];

		double[] th = new double[6];
		for (int i = 0; i < 6; i++)
			th[i] = rot + Math.toRadians(60 * i);

		for (int i = 0; i < 6; i++) {
			double a0 = th[i], a1 = th[(i + 1) % 6];
			double[] d0 = add(mul(u, r * Math.cos(a0)), mul(w, r * Math.sin(a0)));
			double[] d1 = add(mul(u, r * Math.cos(a1)), mul(w, r * Math.sin(a1)));
			double am = (a0 + a1) / 2;
			face(new double[][] {add(c0, d0), add(c0, d1), add(c1, d1), add(c1, d0)},
				add(mul(u, Math.cos(am)), mul(w, Math.sin(am))), base);
		}

		if (cap0 || cap1) {
			double[][] ring0 = new double[6][];
			double[][] ring1 = new double[6][];
			for (int i = 0; i < 6; i++) {
				double[] d = add(mul(u, r * Math.cos(th[i])), mul(w, r * Math.sin(th[i])));
				ring0[i] = add(c0, d);
				ring1[i] = add(c1, d);
			}
			if (cap0) face(ring0, mul(axis, -1), base);
			if (cap1) face(ring1, axis, base);
		}
	}

	/**
	 * 齿轮：沿 axis 有厚度的带齿圆盘。前后两面做成「环形」（内外两圈之间铺四边形），
	 * 中间留孔让传动杆穿过，避免实心盘挡住轴身。
	 */
	static void gear(double[] center, double[] axis, double rInner, double rTip, double rRoot,
		double halfT, int teeth, Color base) {
		axis = norm(axis);
		double[][] bw = basis(axis);
		double[] u = bw[0], w = bw[1];

		// 轮廓：每齿 4 点（齿根-齿顶-齿顶-齿根）
		int m = teeth * 4;
		double[] ang = new double[m];
		double[] rad = new double[m];
		for (int t = 0; t < teeth; t++) {
			double a0 = 2 * Math.PI * t / teeth;
			double step = 2 * Math.PI / teeth;
			ang[t * 4 + 0] = a0 + step * 0.05; rad[t * 4 + 0] = rRoot;
			ang[t * 4 + 1] = a0 + step * 0.22; rad[t * 4 + 1] = rTip;
			ang[t * 4 + 2] = a0 + step * 0.60; rad[t * 4 + 2] = rTip;
			ang[t * 4 + 3] = a0 + step * 0.78; rad[t * 4 + 3] = rRoot;
		}

		double[] front = add(center, mul(axis, halfT));
		double[] back = add(center, mul(axis, -halfT));

		double[][] pf = new double[m][];
		double[][] pb = new double[m][];
		for (int i = 0; i < m; i++) {
			double[] radial = add(mul(u, Math.cos(ang[i])), mul(w, Math.sin(ang[i])));
			pf[i] = add(front, mul(radial, rad[i]));
			pb[i] = add(back, mul(radial, rad[i]));
		}

		// 齿的侧面（有厚度）
		for (int i = 0; i < m; i++) {
			int j = (i + 1) % m;
			face(new double[][] {pb[i], pb[j], pf[j], pf[i]},
				add(mul(u, Math.cos(ang[i])), mul(w, Math.sin(ang[i]))), base);
		}

		// 前后两面：从内孔到轮廓的环形四边形
		for (int i = 0; i < m; i++) {
			int j = (i + 1) % m;
			double[] ri = add(mul(u, Math.cos(ang[i])), mul(w, Math.sin(ang[i])));
			double[] rj = add(mul(u, Math.cos(ang[j])), mul(w, Math.sin(ang[j])));
			double[] inI = add(front, mul(ri, rInner));
			double[] inJ = add(front, mul(rj, rInner));
			face(new double[][] {inI, inJ, pf[j], pf[i]}, axis, scale(base, 1.10));

			double[] bnI = add(back, mul(ri, rInner));
			double[] bnJ = add(back, mul(rj, rInner));
			face(new double[][] {bnI, bnJ, pb[j], pb[i]}, mul(axis, -1), scale(base, 0.72));
		}
	}

	// ---------- 背景徽章 ----------

	static void badge(Graphics2D g) {
		double r = SS * 0.478;
		Ellipse2D disc = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);

		g.setPaint(new LinearGradientPaint(
			new Point2D.Double(cx, cy - r), new Point2D.Double(cx, cy + r),
			new float[] {0f, 0.55f, 1f},
			new Color[] {new Color(0x46A6DC), new Color(0x2A7EBB), new Color(0x17578B)}));
		g.fill(disc);

		Shape oldClip = g.getClip();
		g.setClip(disc);
		double step = r * 0.20;
		g.setStroke(new BasicStroke(SS * 0.0032f));
		g.setColor(new Color(255, 255, 255, 15));
		for (double x = cx - r; x <= cx + r; x += step) {
			g.draw(new Line2D.Double(x, cy - r, x, cy + r));
			g.draw(new Line2D.Double(cx - r, x, cx + r, x));
		}
		g.setStroke(new BasicStroke(SS * 0.011f));
		g.setColor(new Color(255, 255, 255, 34));
		g.draw(new Line2D.Double(cx - step, cy - r, cx - step, cy + r));
		g.draw(new Line2D.Double(cx - r, cy - step * 0.8, cx + r, cy - step * 0.8));
		g.setClip(oldClip);

		g.setStroke(new BasicStroke(SS * 0.008f));
		g.setColor(new Color(255, 255, 255, 38));
		g.draw(disc);
	}

	// ---------- 绘制已收集的面 ----------

	/**
	 * 收集完 FACES 后调用：自动取景（把整体包围盒居中并缩放到圆盘里），
	 * 然后先整体描一圈白边，再按深度从远到近填充。
	 */
	static void flush(Graphics2D g) {
		Collections.sort(FACES);

		// 1) 求屏幕包围盒
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (Face f : FACES)
			for (int i = 0; i < f.xs.length; i++) {
				minX = Math.min(minX, f.xs[i]);
				maxX = Math.max(maxX, f.xs[i]);
				minY = Math.min(minY, f.ys[i]);
				maxY = Math.max(maxY, f.ys[i]);
			}

		// 2) 缩放比例：保证包围盒四角都落在圆盘内（留一点点边距）
		double discR = SS * 0.478;
		double hw = (maxX - minX) / 2, hh = (maxY - minY) / 2;
		double s = Math.min(1, discR * 0.94 / Math.sqrt(hw * hw + hh * hh));

		// 3) 应用：包围盒中心 → 圆盘中心（用完必须还原，否则影响后续绘制）
		java.awt.geom.AffineTransform old = g.getTransform();
		java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform(old);
		at.translate(cx, cy);
		at.scale(s, s);
		at.translate(-(minX + maxX) / 2, -(minY + maxY) / 2);
		g.setTransform(at);

		g.setStroke(new BasicStroke((float) (SS * 0.021 / s), BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND));
		g.setColor(Color.WHITE);
		for (Face f : FACES)
			g.draw(f.path());
		for (Face f : FACES) {
			g.setColor(f.color);
			g.fill(f.path());
		}
	}

	// ---------- 配色 ----------
	static final Color COPPER = new Color(0xC8794A);
	static final Color JOINT = new Color(0x7A4A2C);

	static final Color DIRT = new Color(0x8B6239);
	static final Color WOOD = new Color(0xC2914F);
	static final Color STONE = new Color(0x929292);
	static final Color IRON = new Color(0xDCDCDC);
	static final Color GOLD = new Color(0xE8B62C);
	static final Color OBSIDIAN = new Color(0x3B3350);
	static final Color WOOL = new Color(0xE4DED2);
	// 高对比矿物色：用来做「多材质」的分段，色相差异大才看得清
	static final Color REDSTONE = new Color(0xA93226);
	static final Color EMERALD = new Color(0x1FA463);
	static final Color LAPIS = new Color(0x2A4F9E);
	static final Color AMETHYST = new Color(0x8E6BC8);
	static final Color COPPER_BLOCK = new Color(0xC0713F);

	/** 在 pos 处加一圈很薄的深色「接头」，让材质分段的界更清楚。 */
	static void joint(double[] pos, double[] axis, double r) {
		prism(add(pos, mul(axis, -r * 0.055)), add(pos, mul(axis, r * 0.055)),
			r * 1.045, Math.toRadians(30), JOINT, true, true);
	}

	/** 一根多材质杆：segments 上相邻两点之间用 colors[i] 填充。 */
	static void bandedShaft(List<double[]> seg, double r, Color[] colors, boolean joints) {
		double[] axis = norm(sub(seg.get(seg.size() - 1), seg.get(0)));
		for (int i = 0; i < seg.size() - 1; i++)
			prism(seg.get(i), seg.get(i + 1), r, Math.toRadians(30), colors[i % colors.length],
				i == 0, i == seg.size() - 2);
		if (joints)
			for (int i = 1; i < seg.size() - 1; i++)
				joint(seg.get(i), axis, r);
	}

	// ---------- 三个变体 ----------

	/** A：一根轴由 5 段不同材质拼成 + 一个铜齿轮（最贴近 Create 的构图）。 */
	static void variantA(Graphics2D g) {
		FACES.clear();
		double[] axis = norm(v(0.62, 1.0, 0.34));
		double len = 8.6, r = 0.52;
		double[] base = mul(axis, -len / 2);
		List<double[]> seg = new ArrayList<>();
		for (int i = 0; i <= 5; i++)
			seg.add(add(base, mul(axis, len * i / 5.0)));
		bandedShaft(seg, r, new Color[] {DIRT, REDSTONE, GOLD, IRON, EMERALD}, true);

		// 齿轮正坐在红/金两段的分界线上：两侧各露一半，五段颜色都还看得见
		gear(seg.get(2), axis, r * 1.02, r * 1.78, r * 1.28, r * 0.24, 9, COPPER);
		flush(g);
	}

	/** B：三根并排的异材质轴（屏幕横向分开，互不遮挡）。 */
	static void variantB(Graphics2D g) {
		FACES.clear();
		double[] axis = norm(v(0.60, 1.0, 0.32));

		// 偏移方向选 (1,0,-1)：投影后正好是纯水平，三根轴在屏幕上并排不重叠
		double[] side = norm(v(1, 0, -1));
		double gap = 1.62, len = 7.6, r = 0.46;

		Color[] mats = {DIRT, IRON, GOLD};
		double[] offsets = {-gap, 0, gap};

		for (int k = 0; k < 3; k++) {
			double[] base = add(mul(axis, -len / 2), mul(side, offsets[k]));
			prism(base, add(base, mul(axis, len)), r, Math.toRadians(30), mats[k], true, true);
			joint(add(base, mul(axis, len * 0.30)), axis, r);
			joint(add(base, mul(axis, len * 0.70)), axis, r);
		}

		// 中间那根（铁色，衬铜齿轮）套一个齿轮
		double[] gc = mul(side, offsets[1]);
		gear(gc, axis, r * 1.02, r * 2.45, r * 1.70, r * 0.26, 9, COPPER);
		flush(g);
	}

	/** C：多材质主轴 + 一根垂直伸出的副轴（T 形交汇）并带齿轮。 */
	static void variantC(Graphics2D g) {
		FACES.clear();
		double[] axis = norm(v(0.58, 1.0, 0.30));
		double len = 8.6, r = 0.52;
		double[] base = mul(axis, -len / 2);
		List<double[]> seg = new ArrayList<>();
		for (int i = 0; i <= 5; i++)
			seg.add(add(base, mul(axis, len * i / 5.0)));
		bandedShaft(seg, r, new Color[] {DIRT, REDSTONE, GOLD, IRON, EMERALD}, true);

		// 主轴中段的齿轮
		double[] gc = add(seg.get(2), mul(sub(seg.get(3), seg.get(2)), 0.5));
		gear(gc, axis, r * 1.02, r * 1.90, r * 1.30, r * 0.22, 9, COPPER);

		// 垂直副轴：方向取 axis × (0,1,0)，投影后大致水平伸出去
		double[] arm = norm(cross(axis, v(0, 1, 0)));
		double armLen = 2.2, ar = 0.45;
		prism(gc, add(gc, mul(arm, armLen)), ar, Math.toRadians(30), IRON, true, true);
		gear(add(gc, mul(arm, armLen * 0.62)), arm, ar * 1.02, ar * 2.55, ar * 1.75,
			ar * 0.28, 8, COPPER);
		flush(g);
	}

	// ---------- 输出 ----------

	public static void main(String[] args) throws Exception {
		File outDir = new File(args.length > 0 ? args[0] : ".");
		outDir.mkdirs();

		String[] names = {"variant-a", "variant-b", "variant-c"};
		for (int i = 0; i < names.length; i++) {
			BufferedImage big = render(i);
			write(big, new File(outDir, names[i] + "-512.png"), 512);
			write(big, new File(outDir, names[i] + "-256.png"), 256);
			write(big, new File(outDir, names[i] + "-128.png"), 128);
			System.out.println("wrote " + names[i]);
		}
	}

	static BufferedImage render(int variant) {
		BufferedImage img = new BufferedImage(SS, SS, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		badge(g);

		Shape old = g.getClip();
		double r = SS * 0.478;
		g.setClip(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
		if (variant == 0) variantA(g);
		else if (variant == 1) variantB(g);
		else variantC(g);
		g.setClip(old);
		g.dispose();
		return img;
	}

	/** 多步双线性缩小，边缘更干净。 */
	static void write(BufferedImage src, File out, int size) {
		BufferedImage cur = src;
		int w = SS;
		while (w > size * 2) {
			int nw = w / 2;
			BufferedImage step = new BufferedImage(nw, nw, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = step.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(cur, 0, 0, nw, nw, null);
			g.dispose();
			cur = step;
			w = nw;
		}
		BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dst.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(cur, 0, 0, size, size, null);
		g.dispose();
		try {
			ImageIO.write(dst, "png", out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
