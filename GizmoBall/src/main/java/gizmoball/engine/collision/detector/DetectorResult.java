package gizmoball.engine.collision.detector;

import gizmoball.engine.geometry.shape.AbstractShape;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetectorResult {

    /**
     * 是否发生碰撞
     */
    private boolean hasCollision;

    /**
     * 近似图形（适用于扇形）
     */
    private AbstractShape approximateShape;
}
