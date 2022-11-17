package gizmoball.engine.geometry.shape;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Circle extends AbstractShape {

    /**
     * 半径
     */
    private double radius;

    /**
     * 反序列化调用
     */
    @Deprecated
    public Circle() {
        this(1);
    }

    public Circle(double radius) {
        this(radius, new Transform());
    }

    public Circle(double radius, Transform transform) {
        super(transform);
        this.radius = radius;
    }

    @Override
    public void zoom(int rate) {
        if (rate < 1) return;
        this.radius = this.radius / this.rate * rate;
        this.rate = rate;
    }

    @Override
    public AABB createAABB() {
        return new AABB(transform.getX() - radius,
                transform.getY() - radius,
                transform.getX() + radius,
                transform.getY() + radius);
    }

    @Override
    public Interval project(Vector2 axis) {
        Vector2 center = new Vector2(transform.getX(), transform.getY());
        double c = center.dot(axis);
        return new Interval(c - radius, c + radius);
    }

    @Override
    public Vector2[] getAxes(Vector2[] foci) {
        return null;
    }

    @Override
    public Vector2[] getFoci() {
        Vector2[] foci = new Vector2[1];
        foci[0] = new Vector2(transform.getX(), transform.getY());
        return foci;
    }

    @Override
    public Vector2 getFarthestFeature(Vector2 vector) {
        return this.getFarthestPoint(vector);
    }

    @Override
    public Vector2 getFarthestPoint(Vector2 vector) {
        Vector2 nAxis = vector.getNormalized();
        Vector2 center = new Vector2(transform.getX(), transform.getY());
        center.x += this.radius * nAxis.x;
        center.y += this.radius * nAxis.y;
        return center;
    }

    @Override
    public Mass createMass(double density) {
        double r2 = this.radius * this.radius;
        double mass = density * Math.PI * r2;
        double inertia = mass * r2 * 0.5;
        return new Mass(new Vector2(0, 0), mass, inertia);
    }

}
