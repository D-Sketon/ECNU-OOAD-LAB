package gizmoball.engine.world.entity;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Rectangle;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pipe extends Rectangle {

    private PipeDirection pipeDirection;

    /**
     * 反序列化用
     */
    @Deprecated
    public Pipe() {
        this(0,0);
    }

    public Pipe(double halfWidth, double halfHeight, Transform transform) {
        super(halfWidth, halfHeight, transform);
        this.pipeDirection = PipeDirection.TRANSVERSE;
    }

    public Pipe(double halfWidth, double halfHeight) {
        super(halfWidth, halfHeight);
        this.pipeDirection = PipeDirection.TRANSVERSE;
    }

    public enum PipeDirection{
        TRANSVERSE,

        VERTICAL
    }

    @Override
    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
        System.out.println(this.getTransform().sint);
        if(Math.abs(this.getTransform().sint) > 0.5){
            this.pipeDirection = PipeDirection.VERTICAL;

        } else{
            this.pipeDirection = PipeDirection.TRANSVERSE;
        }
    }


}
