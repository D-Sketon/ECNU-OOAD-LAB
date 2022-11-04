package gizmoball.engine.collision;

import gizmoball.engine.Settings;
import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.detector.AABBDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.SatDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldSolver;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.listener.CollisionListener;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BasicCollisionDetector implements CollisionDetector {


    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies, List<CollisionListener> listeners) {
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
        ManifoldSolver manifoldSolver = new ManifoldSolver();
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                PhysicsBody body1 = bodies.get(i);
                PhysicsBody body2 = bodies.get(j);
                AbstractShape shape1 = body1.getShape();
                AbstractShape shape2 = body2.getShape();
                Manifold manifold = this.processDetect(manifoldSolver, shape1, shape2, listeners);
                if (manifold != null) {
                    Pair<PhysicsBody, PhysicsBody> physicsBodyPhysicsBodyPair = new Pair<>(body1, body2);
                    manifolds.add(new Pair<>(manifold, physicsBodyPhysicsBodyPair));
                }
            }
        }
        return manifolds;
    }

    private Manifold processDetect(ManifoldSolver manifoldSolver, AbstractShape shape1, AbstractShape shape2, List<CollisionListener> listeners) {
        for (CollisionListener listener : listeners) {
            if (!listener.isAllowedBroadPhase(shape1, shape2)) return null;
        }
        if (!AABBDetector.detect(shape1, shape2)) {
            return null;
        }

        for (CollisionListener listener : listeners) {
            if (!listener.isAllowedNarrowPhase(shape1, shape2)) return null;
        }
        Penetration penetration = new Penetration();
        DetectorResult detect = SatDetector.detect(shape1, shape2, null, penetration);
        if (!detect.isHasCollision()) {
            return null;
        }

        for (CollisionListener listener : listeners) {
            if (!listener.isAllowedManifold(shape1, shape2, detect.getApproximateShape(), penetration)) return null;
        }
        Manifold manifold = new Manifold();
        if (!manifoldSolver.getManifold(penetration, shape1, shape2, detect.getApproximateShape(), manifold)) {
            return null;
        }
        return manifold;
    }

    @Override
    public List<ContactConstraint> preLocalSolve(List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds) {
        List<ContactConstraint> contactConstraints = new ArrayList<>();
        for (Pair<Manifold, Pair<PhysicsBody, PhysicsBody>> manifold : manifolds) {
            ContactConstraint contactConstraint = new ContactConstraint(manifold.getValue());
            contactConstraint.update(manifold.getKey());
            contactConstraints.add(contactConstraint);
        }
        return contactConstraints;
    }

    @Override
    public void LocalSolve(SequentialImpulses solver, Vector2 gravity, List<ContactConstraint> constraints, List<PhysicsBody> bodies) {
        for (PhysicsBody body : bodies) {
            body.integrateVelocity(gravity);
        }
        solver.initialize(constraints);
        for (int i = 0; i < Settings.DEFAULT_SOLVER_ITERATIONS; i++) {
            solver.solveVelocityConstraints(constraints);
        }

        for (PhysicsBody body : bodies) {
            body.integratePosition();
        }
        for (int i = 0; i < Settings.DEFAULT_SOLVER_ITERATIONS; i++) {
            solver.solvePositionConstraints(constraints);
        }
    }
}
