package gizmoball.engine.geometry.shape;


import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Rectangle extends Polygon {

    /**
     * 矩形的半宽
     */
    protected double halfWidth;

    /**
     * 矩形的半高
     */
    protected double halfHeight;

    private Rectangle(double halfWidth, double halfHeight, Vector2[] vertices, Transform transform) {
        super(transform, vertices, new Vector2[]{
                new Vector2(0.0, -1.0),
                new Vector2(1.0, 0.0),
                new Vector2(0.0, 1.0),
                new Vector2(-1.0, 0.0)
        });
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
    }


    public Rectangle(double halfWidth, double halfHeight, Transform transform) {
        this(halfWidth, halfHeight, new Vector2[]{
                new Vector2(-halfWidth, -halfHeight),
                new Vector2(halfWidth, -halfHeight),
                new Vector2(halfWidth, halfHeight),
                new Vector2(-halfWidth, halfHeight)
        }, transform);
    }

    public Rectangle(double halfWidth, double halfHeight) {
        this(halfWidth, halfHeight, new Transform());
    }

    /**
     * 反序列化调用
     */
    @Deprecated
    public Rectangle() {
        this(0.0, 0.0);
    }

    @Override
    public void zoom(int rate) {
        super.zoom(rate);
        if (rate < 1) return;
        this.halfWidth = this.halfWidth / this.rate * rate;
        this.halfHeight = this.halfHeight / this.rate * rate;
        this.rate = rate;
    }

}
