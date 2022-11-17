package gizmoball.game.listener;

import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.DetectorUtil;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import gizmoball.game.entity.BlackHole;
import gizmoball.engine.collision.CollisionFilter;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import javafx.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class BlackHoleListener implements TickListener {

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> blackHoles;

    private final List<GizmoPhysicsBody> allBodies;

    /**
     * 重写碰撞检查类
     */
    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector() {
        @Override
        public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> filters) {
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
            for (PhysicsBody body1 : bodies1) {
                Ball ball = (Ball) body1.getShape();
                for (PhysicsBody body2 : bodies2) {
                    BlackHole blackhole = (BlackHole) body2.getShape();
                    gravityAccumulation(body1, body2);
                    DetectorResult detect = DetectorUtil.circleDetect(ball, blackhole, null, null);
                    if (detect.isHasCollision()) {
                        manifolds.add(new Pair<>(null, new Pair<>(body1, body2)));
                    }
                }
            }
            return manifolds;
        }

        private void gravityAccumulation(PhysicsBody body1, PhysicsBody body2) {
            Vector2 bc1 = new Vector2(body1.getShape().getTransform().x, body1.getShape().getTransform().y);
            Vector2 bc2 = new Vector2(body2.getShape().getTransform().x, body2.getShape().getTransform().y);
            Vector2 force = bc1.to(bc2);
            double r = force.getMagnitude();
            force.normalize();
            BlackHole blackhole = (BlackHole) body2.getShape();
            force.multiply(body1.getMass().getMass() * blackhole.getRadius() * 10000 / r / r);
            body1.getForces().add(force);

        }
    };

    /**
     * 黑洞和球碰撞
     */
    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        for (PhysicsBody ball : balls) {
            ball.getForces().clear();
        }
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect = basicCollisionDetector.detect(balls, blackHoles, null);

        for (Pair<Manifold, Pair<PhysicsBody, PhysicsBody>> pair : detect) {
            PhysicsBody ball = pair.getValue().getKey();
            balls.remove(ball);
            allBodies.remove(ball);
        }

        return new ArrayList<>();
    }


}
