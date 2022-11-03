package gizmoball.engine.collision.detector;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;

public class CircleDetector {

    /**
     * <p>使用于narrowPhase</p>
     * 判断两个{@link Circle}是否发生碰撞
     *
     * @param circle1     待测圆
     * @param circle2     待测圆
     * @param shape       近似图形
     * @param penetration 穿透信息
     * @return DetectorResult
     */
    public static DetectorResult detect(Circle circle1, Circle circle2, AbstractShape shape, Penetration penetration) {
        Transform transform1 = circle1.getTransform();
        Transform transform2 = circle2.getTransform();
        // 构造圆心坐标
        Vector2 ce1 = new Vector2(transform1.getX(), transform1.getY());
        Vector2 ce2 = new Vector2(transform2.getX(), transform2.getY());
        Vector2 v = ce1.to(ce2);
        double radii = circle1.getRadius() + circle2.getRadius();
        double mag = v.getMagnitudeSquared();
        // 发生碰撞
        if (mag < radii * radii) {
            if (penetration != null) {
                penetration.setDepth(radii - v.normalize());
                penetration.getNormal().x = v.x;
                penetration.getNormal().y = v.y;
            }
            return new DetectorResult(true, shape);
        }
        return new DetectorResult(false, shape);
    }

}
