package gizmoball.engine.collision.detector;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.collision.Penetration;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.*;

public class DetectorUtil {

    /**
     * <p>使用于broadPhase</p>
     * 判断两个{@link AbstractShape}的AABB是否发生碰撞
     *
     * @param shape1 待测图形
     * @param shape2 待测图形
     * @return boolean
     */
    public static boolean AABBDetect(AbstractShape shape1, AbstractShape shape2) {
        AABB a = shape1.createAABB();
        AABB b = shape2.createAABB();
        return a.overlaps(b);
    }

    /**
     * <p>使用于narrowPhase</p>
     * 判断两个{@link Circle}是否发生碰撞
     *
     * @param circle1     待测圆
     * @param circle2     待测圆
     * @param shape       近似图形
     * @param penetration 穿透信息
     * @return DetectorResult
     */
    public static DetectorResult circleDetect(Circle circle1, Circle circle2, AbstractShape shape, Penetration penetration) {
        Transform transform1 = circle1.getTransform();
        Transform transform2 = circle2.getTransform();
        // 构造圆心坐标
        Vector2 ce1 = new Vector2(transform1.getX(), transform1.getY());
        Vector2 ce2 = new Vector2(transform2.getX(), transform2.getY());
        Vector2 v = ce1.to(ce2);
        double radii = circle1.getRadius() + circle2.getRadius();
        double mag = v.getMagnitudeSquared();
        // 发生碰撞
        if (mag < radii * radii) {
            if (penetration != null) {
                penetration.setDepth(radii - v.normalize());
                penetration.getNormal().x = v.x;
                penetration.getNormal().y = v.y;
            }
            return new DetectorResult(true, shape);
        }
        return new DetectorResult(false, shape);
    }

    /**
     * <p>使用于narrowPhase</p>
     * 判断{@link QuarterCircle}和{@link Circle}是否发生碰撞
     *
     * @param quarterCircle 扇形
     * @param circle        圆形
     * @param penetration   穿透信息
     * @param isFlipped     参数是否发生翻转
     * @return DetectorResult
     */
    public static DetectorResult quarterCircleDetect(QuarterCircle quarterCircle, Circle circle, Penetration penetration, boolean isFlipped) {
        Transform transform1 = quarterCircle.getTransform();
        Transform transform2 = circle.getTransform();

        Vector2 v0 = transform1.getTransformed(quarterCircle.getVertices()[0]);
        Vector2 v1 = transform1.getTransformed(quarterCircle.getVertices()[1]);
        Vector2 v2 = transform1.getTransformed(quarterCircle.getVertices()[2]);

        Vector2 ce1 = v1;
        Vector2 ce2 = new Vector2(transform2.getX(), transform2.getY());
        Vector2 r1 = v1.to(v0);
        Vector2 r2 = v1.to(v2);
        Vector2 c2c = ce1.to(ce2);
        // 圆形在扇形的边之中，将扇形近似为圆形
        if (r1.cross(c2c) * c2c.cross(r2) >= 0 && r1.cross(c2c) * r1.cross(r2) >= 0) {
            Circle circle1 = new Circle(quarterCircle.getRadius(),
                    new Transform(transform1.getCost(), transform1.getSint(), ce1.x, ce1.y));
            if (isFlipped) {
                return circleDetect(circle, circle1, circle1, penetration);
            }
            return circleDetect(circle1, circle, circle1, penetration);
        } else {
            // 将扇形近似为多边形
            Rectangle rectangle = new Rectangle(
                    quarterCircle.getRadius() / 2,
                    quarterCircle.getRadius() / 2,
                    transform1.copy());
            if (isFlipped) {
                return satDetect(circle, rectangle, rectangle, penetration);
            }
            return satDetect(rectangle, circle, rectangle, penetration);
        }
    }

    /**
     * <p>使用于narrowPhase</p>
     * 使用SAT算法判断两个{@link AbstractShape}是否发生碰撞
     *
     * @param shape1      待测图形
     * @param shape2      待测图形
     * @param shape       近似图形
     * @param penetration 穿透信息
     * @return DetectorResult
     */
    public static DetectorResult satDetect(AbstractShape shape1, AbstractShape shape2, AbstractShape shape, Penetration penetration) {
        if (shape1 instanceof QuarterCircle && shape2 instanceof QuarterCircle) {
            // 不考虑扇形和扇形的碰撞
            return new DetectorResult(false, null);
        }
        if (shape1 instanceof Circle && shape2 instanceof Circle) {
            // 圆形和圆形碰撞
            return circleDetect((Circle) shape1, (Circle) shape2, shape, penetration);
        } else if (shape1 instanceof Circle && shape2 instanceof QuarterCircle) {
            // 圆形和扇形碰撞
            return quarterCircleDetect((QuarterCircle) shape2, (Circle) shape1, penetration, true);
        } else if (shape2 instanceof Circle && shape1 instanceof QuarterCircle) {
            // 扇形和圆形碰撞
            return quarterCircleDetect((QuarterCircle) shape1, (Circle) shape2, penetration, false);
        } else if (shape1 instanceof QuarterCircle) {
            // 扇形和多边形碰撞
            QuarterCircle shape11 = (QuarterCircle) shape1;
            Vector2[] vertices = shape11.getVertices();
            Polygon polygon = new Polygon(shape11.getTransform().copy(),
                    new Vector2[]{vertices[0],
                            vertices[1],
                            vertices[2],
                            new Vector2(shape11.getRadius() / Math.sqrt(2), shape11.getRadius() / Math.sqrt(2))});
            return satDetect(polygon, shape2, polygon, penetration);
        } else if (shape2 instanceof QuarterCircle) {
            // 多边形和扇形碰撞
            QuarterCircle shape21 = (QuarterCircle) shape2;
            Vector2[] vertices = shape21.getVertices();
            Polygon polygon = new Polygon(shape21.getTransform().copy(),
                    new Vector2[]{vertices[0],
                            vertices[1],
                            vertices[2],
                            new Vector2(shape21.getRadius() / Math.sqrt(2), shape21.getRadius() / Math.sqrt(2))});
            return satDetect(shape1, polygon, polygon, penetration);
        }
        // 多边形（圆）和多边形（圆）碰撞
        Vector2[] foci1 = shape1.getFoci();
        Vector2[] foci2 = shape2.getFoci();

        Vector2[] axes1 = shape1.getAxes(foci2);
        Vector2[] axes2 = shape2.getAxes(foci1);

        Vector2 currentAxis = null;
        double minOverlap = Double.MAX_VALUE;

        if (axes1 != null) {
            for (Vector2 axis : axes1) {
                if (axis.isZero()) continue;
                // 投影获得分隔
                Interval intervalA = shape1.project(axis);
                Interval intervalB = shape2.project(axis);
                if (!intervalA.overlaps(intervalB)) {
                    return new DetectorResult(false, shape);
                } else {
                    double overlap = intervalA.getOverlap(intervalB);
                    // 如果分隔存在包含关系
                    if (intervalA.containsExclusive(intervalB) || intervalB.containsExclusive(intervalA)) {
                        double max = Math.abs(intervalA.getMax() - intervalB.getMax());
                        double min = Math.abs(intervalA.getMin() - intervalB.getMin());
                        // 穿透深度为被包含图形的投影长度加上离外层图形的最近投影长度和
                        if (max > min) {
                            // max位于axis的正方向，如果max>min意味着向着负方向移动能更快的分离图形
                            // 所以为了保持分离向量的正方向，需要反转向量
                            axis.negate();
                            overlap += min;
                        } else {
                            overlap += max;
                        }
                    }
                    if (overlap < minOverlap) {
                        minOverlap = overlap;
                        currentAxis = axis;
                    }
                }
            }
        }
        // 同上代码
        if (axes2 != null) {
            for (Vector2 axis : axes2) {
                if (axis.isZero()) continue;
                Interval intervalA = shape1.project(axis);
                Interval intervalB = shape2.project(axis);
                if (!intervalA.overlaps(intervalB)) {
                    return new DetectorResult(false, shape);
                } else {
                    double overlap = intervalA.getOverlap(intervalB);
                    if (intervalA.containsExclusive(intervalB) || intervalB.containsExclusive(intervalA)) {
                        double max = Math.abs(intervalA.getMax() - intervalB.getMax());
                        double min = Math.abs(intervalA.getMin() - intervalB.getMin());
                        if (max > min) {
                            axis.negate();
                            overlap += min;
                        } else {
                            overlap += max;
                        }
                    }
                    if (overlap < minOverlap) {
                        minOverlap = overlap;
                        currentAxis = axis;
                    }
                }
            }
        }

        Vector2 center1 = new Vector2(shape1.getTransform().getX(), shape1.getTransform().getY());
        Vector2 center2 = new Vector2(shape2.getTransform().getX(), shape2.getTransform().getY());
        Vector2 cToc = center1.to(center2);
        if (cToc.dot(currentAxis) < 0) {
            currentAxis.negate();
        }
        penetration.getNormal().x = currentAxis.x;
        penetration.getNormal().y = currentAxis.y;
        penetration.setDepth(minOverlap);
        return new DetectorResult(true, shape);
    }
}
