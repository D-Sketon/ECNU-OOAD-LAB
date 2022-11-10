package gizmoball.engine.collision;

import gizmoball.engine.geometry.Vector2;
import lombok.Data;

@Data
public class Penetration {
    /**
     * 穿透法线，Shape1指向Shape2
     */
    protected Vector2 normal;

    /**
     * 穿透深度
     */
    protected double depth;

    public Penetration() {
        this.normal = new Vector2();
    }

}
