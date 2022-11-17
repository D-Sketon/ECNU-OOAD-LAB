package gizmoball.game.listener;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.DetectorUtil;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldSolver;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import gizmoball.engine.collision.CollisionFilter;
import javafx.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class BallListener implements TickListener {

    private final List<PhysicsBody> balls;

    /**
     * 重写碰撞检查类
     */
    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector() {
        @Override
        public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> listeners) {
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
            ManifoldSolver manifoldSolver = new ManifoldSolver();
            for (int i = 0; i < bodies1.size() - 1; i++) {
                for (int j = i + 1; j < bodies1.size(); j++) {
                    PhysicsBody physicsBody1 = bodies1.get(i);
                    PhysicsBody physicsBody2 = bodies1.get(j);
                    Ball ball1 = (Ball) physicsBody1.getShape();
                    Ball ball2 = (Ball) physicsBody2.getShape();
                    Manifold manifold = this.processDetect(manifoldSolver, ball1, ball2);
                    if (manifold != null) {
                        Pair<PhysicsBody, PhysicsBody> physicsBodyPhysicsBodyPair = new Pair<>(physicsBody1, physicsBody2);
                        manifolds.add(new Pair<>(manifold, physicsBodyPhysicsBodyPair));
                    }
                }
            }
            return manifolds;
        }

        private Manifold processDetect(ManifoldSolver manifoldSolver, Ball ball1, Ball ball2) {
            if (!DetectorUtil.AABBDetect(ball1, ball2)) {
                return null;
            }

            Penetration penetration = new Penetration();
            DetectorResult detect = DetectorUtil.circleDetect(ball1, ball2, null, penetration);
            if (!detect.isHasCollision()) {
                return null;
            }
            Manifold manifold = new Manifold();
            if (!manifoldSolver.getManifold(penetration, ball1, ball2, detect.getApproximateShape(), manifold)) {
                return null;
            }
            return manifold;
        }
    };

    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        return basicCollisionDetector.detect(balls, null, null);
    }
}
