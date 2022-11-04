package gizmoball.engine.world.listener;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.detector.CircleDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Blackhole;
import gizmoball.engine.world.filter.CollisionFilter;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BlackholeListener implements TickListener {

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> blackholes;

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
                    Blackhole blackhole = (Blackhole) body2.getShape();
                    gravityAccumulation(body1, body2);
                    DetectorResult detect = CircleDetector.detect(ball, blackhole, null, null);
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
            Blackhole blackhole = (Blackhole) body2.getShape();
            force.multiply(body1.getMass().getMass() * blackhole.getRadius() * 10000 / r / r);
            body1.getForces().add(force);

        }
    };

    public BlackholeListener(List<PhysicsBody> balls, List<PhysicsBody> blackholes) {
        this.balls = balls;
        this.blackholes = blackholes;
    }

    /**
     * 黑洞和球碰撞
     */
    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        for (PhysicsBody ball : balls) {
            ball.getForces().clear();
        }
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect = basicCollisionDetector.detect(balls, blackholes, null);

        for (Pair<Manifold, Pair<PhysicsBody, PhysicsBody>> pair : detect) {
            PhysicsBody ball = pair.getValue().getKey();
            balls.remove(ball);
        }

        return new ArrayList<>();
    }


}
