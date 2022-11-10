package gizmoball.engine.collision.detector;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.QuarterCircle;
import gizmoball.engine.geometry.shape.Rectangle;

public class QuarterCirCleDetector {

    /**
     * <p>使用于narrowPhase</p>
     * 判断{@link QuarterCircle}和{@link Circle}是否发生碰撞
     *
     * @param quarterCircle 扇形
     * @param circle        圆形
     * @param penetration   穿透信息
     * @param isFlipped     参数是否发生翻转
     * @return DetectorResult
     */
    public static DetectorResult detect(QuarterCircle quarterCircle, Circle circle, Penetration penetration, boolean isFlipped) {
        Transform transform1 = quarterCircle.getTransform();
        Transform transform2 = circle.getTransform();

        Vector2 v0 = transform1.getTransformed(quarterCircle.getVertices()[0]);
        Vector2 v1 = transform1.getTransformed(quarterCircle.getVertices()[1]);
        Vector2 v2 = transform1.getTransformed(quarterCircle.getVertices()[2]);

        Vector2 ce1 = v1;
        Vector2 ce2 = new Vector2(transform2.getX(), transform2.getY());
        Vector2 r1 = v1.to(v0);
        Vector2 r2 = v1.to(v2);
        Vector2 c2c = ce1.to(ce2);
        // 圆形在扇形的边之中，将扇形近似为圆形
        if (r1.cross(c2c) * c2c.cross(r2) >= 0 && r1.cross(c2c) * r1.cross(r2) >= 0) {
            Circle circle1 = new Circle(quarterCircle.getRadius(),
                    new Transform(transform1.getCost(), transform1.getSint(), ce1.x, ce1.y));
            if (isFlipped) {
                return CircleDetector.detect(circle, circle1, circle1, penetration);
            }
            return CircleDetector.detect(circle1, circle, circle1, penetration);
        } else {
            // 将扇形近似为多边形
            Rectangle rectangle = new Rectangle(
                    quarterCircle.getRadius() / 2,
                    quarterCircle.getRadius() / 2,
                    transform1.copy());
            if (isFlipped) {
                return SatDetector.detect(circle, rectangle, rectangle, penetration);
            }
            return SatDetector.detect(rectangle, circle, rectangle, penetration);
        }
    }

}
