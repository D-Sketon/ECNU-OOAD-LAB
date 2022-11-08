package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;

public class Ball extends Circle {
    public Ball() {
    }

    public Ball(double radius) {
        super(radius);
    }

    public Ball(double radius, Transform transform) {
        super(radius, transform);
    }
}
