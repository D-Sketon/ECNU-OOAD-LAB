package gizmoball.engine.collision;

import gizmoball.engine.physics.PhysicsBody;

import java.util.ArrayList;
import java.util.List;

public class BasicCollisionDetector implements CollisionDetector{

    @Override
    public List<CollisionData> detectCollision(List<PhysicsBody> bodies){
        List<CollisionData> collisions = new ArrayList<>();
        // Maybe TODO with quad-tree
        for(int i = 0; i < bodies.size(); i++){
            for(int j = i + 1; j < bodies.size(); j++){
                PhysicsBody body1 = bodies.get(i);
                PhysicsBody body2 = bodies.get(j);
            }
        }
        return collisions;
    }

}
