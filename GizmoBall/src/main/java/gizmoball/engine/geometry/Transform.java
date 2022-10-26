package gizmoball.engine.geometry;

import gizmoball.engine.geometry.shape.*;

/**
 * 物体旋转/位移，对物体做如下设定：
 * <p>
 *     {@link Circle} 和 {@link Rectangle} 中心点位于重心上
 * </p>
 * <p>
 *     {@link Triangle} 中心点位于直角顶点上
 * </p>
 * <p>
 *     {@link QuarterCircle} 中心点位于圆心上
 * </p>
 * <p>
 *     其余组合图形若无说明则默认位于重心上
 * </p>
 */
public class Transform {

    /**
     * 旋转角度的cos
     */
    protected double cost = 1.0;

    /**
     * 旋转角度的sin
     */
    protected double sint = 0.0;

    /**
     * 相对于原点的x轴偏移
     */
    protected double x = 0.0;

    /**
     * 相对于原点的y轴偏移
     */
    protected double y = 0.0;

    // 存在问题
    /**
     * 绕着某一点做旋转
     *
     * @param c 旋转角度的cos
     * @param s 旋转角度的sin
     * @param x 旋转点的x坐标
     * @param y 旋转点的y坐标
     */
    public void rotate(double c, double s, double x, double y) {
        this.cost = Transform.sandwich(c * this.cost - s * this.sint, -1.0, 1.0);
        this.sint = Transform.sandwich(s * this.cost + c * this.sint, -1.0, 1.0);

        double cx = this.x - x;
        double cy = this.y - y;
        this.x = c * cx - s * cy + x;
        this.y = s * cx + c * cy + y;
    }

    /**
     * 绕着某一点做旋转
     *
     * @param theta 旋转角度
     * @param x     旋转点的x坐标
     * @param y     旋转点的y坐标
     */
    public void rotate(double theta, double x, double y) {
        this.rotate(Math.cos(theta), Math.sin(theta), x, y);
    }

    /**
     * 按给定坐标平移
     *
     * @param x x轴平移距离
     * @param y y轴平移距离
     */
    public void translate(double x, double y) {
        this.x += x;
        this.y += y;
    }

    /**
     * 按给定向量平移
     *
     * @param vector2 方向向量
     */
    public void translate(Vector2 vector2) {
        this.translate(vector2.getX(), vector2.getY());
    }

    /**
     * 夹逼，消除double误差
     *
     * @param value 原值
     * @param left  下界
     * @param right 上界
     * @return 夹逼后的值，必定位于上界和下界之间
     */
    public static double sandwich(double value, double left, double right) {
        return (value <= right && value >= left) ? value : (value < left ? left : right);
    }
}
