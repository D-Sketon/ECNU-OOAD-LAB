package gizmoball.engine.collision.manifold;

import gizmoball.engine.geometry.Vector2;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 单个碰撞点
 */
@Data
@AllArgsConstructor
public class ManifoldPoint {

    private ManifoldPointId id;

    /**
     * 点的坐标
     */
    private Vector2 point;

    /**
     * 穿透深度（来源于穿透信息）
     */
    private double depth;
}
