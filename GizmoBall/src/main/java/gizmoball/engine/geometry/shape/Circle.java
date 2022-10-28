package gizmoball.engine.geometry.shape;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Circle extends AbstractShape {

    private double radius;

    public Circle(Transform transform) {
        super(transform);
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

}
