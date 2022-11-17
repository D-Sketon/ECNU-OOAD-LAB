package gizmoball.engine.geometry.shape;

import gizmoball.engine.geometry.Vector2;

/**
 * 凸面形接口
 */
public interface Convex {

    /**
     * 基于另一个图形的焦点获取分离轴
     *
     * @param foci 焦点数组
     * @return Vector2[]
     */
    Vector2[] getAxes(Vector2[] foci);

    /**
     * 获取焦点
     *
     * @return Vector2[]
     */
    Vector2[] getFoci();

    /**
     * 获得离给定{@link Vector2}最远的碰撞特征
     *
     * @param vector 方向向量
     * @return Feature
     */
    Vector2 getFarthestFeature(Vector2 vector);

    /**
     * 获得离给定{@link Vector2}最远的点（顶点）
     *
     * @param vector 方向向量
     * @return Vector2
     */
    Vector2 getFarthestPoint(Vector2 vector);

}
