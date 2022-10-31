package gizmoball.engine.physics;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 物体重量
 */
@Data
@RequiredArgsConstructor
public class Mass {

    /** The mass in kg */
	final double mass;

    // 以下属性计算时**可能**会用到
	/** The inertia tensor in kg &middot; m<sup>2</sup> */
	final double inertia;

	/** The inverse mass */
	final double invMass;

	/** The inverse inertia tensor */
	final double invInertia;
}
