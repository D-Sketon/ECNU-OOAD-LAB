package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
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

    public void flip() {
        System.out.println((this.angular / 180) * Math.PI);
        if(this.direction == Direction.LEFT){
            rotate(this.lb, this.lt, vertices[3],(this.angular / 180) * Math.PI);
            rotate(this.lb, this.rb, vertices[1],(this.angular / 180) * Math.PI);
            rotate(this.lb, this.rt, vertices[2],(this.angular / 180) * Math.PI);
        } else {
            rotate(this.rb, this.lt, vertices[3],- (this.angular / 180) * Math.PI);
            rotate(this.rb, this.lb, vertices[0], - (this.angular / 180) * Math.PI);
            rotate(this.rb, this.rt, vertices[2],- (this.angular / 180) * Math.PI);
        }
        for (Vector2 vertex : vertices) {
            log.info(vertex.toString());
        }
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
    public void rotate(Vector2 fix, Vector2 move, Vector2 v, double theta){;
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        double cx = move.x - fix.x;
        double cy = move.y - fix.y;
        v.x = c * cx - s * cy + fix.x;
        v.y = s * cx + c * cy + fix.y;

    }
}
