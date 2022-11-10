package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;

public class BlackHole extends Circle {

    /**
     * 反序列化用
     */
    @Deprecated
    public BlackHole() {
    }

    public BlackHole(double radius) {
        super(radius);
    }

    public BlackHole(double radius, Transform transform) {
        super(radius, transform);
    }
}
