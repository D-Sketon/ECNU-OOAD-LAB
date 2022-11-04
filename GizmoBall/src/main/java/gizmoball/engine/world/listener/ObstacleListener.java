package gizmoball.engine.world.listener;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.detector.AABBDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.SatDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldSolver;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.filter.CollisionFilter;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ObstacleListener implements TickListener {

    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector();

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> obstacles;


    public ObstacleListener(List<PhysicsBody> balls, List<PhysicsBody> obstacles) {
        this.balls = balls;
        this.obstacles = obstacles;
    }


    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        return basicCollisionDetector.detect(balls, obstacles, new ArrayList<>());
    }
}
