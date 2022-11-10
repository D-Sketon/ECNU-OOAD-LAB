package gizmoball.engine.geometry.shape;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>形状的抽象</p>
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class", visible = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractShape implements Convex {

    /**
     * 物体旋转/位移
     */
    protected Transform transform;

    /**
     * 物体缩放比率
     */
    protected int rate = 1;

    protected AbstractShape() {
        this(new Transform());
    }

    protected AbstractShape(Transform transform) {
        this.transform = transform;
    }

    /**
     * 物体缩放
     *
     * @param rate 缩放比率，最小为1
     */
    public abstract void zoom(int rate);

    /**
     * 返回对应图形的AABB
     *
     * @return AABB
     */
    public abstract AABB createAABB();

    /**
     * 返回投影到指定分离轴后的{@link Interval}
     *
     * @param axis 分离轴
     * @return Interval
     */
    public abstract Interval project(Vector2 axis);

    /**
     * 根据所给物体密度获得物体质量
     *
     * @param density 物体密度
     * @return Mass
     */
    public abstract Mass createMass(double density);

    /**
     * 绕着某一点做旋转
     *
     * @param c 旋转角度的cos
     * @param s 旋转角度的sin
     * @param x 旋转点的x坐标
     * @param y 旋转点的y坐标
     */
    public void rotate(double c, double s, double x, double y) {
        transform.rotate(c, s, x, y);
    }

    /**
     * 绕着某一点做旋转
     *
     * @param theta 旋转角度
     * @param x     旋转点的x坐标
     * @param y     旋转点的y坐标
     */
    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
    }

    /**
     * 按给定坐标平移
     *
     * @param x x轴平移距离
     * @param y y轴平移距离
     */
    public void translate(double x, double y) {
        transform.translate(x, y);
    }

    /**
     * 按给定向量平移
     *
     * @param vector2 方向向量
     */
    public void translate(Vector2 vector2) {
        transform.translate(vector2);
    }

    /**
     * 获得本地坐标系下的点坐标
     *
     * @param worldPoint 世界坐标系下的坐标
     * @return Vector2
     */
    public Vector2 getLocalPoint(Vector2 worldPoint) {
        return this.transform.getInverseTransformed(worldPoint);
    }
}
