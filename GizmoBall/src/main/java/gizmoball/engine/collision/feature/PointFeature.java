package gizmoball.engine.collision.feature;

import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点的碰撞特征（基于{@link Circle}）
 */
@Data
@NoArgsConstructor
public class PointFeature extends Feature {

    /**
     * 点的坐标
     */
    private Vector2 point;

    public PointFeature(Vector2 point) {
        this(point, -1);
    }

    public PointFeature(Vector2 point, int index) {
        super(index);
        this.point = point;
    }
}
