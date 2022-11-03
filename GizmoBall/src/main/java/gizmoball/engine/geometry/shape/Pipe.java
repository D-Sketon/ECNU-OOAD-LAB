package gizmoball.engine.geometry.shape;

import gizmoball.engine.geometry.Transform;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pipe extends Rectangle{

    private PipeDirection pipeDirection;

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
    };

    @Override


    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
        this.pipeDirection = this.pipeDirection == PipeDirection.TRANSVERSE ? PipeDirection.VERTICAL : PipeDirection.TRANSVERSE;
    }


}
