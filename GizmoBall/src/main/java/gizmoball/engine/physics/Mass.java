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

    /**
     * 质量类型
     */
    private MassType type;

    /**
     * 质心坐标
     */
    private Vector2 center;

    /**
     * 质量大小（kg）
     */
    private double mass;

    /**
     * 转动惯量
     */
    private double inertia;

    /**
     * 质量大小倒数
     */
    private double InverseMass;

    /**
     * 转动惯量倒数
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
