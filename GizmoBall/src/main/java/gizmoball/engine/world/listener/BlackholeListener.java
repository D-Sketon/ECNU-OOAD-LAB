package gizmoball.engine.world.listener;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.detector.CircleDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Blackhole;
import gizmoball.engine.world.filter.CollisionFilter;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BlackholeListener implements TickListener{

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> blackholes;

    /**
     * 重写碰撞检查类
     */
    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector(){
        @Override
        public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> filters) {
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds = new ArrayList<>();
            for(PhysicsBody body1 : bodies1){
                Ball ball = (Ball) body1.getShape();
                for(PhysicsBody body2 : bodies2){
                    Blackhole blackhole = (Blackhole) body2.getShape();
                    DetectorResult detect = CircleDetector.detect(ball, blackhole, null, null);
                    if(detect.isHasCollision()){
                        manifolds.add(new Pair<>(null, new Pair<>(body1, body2)));
                    }
                }
            }
            return manifolds;
        }
    };

    public BlackholeListener(List<PhysicsBody> balls, List<PhysicsBody> blackholes) {
        this.balls = balls;
        this.blackholes = blackholes;
    }

    /**
     * 黑洞和球碰撞
     *
     */
    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {

        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect = basicCollisionDetector.detect(balls, blackholes, null);

        for(Pair<Manifold, Pair<PhysicsBody, PhysicsBody>> pair : detect){
            PhysicsBody ball = pair.getValue().getKey();
            balls.remove(ball);
        }

        return new ArrayList<>();
    }


}
