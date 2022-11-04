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

    public static Vector2[] getCounterClockwiseEdgeNormals(Vector2[] vertices) {
        if (vertices == null) return null;

        int size = vertices.length;
        if (size == 0) return null;

        Vector2[] normals = new Vector2[size];
        for (int i = 0; i < size; i++) {
            Vector2 p1 = vertices[i];
            Vector2 p2 = (i + 1 == size) ? vertices[0] : vertices[i + 1];
            Vector2 n = p1.to(p2).left();
            n.normalize();
            normals[i] = n;
        }
        return normals;
    }

    @Override
    public void zoom(int rate) {
        super.zoom(rate);
        this.rate = rate;
    }
}
