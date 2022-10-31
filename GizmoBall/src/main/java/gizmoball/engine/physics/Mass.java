package gizmoball.engine.physics;

import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物体重量
 */
@Data
@NoArgsConstructor
public class Mass {

    private MassType type;

    private Vector2 center;

    /**
     * The mass in kg
     */
    private double mass;

    // 以下属性计算时**可能**会用到
    /**
     * The inertia tensor in kg &middot; m<sup>2</sup>
     */
    private double inertia;

    /**
     * The inverse mass
     */
    private double InverseMass;

    /**
     * The inverse inertia tensor
     */
    private double InverseInertia;

    public Mass(Vector2 center, double mass, double inertia) {
        this.type = MassType.NORMAL;
        this.center = center.copy();
        this.mass = mass;
        this.inertia = inertia;
        if (mass > Epsilon.E) {
            this.InverseMass = 1.0 / mass;
        } else {
            this.InverseMass = 0.0;
        }
        if (inertia > Epsilon.E) {
            this.InverseInertia = 1.0 / inertia;
        } else {
            this.InverseInertia = 0.0;
        }
        if (mass <= Epsilon.E && inertia <= Epsilon.E) {
            this.type = MassType.INFINITE;
        }
    }

}
