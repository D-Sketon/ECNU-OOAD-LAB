package gizmoball.engine.collision;

import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;
import lombok.Data;

/**
 * 一次碰撞的信息，应该包含碰撞的两个对象
 * (碰撞点，法线，是否穿透？)
 */
@Data
public class CollisionData {

    private Pair<PhysicsBody, PhysicsBody> pair;
}
