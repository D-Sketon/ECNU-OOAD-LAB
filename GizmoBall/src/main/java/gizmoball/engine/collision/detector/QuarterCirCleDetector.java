package gizmoball.engine.collision.detector;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.QuarterCircle;

public class QuarterCirCleDetector {

    public static boolean detect(QuarterCircle quarterCircle, Circle circle, Penetration penetration) {
        Transform transform1 = quarterCircle.getTransform();
        Transform transform2 = circle.getTransform();
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
            return true;
        }
        return false;
    }

}
