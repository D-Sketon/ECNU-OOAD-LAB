package gizmoball.engine.collision;

import gizmoball.engine.physics.PhysicsBody;

import java.util.List;

public interface CollisionDetector {

    List<CollisionData> detectCollision(List<PhysicsBody> bodies);
}
