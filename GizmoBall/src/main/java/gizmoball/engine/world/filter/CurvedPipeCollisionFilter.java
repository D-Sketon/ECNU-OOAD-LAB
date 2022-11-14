package gizmoball.engine.world.filter;

import gizmoball.engine.Settings;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.CurvedPipe;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CurvedPipeCollisionFilter implements CollisionFilter {

    private final Vector2 gravity;

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

        if (!(shape2 instanceof CurvedPipe)) {
            return true;
        }
        CurvedPipe curvedPipe = (CurvedPipe) shape2;
        Ball ball = (Ball) shape1;

        Transform transform1 = curvedPipe.getTransform();
        Transform transform2 = ball.getTransform();

        Vector2 v0 = transform1.getTransformed(curvedPipe.getVertices()[0]);
        Vector2 v1 = transform1.getTransformed(curvedPipe.getVertices()[1]);
        Vector2 v2 = transform1.getTransformed(curvedPipe.getVertices()[2]);

        Vector2 ce1 = v1;
        Vector2 ce2 = new Vector2(transform2.getX(), transform2.getY());
        Vector2 r1 = v1.to(v0);
        Vector2 r2 = v1.to(v2);
        Vector2 c2c = ce1.to(ce2);
        // 圆形是否在扇形的边之中
        boolean isInSide = r1.cross(c2c) * c2c.cross(r2) >= 0 && r1.cross(c2c) * r1.cross(r2) >= 0;
        // 圆形是否在扇形之中
        boolean isInside = c2c.getMagnitude() < curvedPipe.getRadius();
        if (isInside && isInSide) {
            // 在内部就要施加反重力
            maintainPipeProperty(body1, body2, ce2, ce1);
            // 在内和弧线发生碰撞，需要反转法线并改变深度
            if (c2c.getMagnitude() + ball.getRadius() >= curvedPipe.getRadius()) {
                penetration.getNormal().negate();
                penetration.setDepth(ball.getRadius() * 2 - penetration.getDepth());
                return true;
            }
            // 在内但并没有发生碰撞
            return false;
        } else if (isInside) {
            // 严格判断是否从管道口进出
            if (penetration.getNormal().dot(r1.getNormalized()) < 1e5 * Epsilon.E ||
                    penetration.getNormal().dot(r2.getNormalized()) < 1e5 * Epsilon.E) {
                return false;
            }
            // 否则视为产生碰撞
            return true;
        } else {
            // 在外和弧线发生碰撞
            double magnitude0 = c2c.project(r1.getNormalized()).getMagnitude();
            double magnitude1 = c2c.project(r2.getNormalized()).getMagnitude();
            if (magnitude0 - Settings.PIPE_PIERCE_BIAS <= r1.getMagnitude() &&
                    magnitude1 - Settings.PIPE_PIERCE_BIAS <= r1.getMagnitude()) {
                return false;
            }
            return true;
        }
    }

    private void maintainPipeProperty(PhysicsBody body1, PhysicsBody body2, Vector2 c0, Vector2 c1) {
        body1.getForces().clear();
        body1.integrateVelocity(gravity.getNegative());
        Vector2 to = c0.to(c1);
        Vector2 linearVelocity = body1.getLinearVelocity();
        if (body1.getShape().getRate() == body2.getShape().getRate()) {
            if (to.cross(linearVelocity) > 0) {
                Vector2 multiply = to.right().getNormalized().multiply(linearVelocity.getMagnitude());
                linearVelocity.x = multiply.x;
                linearVelocity.y = multiply.y;
            } else {
                Vector2 multiply = to.left().getNormalized().multiply(linearVelocity.getMagnitude());
                linearVelocity.x = multiply.x;
                linearVelocity.y = multiply.y;
            }
        }
        if (linearVelocity.getMagnitude() < 90) {
            linearVelocity.multiply(90 / linearVelocity.getMagnitude());
        }
    }
}
