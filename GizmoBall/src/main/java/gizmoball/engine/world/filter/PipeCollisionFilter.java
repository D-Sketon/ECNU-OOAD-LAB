package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Pipe;

public class PipeCollisionFilter implements CollisionFilter {
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

        if(!(shape2 instanceof Pipe)) {
            return true;
        }
        Ball ball = (Ball) shape1;
        Pipe pipe = (Pipe) shape2;

        //圆心在管道两侧，直接弹飞
        if(isOutPipe(ball, pipe)){
            return true;
        }

        //是否与管壁发生碰撞
        if(!isCollision(ball, pipe)){
            return false;
        }

        //与管壁发生碰撞
        solveCollision(ball, pipe, penetration);


        return true;
    }

    public void solveCollision(Ball ball, Pipe pipe, Penetration penetration) {
        Vector2 normal = penetration.getNormal();
        if(pipe.getPipeDirection() == Pipe.PipeDirection.TRANSVERSE){
            if(normal.y > 0){
                double maxY = pipe.getTransform().y + pipe.getHalfHeight();
                //penetration.setDepth(ball.getRadius() - (maxY - ball.getTransform().y));
                penetration.setDepth(0);
            }
            else{
                double maxY = pipe.getTransform().y - pipe.getHalfHeight();
                //penetration.setDepth(ball.getRadius() - (ball.getTransform().y - maxY));
                penetration.setDepth(0);
            }
            normal.x = 0;
            normal.normalize();
            penetration.setNormal(normal.negate());
        }
        else{
            if(normal.x > 0){
                double maxX = pipe.getTransform().x + pipe.getHalfWidth();
                penetration.setDepth(ball.getRadius() - (maxX - ball.getTransform().x));
            }
            else{
                double maxX = pipe.getTransform().x - pipe.getHalfWidth();
                penetration.setDepth(ball.getRadius() - (ball.getTransform().x - maxX));
            }
            normal.y = 0;
            normal.normalize();
            penetration.setNormal(normal.negate());
        }
    }

    public boolean isOutPipe(Ball ball, Pipe pipe){
        double pipeX = pipe.getTransform().getX();
        double pipeY = pipe.getTransform().getY();
        double ballX = ball.getTransform().getX();
        double ballY = ball.getTransform().getY();

        if(pipe.getPipeDirection() == Pipe.PipeDirection.TRANSVERSE){
            return !((ballY < pipeY + pipe.getHalfHeight()) && (ballY > pipeY - pipe.getHalfHeight()));
        }
        else{
            return !((ballX < pipeX + pipe.getHalfWidth()) && (ballX > pipeX - pipe.getHalfWidth()));
        }
    }

    public boolean isCollision(Ball ball, Pipe pipe){
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
        if(pipeDirection == Pipe.PipeDirection.TRANSVERSE){
            if(ballY + radius > maxY || ballY - radius < minY){
                return true;
            }
            return false;
        }

        //竖直管道
        if(pipeDirection == Pipe.PipeDirection.VERTICAL){
            if(ballX + radius > maxX || ballX - radius < minX){
                return true;
            }
            return false;
        }
        return true;
    }
}
