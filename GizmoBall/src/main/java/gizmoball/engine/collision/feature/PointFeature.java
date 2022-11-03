package gizmoball.engine.collision.feature;

import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 点的碰撞特征（基于{@link Circle}）
 */
@Data
@AllArgsConstructor
public class PointFeature extends Feature {

    /**
     * 点的坐标
     */
    private Vector2 point;

}
