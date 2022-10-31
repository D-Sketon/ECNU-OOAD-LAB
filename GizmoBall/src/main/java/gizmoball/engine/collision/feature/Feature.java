package gizmoball.engine.collision.feature;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.manifold.Manifold;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 碰撞特征，用于从{@link Penetration}中获得{@link Manifold}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Feature {

    /**
     * 特征编号
     */
    private int index;
}
