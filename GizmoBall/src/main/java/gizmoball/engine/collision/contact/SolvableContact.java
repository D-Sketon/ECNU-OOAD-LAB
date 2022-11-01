package gizmoball.engine.collision.contact;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
final class SolvableContact {
    /**
     * The contact point in world space
     */
    private Vector2 p;

    /**
     * The contact penetration depth
     */
    private double depth;

    /**
     * The contact point in {@link PhysicsBody}1 space
     */
    private Vector2 p1;

    /**
     * The contact point in {@link PhysicsBody}2 space
     */
    private Vector2 p2;

    /**
     * The {@link Vector2} from the center of {@link PhysicsBody}1 to the contact point
     */
    private Vector2 r1;

    /**
     * The {@link Vector2} from the center of {@link PhysicsBody}2 to the contact point
     */
    private Vector2 r2;

    /**
     * The accumulated normal impulse
     */
    double jn;

    /**
     * The accumulated tangent impulse
     */
    double jt;

    /**
     * The accumulated position impulse
     */
    double jp;

    /**
     * The mass normal
     */
    private double massN;

    /**
     * The mass tangent
     */
    private double massT;

    /**
     * The velocity bias
     */
    double vb;

    /**
     * True if the contact was ignored during solving
     */
    private boolean ignored;

    public SolvableContact(Vector2 point, double depth, Vector2 p1, Vector2 p2) {
        this.p = point;
        this.depth = depth;
        this.p1 = p1;
        this.p2 = p2;
    }
}
