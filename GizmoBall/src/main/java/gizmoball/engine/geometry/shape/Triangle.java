package gizmoball.engine.geometry.shape;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Triangle extends Polygon {

    public Triangle(Vector2[] vertices) {
        this(vertices, new Transform());
    }

    public Triangle(Vector2[] vertices, Transform transform) {
        // TODO
        super(transform, vertices, new Vector2[]{});
    }

    @Override
    public void zoom(int rate) {
        super.zoom(rate);
        this.rate = rate;
    }
}
