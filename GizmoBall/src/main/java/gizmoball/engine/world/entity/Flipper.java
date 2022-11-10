package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Flipper extends Rectangle {

    private Vector2 lb;
    private Vector2 rb;
    private Vector2 lt;
    private Vector2 rt;

    private Direction direction;

    private boolean isUp;

    private double angular;

    public Flipper(double halfWidth, double halfHeight, Transform transform, Direction direction) {
        super(halfWidth, halfHeight, transform);
        this.direction = direction;
        this.lb = vertices[0].copy();
        this.lt = vertices[3].copy();
        this.rb = vertices[1].copy();
        this.rt = vertices[2].copy();
        this.isUp = false;
        this.angular = 0;
    }

    public Flipper(double halfWidth, double halfHeight, Direction direction) {
        super(halfWidth, halfHeight);
        this.direction = direction;
        this.lb = vertices[0].copy();
        this.lt = vertices[3].copy();
        this.rb = vertices[1].copy();
        this.rt = vertices[2].copy();
        this.isUp = false;
        this.angular = 0;
    }

    public Flipper(Direction direction) {
        this.direction = direction;
    }

    /**
     * 反序列化用
     */
    @Deprecated
    public Flipper() {

    }

    public void flip() {
        if (this.direction == Direction.LEFT) {
            rotate(this.lb, this.lt, vertices[3], (this.angular / 180) * Math.PI);
            rotate(this.lb, this.rb, vertices[1], (this.angular / 180) * Math.PI);
            rotate(this.lb, this.rt, vertices[2], (this.angular / 180) * Math.PI);
        } else {
            rotate(this.rb, this.lt, vertices[3], -(this.angular / 180) * Math.PI);
            rotate(this.rb, this.lb, vertices[0], -(this.angular / 180) * Math.PI);
            rotate(this.rb, this.rt, vertices[2], -(this.angular / 180) * Math.PI);
        }
    }

    public enum Direction {
        LEFT,
        RIGHT
    }

    public void rise() {
        this.isUp = true;
    }

    /**
     * 绕着定点旋转theta度
     *
     * @param fix   固定点
     * @param move  被旋转点旋转前的位置
     * @param v     被旋转点
     * @param theta 旋转角度
     */
    private void rotate(Vector2 fix, Vector2 move, Vector2 v, double theta) {
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        double cx = move.x - fix.x;
        double cy = move.y - fix.y;
        v.x = c * cx - s * cy + fix.x;
        v.y = s * cx + c * cy + fix.y;
    }

    @Override
    public void zoom(int rate) {
        super.zoom(rate);
        this.lb = vertices[0].copy();
        this.lt = vertices[3].copy();
        this.rb = vertices[1].copy();
        this.rt = vertices[2].copy();
    }
}
