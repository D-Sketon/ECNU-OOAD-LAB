package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;

public interface CollisionFilter {

    boolean isAllowedBroadPhase(PhysicsBody body1, PhysicsBody body2);

    boolean isAllowedNarrowPhase(PhysicsBody body1, PhysicsBody body2);

    boolean isAllowedManifold(PhysicsBody body1, PhysicsBody body2, AbstractShape shape, Penetration penetration);

}
