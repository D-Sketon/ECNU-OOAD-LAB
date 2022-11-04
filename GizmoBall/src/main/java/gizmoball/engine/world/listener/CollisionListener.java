package gizmoball.engine.world.listener;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.shape.AbstractShape;

public interface CollisionListener {

    boolean isAllowedBroadPhase(AbstractShape shape1, AbstractShape shape2);

    boolean isAllowedNarrowPhase(AbstractShape shape1, AbstractShape shape2);

    boolean isAllowedManifold(AbstractShape shape1, AbstractShape shape2, AbstractShape shape, Penetration penetration);

}
