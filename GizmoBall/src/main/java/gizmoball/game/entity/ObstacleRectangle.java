package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Rectangle;

public class ObstacleRectangle extends Rectangle {
    public ObstacleRectangle(double halfWidth, double halfHeight, Transform transform) {
        super(halfWidth, halfHeight, transform);
    }

    public ObstacleRectangle(double halfWidth, double halfHeight) {
        super(halfWidth, halfHeight);
    }

    public ObstacleRectangle() {
    }
}
