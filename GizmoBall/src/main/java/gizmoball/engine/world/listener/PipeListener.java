package gizmoball.engine.world.listener;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.filter.CollisionFilter;
import gizmoball.engine.world.filter.CurvedPipeCollisionFilter;
import gizmoball.engine.world.filter.PipeCollisionFilter;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PipeListener implements TickListener {

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> pipes;

    private final List<CollisionFilter> filters;

    private final BasicCollisionDetector collisionDetector = new BasicCollisionDetector();

    public PipeListener(List<PhysicsBody> balls, List<PhysicsBody> pipes, Vector2 gravity) {
        this.balls = balls;
        this.pipes = pipes;
        this.filters = new ArrayList<>();
        filters.add(new PipeCollisionFilter(gravity));
        filters.add(new CurvedPipeCollisionFilter(gravity));
    }

    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        return collisionDetector.detect(balls, pipes, filters);
    }

}
