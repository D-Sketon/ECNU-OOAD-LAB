package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

import static gizmoball.engine.Settings.FLIPPER_TICKS;


@Getter
@Setter
public class Flipper extends Rectangle {
    private Direction direction;

    private boolean isUp;

    /**
     * 挡板在高点维持一段时间
     */
    private Integer ticks;

    private double angular;

    public Flipper(double halfWidth, double halfHeight, Transform transform, Direction direction) {
        super(halfWidth, halfHeight, transform);
        this.direction = direction;
        init();
    }

    public Flipper(double halfWidth, double halfHeight, Direction direction) {
        super(halfWidth, halfHeight);
        this.direction = direction;
        init();
    }

    public Flipper(Direction direction) {
        this.direction = direction;
    }

    /**
     * 初始化信息
     */
    void init() {
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
        System.out.println(getTransform());
    }

    public void decreaseTicks() {
        this.ticks--;
    }

    public enum Direction {
        LEFT,
        RIGHT
    }

    public void rise() {
        this.isUp = true;
        this.ticks = FLIPPER_TICKS;
    }

}
