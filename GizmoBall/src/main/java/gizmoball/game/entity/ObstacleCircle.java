package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;

public class ObstacleCircle extends Circle {
    public ObstacleCircle() {
    }

    public ObstacleCircle(double radius) {
        super(radius);
    }

    public ObstacleCircle(double radius, Transform transform) {
        super(radius, transform);
    }
}
