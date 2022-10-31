package gizmoball.engine.collision.manifold;

import gizmoball.engine.geometry.Vector2;
import lombok.Data;

import java.util.List;

/**
 * 多个碰撞点组成的流形
 */
@Data
public class Manifold {

    /**
     * 碰撞点列表
     */
    private List<ManifoldPoint> points;

    /**
     * 分离法线的反向量
     */
    private Vector2 normal;
}
