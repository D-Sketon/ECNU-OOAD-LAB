package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Pipe;

public class PipeCollisionFilter implements CollisionFilter {

    private Vector2 gravity;

    private double ballX;
    private double ballY;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double radius;
    private Pipe.PipeDirection pipeDirection;

    public PipeCollisionFilter(Vector2 gravity) {
        this.gravity = gravity;
    }

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

        if (!(shape2 instanceof Pipe)) {
            return true;
        }
        Ball ball = (Ball) shape1;
        Pipe pipe = (Pipe) shape2;

        double pipeX = pipe.getTransform().getX();
        double pipeY = pipe.getTransform().getY();
        ballX = ball.getTransform().getX();
        ballY = ball.getTransform().getY();
        radius = ball.getRadius();
        pipeDirection = pipe.getPipeDirection();

        minX = pipeX - pipe.getHalfWidth();
        maxX = pipeX + pipe.getHalfWidth();
        minY = pipeY - pipe.getHalfHeight();
        maxY = pipeY + pipe.getHalfHeight();

        //圆心在管道两侧，直接弹飞
        if (isOutPipe()) {
            return true;
        }
        //是否与管壁发生碰撞
        if (!isCollision()) {
            body1.integrateVelocity(gravity.getNegative());
            return false;
        }
        //防止大圆进入小管道
        Vector2[] vertices = pipe.getVertices();
        for (Vector2 vertex : vertices) {
            Vector2 transformed = pipe.getTransform().getTransformed(vertex);
            if (ball.getRadius() - transformed.to(new Vector2(ball.getTransform().x, ball.getTransform().y)).getMagnitude() > 1e10 * Epsilon.E) {
                penetration.setDepth(penetration.getDepth() / 10);
                return true;
            }
        }
        if (!isInPipe()) {
            if (penetration.getNormal().dot(pipe.getNormals()[0]) < 1e5 * Epsilon.E || penetration.getNormal().dot(pipe.getNormals()[1]) < 1e5 * Epsilon.E) {
                return false;
            }
            return true;
        }
        //与管壁发生碰撞
        body1.integrateVelocity(gravity.getNegative());
        solveCollision(ball, pipe, penetration);

        return true;
    }

    private boolean isInPipe() {
        return ballX > minX && ballX < maxX && ballY < maxY && ballY > minY;
    }

    public void solveCollision(Ball ball, Pipe pipe, Penetration penetration) {
        Vector2 normal = penetration.getNormal();
        if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
            boolean isHigh = ball.getTransform().y > pipe.getTransform().y;
            if (isHigh) {
                penetration.setDepth(radius - (maxY - ball.getTransform().y));
                normal.x = 0;
                normal.y = 1;
            } else {
                penetration.setDepth(radius - (ball.getTransform().y - minY));
                normal.x = 0;
                normal.y = -1;
            }
        } else {
            boolean isRight = ball.getTransform().x > pipe.getTransform().x;
            if (isRight) {
                penetration.setDepth(radius - (maxX - ball.getTransform().x));
                normal.x = 1;
                normal.y = 0;
            } else {
                penetration.setDepth(radius - (ball.getTransform().x - minX));
                normal.x = -1;
                normal.y = 0;
            }
        }
    }

    public boolean isOutPipe() {
        if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
            return !((ballY < maxY) && (ballY > minY));
        } else {
            return !((ballX < maxX) && (ballX > minX));
        }
    }

    public boolean isCollision() {
        if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
            return ballY + radius > maxY || ballY - radius < minY;
        } else {
            return ballX + radius > maxX || ballX - radius < minX;
        }
    }
}
