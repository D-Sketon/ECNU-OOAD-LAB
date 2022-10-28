package gizmoball.engine.collision;

import gizmoball.engine.geometry.Vector2;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Penetration {
    /**
     * The normalized axis of projection
     */
    protected final Vector2 normal;

    /**
     * The penetration amount on this axis
     */
    protected double depth;

    public Penetration() {
        this.normal = new Vector2();
    }

    protected Penetration(Vector2 normal, double depth) {
        this.normal = normal.copy();
        this.depth = depth;
    }
    
}
