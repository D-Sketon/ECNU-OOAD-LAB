package gizmoball.engine.collision.manifold;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.feature.EdgeFeature;
import gizmoball.engine.collision.feature.Feature;
import gizmoball.engine.collision.feature.PointFeature;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.QuarterCircle;

import java.util.ArrayList;
import java.util.List;

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
        Feature feature1 = shape1.getFarthestFeature(n);

        if (feature1 instanceof PointFeature) {
            PointFeature vertex = (PointFeature) feature1;
            ManifoldPoint mp = new ManifoldPoint(vertex.getPoint(), penetration.getDepth());
            manifold.getPoints().add(mp);
            manifold.getNormal().x = -n.x;
            manifold.getNormal().y = -n.y;
            return true;
        }

        Vector2 ne = n.getNegative();
        Feature feature2 = shape2.getFarthestFeature(ne);
        if (feature2 instanceof PointFeature) {
            PointFeature vertex = (PointFeature) feature2;
            ManifoldPoint mp = new ManifoldPoint(vertex.getPoint(), penetration.getDepth());
            manifold.getPoints().add(mp);
            manifold.getNormal().x = ne.x;
            manifold.getNormal().y = ne.y;
            return true;
        }
        // 如果两个Feature都不是PointFeature
        // 参考边
        EdgeFeature reference = (EdgeFeature) feature1;
        // 入射边
        EdgeFeature incident = (EdgeFeature) feature2;

        boolean flipped = false;
        Vector2 e1 = reference.getEdge();
        Vector2 e2 = incident.getEdge();

        // 保证参考边是最垂直于分离法线的边
        // 参考边将用于剪裁入射边顶点以生成接触流形
        if (Math.abs(e1.dot(n)) > Math.abs(e2.dot(n))) {
            EdgeFeature e = reference;
            reference = incident;
            incident = e;
            flipped = true;
        }

        Vector2 refev = reference.getEdge();
        refev.normalize();
        // 第一次剪裁
        double offset1 = -refev.dot(reference.getVertex1().getPoint());
        List<PointFeature> clip1 = this.clip(incident.getVertex1(), incident.getVertex2(), refev.getNegative(), offset1);
        if (clip1.size() < 2) {
            return false;
        }
        // 第二次剪裁
        double offset2 = refev.dot(reference.getVertex2().getPoint());
        List<PointFeature> clip2 = this.clip(clip1.get(0), clip1.get(1), refev, offset2);
        if (clip2.size() < 2) {
            return false;
        }

        Vector2 frontNormal = refev.getRightHandOrthogonalVector();
        double frontOffset = frontNormal.dot(reference.getMax().getPoint());
        manifold.getNormal().x = flipped ? -frontNormal.x : frontNormal.x;
        manifold.getNormal().y = flipped ? -frontNormal.y : frontNormal.y;

        // 第三次剪裁
        for (PointFeature vertex : clip2) {
            Vector2 point = vertex.getPoint();
            double depth = frontNormal.dot(point) - frontOffset;
            if (depth >= 0.0) {
                ManifoldPoint mp = new ManifoldPoint(point, depth);
                manifold.getPoints().add(mp);
            }
        }
        return manifold.getPoints().size() != 0;
    }

    /**
     * 剪裁入射边
     *
     * @param v1     特征点
     * @param v2     特征点
     * @param n      参考边向量
     * @param offset 剪裁偏移
     * @return List
     */
    protected List<PointFeature> clip(PointFeature v1, PointFeature v2, Vector2 n, double offset) {
        List<PointFeature> points = new ArrayList<>(2);
        Vector2 p1 = v1.getPoint();
        Vector2 p2 = v2.getPoint();

        double d1 = n.dot(p1) - offset;
        double d2 = n.dot(p2) - offset;

        if (d1 <= 0.0) points.add(v1);
        if (d2 <= 0.0) points.add(v2);

        if (d1 * d2 < 0.0) {
            Vector2 e = p1.to(p2);
            double u = d1 / (d1 - d2);
            e.multiply(u);
            e.add(p1);
            points.add(new PointFeature(e));
        }
        return points;
    }
}
