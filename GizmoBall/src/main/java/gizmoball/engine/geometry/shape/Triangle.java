package gizmoball.engine.geometry.shape;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Triangle extends Polygon {

    /**
     * 反序列化调用
     */
    @Deprecated
    public Triangle() {
        this(null);
    }

    public Triangle(Vector2[] vertices) {
        this(vertices, new Transform());
    }

    public Triangle(Vector2[] vertices, Transform transform) {
        super(transform, vertices, getCounterClockwiseEdgeNormals(vertices));
    }

    @Override
    public void zoom(int rate) {
        super.zoom(rate);
        this.rate = rate;
    }
}
