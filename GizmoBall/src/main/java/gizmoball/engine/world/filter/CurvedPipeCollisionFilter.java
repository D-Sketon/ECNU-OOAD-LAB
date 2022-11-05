package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.QuarterCircle;

public class CurvedPipeCollisionFilter implements CollisionFilter {
    @Override
    public boolean isAllowedBroadPhase(AbstractShape shape1, AbstractShape shape2) {
        return true;
    }

    @Override
    public boolean isAllowedNarrowPhase(AbstractShape shape1, AbstractShape shape2) {
        return true;
    }

    @Override
    public boolean isAllowedManifold(AbstractShape shape1, AbstractShape shape2, AbstractShape shape, Penetration penetration) {
        QuarterCircle quarterCircle;
        Circle circle;

        if (shape1 instanceof QuarterCircle && shape2 instanceof Circle) {
            quarterCircle = (QuarterCircle) shape1;
            circle = (Circle) shape2;
        } else if (shape2 instanceof QuarterCircle && shape1 instanceof Circle) {
            quarterCircle = (QuarterCircle) shape2;
            circle = (Circle) shape1;
        } else {
            return true;
        }

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
        // 圆形是否在扇形的边之中
        boolean isInSide = r1.cross(c2c) * c2c.cross(r2) >= 0 && r1.cross(c2c) * r1.cross(r2) >= 0;
        // 圆形是否在扇形之中
        boolean isInside = c2c.getMagnitude() < quarterCircle.getRadius();

        if (isInside && isInSide) {
            // 在内和弧线发生碰撞，需要反转法线并改变深度
            if (c2c.getMagnitude() + circle.getRadius() >= quarterCircle.getRadius()) {
                penetration.getNormal().negate();
                penetration.setDepth(circle.getRadius() - penetration.getDepth());
                return true;
            }
            // 在内但并没有发生碰撞
            return false;
        } else if (isInside) {
            // 从管道口进入
            if(penetration.getNormal().dot(r1) < Epsilon.E || penetration.getNormal().dot(r2) < Epsilon.E) {
                return false;
            }
            return true;
        } else {
            // 在外和弧线发生碰撞
            return true;
        }
    }
}
