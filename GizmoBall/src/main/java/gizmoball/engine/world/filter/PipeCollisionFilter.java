package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Pipe;

public class PipeCollisionFilter implements CollisionFilter {
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
        if(!(shape2 instanceof Pipe)) {
            return true;
        }
        Ball ball = (Ball) shape1;
        Pipe pipe = (Pipe) shape2;

        //球是否在管道内部
        if(isInPipe(ball, pipe)){
            //判断是否与管壁发生了碰撞
            return isCollision(ball, pipe, penetration);
        }

        //球在管道外部
        //判断是否应该进入管道
        Vector2 normal = penetration.getNormal();
        double x = normal.x;
        double y = normal.y;
        if(!((Math.abs(x) > Math.abs(y) && pipe.getPipeDirection() == Pipe.PipeDirection.TRANSVERSE)
                || (Math.abs(x) <= Math.abs(y) && pipe.getPipeDirection() == Pipe.PipeDirection.VERTICAL))){
            return true;
        }

        return false;
    }

    public boolean isInPipe(Ball ball, Pipe pipe){
        double pipeX = pipe.getTransform().getX();
        double pipeY = pipe.getTransform().getY();
        double ballX = ball.getTransform().getX();
        double ballY = ball.getTransform().getY();

        double minX = pipeX - pipe.getHalfWidth();
        double maxX = pipeX + pipe.getHalfWidth();
        double minY = pipeY - pipe.getHalfHeight();
        double maxY = pipeY + pipe.getHalfHeight();


        if(ballX < minX || ballX > maxX){
            return false;
        }

        if(ballY < minY || ballY > maxY){
            return false;
        }

        return true;
    }

    public boolean isCollision(Ball ball, Pipe pipe, Penetration penetration){
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
                double depth = penetration.getDepth();
                Vector2 normal = penetration.getNormal();
                normal.y = -normal.y;
                normal.x = 0;
                normal.normalize();
                penetration.setNormal(normal);
                penetration.setDepth(2 * ball.getRadius() - depth);
                return true;
            }
            return false;
        }

        //竖直管道
        if(pipeDirection == Pipe.PipeDirection.VERTICAL){
            if(ballX + radius > maxX || ballX + radius < minX){
                double depth = penetration.getDepth();
                Vector2 normal = penetration.getNormal();
                normal.x = -normal.x;
                normal.y = 0;
                normal.normalize();
                penetration.setNormal(normal);
                penetration.setDepth(2 * ball.getRadius() - depth);
                return true;
            }
            return false;
        }
        return true;
    }
}
