package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

import static gizmoball.engine.Settings.FILPPER_TICKS;


@Getter
@Setter
public class Flipper extends Rectangle {

    private Vector2 lb;
    private Vector2 rb;
    private Vector2 lt;
    private Vector2 rt;

    private Vector2[] normal_copy;

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
    void init(){
        this.lb = vertices[0].copy();
        this.lt = vertices[3].copy();
        this.rb = vertices[1].copy();
        this.rt = vertices[2].copy();
        normal_copy = new Vector2[4];
        for(int i = 0; i < 4; i++){
            normal_copy[i] = normals[i].copy();
        }
        this.isUp = false;
        this.angular = 0;
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
            rotateNormals((this.angular / 180) * Math.PI);
        } else {
            rotate(this.rb, this.lt, vertices[3], -(this.angular / 180) * Math.PI);
            rotate(this.rb, this.lb, vertices[0], -(this.angular / 180) * Math.PI);
            rotate(this.rb, this.rt, vertices[2], -(this.angular / 180) * Math.PI);
            rotateNormals(-(this.angular / 180) * Math.PI);
        }
    }

    private void rotateNormals(double theta) {
        for(int i = 0; i < 4; i++){
            Vector2 vector_copy = normal_copy[i];
            Vector2 normal = normals[i];
            double c = Math.cos(theta);
            double s = Math.sin(theta);
            double x = vector_copy.x;
            double y = vector_copy.y;
            normal.x = c * x - s * y;
            normal.y = s * x + c * y;
        }
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
        this.ticks = FILPPER_TICKS;
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
