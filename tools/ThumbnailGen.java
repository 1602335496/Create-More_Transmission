import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * 视频封面生成器（16:9），纯 JDK Java2D，无第三方依赖。
 *
 * 与 IconGen 同一套视觉语言（蓝色工程网格 + 等距立体传动杆 + 白色贴纸描边），
 * 但按封面需求重新排版：左侧大字标题、右侧主体视觉、强调「144 种」这个数字。
 * 几何代码与 IconGen 重复了一份，是为了两个工具都能单独跑、互不牵连。
 *
 * 用法：
 *     java tools/ThumbnailGen.java <输出目录>
 * 输出 cover-a / cover-b，各 1920×1080、1280×720、480×270（手机端缩略预览）三种尺寸。
 */
public class ThumbnailGen {

	// ---------- 画布（先按 1920×1080 画，最后缩到目标尺寸） ----------
	static int W = 1920, H = 1080;             // 画布尺寸（4:3 时高度不变，只收窄宽度）
	static final double COS30 = Math.cos(Math.toRadians(30));
	static final double SIN30 = 0.5;
	static final double VIEW = 1 / Math.sqrt(3);

	/** 画布越窄，主标题要按比例缩小才不会撞到右侧主体视觉。 */
	static double TS = 1.0;

	static double scale = 190;                 // 世界单位 → 像素
	static double pcx;                         // 投影中心（主体视觉摆右侧）
	static double pcy;

	/** 切换画布尺寸：投影中心与标题缩放都跟着重算。 */
	static void setCanvas(int w, int h) {
		W = w;
		H = h;
		pcx = W * 0.715;
		pcy = H * 0.52;
		TS = Math.min(1.0, W / 1920.0 * 1.12);
	}

	static double[] proj(double[] p) {
		return new double[] {
			pcx + (p[0] - p[2]) * COS30 * scale,
			pcy + ((p[0] + p[2]) * SIN30 - p[1]) * scale
		};
	}

	// ---------- 向量 ----------
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

	// 光源取「上前右」：轴沿 Z 轴时，可见的是 +X 与 +Y 两个面，
	// 齿轮可见的是 +Z 面，三个分量都为正才能都吃到光。
	static final double[] LIGHT = norm(v(0.40, 0.80, 0.45));

	static Color shade(Color base, double[] n) {
		// 环境光下限抬到 0.74：掠射面（比如齿轮孔中透出的轴身）不至于黑成一个洞
		return mul(base, 0.74 + 0.56 * Math.max(0, dot(norm(n), LIGHT)));
	}

	static Color mul(Color c, double f) {
		return new Color(clamp((int) Math.round(c.getRed() * f)),
			clamp((int) Math.round(c.getGreen() * f)),
			clamp((int) Math.round(c.getBlue() * f)), c.getAlpha());
	}

	static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }

	static int clamp(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }

	// ---------- 面 ----------
	/** 面的绘制方式：实体（描边+填充）、像素格（只填充）、仅描边（只画白边）。 */
	static final int SOLID = 0, PIXEL = 1, EDGE = 2;

	static class Face {
		final double[] xs, ys;
		final double[] ds;         // 每个顶点的深度（正交投影下屏幕空间线性，用于逐像素插值）
		final double[] hxs, hys;   // 内孔（可为 null）
		final double depth;
		final Color color;
		final int mode;
		final int layer;

		Face(double[] xs, double[] ys, double[] ds, double[] hxs, double[] hys, double depth,
			Color color, int mode, int layer) {
			this.xs = xs; this.ys = ys; this.ds = ds; this.hxs = hxs; this.hys = hys;
			this.depth = depth; this.color = color; this.mode = mode; this.layer = layer;
		}

		Path2D.Double path() {
			// 奇偶环绕：带孔的面对一次填充就成型，不会像"每瓣单独填"那样留下辐射状接缝
			Path2D.Double p = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			p.moveTo(xs[0], ys[0]);
			for (int i = 1; i < xs.length; i++) p.lineTo(xs[i], ys[i]);
			p.closePath();
			if (hxs != null) {
				p.moveTo(hxs[0], hys[0]);
				for (int i = 1; i < hxs.length; i++) p.lineTo(hxs[i], hys[i]);
				p.closePath();
			}
			return p;
		}
	}

	static final List<Face> FACES = new ArrayList<>();
	static final double[] VIEWVEC = norm(v(VIEW, VIEW, VIEW));

	/**
	 * 收集一个多边形面。**背面自动剔除**——凸多面体的可见面在投影上互不重叠，
	 * 于是不需要按深度排序，齿轮和轴互相穿插时也不会出现排序错乱（风车状破碎面）。
	 */
	static void addFace(double[][] pts, double[] normal, Color base, int mode) {
		if (dot(norm(normal), VIEWVEC) <= 0.0) return;   // 背面，不可见
		addRaw(pts, mode == SOLID ? shade(base, normal) : base, mode);
	}

	static void face(double[][] pts, double[] normal, Color base) {
		addFace(pts, normal, base, SOLID);
	}

	/** 把四边形面按 nx×ny 个色格填充，模仿 Minecraft 的像素材质。 */
	static void facePixels(double[][] p, double[] normal, Color base, Color accent,
		int nx, int ny, double accentRows, long seed) {
		if (dot(norm(normal), VIEWVEC) <= 0.0) return;

		double[] p0 = p[0], p1 = p[1], p2 = p[2], p3 = p[3];
		double[] nrm = norm(normal);

		// 先用整面的底色铺满：色格是抗锯齿填充的，格与格之间会留出细缝，
		// 缝里会透出深色背景形成「黑条纹」。先铺一层整面底色就把它封住了。
		addUnderlay(p, shade(base, nrm));

		for (int iy = 0; iy < ny; iy++) {
			for (int ix = 0; ix < nx; ix++) {
				double u0 = ix / (double) nx, u1 = (ix + 1) / (double) nx;
				double v0 = iy / (double) ny, v1 = (iy + 1) / (double) ny;
				double[][] cell = {
					bilin(p0, p1, p2, p3, u0, v0), bilin(p0, p1, p2, p3, u1, v0),
					bilin(p0, p1, p2, p3, u1, v1), bilin(p0, p1, p2, p3, u0, v1)
				};
				double h = hash(ix, iy, seed);
				// iy=0 是面顶那一行：草方块侧面的绿边，交界行做锯齿
				boolean green = accent != null && (iy < accentRows || (iy < accentRows + 1 && h > 0.45));
				Color c = green ? accent : base;
				addRaw(cell, mul(shade(c, nrm), 0.94 + h * 0.12), PIXEL);
			}
		}
		addRaw(p, Color.WHITE, EDGE);   // 外轮廓只描白边，不填充
	}

	/** 只填充、不描边（用于齿轮的环形与齿面，避免每一瓣都描白边出现辐射线）。 */
	static void faceFill(double[][] pts, double[] normal, Color base) {
		if (dot(norm(normal), VIEWVEC) <= 0.0) return;
		addRaw(pts, shade(base, normal), PIXEL);
	}

	// ---------- 真实贴图映射 ----------

	/** 从 NeoForge 的 client-extra jar 里读原版贴图（mvn/gradle 构建产物，不随仓库分发）。 */
	static BufferedImage loadVanillaTexture(String entry) {
		File dir = new File("build/moddev/artifacts");
		File[] jars = dir.listFiles((d, n) -> n.contains("client-extra") && n.endsWith(".jar"));
		if (jars == null || jars.length == 0)
			throw new IllegalStateException("找不到 client-extra jar，请先跑一次 gradlew build");
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(jars[0])) {
			java.util.zip.ZipEntry e = zip.getEntry(entry);
			if (e == null) throw new IllegalStateException("jar 里没有 " + entry);
			return ImageIO.read(zip.getInputStream(e));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Minecraft 的面亮度系数：顶 1.0 / 底 0.5 / 南北 0.8 / 东西 0.6。 */
	static double mcFaceShade(double[] n) {
		double[] nn = norm(n);
		if (nn[1] > 0.5) return 1.0;
		if (nn[1] < -0.5) return 0.5;
		return Math.abs(nn[0]) > Math.abs(nn[2]) ? 0.6 : 0.8;
	}

	/** 贴图区域的像素平均色，用来预铺整面、封住色格之间的抗锯齿缝。 */
	static Color avgColor(BufferedImage tex, int tx0, int ty0, int tw, int th) {
		long r = 0, g = 0, b = 0;
		int n = 0;
		for (int y = ty0; y < ty0 + th; y++)
			for (int x = tx0; x < tx0 + tw; x++) {
				Color c = new Color(tex.getRGB(x, y));
				r += c.getRed(); g += c.getGreen(); b += c.getBlue();
				n++;
			}
		return new Color((int) (r / n), (int) (g / n), (int) (b / n));
	}

	/**
	 * 用真实贴图铺一个面：把贴图的 (tx0,ty0,tw,th) 区域贴着面平铺（近邻采样），
	 * 并按 Minecraft 的面亮度打光——这样杆子看起来就是游戏里的样子。
	 * quad 约定：p0→p1 是横向（对应 tw 像素），p0→p3 是纵向（对应 th 像素）。
	 *
	 * @param lenBlocks 面的实际长度（以方块为单位），决定纵向平铺多少次；精确映射传 1
	 * @param edge      是否给这个面描白边（共面的面之间不要描，否则会出现多余白线）
	 */
	static final int F_EDGE = 1, F_UNDERLAY = 2, F_ALPHA = 4;

	/** 色格的最大世界边长：越小小格越密、深度排序越准，代价是面数变多。 */
	static final double CELL = 0.2;

	static double dist(double[] a, double[] b) { return Math.sqrt(dot(sub(a, b), sub(a, b))); }

	static void faceTex(double[][] q, double[] normal, BufferedImage tex,
		int tx0, int ty0, int tw, int th, double lenBlocks, int flags) {
		if (dot(norm(normal), VIEWVEC) <= 0.0) return;
		boolean alphaCut = (flags & F_ALPHA) != 0;
		double shade = mcFaceShade(normal);
		// 色格数量必须按「世界尺寸」细分，不能只按贴图像素：像齿轮轮毂侧面那种大面
		// 如果只切出几格，每格铺满一整面墙，重心排序会失效，面就会被画到别的面之上。
		double wWorld = (dist(q[0], q[1]) + dist(q[2], q[3])) / 2;
		double hWorld = (dist(q[0], q[3]) + dist(q[1], q[2])) / 2;
		int nx = Math.max(Math.max(1, tw), (int) Math.ceil(wWorld / CELL));
		int ny = Math.max(Math.max(1, (int) Math.round(th * lenBlocks)),
			(int) Math.ceil(hWorld / CELL));

		// 先预铺整面平均色，封住色格之间的抗锯齿缝（叠加层没有底色可铺）
		if ((flags & F_UNDERLAY) != 0)
			addUnderlay(q, mul(avgColor(tex, tx0, ty0, tw, th), shade));

		double[] p0 = q[0], p1 = q[1], p2 = q[2], p3 = q[3];
		for (int iy = 0; iy < ny; iy++) {
			for (int ix = 0; ix < nx; ix++) {
				double u0 = ix / (double) nx, u1 = (ix + 1) / (double) nx;
				double v0 = iy / (double) ny, v1 = (iy + 1) / (double) ny;
				double[][] cell = {
					bilin(p0, p1, p2, p3, u0, v0), bilin(p0, p1, p2, p3, u1, v0),
					bilin(p0, p1, p2, p3, u1, v1), bilin(p0, p1, p2, p3, u0, v1)
				};
				// 注意：这里不做外扩。有了逐像素 z-buffer，色格精确平铺就已经无缝，
				// 外扩反而会溢出到面外、把白色贴纸描边盖掉。
				int sx = tx0 + Math.min(tw - 1, ix * tw / nx);
				int vTexel = (int) (iy * th * lenBlocks / ny);   // 纵向按材质长度平铺
				int sy = ty0 + (vTexel % th);
				Color raw = new Color(tex.getRGB(sx, sy), true);
				if (alphaCut && raw.getAlpha() < 128) continue;   // 叠加层的镂空处不画
				addRaw(cell, mul(new Color(raw.getRed(), raw.getGreen(), raw.getBlue()), shade), PIXEL);
			}
		}
		if ((flags & F_EDGE) != 0) addRaw(q, Color.WHITE, EDGE);
	}

	/**
	 * 按 dirt_shaft 的真实模型画一根传动杆：4×16×4 的方形棱柱，
	 * 侧面取贴图 [6,0,10,16] 竖条，端面取 [6,6,10,10]。
	 *
	 * @param lenBlocks 杆长（方块数）
	 */
	static void realShaft(double[] c0, double[] c1, double lenBlocks, BufferedImage tex) {
		double[] axis = norm(sub(c1, c0));
		double[][] bw = basis(axis);
		double[] u = bw[0], w = bw[1];
		double[] th = angles(PHASE);

		for (int i = 0; i < SIDES; i++) {
			double a0 = th[i], a1 = th[(i + 1) % SIDES];
			double[] d0 = add(mul(u, R * Math.cos(a0)), mul(w, R * Math.sin(a0)));
			double[] d1 = add(mul(u, R * Math.cos(a1)), mul(w, R * Math.sin(a1)));
			double am = (a0 + a1) / 2;
			// p0→p1 横向（4 像素宽），p0→p3 纵向（每方块 16 像素）
			faceTex(new double[][] {add(c0, d0), add(c0, d1), add(c1, d1), add(c1, d0)},
				add(mul(u, Math.cos(am)), mul(w, Math.sin(am))), tex, 6, 0, 4, 16, lenBlocks, F_EDGE | F_UNDERLAY);
		}
		faceTex(ring(c0, u, w, R, th), mul(axis, -1), tex, 6, 6, 4, 4, 1, F_EDGE | F_UNDERLAY);
		faceTex(ring(c1, u, w, R, th), axis, tex, 6, 6, 4, 4, 1, F_EDGE | F_UNDERLAY);
	}

	// ---------- Minecraft 方块模型（box 元素）渲染 ----------

	/** 从 Create 的源码仓库读它的原版贴图。 */
	static BufferedImage loadCreateTexture(String name) {
		File f = new File(CREATE_SRC + "/src/main/resources/assets/create/textures/block/" + name + ".png");
		try {
			return ImageIO.read(f);
		} catch (Exception e) {
			throw new RuntimeException("读不到 Create 贴图：" + f, e);
		}
	}

	static final String CREATE_SRC =
		"C:/Users/16023/Desktop/cursor_name/Create-mc1.21.1-dev/Create-mc1.21.1-dev";

	/**
	 * 给贴图整体乘一个色调。Create 的原版小齿轮纹理是木褐色（模型里 particle 指向
	 * stripped_spruce_log_top），放在泥土杆上会糊成一片棕。这里染成黄铜色拉开对比——
	 * 形状与纹理细节仍是原版，只改色调。
	 */
	static BufferedImage tint(BufferedImage src, double r, double g, double b) {
		BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(),
			BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < src.getHeight(); y++)
			for (int x = 0; x < src.getWidth(); x++) {
				Color c = new Color(src.getRGB(x, y), true);
				out.setRGB(x, y, new Color(clamp((int) (c.getRed() * r)),
					clamp((int) (c.getGreen() * g)), clamp((int) (c.getBlue() * b)),
					c.getAlpha()).getRGB());
			}
		return out;
	}

	/** 构造把「模型 Y 轴」对齐到 target 的旋转矩阵（三个列向量）。 */
	static double[][] alignY(double[] target) {
		double[] ty = norm(target);
		double[] tx = Math.abs(dot(ty, v(1, 0, 0))) > 0.9 ? v(0, 0, 1) : v(1, 0, 0);
		double[] tz = norm(cross(tx, ty));
		tx = cross(ty, tz);
		return new double[][] {norm(tx), ty, norm(tz)};
	}

	static double[] applyM(double[][] m, double[] p) {
		return add(add(mul(m[0], p[0]), mul(m[1], p[1])), mul(m[2], p[2]));
	}

	// 模型面的顺序：北 东 南 西 上 下
	static final int FN = 0, FE = 1, FS = 2, FW = 3, FU = 4, FD = 5;
	static final double[][] FACE_N = {{0, 0, -1}, {1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

	/**
	 * 画一个 Minecraft 方块模型的 box 元素（还原原版模型的 UV 朝向约定）。
	 *
	 * @param from  最小角，单位 1/16 方块
	 * @param to    最大角，单位 1/16 方块
	 * @param rotYDeg 绕 Y 轴旋转角度（模型空间，绕 origin16），用于 Create 齿轮的斜向齿条
	 * @param origin16 旋转中心，单位 1/16 方块
	 * @param uv    uv[面] = {u1,v1,u2,v2}，贴图像素坐标
	 * @param axis  把模型 Y 轴对齐到哪个世界方向
	 * @param offset 模型 (8,8,8) 落在世界坐标的哪个点
	 */
	static void modelBox(double[] from, double[] to, double rotYDeg, double[] origin16,
		double[][] uv, BufferedImage tex, double[] axis, double[] offset, boolean[] edgeFace,
		double scale) {

		double x1 = from[0] / 16, y1 = from[1] / 16, z1 = from[2] / 16;
		double x2 = to[0] / 16, y2 = to[1] / 16, z2 = to[2] / 16;
		double[] org = mul(origin16, 1 / 16.0);
		double ang = Math.toRadians(rotYDeg);
		double[][] m = alignY(axis);

		for (int f = 0; f < 6; f++) {
			double[][] q = new double[4][];
			for (int k = 0; k < 4; k++) {
				// q 的顺序：UV 左上 → 右上 → 右下 → 左下
				double u = (k == 1 || k == 2) ? 1 : 0;
				double vv = (k == 2 || k == 3) ? 1 : 0;
				double[] p;
				switch (f) {
					case FN: p = v(x1 + u * (x2 - x1), y2 - vv * (y2 - y1), z1); break;
					case FS: p = v(x2 - u * (x2 - x1), y2 - vv * (y2 - y1), z2); break;
					case FE: p = v(x2, y2 - vv * (y2 - y1), z2 - u * (z2 - z1)); break;
					case FW: p = v(x1, y2 - vv * (y2 - y1), z1 + u * (z2 - z1)); break;
					case FU: p = v(x1 + u * (x2 - x1), y2, z1 + vv * (z2 - z1)); break;
					default: p = v(x1 + u * (x2 - x1), y1, z2 - vv * (z2 - z1)); break;
				}
				// 绕 Y 轴旋转（模型空间）
				if (rotYDeg != 0) {
					double dx = p[0] - org[0], dz = p[2] - org[2];
					p = v(org[0] + dx * Math.cos(ang) + dz * Math.sin(ang), p[1],
						org[2] - dx * Math.sin(ang) + dz * Math.cos(ang));
				}
				// 对齐到世界坐标
				p = add(applyM(m, mul(sub(p, v(0.5, 0.5, 0.5)), scale)), offset);
				q[k] = p;
			}
			// 法线也要跟着转
			double[] n = FACE_N[f];
			if (rotYDeg != 0) {
				double dx = n[0], dz = n[2];
				n = v(dx * Math.cos(ang) + dz * Math.sin(ang), n[1],
					-dx * Math.sin(ang) + dz * Math.cos(ang));
			}
			n = applyM(m, n);

			int tx0 = (int) Math.round(uv[f][0]), ty0 = (int) Math.round(uv[f][1]);
			int tw = Math.max(1, (int) Math.round(uv[f][2] - uv[f][0]));
			int th = Math.max(1, (int) Math.round(uv[f][3] - uv[f][1]));
			faceTex(q, n, tex, tx0, ty0, tw, th, 1,
				F_UNDERLAY | ((edgeFace == null || edgeFace[f]) ? F_EDGE : 0));
		}
	}

	/** 生成标准六面 UV（整张贴图）。 */
	static double[][] fullUV() {
		return new double[][] {{0, 0, 16, 16}, {0, 0, 16, 16}, {0, 0, 16, 16},
			{0, 0, 16, 16}, {0, 0, 16, 16}, {0, 0, 16, 16}};
	}

	/** 一个原版方块（整张贴图六面）：顶/侧/底可分别指定，size 为边长（方块数）。 */
	static void vanillaBlock(double[] center, double size, BufferedImage top, BufferedImage side,
		BufferedImage bottom) {
		double[] uv = {0, 0, 16, 16};
		for (int f = 0; f < 6; f++) {
			BufferedImage t = (f == FU) ? top : (f == FD ? bottom : side);
			if (t == null) t = side;
			modelBoxFace(v(0, 0, 0), v(16, 16, 16), f, uv, t, v(0, 1, 0), center, size,
				F_EDGE | F_UNDERLAY);
		}
	}

	/**
	 * 原版草方块，严格按 grass_block.json 的两层结构来：
	 *   底层：底面 dirt / 顶面 grass_block_top（灰度贴图，要乘群系草色）/ 四面 grass_block_side（本身已上色）
	 *   叠加层：四面再叠一层 grass_block_side_overlay（灰度+透明，同样乘草色）
	 * 之前直接把未染色的 grass_block_top 贴上去，所以顶面发灰。
	 */
	static void grassBlock(double[] center, double size) {
		double[] uv = {0, 0, 16, 16};
		modelBoxFace(v(0, 0, 0), v(16, 16, 16), FD, uv, TEX, v(0, 1, 0), center, size,
			F_EDGE | F_UNDERLAY);
		modelBoxFace(v(0, 0, 0), v(16, 16, 16), FU, uv, TEX_GRASS_TOP, v(0, 1, 0), center, size,
			F_EDGE | F_UNDERLAY);
		int saved = LAYER;
		for (int f = 0; f < 6; f++) {
			if (f == FU || f == FD) continue;
			modelBoxFace(v(0, 0, 0), v(16, 16, 16), f, uv, TEX_GRASS_SIDE, v(0, 1, 0), center, size,
				F_EDGE | F_UNDERLAY);
			LAYER = saved + 1;   // 叠加层必须压在底层侧面之上
			modelBoxFace(v(0, 0, 0), v(16, 16, 16), f, uv, TEX_GRASS_OVERLAY, v(0, 1, 0), center,
				size, F_ALPHA);
			LAYER = saved;
		}
	}

	/** 单个面的 box 绘制（内部用）。box 以 (8,8,8) 为原点，故 center 传方块中心。 */
	static void modelBoxFace(double[] from, double[] to, int f, double[] uv,
		BufferedImage tex, double[] axis, double[] center, double size, int flags) {
		double x1 = from[0] / 16, y1 = from[1] / 16, z1 = from[2] / 16;
		double x2 = to[0] / 16, y2 = to[1] / 16, z2 = to[2] / 16;
		double[][] m = alignY(axis);
		double[][] q = new double[4][];
		for (int k = 0; k < 4; k++) {
			double u = (k == 1 || k == 2) ? 1 : 0;
			double vv = (k == 2 || k == 3) ? 1 : 0;
			double[] p;
			switch (f) {
				case FN: p = v(x1 + u * (x2 - x1), y2 - vv * (y2 - y1), z1); break;
				case FS: p = v(x2 - u * (x2 - x1), y2 - vv * (y2 - y1), z2); break;
				case FE: p = v(x2, y2 - vv * (y2 - y1), z2 - u * (z2 - z1)); break;
				case FW: p = v(x1, y2 - vv * (y2 - y1), z1 + u * (z2 - z1)); break;
				case FU: p = v(x1 + u * (x2 - x1), y2, z1 + vv * (z2 - z1)); break;
				default: p = v(x1 + u * (x2 - x1), y1, z2 - vv * (z2 - z1)); break;
			}
			q[k] = add(applyM(m, mul(sub(p, v(0.5, 0.5, 0.5)), size)), center);
		}
		faceTex(q, applyM(m, FACE_N[f]), tex,
			(int) uv[0], (int) uv[1], Math.max(1, (int) (uv[2] - uv[0])),
			Math.max(1, (int) (uv[3] - uv[1])), 1, flags);
	}

	// ---------- 机械动力的原版齿轮 ----------

	/** 齿轮相对模型放大一点：模型原尺寸在封面里星角太细看不清。 */
	static final double COG_SCALE = 1.0;

	static BufferedImage TEX_COGWHEEL, TEX_COGWHEEL_AXIS, TEX_AXIS_TOP;
	static BufferedImage TEX_DIRT_V, TEX_GRASS_TOP, TEX_GRASS_SIDE, TEX_GRASS_OVERLAY,
		TEX_GOLD, TEX_DIAMOND;

	/** 原版平原群系的草色 #91BD59：草方块顶面与侧面叠加层都要乘它。 */
	static final double GRASS_R = 145 / 255.0, GRASS_G = 189 / 255.0, GRASS_B = 89 / 255.0;

	/** 六面用同一张 32×32 贴图的 UV 声明。 */
	static double[] uv32(double u1, double v1, double u2, double v2) {
		return new double[] {u1, v1, u2, v2};
	}

	/**
	 * 按 Create 的 cogwheel.json 逐个 box 还原齿轮：
	 * 一根贯穿的轴 + 四根交叉的齿条（0°/45°/-45°/90°，形成 8 角星）+ 内外两个方形轮毂。
	 * 贴图尺寸 32×32，UV 直接就是像素坐标。
	 */
	static void createCogwheel(double[] center, double[] axis) {
		final double S = 1.0;   // 模型 1 格 = 1 世界单位

		// 注意：模型里的 Axis 元素（贯穿轴）这里不画——封面中已经有一根泥土传动杆
		// 穿过齿轮，两者完全重叠只会 z-fighting。视觉上就是「泥土杆穿过齿轮」，一致。
		// 四根齿条 + 两个轮毂（32×32 贴图空间）
		double[][] barUV = {
			uv32(7, 8, 16, 9.5),   // north
			uv32(5, 8, 6.5, 9.5),  // east
			uv32(7, 8, 16, 9.5),   // south
			uv32(5, 8, 6.5, 9.5),  // west
			uv32(7, 6, 16, 7.5),   // up
			uv32(7, 6, 16, 7.5)    // down
		};
		// 四根齿条同处 y 6.5~9.5，中心互相穿插。游戏里靠逐像素 z-buffer 才排得对，
		// 这里用画家算法会把同高共面的面排出花来。办法是给每根齿条一点点层高差
		// （EPS，肉眼不可见），让共面的面有确定顺序；法线相同所以明暗一致，
		// 交叉处不会再出现明暗斑块——而交叉区本来就被轮毂盖住。
		double EPS = 0.004;
		double[][] barDef = {{0, 0}, {45, 1}, {-45, 2}, {0, 3}};
		for (int b = 0; b < barDef.length; b++) {
			double rot = barDef[b][0];
			double dy = EPS * barDef[b][1];
			double y1 = 6.5 + dy, y2 = 9.5 + dy;
			if (b == 3) {
				modelBox(v(6.5, y1, -1), v(9.5, y2, 17), 0, v(8, 8, 8),
					new double[][] {uv32(5, 8, 6.5, 9.5), uv32(7, 8, 16, 9.5), uv32(5, 8, 6.5, 9.5),
						uv32(7, 8, 16, 9.5), uv32(7, 6, 16, 7.5), uv32(7, 6, 16, 7.5)},
					TEX_COGWHEEL, axis, center, null, COG_SCALE);
			} else {
				modelBox(v(-1, y1, 6.5), v(17, y2, 9.5), rot, v(8, 8, 8), barUV,
					TEX_COGWHEEL, axis, center, null, COG_SCALE);
			}
		}

		// GearCaseInner
		double[][] innerUV = new double[6][];
		java.util.Arrays.fill(innerUV, uv32(0, 6, 6, 7.5));
		innerUV[FU] = uv32(4, 0, 10, 6);
		innerUV[FD] = uv32(4, 0, 10, 6);
		modelBox(v(2, 6.55, 2), v(14, 9.45, 14), 0, v(8, 8, 8), innerUV,
			TEX_COGWHEEL, axis, center, null, COG_SCALE);

		// GearCaseOuter
		double[][] outerUV = new double[6][];
		java.util.Arrays.fill(outerUV, uv32(0, 4, 4, 6));
		outerUV[FU] = uv32(0, 0, 4, 4);
		outerUV[FD] = uv32(0, 0, 4, 4);
		modelBox(v(4, 6, 4), v(12, 10, 12), 0, v(8, 8, 8), outerUV,
			TEX_COGWHEEL, axis, center, null, COG_SCALE);
	}

	/** 杆的横截面半对角线：模型是 4/16 方块宽，半宽 0.125，半对角线 ×√2。 */
	static final double R = 0.125 * Math.sqrt(2);

	/** 杆身贴图（原版泥土）。 */
	static BufferedImage TEX;

	/** 当前绘制层级；gear() 会临时抬高，让齿轮整体压在轴之后绘制。 */
	static int LAYER = 0;

	/**
	 * 整面底色垫底。深度取该面顶点的**最小值**再减一点，保证它一定排在自己所有色格之前。
	 * （若按「面的平均深度」排序，靠近相机那一半的色格会先画、然后被垫底盖掉，
	 * 表现为面的一半是纯色、另一半才有贴图。）
	 */
	static void addUnderlay(double[][] pts, Color color) {
		int n = pts.length;
		double[] xs = new double[n], ys = new double[n], ds = new double[n];
		double dmin = Double.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			double[] s = proj(pts[i]);
			xs[i] = s[0]; ys[i] = s[1];
			// 垫底色必须真的往后压一点点：它和色格在同一个平面上，
			// 深度相等时严格大于的测试会让后画的色格写不进去，整面就变成纯色。
			ds[i] = dot(pts[i], VIEWVEC) - 0.002;
			dmin = Math.min(dmin, ds[i]);
		}
		FACES.add(new Face(xs, ys, ds, null, null, dmin - 1e-3, color, PIXEL, LAYER));
	}

	/** 直接登记一个多边形（不做剔除、不上明暗），depth 由自身顶点算出。 */
	static void addRaw(double[][] pts, Color color, int mode) {
		int n = pts.length;
		double[] xs = new double[n], ys = new double[n], ds = new double[n];
		double d = 0;
		for (int i = 0; i < n; i++) {
			double[] s = proj(pts[i]);
			xs[i] = s[0]; ys[i] = s[1];
			ds[i] = dot(pts[i], VIEWVEC);
			d += ds[i];
		}
		FACES.add(new Face(xs, ys, ds, null, null, d / n, color, mode, LAYER));
	}

	/** 带内孔的多边形：一次填充出「环」，避免逐瓣填充的接缝。 */
	static void addRawHole(double[][] outer, double[][] hole, Color color, int mode) {
		int n = outer.length, hn = hole.length;
		double[] xs = new double[n], ys = new double[n], ds = new double[n];
		double[] hxs = new double[hn], hys = new double[hn];
		double d = 0;
		for (int i = 0; i < n; i++) {
			double[] s = proj(outer[i]);
			xs[i] = s[0]; ys[i] = s[1];
			ds[i] = dot(outer[i], VIEWVEC);
			d += ds[i];
		}
		for (int i = 0; i < hn; i++) {
			double[] s = proj(hole[i]);
			hxs[i] = s[0]; hys[i] = s[1];
		}
		FACES.add(new Face(xs, ys, ds, hxs, hys, d / n, color, mode, LAYER));
	}

	static double[] bilin(double[] p0, double[] p1, double[] p2, double[] p3, double u, double v) {
		double[] a = add(mul(p0, 1 - u), mul(p1, u));
		double[] b = add(mul(p3, 1 - u), mul(p2, u));
		return add(mul(a, 1 - v), mul(b, v));
	}

	static double hash(int x, int y, long seed) {
		long h = x * 73856093L ^ y * 19349663L ^ seed * 83492791L;
		h = (h ^ (h >>> 13)) * 1274126177L;
		return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0;
	}

	/** 轴的横截面边数：4 = 方形棱柱（Minecraft 的方块感），6 = 六棱柱。 */
	static final int SIDES = 4;

	/** 方形截面的相位：45° 让四个面正对世界轴，等距投影下正好看到一个顶面 + 一个侧面。 */
	static final double PHASE = Math.toRadians(45);

	static double[][] basis(double[] axis) {
		double[] up = Math.abs(dot(axis, v(0, 1, 0))) > 0.92 ? v(1, 0, 0) : v(0, 1, 0);
		double[] u = norm(cross(up, axis));
		return new double[][] {u, cross(axis, u)};
	}

	/** 横截面 N 边形的顶点角度。 */
	static double[] angles(double rot) {
		double[] th = new double[SIDES];
		for (int i = 0; i < SIDES; i++) th[i] = rot + 2 * Math.PI * i / SIDES;
		return th;
	}

	static double[][] ring(double[] c, double[] u, double[] w, double r, double[] th) {
		double[][] out = new double[SIDES][];
		for (int i = 0; i < SIDES; i++)
			out[i] = add(c, add(mul(u, r * Math.cos(th[i])), mul(w, r * Math.sin(th[i]))));
		return out;
	}

	static void prism(double[] c0, double[] c1, double r, double rot, Color base,
		boolean cap0, boolean cap1) {
		double[] axis = norm(sub(c1, c0));
		double[][] bw = basis(axis);
		double[] u = bw[0], w = bw[1];
		double[] th = angles(rot);

		for (int i = 0; i < SIDES; i++) {
			double a0 = th[i], a1 = th[(i + 1) % SIDES];
			double[] d0 = add(mul(u, r * Math.cos(a0)), mul(w, r * Math.sin(a0)));
			double[] d1 = add(mul(u, r * Math.cos(a1)), mul(w, r * Math.sin(a1)));
			double am = (a0 + a1) / 2;
			face(new double[][] {add(c0, d0), add(c0, d1), add(c1, d1), add(c1, d0)},
				add(mul(u, Math.cos(am)), mul(w, Math.sin(am))), base);
		}
		if (cap0) face(ring(c0, u, w, r, th), mul(axis, -1), base);
		if (cap1) face(ring(c1, u, w, r, th), axis, base);
	}

	static void gear(double[] center, double[] axis, double rInner, double rTip, double rRoot,
		double halfT, int teeth, Color base) {
		int savedLayer = LAYER;
		LAYER = savedLayer + 1;   // 齿轮整体画在轴之后：中孔透出轴身，齿不再和轴前后穿插
		axis = norm(axis);
		double[][] bw = basis(axis);
		double[] u = bw[0], w = bw[1];

		int m = teeth * 4;
		double[] ang = new double[m], rad = new double[m];
		for (int t = 0; t < teeth; t++) {
			double a0 = 2 * Math.PI * t / teeth, step = 2 * Math.PI / teeth;
			ang[t * 4 + 0] = a0 + step * 0.05; rad[t * 4 + 0] = rRoot;
			ang[t * 4 + 1] = a0 + step * 0.22; rad[t * 4 + 1] = rTip;
			ang[t * 4 + 2] = a0 + step * 0.60; rad[t * 4 + 2] = rTip;
			ang[t * 4 + 3] = a0 + step * 0.78; rad[t * 4 + 3] = rRoot;
		}
		// 朝向相机的那一面（另一面被背面剔除，不用画）
		boolean nearSide = dot(axis, VIEWVEC) > 0;
		double[] nearC = add(center, mul(axis, nearSide ? halfT : -halfT));
		double[] farC = add(center, mul(axis, nearSide ? -halfT : halfT));
		double[] nearN = nearSide ? axis : mul(axis, -1);

		double[][] nearStar = new double[m][], farStar = new double[m][];
		for (int i = 0; i < m; i++) {
			double[] radial = add(mul(u, Math.cos(ang[i])), mul(w, Math.sin(ang[i])));
			nearStar[i] = add(nearC, mul(radial, rad[i]));
			farStar[i] = add(farC, mul(radial, rad[i]));
		}

		// 齿的侧面：只填充，不描边
		for (int i = 0; i < m; i++) {
			int j = (i + 1) % m;
			faceFill(new double[][] {farStar[i], farStar[j], nearStar[j], nearStar[i]},
				add(mul(u, Math.cos(ang[i])), mul(w, Math.sin(ang[i]))), base);
		}

		// 可见面：整块星形挖掉中心孔，用带洞多边形一次填充。
		// （逐瓣填环会因抗锯齿在环内留下一圈辐射状接缝）
		int bore = 40;
		double[][] nearBore = new double[bore][];
		for (int i = 0; i < bore; i++) {
			double a = 2 * Math.PI * i / bore;
			nearBore[i] = add(nearC, mul(add(mul(u, Math.cos(a)), mul(w, Math.sin(a))), rInner));
		}
		addRawHole(nearStar, nearBore, shade(mul(base, 1.06), nearN), PIXEL);
		// 整个齿轮只在最外轮廓描一圈白边
		addRaw(nearStar, Color.WHITE, EDGE);
		LAYER = savedLayer;
	}

	// ---------- 配色 ----------
	static final Color COPPER = new Color(0xC8794A);
	static final Color JOINT = new Color(0x7A4A2C);
	static final Color DIRT = new Color(0x8B6239);
	static final Color REDSTONE = new Color(0xA93226);
	static final Color GOLD = new Color(0xE8B62C);
	static final Color IRON = new Color(0xDCDCDC);
	static final Color EMERALD = new Color(0x1FA463);
	static final Color STONE = new Color(0x929292);
	static final Color AMETHYST = new Color(0x8E6BC8);
	static final Color LAPIS = new Color(0x2A4F9E);
	// Minecraft 方块的标志性颜色
	static final Color GRASS_TOP = new Color(0x74B04A);
	static final Color GRASS_SIDE = new Color(0x5E9440);
	static final Color DIAMOND = new Color(0x57D6C4);

	static void joint(double[] pos, double[] axis, double r) {
		prism(add(pos, mul(axis, -r * 0.055)), add(pos, mul(axis, r * 0.055)),
			r * 1.06, PHASE, JOINT, true, true);
	}

	/** 像素材质的棱柱（侧面按 nx×ny 色格铺，模仿 Minecraft 方块贴图）。 */
	static void prismPixel(double[] c0, double[] c1, double r, double rot, Color base,
		int nx, int ny, long seed) {
		double[] axis = norm(sub(c1, c0));
		double[][] bw = basis(axis);
		double[] u = bw[0], w = bw[1];
		double[] th = angles(rot);

		for (int i = 0; i < SIDES; i++) {
			double a0 = th[i], a1 = th[(i + 1) % SIDES];
			double[] d0 = add(mul(u, r * Math.cos(a0)), mul(w, r * Math.sin(a0)));
			double[] d1 = add(mul(u, r * Math.cos(a1)), mul(w, r * Math.sin(a1)));
			double am = (a0 + a1) / 2;
			facePixels(new double[][] {add(c0, d0), add(c0, d1), add(c1, d1), add(c1, d0)},
				add(mul(u, Math.cos(am)), mul(w, Math.sin(am))), base, null, nx, ny, 0, seed + i);
		}
		// 两端端盖
		face(ring(c0, u, w, r, th), mul(axis, -1), base);
		face(ring(c1, u, w, r, th), axis, base);
	}

	/**
	 * 等距立方体，模仿 Minecraft 方块：顶面 + 两个可见侧面。
	 * accent/accentRows 用来做草方块侧面顶部的绿边（带锯齿）。pix = 每面像素格数。
	 */
	static void block(double[] min, double s, Color top, Color sideX, Color sideZ,
		Color accent, int pix, double accentRows, long seed) {
		double[] a = min, b = add(min, v(s, s, s));
		double[] p000 = v(a[0], a[1], a[2]), p100 = v(b[0], a[1], a[2]);
		double[] p010 = v(a[0], b[1], a[2]), p110 = v(b[0], b[1], a[2]);
		double[] p001 = v(a[0], a[1], b[2]), p101 = v(b[0], a[1], b[2]);
		double[] p011 = v(a[0], b[1], b[2]), p111 = v(b[0], b[1], b[2]);

		// 顶面
		facePixels(new double[][] {p010, p110, p111, p011}, v(0, 1, 0), top, null, pix, pix, 0, seed);
		// 侧面：p0→p1 取顶边，这样 iy=0 那一行就是面的顶部（草边才对得上）
		facePixels(new double[][] {p110, p111, p101, p100}, v(1, 0, 0), sideX, accent, pix, pix, accentRows, seed + 1);
		facePixels(new double[][] {p010, p011, p001, p000}, v(-1, 0, 0), sideX, accent, pix, pix, accentRows, seed + 2);
		facePixels(new double[][] {p011, p111, p101, p001}, v(0, 0, 1), sideZ, accent, pix, pix, accentRows, seed + 3);
		facePixels(new double[][] {p010, p110, p100, p000}, v(0, 0, -1), sideZ, accent, pix, pix, accentRows, seed + 4);
	}

	/** 收集完面后：自动取景到指定区域，先白描边再按深度填充。 */
	/** 光栅化超采样倍数：先在 SS 倍画布上逐像素做深度测试，再缩回主画布得到抗锯齿。 */
	static final int SS = 2;

	/**
	 * 把 FACES 里的几何**用软件 z-buffer 光栅化**后合成到画布上。
	 *
	 * 之前用「画家算法」（按面/色格重心深度排序）渲染，遇到齿轮这种自相交模型
	 * （四根齿条同高共面、还要和杆互相穿插）就必然排错，中心会糊成一片斑块。
	 * 游戏里靠的是逐像素 z-buffer，所以这里也照做：每个色格本来就带三维坐标，
	 * 顶点深度在正交投影下对屏幕坐标是线性的，逐像素插值即可。
	 *
	 * 白描边的做法：把所有面朝外扩张一点、深度压后一点点先画成白色，
	 * 再正常画实体——实体在同样位置更深，会盖掉内侧一半，只留下外侧的贴纸边。
	 */
	static void flush(Graphics2D g, double tx, double ty, double tw, double th, double outlineW) {
		if (FACES.isEmpty()) return;

		// ---- 自动取景：包围盒居中并缩放到目标框 ----
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (Face f : FACES)
			for (int i = 0; i < f.xs.length; i++) {
				minX = Math.min(minX, f.xs[i]); maxX = Math.max(maxX, f.xs[i]);
				minY = Math.min(minY, f.ys[i]); maxY = Math.max(maxY, f.ys[i]);
			}
		double s = Math.min(tw / (maxX - minX), th / (maxY - minY));
		double ox = tx - (minX + maxX) / 2 * s;
		double oy = ty - (minY + maxY) / 2 * s;

		int sw = W * SS, sh = H * SS;
		BufferedImage tmp = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
		int[] pix = ((java.awt.image.DataBufferInt) tmp.getRaster().getDataBuffer()).getData();
		// 注意方向：depth = dot(p, VIEWVEC) 是**越大越靠近相机**，所以缓冲初始化为负无穷、
		// 测试用 d > zbuf，描边用负偏移把它压后。
		float[] zbuf = new float[sw * sh];
		java.util.Arrays.fill(zbuf, -Float.MAX_VALUE);

		double dilate = outlineW * SS / 2.0;   // 描边向外扩张的半径（像素）；<= 0 表示不描边
		// 深度测试已经是逐像素的，这里排序只是让完全同深度的重叠有稳定顺序（避免闪烁）
		Collections.sort(FACES, (p, q) -> {
			if (p.layer != q.layer) return Integer.compare(p.layer, q.layer);
			return Double.compare(p.depth, q.depth);
		});
		if (dilate > 0)
			for (Face f : FACES)
				if (f.mode != PIXEL) raster(pix, zbuf, sw, sh, f, s, ox, oy, 0xFFFFFFFF, dilate, -0.02);
		if (System.getenv("OUTLINE_ONLY") == null)
			for (Face f : FACES)
				if (f.mode != EDGE) raster(pix, zbuf, sw, sh, f, s, ox, oy, f.color.getRGB(), 0, 0);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(tmp, 0, 0, W, H, null);
	}

	/**
	 * 光栅化一个面：顶点变换到 SS 倍屏幕坐标，解出深度平面，再逐像素做深度测试。
	 * @param dilate 向外扩张的像素数（描边用）；@param bias 深度偏移（描边压后一点）
	 */
	static void raster(int[] pix, float[] zbuf, int sw, int sh, Face f,
		double s, double ox, double oy, int rgb, double dilate, double bias) {

		int n = f.xs.length;
		double[] X = new double[n], Y = new double[n], Z = new double[n];
		double[] OX = new double[n], OY = new double[n];   // 扩张之前的原多边形
		double cx = 0, cy = 0;
		for (int i = 0; i < n; i++) {
			X[i] = (f.xs[i] * s + ox) * SS;
			Y[i] = (f.ys[i] * s + oy) * SS;
			Z[i] = f.ds[i];
			OX[i] = X[i]; OY[i] = Y[i];
			cx += X[i]; cy += Y[i];
		}
		cx /= n; cy /= n;

		if (dilate != 0) {
			// 按每条边的**外法线**做真正的多边形外扩。
			// （从重心推顶点是错的：细长四边形从重心推角点，位移几乎全落在长边方向上，
			//   垂直方向只挪了一两个像素，描边就会细到看不见。）
			double[] ox2 = new double[n], oy2 = new double[n];
			for (int i = 0; i < n; i++) {
				int j = (i + 1) % n;
				double ex = X[j] - X[i], ey = Y[j] - Y[i];
				double el = Math.hypot(ex, ey);
				if (el < 1e-6) continue;
				double nx0 = ey / el, ny0 = -ex / el;          // 法线候选
				double mx = (X[i] + X[j]) / 2 - cx, my = (Y[i] + Y[j]) / 2 - cy;
				if (nx0 * mx + ny0 * my < 0) { nx0 = -nx0; ny0 = -ny0; }   // 朝外
				ox2[i] += nx0; oy2[i] += ny0;
				ox2[j] += nx0; oy2[j] += ny0;
			}
			for (int i = 0; i < n; i++) {
				double l = Math.hypot(ox2[i], oy2[i]);
				if (l < 1e-6) continue;
				double cosHalf = Math.max(0.35, l);   // 尖角处限制外扩，避免尖刺
				X[i] += ox2[i] / l * dilate * Math.min(1.0, 1.0 / cosHalf) * cosHalf;
				Y[i] += oy2[i] / l * dilate * Math.min(1.0, 1.0 / cosHalf) * cosHalf;
			}
		}

		// 深度平面：正交投影下深度对屏幕坐标线性，用三个顶点解出来
		double det = (X[1] - X[0]) * (Y[2] - Y[0]) - (X[2] - X[0]) * (Y[1] - Y[0]);
		double a = 0, b = 0, c = Z[0];
		if (Math.abs(det) > 1e-9) {
			a = ((Z[1] - Z[0]) * (Y[2] - Y[0]) - (Z[2] - Z[0]) * (Y[1] - Y[0])) / det;
			b = ((Z[2] - Z[0]) * (X[1] - X[0]) - (Z[1] - Z[0]) * (X[2] - X[0])) / det;
			c = Z[0] - a * X[0] - b * Y[0];
		}

		int x0 = (int) Math.floor(min(X)), x1 = (int) Math.ceil(max(X));
		int y0 = (int) Math.floor(min(Y)), y1 = (int) Math.ceil(max(Y));
		x0 = Math.max(0, x0); y0 = Math.max(0, y0);
		x1 = Math.min(sw - 1, x1); y1 = Math.min(sh - 1, y1);

		// 内孔也参与奇偶判定，天然挖空
		int hn = f.hxs == null ? 0 : f.hxs.length;
		double[] hx = new double[hn], hy = new double[hn];
		for (int i = 0; i < hn; i++) {
			hx[i] = (f.hxs[i] * s + ox) * SS;
			hy[i] = (f.hys[i] * s + oy) * SS;
		}

		for (int y = y0; y <= y1; y++) {
			int row = y * sw;
			for (int x = x0; x <= x1; x++) {
				if (!inside(X, Y, x, y)) continue;
				// 描边只画扩张出来的那一圈环，不能把原多边形也填掉
				if (dilate != 0 && inside(OX, OY, x, y)) continue;
				if (hn > 0 && inside(hx, hy, x, y)) continue;
				float d = (float) (a * x + b * y + c + bias);
				if (d > zbuf[row + x]) {
					zbuf[row + x] = d;
					pix[row + x] = rgb;
				}
			}
		}
	}

	/** 奇偶规则点内判定。 */
	static boolean inside(double[] X, double[] Y, double px, double py) {
		boolean in = false;
		for (int i = 0, j = X.length - 1; i < X.length; j = i++) {
			if ((Y[i] > py) != (Y[j] > py)
				&& px < (X[j] - X[i]) * (py - Y[i]) / (Y[j] - Y[i]) + X[i])
				in = !in;
		}
		return in;
	}

	static double min(double[] a) {
		double m = Double.MAX_VALUE;
		for (double v : a) m = Math.min(m, v);
		return m;
	}

	static double max(double[] a) {
		double m = -Double.MAX_VALUE;
		for (double v : a) m = Math.max(m, v);
		return m;
	}

	/** 一根多材质杆（与图标同款配色顺序）。 */
	static void bandedShaft(Graphics2D g, double[] axis, double len, double r, Color[] cols,
		double tx, double ty, double tw, double th) {
		FACES.clear();
		int n = cols.length;
		double[] base = mul(axis, -len / 2);
		List<double[]> seg = new ArrayList<>();
		for (int i = 0; i <= n; i++) seg.add(add(base, mul(axis, len * i / n)));
		for (int i = 0; i < n; i++)
			prism(seg.get(i), seg.get(i + 1), r, PHASE, cols[i], i == 0, i == n - 1);
		for (int i = 1; i < n; i++)
			if (i != n / 2) joint(seg.get(i), axis, r);   // 齿轮那一格不放接头
		gear(seg.get(n / 2), axis, r * 0.62, r * 1.78, r * 1.28, r * 0.24, 9, COPPER);
		flush(g, tx, ty, tw, th, 0);
	}

	// ---------- 文字 ----------

	static Font pickFont(String[] names, int style, int size) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (String n : names)
			for (Font f : ge.getAllFonts())
				if (f.getFontName().equalsIgnoreCase(n) && f.canDisplay('传'))
					return f.deriveFont(style, size);
		return new Font("SansSerif", style, size);
	}

	static Font DISPLAY;   // 特大标题
	static Font HEAVY;     // 次级标题
	static Font BODY;      // 正文/徽章

	static void initFonts() {
		DISPLAY = pickFont(new String[] {"Noto Sans SC Bold", "Microsoft YaHei Bold", "MiSans", "SimHei"}, Font.BOLD, 100);
		HEAVY = pickFont(new String[] {"Noto Sans SC Bold", "Microsoft YaHei Bold", "MiSans"}, Font.BOLD, 100);
		BODY = pickFont(new String[] {"Noto Sans SC Medium", "MiSans", "Microsoft YaHei"}, Font.PLAIN, 100);
	}

	/** 带字距的文字轮廓。 */
	static Shape textShape(String s, Font f, double tracking) {
		FontRenderContext frc = new FontRenderContext(null, true, true);
		Path2D.Double path = new Path2D.Double();
		double x = 0;
		for (int i = 0; i < s.length(); i++) {
			String ch = s.substring(i, i + 1);
			GlyphVector gv = f.createGlyphVector(frc, ch);
			path.append(gv.getOutline((float) x, 0), false);
			x += gv.getGlyphMetrics(0).getAdvanceX() + tracking;
		}
		// 去掉尾部多加的字距对宽度的贡献
		return path;
	}

	/** 描边 + 渐变填充 + 投影的大字。返回实际绘制宽度。 */
	static double drawBigText(Graphics2D g, String s, Font f, double tracking, double cx, double baselineY,
		Color top, Color bottom, Color outline, float outlineW, double shadowDy) {
		Shape sh = textShape(s, f, tracking);
		Rectangle2D b = sh.getBounds2D();
		double dx = cx - (b.getX() + b.getWidth() / 2);

		AffineTransform at = AffineTransform.getTranslateInstance(dx, baselineY - b.getMaxY());
		Shape placed = at.createTransformedShape(sh);
		Rectangle2D pb = placed.getBounds2D();

		// 投影
		if (shadowDy > 0) {
			g.setColor(new Color(0, 0, 0, 130));
			g.translate(0, shadowDy);
			g.fill(placed);
			g.translate(0, -shadowDy);
		}
		// 描边（先画粗边，再盖填充，留下外半圈）
		g.setColor(outline);
		g.setStroke(new BasicStroke(outlineW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(placed);
		// 填充
		g.setPaint(new LinearGradientPaint(
			new Point2D.Double(pb.getCenterX(), pb.getMinY()),
			new Point2D.Double(pb.getCenterX(), pb.getMaxY()),
			new float[] {0f, 0.55f, 1f},
			new Color[] {top, bottom, mul(bottom, 0.86)}));
		g.fill(placed);
		return pb.getWidth();
	}

	/** 居中绘制的普通文字（无描边）。 */
	static void drawPlain(Graphics2D g, String s, Font f, double tracking, double cx, double baselineY, Color c) {
		Shape sh = textShape(s, f, tracking);
		Rectangle2D b = sh.getBounds2D();
		AffineTransform at = AffineTransform.getTranslateInstance(
			cx - (b.getX() + b.getWidth() / 2), baselineY - b.getMaxY());
		g.setColor(c);
		g.fill(at.createTransformedShape(sh));
	}

	/** 左对齐绘制的普通文字。 */
	static void drawLeft(Graphics2D g, String s, Font f, double tracking, double x, double baselineY, Color c) {
		Shape sh = textShape(s, f, tracking);
		Rectangle2D b = sh.getBounds2D();
		AffineTransform at = AffineTransform.getTranslateInstance(x - b.getX(), baselineY - b.getMaxY());
		g.setColor(c);
		g.fill(at.createTransformedShape(sh));
	}

	static double textWidth(String s, Font f, double tracking) {
		return textShape(s, f, tracking).getBounds2D().getWidth();
	}

	/** 圆角药丸背景，返回宽度。 */
	static double pill(Graphics2D g, String s, Font f, double tracking, double cx, double cy,
		Color bg, Color fg, double padX, double padY) {
		double tw = textWidth(s, f, tracking);
		Rectangle2D tb = f.getStringBounds(s, new FontRenderContext(null, true, true));
		double w = tw + padX * 2, h = tb.getHeight() * 0.92 + padY * 2;
		RoundRectangle2D rr = new RoundRectangle2D.Double(cx - w / 2, cy - h / 2, w, h, h, h);
		g.setColor(alpha(Color.BLACK, 70));
		g.fill(new RoundRectangle2D.Double(rr.getX(), rr.getY() + 6, rr.getWidth(), rr.getHeight(), h, h));
		g.setColor(bg);
		g.fill(rr);
		drawPlain(g, s, f, tracking, cx, cy + tb.getHeight() * 0.34, fg);
		return w;
	}

	/** 左对齐药丸：左边距固定，任何画布比例下都不会出界。 */
	static void pillLeft(Graphics2D g, String s, Font f, double tracking, double leftX, double cy,
		Color bg, Color fg, double padX, double padY) {
		double w = textWidth(s, f, tracking) + padX * 2;
		pill(g, s, f, tracking, leftX + w / 2, cy, bg, fg, padX, padY);
	}

	/**
	 * 分段药丸：同一行里放不同字号/颜色的文字（比如把「144」放大高亮、后面跟小字说明）。
	 * 各段按墨迹中心对齐。
	 */
	static double pillMulti(Graphics2D g, String[] parts, Font[] fonts, Color[] fgs, double tracking,
		double cx, double cy, Color bg, double padX, double padY, double gap) {
		int n = parts.length;
		double[] ws = new double[n];
		double total = gap * (n - 1);
		double maxH = 0;
		FontRenderContext frc = new FontRenderContext(null, true, true);
		for (int i = 0; i < n; i++) {
			ws[i] = textWidth(parts[i], fonts[i], tracking);
			total += ws[i];
			maxH = Math.max(maxH, fonts[i].getStringBounds(parts[i], frc).getHeight());
		}
		double w = total + padX * 2, h = maxH * 0.92 + padY * 2;
		RoundRectangle2D rr = new RoundRectangle2D.Double(cx - w / 2, cy - h / 2, w, h, h, h);
		g.setColor(alpha(Color.BLACK, 80));
		g.fill(new RoundRectangle2D.Double(rr.getX(), rr.getY() + 7, rr.getWidth(), rr.getHeight(), h, h));
		g.setColor(bg);
		g.fill(rr);
		g.setStroke(new BasicStroke(5f));
		g.setColor(alpha(Color.WHITE, 60));
		g.draw(rr);

		double x = cx - total / 2;
		for (int i = 0; i < n; i++) {
			Shape sh = textShape(parts[i], fonts[i], tracking);
			Rectangle2D b = sh.getBounds2D();
			// 按墨迹垂直居中
			AffineTransform at = AffineTransform.getTranslateInstance(
				x - b.getX(), cy - (b.getMinY() + b.getMaxY()) / 2);
			g.setColor(fgs[i]);
			g.fill(at.createTransformedShape(sh));
			x += ws[i] + gap;
		}
		return w;
	}

	// ---------- 背景 ----------

	static void background(Graphics2D g, boolean warm) {
		// 深蓝底：比图标更深，让金色大字跳出来
		g.setPaint(new LinearGradientPaint(
			new Point2D.Double(0, 0), new Point2D.Double(W * 0.35, H),
			new float[] {0f, 0.5f, 1f},
			new Color[] {new Color(0x123A5C), new Color(0x0C2740), new Color(0x061524)}));
		g.fillRect(0, 0, W, H);

		// 主体背后的暖色光晕
		double glowX = warm ? W * 0.715 : W * 0.5;
		g.setPaint(new RadialGradientPaint(
			new Point2D.Double(glowX, H * 0.52), (float) (W * 0.42),
			new float[] {0f, 0.55f, 1f},
			new Color[] {new Color(255, 168, 76, 78), new Color(255, 140, 40, 26), new Color(0, 0, 0, 0)}));
		g.fillRect(0, 0, W, H);

		// 工程网格
		double step = 78;
		g.setStroke(new BasicStroke(2f));
		g.setColor(new Color(255, 255, 255, 13));
		for (double x = 0; x <= W; x += step) g.draw(new java.awt.geom.Line2D.Double(x, 0, x, H));
		for (double y = 0; y <= H; y += step) g.draw(new java.awt.geom.Line2D.Double(0, y, W, y));

		// 两条亮线（呼应图标里的十字）
		g.setStroke(new BasicStroke(6f));
		g.setColor(new Color(255, 255, 255, 26));
		g.draw(new java.awt.geom.Line2D.Double(W * 0.055, 0, W * 0.055, H));
		g.draw(new java.awt.geom.Line2D.Double(0, H * 0.20, W, H * 0.20));

		// 四角压暗
		g.setPaint(new RadialGradientPaint(
			new Point2D.Double(W / 2.0, H / 2.0), (float) (W * 0.62),
			new float[] {0.55f, 1f}, new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)}));
		g.fillRect(0, 0, W, H);
	}

	/** 底部一排材质色块，暗示「材质很多」。 */
	static void materialStrip(Graphics2D g) {
		Color[] cols = {DIRT, STONE, IRON, GOLD, EMERALD, REDSTONE, LAPIS, AMETHYST,
			DIRT, STONE, IRON, GOLD, EMERALD, REDSTONE, LAPIS, AMETHYST};
		double sw = W / (double) cols.length;
		for (int i = 0; i < cols.length; i++) {
			g.setColor(mul(cols[i], 0.85));
			g.fillRect((int) Math.floor(i * sw), H - 16, (int) Math.ceil(sw) + 1, 16);
		}
	}

	/**
	 * 底部一条 Minecraft 风格的草方块地面：像素格泥土 + 顶部带锯齿的草皮。
	 * 这是整张图里最直白的「这是我的世界」信号。
	 */
	static void groundBand(Graphics2D g) {
		int bandH = 54, cell = 16;
		int yTop = H - bandH;

		// 泥土层
		for (int x = 0; x < W; x += cell)
			for (int y = yTop; y < H; y += cell) {
				double h = hash(x / cell, y / cell, 4242);
				g.setColor(mul(DIRT, 0.86 + h * 0.24));
				g.fillRect(x, y, cell + 1, cell + 1);
			}

		// 草皮：每列深浅不一，做出像素锯齿
		for (int x = 0; x < W; x += cell) {
			int gh = 14 + (int) (hash(x / cell, 7, 777) * 18);
			for (int y = yTop; y < yTop + gh; y += cell) {
				double h = hash(x / cell, y / cell, 888);
				g.setColor(mul(GRASS_TOP, 0.86 + h * 0.26));
				g.fillRect(x, y, cell + 1, Math.min(cell + 1, yTop + gh - y));
			}
		}
	}

	// ---------- 变体 A：左文右图 ----------
	static void coverA(Graphics2D g) {
		background(g, true);

		// ---- 主体：按 dirt_shaft 的真实模型 + 真实贴图画的泥土传动杆，配机械动力的原版齿轮 ----
		// 轴取世界 Z 轴（等距轴之一）：齿轮垂直于它，投影出来才是正确的椭圆透视。
		FACES.clear();
		double[] axis = v(0, 0, -1);
		double len = 2.25;
		double[] base = mul(axis, -len / 2);
		realShaft(base, add(base, mul(axis, len)), len, TEX);
		createCogwheel(add(base, mul(axis, len * 0.5)), axis);
		flush(g, W * 0.700, H * 0.500, W * 0.32, H * 0.56, 0);

		// ---- 前景：三个原版方块，点明这是「我的世界」的模组 ----
		FACES.clear();
		grassBlock(v(0, 0, 0), 1.15);
		vanillaBlock(v(1.30, -0.10, 0.45), 0.85, TEX_DIAMOND, TEX_DIAMOND, TEX_DIAMOND);
		vanillaBlock(v(0.40, 1.10, 0.20), 0.70, TEX_GOLD, TEX_GOLD, TEX_GOLD);
		flush(g, W * 0.868, H * 0.795, W * 0.25, H * 0.29, 0);

		// 左上角信息药丸
		pillLeft(g, "我的世界 1.21.1 · Create 模组", BODY.deriveFont(30f), 1.5, 60, 76,
			alpha(new Color(0x0A2D4A), 230), new Color(0xBBD8F0), 30, 13);

		// 主标题
		double cxTitle = W * 0.28;
		drawPlain(g, "机械动力", HEAVY.deriveFont(76f), 10, cxTitle, 320, new Color(0xE8F2FA));
		drawBigText(g, "更多传动", DISPLAY.deriveFont((float) (200 * TS)), 4, cxTitle, 560,
			new Color(0xFFE580), new Color(0xF2A01E), new Color(0x2A1A06), 26, 12);

		// 核心数字：144 放大高亮
		pillMulti(g, new String[] {"144", "种材质传动杆"},
			new Font[] {HEAVY.deriveFont((float) (72 * TS)), HEAVY.deriveFont((float) (46 * TS))},
			new Color[] {new Color(0xFFE580), Color.WHITE},
			1.0, cxTitle, 672, new Color(0xE04A2F), 46, 18, 13);

		// 英文名
		drawPlain(g, "More Transmission", BODY.deriveFont(36f), 3, cxTitle, 792,
			alpha(Color.WHITE, 140));

		groundBand(g);
	}

	// ---------- 变体 B：上字 + 下方一排多材质杆 ----------
	static void coverB(Graphics2D g) {
		background(g, false);

		// 顶部标题区（完整模组名）
		drawBigText(g, "机械动力：更多传动", DISPLAY.deriveFont(122f), 8, W / 2.0, 196,
			new Color(0xFFE580), new Color(0xF2A01E), new Color(0x2A1A06), 22, 10);

		// 下方四根杆，色带错开，凑出「材质很多」的观感
		Color[] palette = {DIRT, REDSTONE, GOLD, EMERALD, IRON, STONE, AMETHYST, LAPIS};
		int shafts = 4;
		double slot = W / (double) shafts;
		double[] axis = v(0, 0, -1);
		double len = 7.2, r = 0.52;

		for (int i = 0; i < shafts; i++) {
			FACES.clear();
			double[] base = mul(axis, -len / 2);
			Color[] cols = new Color[4];
			for (int k = 0; k < 4; k++) cols[k] = palette[(i * 2 + k) % palette.length];
			List<double[]> seg = new ArrayList<>();
			for (int k = 0; k <= 4; k++) seg.add(add(base, mul(axis, len * k / 4.0)));
			for (int k = 0; k < 4; k++)
				prism(seg.get(k), seg.get(k + 1), r, PHASE, cols[k], k == 0, k == 3);
			for (int k = 1; k < 4; k++) joint(seg.get(k), axis, r);
			flush(g, slot * i + slot / 2, H * 0.575, slot * 0.72, H * 0.46, 0);
		}

		// 底部大字：强调数量
		drawBigText(g, "144 种材质", DISPLAY.deriveFont(104f), 6, W / 2.0, H - 78,
			Color.WHITE, new Color(0xBBD8F0), new Color(0x061524), 20, 8);

		groundBand(g);
	}

	// ---------- 输出 ----------

	public static void main(String[] args) throws Exception {
		initFonts();
		// dirt_shaft 的模型直接引用 minecraft:block/dirt，所以贴图就是原版泥土贴图
		TEX = loadVanillaTexture("assets/minecraft/textures/block/dirt.png");
		// 原版方块贴图
		// grass_block_top 与 side_overlay 都是灰度遮罩，必须乘群系草色；grass_block_side 本身已上色
		TEX_GRASS_TOP = tint(loadVanillaTexture("assets/minecraft/textures/block/grass_block_top.png"),
			GRASS_R, GRASS_G, GRASS_B);
		TEX_GRASS_SIDE = loadVanillaTexture("assets/minecraft/textures/block/grass_block_side.png");
		TEX_GRASS_OVERLAY = tint(
			loadVanillaTexture("assets/minecraft/textures/block/grass_block_side_overlay.png"),
			GRASS_R, GRASS_G, GRASS_B);
		TEX_GOLD = loadVanillaTexture("assets/minecraft/textures/block/gold_block.png");
		TEX_DIAMOND = loadVanillaTexture("assets/minecraft/textures/block/diamond_block.png");
		// 机械动力的原版齿轮贴图：色相不动，只整体提亮（Create 的齿轮纹理本身很暗，
		// 放在深色封面上会糊成一个黑褐色块；这里等于给它打光）
		TEX_COGWHEEL = tint(loadCreateTexture("cogwheel"), 1.42, 1.36, 1.30);
		TEX_COGWHEEL_AXIS = tint(loadCreateTexture("cogwheel_axis"), 1.42, 1.36, 1.30);
		TEX_AXIS_TOP = tint(loadCreateTexture("axis_top"), 1.42, 1.36, 1.30);
		File outDir = new File(args.length > 0 ? args[0] : ".");
		outDir.mkdirs();

		// 诊断视图：只渲染齿轮本身，方便核对模型是否还原正确
		if (args.length > 1 && args[1].equals("cog")) {
			BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			background(g, true);
			FACES.clear();
			createCogwheel(v(0, 0, 0), v(0, 0, -1));
			flush(g, W / 2.0, H / 2.0, W * 0.7, H * 0.8, 0);
			g.dispose();
			write(img, new File(outDir, "_cog-only.png"), 1280, 720);
			System.out.println("wrote _cog-only");
			return;
		}

		String[] names = {"cover-a", "cover-b"};

		// ---- 16:9 ----
		setCanvas(1920, 1080);
		for (int i = 0; i < names.length; i++) {
			BufferedImage big = render(i);
			write(big, new File(outDir, names[i] + "-1920x1080.png"), 1920, 1080);
			write(big, new File(outDir, names[i] + "-1280x720.png"), 1280, 720);
			write(big, new File(outDir, names[i] + "-480x270.png"), 480, 270);
			System.out.println("wrote " + names[i]);
		}

		// ---- 4:3（高度仍为 1080，竖向排版不变，只收窄横向）----
		setCanvas(1440, 1080);
		for (int i = 0; i < names.length; i++) {
			BufferedImage big = render(i);
			write(big, new File(outDir, names[i] + "-1440x1080.png"), 1440, 1080);
			write(big, new File(outDir, names[i] + "-1200x900.png"), 1200, 900);
			write(big, new File(outDir, names[i] + "-480x360.png"), 480, 360);
			System.out.println("wrote " + names[i] + " (4:3)");
		}
	}

	static BufferedImage render(int variant) {
		BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (variant == 0) coverA(g); else coverB(g);
		g.dispose();
		return img;
	}

	static void write(BufferedImage src, File out, int w, int h) {
		BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(src, 0, 0, w, h, null);
		g.dispose();
		try {
			ImageIO.write(dst, "png", out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
