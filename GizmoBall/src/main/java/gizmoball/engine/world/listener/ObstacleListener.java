package gizmoball.engine.world.listener;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.detector.AABBDetector;
import gizmoball.engine.collision.detector.DetectorResult;
import gizmoball.engine.collision.detector.SatDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldSolver;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.filter.CollisionFilter;
import javafx.util.Pair;

import java.util.List;

public class ObstacleListener implements TickListener{
    
    private BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector(){

        private Manifold processDetect(ManifoldSolver manifoldSolver, AbstractShape shape1, AbstractShape shape2, List<CollisionFilter> filters) {
            if (!AABBDetector.detect(shape1, shape2)) {
                return null;
            }
            
            Penetration penetration = new Penetration();
            DetectorResult detect = SatDetector.detect(shape1, shape2, null, penetration);
            if (!detect.isHasCollision()) {
                return null;
            }
            
            Manifold manifold = new Manifold();
            if (!manifoldSolver.getManifold(penetration, shape1, shape2, detect.getApproximateShape(), manifold)) {
                return null;
            }
            return manifold;
        }
    };
    
    private List<PhysicsBody> balls;
    
    private List<PhysicsBody> obstacles;
    

    public ObstacleListener(List<PhysicsBody> balls, List<PhysicsBody> obstacles) {
        this.balls = balls;
        this.obstacles = obstacles;
    }


    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        return basicCollisionDetector.detect(balls, obstacles, null);
    }
}
