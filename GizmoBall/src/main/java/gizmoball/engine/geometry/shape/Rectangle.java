package gizmoball.engine.geometry.shape;


import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Rectangle extends Polygon {

    /**
     * 矩形的半宽
     */
    protected double width;

    /**
     * 矩形的半高
     */
    protected double height;

    private Rectangle(double width, double height, Vector2[] vertices, Transform transform) {
        super(transform, vertices, new Vector2[]{
                new Vector2(0.0, -1.0),
                new Vector2(1.0, 0.0),
                new Vector2(0.0, 1.0),
                new Vector2(-1.0, 0.0)
        });
        this.width = width;
        this.height = height;
    }


    public Rectangle(double width, double height, Transform transform) {
        this(width, height, new Vector2[]{
                new Vector2(-width, -height),
                new Vector2(width, -height),
                new Vector2(width, height),
                new Vector2(-width, height)
        }, transform);
    }


    @Override
    public void zoom(int rate) {
        super.zoom(rate);
        if (rate < 1) return;
        this.width = this.width / this.rate * rate;
        this.height = this.height / this.rate * rate;
        this.rate = rate;
    }
    
}
