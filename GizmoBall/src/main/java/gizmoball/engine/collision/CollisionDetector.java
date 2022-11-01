package gizmoball.engine.collision;

import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;

import java.util.List;

public interface CollisionDetector {

    List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> narrowPhase(List<PhysicsBody> bodies);

    List<ContactConstraint> preLocalSolve(List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds);

    void LocalSolve(SequentialImpulses solver, Vector2 gravity, List<ContactConstraint> constraints, List<PhysicsBody> bodies);

}
