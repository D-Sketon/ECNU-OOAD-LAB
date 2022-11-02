package gizmoball.engine.collision.detector;

import gizmoball.engine.geometry.shape.AbstractShape;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetectorResult {

    private boolean hasCollision;

    private AbstractShape approximateShape;
}
