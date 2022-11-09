package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;

public class Blackhole extends Circle {

    /**
     * 反序列化用
     */
    @Deprecated
    public Blackhole() {
    }

    public Blackhole(double radius) {
        super(radius);
    }

    public Blackhole(double radius, Transform transform) {
        super(radius, transform);
    }
}
