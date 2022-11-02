package gizmoball.engine.geometry.shape;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.collision.feature.Feature;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuarterCircle extends AbstractShape {

    private double radius;

    private Vector2[] vertices;

    public QuarterCircle(double radius) {
        this(new Transform(), radius);
    }

    public QuarterCircle(Transform transform, double radius) {
        super(transform);
        this.radius = radius;
        this.vertices = new Vector2[3];
        this.vertices[0] = new Vector2(-radius / 2, radius / 2);
        this.vertices[1] = new Vector2(-radius / 2, -radius / 2);
        this.vertices[2] = new Vector2(radius / 2, -radius / 2);
    }

    @Override
    public void zoom(int rate) {
        if (rate < 1) return;
        this.radius = this.radius / this.rate * rate;
        int size = vertices.length;
        for (int i = 0; i < size; i++) {
            vertices[i] = vertices[i].multiply((double) rate / this.rate);
        }
        this.rate = rate;
    }

    @Override
    public Mass createMass(double density) {
        double r2 = this.radius * this.radius;

        double mass = density * Math.PI * r2 / 4;
        double inertia = mass * r2 * 0.5;
        return new Mass(new Vector2(4 * Math.sqrt(2) * radius / 3 / Math.PI - radius / 2,
                4 * Math.sqrt(2) * radius / 3 / Math.PI - radius / 2), mass, inertia);
    }

    @Override
    public AABB createAABB() {
        return new Rectangle(this.radius / 2, this.radius / 2, this.transform.copy()).createAABB();
    }

    @Override
    public Interval project(Vector2 axis) {
        return null;
    }

    @Override
    public Vector2[] getAxes(Vector2[] foci) {
        return null;
    }

    @Override
    public Vector2[] getFoci() {
        return null;
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
