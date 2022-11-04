package gizmoball.engine.collision;

import gizmoball.engine.geometry.Vector2;
import lombok.Data;

@Data
public class Penetration {
    /**
     * The normalized axis of projection
     */
    protected Vector2 normal;

    /**
     * The penetration amount on this axis
     */
    protected double depth;

    public Penetration() {
        this.normal = new Vector2();
    }

}
