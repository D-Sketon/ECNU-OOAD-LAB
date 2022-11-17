package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Triangle;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class Flipper extends Triangle {
    private Direction direction;

    private boolean isUp;

    private double angular;


    public Flipper(Vector2[] vertices, Direction direction) {
        super(vertices, new Transform());
        this.direction = direction;
        init();
    }

    public Flipper(Vector2[] vertices, Transform transform, Direction direction) {
        super(vertices, transform);
        this.direction = direction;
        init();
    }

    public Flipper(Direction direction) {
        this.direction = direction;
    }

    /**
     * 初始化信息
     */
    private void init() {
        this.isUp = false;
        this.angular = 0;
    }

    /**
     * 反序列化用
     */
    @Deprecated
    public Flipper() {

    }

    public void flip(double theta) {
        angular += theta;
        if (this.direction == Direction.LEFT) {
            Vector2 transformed = getTransform().getTransformed(vertices[0]);
            rotate(theta / 180 * Math.PI, transformed.x, transformed.y);
        } else {
            Vector2 transformed = getTransform().getTransformed(vertices[1]);
            rotate(-theta / 180 * Math.PI, transformed.x, transformed.y);
        }
    }


    public enum Direction {
        LEFT,
        RIGHT
    }

    public void rise() {
        this.isUp = true;
    }


    public void down() {
        this.isUp = false;
    }

}
