package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Pipe;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PipeCollisionFilter implements CollisionFilter {

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

        if (!(shape2 instanceof Pipe)) {
            return true;
        }
        Ball ball = (Ball) shape1;
        Pipe pipe = (Pipe) shape2;

        //圆心在管道两侧，直接弹飞
        if (isOutPipe(ball, pipe)) {
            return true;
        }

        //是否与管壁发生碰撞
        if (!isCollision(ball, pipe)) {
            return false;
        }
        //防止大圆进入小管道
        Vector2[] vertices = pipe.getVertices();
        for (Vector2 vertex : vertices) {
            Vector2 transformed = pipe.getTransform().getTransformed(vertex);
            if (ball.getRadius() - transformed.to(new Vector2(ball.getTransform().x, ball.getTransform().y)).getMagnitude() > Epsilon.E) {
                penetration.setDepth(penetration.getDepth()/10);
                return true;
            }
        }
        if (!isInPipe(ball, pipe)) {
            if (penetration.getNormal().dot(pipe.getNormals()[0]) < 1e5 * Epsilon.E || penetration.getNormal().dot(pipe.getNormals()[1]) < 1e5 * Epsilon.E) {
                return false;
            }
            return true;
        }
        //与管壁发生碰撞
        solveCollision(ball, pipe, penetration);

        return true;
    }

    private boolean isInPipe(Ball ball, Pipe pipe) {
        double pipeX = pipe.getTransform().getX();
        double pipeY = pipe.getTransform().getY();
        double ballX = ball.getTransform().getX();
        double ballY = ball.getTransform().getY();

        double minX = pipeX - pipe.getHalfWidth();
        double maxX = pipeX + pipe.getHalfWidth();
        double minY = pipeY - pipe.getHalfHeight();
        double maxY = pipeY + pipe.getHalfHeight();
        return ballX > minX && ballX < maxX && ballY < maxY && ballY > minY;
    }

    public void solveCollision(Ball ball, Pipe pipe, Penetration penetration) {
        Vector2 normal = penetration.getNormal();
        if (pipe.getPipeDirection() == Pipe.PipeDirection.TRANSVERSE) {
            boolean isHigh = ball.getTransform().y > pipe.getTransform().y;
            if (isHigh) {
                double maxY = pipe.getTransform().y + pipe.getHalfHeight();
                penetration.setDepth(ball.getRadius() - (maxY - ball.getTransform().y));
                normal.x = 0;
                normal.y = 1;
            } else {
                double minY = pipe.getTransform().y - pipe.getHalfHeight();
                penetration.setDepth(ball.getRadius() - (ball.getTransform().y - minY));
                normal.x = 0;
                normal.y = -1;
            }
        } else {
            boolean isRight = ball.getTransform().x > pipe.getTransform().x;
            if (isRight) {
                double maxX = pipe.getTransform().x + pipe.getHalfWidth();
                penetration.setDepth(ball.getRadius() - (maxX - ball.getTransform().x));
                normal.x = 1;
                normal.y = 0;
            } else {
                double minX = pipe.getTransform().x - pipe.getHalfWidth();
                penetration.setDepth(ball.getRadius() - (ball.getTransform().x - minX));
                normal.x = -1;
                normal.y = 0;
            }
        }
    }

    public boolean isOutPipe(Ball ball, Pipe pipe) {
        double pipeX = pipe.getTransform().getX();
        double pipeY = pipe.getTransform().getY();
        double ballX = ball.getTransform().getX();
        double ballY = ball.getTransform().getY();

        if (pipe.getPipeDirection() == Pipe.PipeDirection.TRANSVERSE) {
            return !((ballY < pipeY + pipe.getHalfHeight()) && (ballY > pipeY - pipe.getHalfHeight()));
        } else {
            return !((ballX < pipeX + pipe.getHalfWidth()) && (ballX > pipeX - pipe.getHalfWidth()));
        }
    }

    public boolean isCollision(Ball ball, Pipe pipe) {
        double pipeX = pipe.getTransform().getX();
        double pipeY = pipe.getTransform().getY();
        double ballX = ball.getTransform().getX();
        double ballY = ball.getTransform().getY();

        double minX = pipeX - pipe.getHalfWidth();
        double maxX = pipeX + pipe.getHalfWidth();
        double minY = pipeY - pipe.getHalfHeight();
        double maxY = pipeY + pipe.getHalfHeight();
        double radius = ball.getRadius();
        Pipe.PipeDirection pipeDirection = pipe.getPipeDirection();

        //横向管道
        if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
            if (ballY + radius > maxY || ballY - radius < minY) {
                return true;
            }
            return false;
        }

        //竖直管道
        if (pipeDirection == Pipe.PipeDirection.VERTICAL) {
            if (ballX + radius > maxX || ballX - radius < minX) {
                return true;
            }
            return false;
        }
        return true;
    }
}
