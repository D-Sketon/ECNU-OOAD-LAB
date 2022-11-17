package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Pipe;

import static gizmoball.engine.Settings.PIPE_PIERCE_BIAS;

public class PipeCollisionFilter implements CollisionFilter {

    private final Vector2 gravity;

    private double ballX;
    private double ballY;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double radius;
    private Pipe.PipeDirection pipeDirection;

    private Vector2 linearVelocity;

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
        linearVelocity = body1.getLinearVelocity();
        //防止大圆进入小管道
        if (radius > pipe.getHalfHeight()) {
            return true;
        }
        //圆心在管道两侧，直接弹飞
        if (isOutPipe()) {
            return true;
        }
        //是否与管壁发生碰撞
        if (!isCollision()) {
            // 没有与管壁发生碰撞并且在管道内，需要反转重力
            if (isInPipe()) {
                maintainPipeProperty(body1, body2);
            }
            return false;
        }
        if (!isInPipe()) {
            if (penetration.getNormal().dot(pipe.getNormals()[0]) < 1e5 * Epsilon.E || penetration.getNormal().dot(pipe.getNormals()[1]) < 1e5 * Epsilon.E) {
                return false;
            }
            return true;
        }
        //与管壁发生碰撞
        maintainPipeProperty(body1, body2);
        solveCollision(ball, pipe, penetration);

        return true;
    }

    private boolean isInPipe() {
        return ballX > minX && ballX < maxX && ballY < maxY && ballY > minY;
    }

    private void solveCollision(Ball ball, Pipe pipe, Penetration penetration) {
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

    private boolean isOutPipe() {
        if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
            if (ballY > maxY || ballY < minY) {
                return true;
            }

            if (ballX < minX) {
                Vector2 v1 = new Vector2(minX - ballX, maxY - ballY);
                Vector2 v2 = new Vector2(minX - ballX, minY - ballY);
                if (v1.getMagnitude() + PIPE_PIERCE_BIAS < radius || v2.getMagnitude() + PIPE_PIERCE_BIAS < radius) {
                    return true;
                }
            } else if (ballX > maxX) {
                Vector2 v1 = new Vector2(maxX - ballX, maxY - ballY);
                Vector2 v2 = new Vector2(maxX - ballX, minY - ballY);
                if (v1.getMagnitude() + PIPE_PIERCE_BIAS < radius || v2.getMagnitude() + PIPE_PIERCE_BIAS < radius) {
                    return true;
                }
            }
        } else {
            if (ballX > maxX || ballX < minX) {
                return true;
            }

            if (ballY < minY) {
                Vector2 v1 = new Vector2(minX - ballX, minY - ballY);
                Vector2 v2 = new Vector2(maxX - ballX, minY - ballY);
                if (v1.getMagnitude() + PIPE_PIERCE_BIAS < radius || v2.getMagnitude() + PIPE_PIERCE_BIAS < radius) {
                    return true;
                }
            } else if (ballY > maxY) {
                Vector2 v1 = new Vector2(minX - ballX, maxY - ballY);
                Vector2 v2 = new Vector2(maxX - ballX, maxY - ballY);
                if (v1.getMagnitude() + PIPE_PIERCE_BIAS < radius || v2.getMagnitude() + PIPE_PIERCE_BIAS < radius) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCollision() {
        if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
            return ballY + radius > maxY || ballY - radius < minY;
        } else {
            return ballX + radius > maxX || ballX - radius < minX;
        }
    }

    private void maintainPipeProperty(PhysicsBody body1, PhysicsBody body2) {
        body1.getForces().clear();
        body1.integrateVelocity(gravity.getNegative());
        if (body1.getShape().getRate() == body2.getShape().getRate()) {
            if (pipeDirection == Pipe.PipeDirection.TRANSVERSE) {
                linearVelocity.y = 0;
            } else if (pipeDirection == Pipe.PipeDirection.VERTICAL) {
                linearVelocity.x = 0;
            }
        }
        if (linearVelocity.getMagnitude() < 30) {
            linearVelocity.multiply(30 / linearVelocity.getMagnitude());
        }
    }
}
