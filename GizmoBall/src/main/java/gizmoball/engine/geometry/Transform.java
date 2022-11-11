package gizmoball.engine.geometry;

import gizmoball.engine.collision.Interval;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 物体旋转/位移
 */
@Getter
@Setter
@ToString
public class Transform {

    /**
     * 旋转角度的cos
     */
    public double cost;

    /**
     * 旋转角度的sin
     */
    public double sint;

    /**
     * 相对于原点的x轴偏移，相当于世界的x坐标
     */
    public double x;

    /**
     * 相对于原点的y轴偏移，相当于世界的y坐标
     */
    public double y;

    public Transform() {
        this(1.0, 0.0, 0.0, 0.0);
    }

    public Transform(double cost, double sint, double x, double y) {
        this.cost = cost;
        this.sint = sint;
        this.x = x;
        this.y = y;
    }

    /**
     * 基于本{@link Transform}复制一个新{@link Transform}
     *
     * @return Transform
     */
    public Transform copy() {
        return new Transform(this.cost, this.sint, this.x, this.y);
    }

    /**
     * 绕着某一点做旋转
     *
     * @param c 旋转角度的cos
     * @param s 旋转角度的sin
     * @param x 旋转点的x坐标
     * @param y 旋转点的y坐标
     */
    public void rotate(double c, double s, double x, double y) {
        double cosT = this.cost;
        double sinT = this.sint;
        this.cost = Interval.sandwich(c * cosT - s * sinT, -1.0, 1.0);
        this.sint = Interval.sandwich(s * cosT + c * sinT, -1.0, 1.0);
        if (Math.abs(this.cost) < Epsilon.E) {
            this.cost = 0;
        }
        if (Math.abs(this.sint) < Epsilon.E) {
            this.sint = 0;
        }
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
     * 按给定{@link Vector2}平移
     *
     * @param x x轴平移距离
     * @param y y轴平移距离
     */
    public void translate(double x, double y) {
        this.x += x;
        this.y += y;
    }

    /**
     * 按给定{@link Vector2}平移
     *
     * @param vector2 方向向量
     */
    public void translate(Vector2 vector2) {
        this.translate(vector2.x, vector2.y);
    }

    /**
     * 对给定{@link Vector2}的的x坐标进行变换
     *
     * @param vector 待变换{@link Vector2}
     * @return double
     */
    public double getTransformedX(Vector2 vector) {
        return this.cost * vector.x - this.sint * vector.y + this.x;
    }

    /**
     * 对给定{@link Vector2}的的y坐标进行变换
     *
     * @param vector 待变换{@link Vector2}
     * @return double
     */
    public double getTransformedY(Vector2 vector) {
        return this.sint * vector.x + this.cost * vector.y + this.y;
    }

    /**
     * 对给定{@link Vector2}的的x和y坐标进行旋转平移变换
     *
     * @param vector 变换{@link Vector2}
     */
    public void transform(Vector2 vector) {
        double x = vector.x;
        double y = vector.y;
        vector.x = this.cost * x - this.sint * y + this.x;
        vector.y = this.sint * x + this.cost * y + this.y;
    }


    /**
     * 对给定{@link Vector2}的的x和y坐标进行旋转平移变换，返回变换后的新向量
     *
     * @param vector 待变换{@link Vector2}
     * @return Vector2
     */
    public Vector2 getTransformed(Vector2 vector) {
        Vector2 tv = new Vector2();
        double x = vector.x;
        double y = vector.y;
        tv.x = this.cost * x - this.sint * y + this.x;
        tv.y = this.sint * x + this.cost * y + this.y;
        return tv;
    }

    /**
     * 对给定{@link Vector2}的的x和y坐标进行逆旋转平移变换，返回变换后的新向量
     *
     * @param vector 待变换{@link Vector2}
     * @return Vector
     */
    public Vector2 getInverseTransformed(Vector2 vector) {
        Vector2 tv = new Vector2();
        double tx = vector.x - this.x;
        double ty = vector.y - this.y;
        tv.x = this.cost * tx + this.sint * ty;
        tv.y = -this.sint * tx + this.cost * ty;
        return tv;
    }

    /**
     * 对给定{@link Vector2}的的x和y坐标进行旋转变换，返回变换后的新向量
     *
     * @param vector 待变换{@link Vector2}
     * @return Vector2
     */
    public Vector2 getTransformedR(Vector2 vector) {
        Vector2 v = new Vector2();
        double x = vector.x;
        double y = vector.y;
        v.x = this.cost * x - this.sint * y;
        v.y = this.sint * x + this.cost * y;
        return v;
    }

    /**
     * 对给定{@link Vector2}的的x和y坐标进行旋转变换
     *
     * @param vector 待变换{@link Vector2}
     */
    public void transformR(Vector2 vector) {
        double x = vector.x;
        double y = vector.y;
        vector.x = this.cost * x - this.sint * y;
        vector.y = this.sint * x + this.cost * y;
    }


    /**
     * 对给定{@link Vector2}的的x和y坐标进行旋转逆变换
     *
     * @param vector 待变换{@link Vector2}
     * @return Vector2
     */
    public Vector2 getInverseTransformedR(Vector2 vector) {
        Vector2 v = new Vector2();
        double x = vector.x;
        double y = vector.y;
        // sin(-a) = -sin(a)
        v.x = this.cost * x + this.sint * y;
        v.y = -this.sint * x + this.cost * y;
        return v;
    }

    /**
     * 根据cost,sint获取角度
     *
     * @return double
     */
    public double getAngle() {
        double angle = 0;
        if (cost > 0 && sint > 0) {
            angle = Math.toDegrees(Math.asin(sint));
        } else if (cost > 0 && sint < 0) {
            angle = Math.toDegrees(Math.asin(sint));
        } else if (cost < 0 && sint > 0) {
            angle = 180 - Math.toDegrees(Math.asin(sint));
        } else if (cost < 0 && sint < 0) {
            angle = -180 - Math.toDegrees(Math.asin(sint));
        } else if (cost == 0 && sint > 0) {
            angle = 90;
        } else if (cost == 0 && sint < 0) {
            angle = -90;
        } else if (cost > 0 && sint == 0) {
            angle = 0;
        } else if (cost < 0 && sint == 0) {
            angle = 180;
        }
        return angle;
    }

}
