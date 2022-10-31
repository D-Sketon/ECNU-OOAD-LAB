package gizmoball.engine.geometry.shape;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.collision.feature.Feature;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuarterCircle extends AbstractShape {

    private double radius;

    private double alpha;

    final double cosAlpha;

    final Vector2[] vertices;

    final Vector2[] normals;

    public QuarterCircle(Transform transform) {
        this(transform, 1.0);
    }

    public QuarterCircle(Transform transform, double radius) {
        super(transform);

        double theta = Math.PI / 2;
        double cosAlpha =
                this.alpha = theta * 0.5;

        // compute the triangular section of the pie (and cache cos(alpha))
        double x = radius * (this.cosAlpha = Math.cos(this.alpha));
        double y = radius * Math.sin(this.alpha);
        this.vertices = new Vector2[]{
                // the origin
                new Vector2(),
                // the top point
                new Vector2(x, y),
                // the bottom point
                new Vector2(x, -y)
        };

        Vector2 v1 = this.vertices[1].to(this.vertices[0]);
        Vector2 v2 = this.vertices[0].to(this.vertices[2]);
        v1.left().normalize();
        v2.left().normalize();
        this.normals = new Vector2[]{v1, v2};
    }

    @Override
    public void zoom(int rate) {
        if (rate < 1) return;
        this.radius = this.radius / this.rate * rate;
        this.rate = rate;
    }

    @Override
    public AABB createAABB() {
        Vector2 vector2 = new Vector2(this.radius / 2, this.radius / 2);
        Vector2 transformed = this.transform.getTransformed(vector2);
        Transform newTransform = new Transform(this.transform.getCost(), this.transform.getSint(), transformed.x, transformed.y);
        return new Rectangle(this.radius / 2, this.radius / 2, newTransform).createAABB();
    }

    @Override
    public Interval project(Vector2 axis) {
        return null;
    }

    @Override
    public Vector2[] getAxes(Vector2[] foci) {
        int fociSize = foci != null ? foci.length : 0;
        int size = this.vertices.length;
        Vector2[] axes = new Vector2[2 + fociSize];
        int index = 0;
        axes[index++] = transform.getTransformedR(this.normals[0]);
        axes[index++] = transform.getTransformedR(this.normals[1]);
        Vector2 focus = transform.getTransformed(this.vertices[0]);
        for (int i = 0; i < fociSize; i++) {
            Vector2 f = foci[i];
            Vector2 closest = focus;
            double d = f.distanceSquared(closest);
            for (int j = 1; j < size; j++) {
                Vector2 p = this.vertices[j];
                p = transform.getTransformed(p);
                double dt = f.distanceSquared(p);
                if (dt < d) {
                    closest = p;
                    d = dt;
                }
            }
            Vector2 axis = f.to(closest);
            axis.normalize();
            axes[index++] = axis;
        }
        return axes;
    }

    @Override
    public Vector2[] getFoci() {
        return new Vector2[]{
                transform.getTransformed(this.vertices[0])
        };
    }

    @Override
    public Feature getFarthestFeature(Vector2 vector) {
        return null;
    }

    @Override
    public Vector2 getFarthestPoint(Vector2 vector) {
        return null;
    }

}
