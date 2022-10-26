package gizmoball.engine.geometry.shape;

import gizmoball.engine.geometry.Vector2;
import lombok.Data;

/**
 * 形状的抽象
 */
@Data
public abstract class AbstractShape {

    protected Vector2 center;
}
