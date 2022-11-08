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
        this.lb = vertices[0];
        this.lt = vertices[3];
        this.rb = vertices[1];
        this.rt = vertices[2];
        this.isUp = false;
        this.angular = 0;
    }

    public Flipper(double halfWidth, double halfHeight, Direction direction) {
        super(halfWidth, halfHeight);
        this.direction = direction;
        this.lb = vertices[0];
        this.lt = vertices[3];
        this.rb = vertices[1];
        this.rt = vertices[2];
        this.isUp = false;
        this.angular = 0;
    }

    public Flipper(Direction direction) {
        this.direction = direction;
    }

    public void flip() {
        System.out.println((this.angular / 180) * Math.PI);
        if(this.direction == Direction.LEFT){
            rotate(this.lb, this.lt, (this.angular / 180) * Math.PI);
            rotate(this.lb, this.rb, (this.angular / 180) * Math.PI);
            rotate(this.lb, this.rt, (this.angular / 180) * Math.PI);
        } else {
            rotate(this.rb, this.lt, (-this.angular / 180) * Math.PI);
            rotate(this.rb, this.lb, (-this.angular / 180) * Math.PI);
            rotate(this.rb, this.rt, (-this.angular / 180) * Math.PI);
        }
        System.out.println(this.vertices);
    }

    public enum Direction{
        LEFT,
        RIGHT
    }

    public void rise(){
        this.isUp = true;
    }

    /**
     * 绕着定点旋转theta度
     * @param fix
     * @param move
     * @param theta
     */
    public void rotate(Vector2 fix, Vector2 move, double theta){;
        Vector2 to = fix.to(move);
        double magnitude = to.getMagnitude();
        move.x = fix.x + Math.cos(theta) * magnitude;
        move.y = fix.y + Math.sin(theta) * magnitude;
    }
}
