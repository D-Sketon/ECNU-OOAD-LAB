package gizmoball.engine.collision.feature;

import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Polygon;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 边的碰撞特征（基于{@link Polygon}）
 */
@Data
@AllArgsConstructor
public class EdgeFeature extends Feature {

    /**
     * 边的第一个顶点
     */
    private PointFeature vertex1;

    /**
     * 边的第二个顶点
     */
    private PointFeature vertex2;

    /**
     * vertex1和vertex2中投影最远的顶点
     */
    private PointFeature max;

    /**
     * 边的方向向量（方向从右向左）
     */
    private Vector2 edge;

}
