package gizmoball.game.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Triangle;

public class ObstacleTriangle extends Triangle {
    public ObstacleTriangle() {
    }

    public ObstacleTriangle(Vector2[] vertices) {
        super(vertices);
    }

    public ObstacleTriangle(Vector2[] vertices, Transform transform) {
        super(vertices, transform);
    }
}
