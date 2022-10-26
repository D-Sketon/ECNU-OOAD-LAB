package gizmoball.engine.geometry;

import lombok.Data;

@Data
public class Vector2 {

    private double x;

    private double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
}
