package gizmoball.engine.collision.feature;

import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Polygon;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 边的碰撞特征（基于{@link Polygon}）
 */
@Data
@NoArgsConstructor
@Deprecated
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

    public EdgeFeature(PointFeature vertex1, PointFeature vertex2, PointFeature max, Vector2 edge) {
        this(vertex1, vertex2, max, edge, -1);
    }

    public EdgeFeature(PointFeature vertex1, PointFeature vertex2, PointFeature max, Vector2 edge, int index) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.max = max;
        this.edge = edge;
    }

}
