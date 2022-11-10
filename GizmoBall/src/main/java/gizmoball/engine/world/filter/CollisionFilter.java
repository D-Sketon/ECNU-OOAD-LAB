package gizmoball.engine.world.filter;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;

public interface CollisionFilter {

    /**
     * 是否允许BroadPhase
     *
     * @param body1 物体1
     * @param body2 物体2
     * @return boolean
     */
    boolean isAllowedBroadPhase(PhysicsBody body1, PhysicsBody body2);

    /**
     * 是否允许NarrowPhase
     *
     * @param body1 物体1
     * @param body2 物体2
     * @return boolean
     */
    boolean isAllowedNarrowPhase(PhysicsBody body1, PhysicsBody body2);

    /**
     * 是否允许计算接触流形
     *
     * @param body1       物体1
     * @param body2       物体2
     * @param shape       近似图形
     * @param penetration 穿透信息
     * @return boolean
     */
    boolean isAllowedManifold(PhysicsBody body1, PhysicsBody body2, AbstractShape shape, Penetration penetration);

}
