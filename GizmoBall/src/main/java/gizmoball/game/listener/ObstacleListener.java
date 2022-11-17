package gizmoball.game.listener;

import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ObstacleListener implements TickListener {

    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector();

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> obstacles;


    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        return basicCollisionDetector.detect(balls, obstacles, new ArrayList<>());
    }
}
