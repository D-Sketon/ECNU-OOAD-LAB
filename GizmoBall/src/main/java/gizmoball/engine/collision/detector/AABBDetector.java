package gizmoball.engine.collision.detector;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.shape.AbstractShape;

public class AABBDetector {

    /**
     * <p>使用于broadPhase</p>
     * 判断两个{@link AbstractShape}的AABB是否发生碰撞
     * 
     * @param shape1 待测图形
     * @param shape2 待测图形
     * @return boolean
     */
    public static boolean detect(AbstractShape shape1, AbstractShape shape2) {
        AABB a = shape1.createAABB();
        AABB b = shape2.createAABB();
        return a.overlaps(b);
    }
}
