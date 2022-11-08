package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.QuarterCircle;
import gizmoball.engine.physics.PhysicsBody;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CurvedPipeCollisionFilter implements CollisionFilter {

    private Vector2 gravity;
    @Override
    public boolean isAllowedBroadPhase(PhysicsBody body1, PhysicsBody body2) {
        return true;
    }

    @Override
    public boolean isAllowedNarrowPhase(PhysicsBody body1, PhysicsBody body2) {
        return true;
    }

    @Override
    public boolean isAllowedManifold(PhysicsBody body1, PhysicsBody body2, AbstractShape shape, Penetration penetration) {
        AbstractShape shape1 = body1.getShape();
        AbstractShape shape2 = body2.getShape();
        Vector2 linearVelocity = body1.getLinearVelocity();

        if(!(shape2 instanceof QuarterCircle)) {
            return true;
        }
        QuarterCircle quarterCircle = (QuarterCircle) shape2;
        Circle circle = (Circle) shape1;

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
            // 在内部就要施加反重力
            body1.integrateVelocity(gravity.getNegative());
            if (linearVelocity.getMagnitude() < 90) {
                linearVelocity.multiply(90 / linearVelocity.getMagnitude());
            }
            // 在内和弧线发生碰撞，需要反转法线并改变深度
            if (c2c.getMagnitude() + circle.getRadius() >= quarterCircle.getRadius()) {
                penetration.getNormal().negate();
                penetration.setDepth(circle.getRadius() - penetration.getDepth());
                return true;
            }
            // 在内但并没有发生碰撞
            return false;
        } else if (isInside) {
            // 严格判断是否从管道口进出
            if (penetration.getNormal().dot(r1) < 1e5 * Epsilon.E || penetration.getNormal().dot(r2) < 1e5 * Epsilon.E) {
                return false;
            }
            // 否则视为产生碰撞
            return true;
        } else {
            // 在外和弧线发生碰撞
            return true;
        }
    }
}
