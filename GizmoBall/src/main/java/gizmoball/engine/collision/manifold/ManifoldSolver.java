package gizmoball.engine.collision.manifold;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.QuarterCircle;

/**
 * 接触流形求解器，将{@link Penetration}解析为{@link Manifold}
 */
public class ManifoldSolver {

    /**
     * 基于{@link Penetration}计算出碰撞流形
     * <p>
     * 分为两个步骤：查找特征和剪裁
     * </p>
     *
     * @param penetration 穿透信息
     * @param shape1      被碰撞图形
     * @param shape2      碰撞图形
     * @param shape       近似图形
     * @param manifold    碰撞点信息
     * @return boolean
     */
    public boolean getManifold(Penetration penetration, AbstractShape shape1, AbstractShape shape2, AbstractShape shape, Manifold manifold) {
        if (shape != null) {
            if (shape1 instanceof QuarterCircle) {
                shape1 = shape;
            } else if (shape2 instanceof QuarterCircle) {
                shape2 = shape;
            }
        }
        // 获得分离法线的单位向量（理论上已被规范化）
        Vector2 n = penetration.getNormal();
        Vector2 vertex = shape1.getFarthestFeature(n);

        if (vertex != null) {
            ManifoldPoint mp = new ManifoldPoint(vertex, penetration.getDepth());
            manifold.getPoints().add(mp);
            manifold.getNormal().x = -n.x;
            manifold.getNormal().y = -n.y;
            return true;
        }

        Vector2 ne = n.getNegative();
        Vector2 vertex2 = shape2.getFarthestFeature(ne);
        if (vertex2 != null) {
            ManifoldPoint mp = new ManifoldPoint(vertex2, penetration.getDepth());
            manifold.getPoints().add(mp);
            manifold.getNormal().x = ne.x;
            manifold.getNormal().y = ne.y;
            return true;
        }
        // 不考虑多边形之间的碰撞
        return false;
    }

}
