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
//         Pipe pipe;
//         Circle circle;
//
//         if (shape1 instanceof Pipe && shape2 instanceof Circle) {
//             pipe = (Pipe) shape1;
//             circle = (Circle) shape2;
//         } else if (shape2 instanceof Pipe && shape1 instanceof Circle) {
//             pipe = (Pipe) shape2;
//             circle = (Circle) shape1;
//         } else {
//             return true;
//         }
//        Ball ball = (Ball) shape1;
//        Pipe pipe = (Pipe) shape2;
//
//        Vector2 normal = penetration.getNormal();
//
//        double x = normal.x;
//        double y = normal.y;
//
//        //判断球弹入方向
//        return (!(Math.abs(x) > Math.abs(y)) || pipe.getPipeDirection() != Pipe.PipeDirection.TRANSVERSE)
//                && (!(Math.abs(x) <= Math.abs(y)) || pipe.getPipeDirection() != Pipe.PipeDirection.VERTICAL);
        return true;
    }
}
