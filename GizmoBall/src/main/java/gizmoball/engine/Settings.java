package gizmoball.engine;


import gizmoball.engine.physics.PhysicsBody;

public class Settings {

    public static final double DEFAULT_ANGULAR_DAMPING = 0.01;
    /**
     * The default step frequency of the dynamics engine; in seconds
     */
    public static final double DEFAULT_STEP_FREQUENCY = 1.0 / 60.0;

    /**
     * The default maximum translation a {@link PhysicsBody} can have in one time step; in meters
     */
    public static final double DEFAULT_MAXIMUM_TRANSLATION = 2.0;

    /**
     * The default maximum rotation a {@link PhysicsBody} can have in one time step; in radians
     */
    public static final double DEFAULT_MAXIMUM_ROTATION = 0.5 * Math.PI;

    /**
     * The default number of solver iterations
     */
    public static final int DEFAULT_SOLVER_ITERATIONS = 10;

    /**
     * The default linear tolerance; in meters
     */
    public static final double DEFAULT_LINEAR_TOLERANCE = 0.005;

    /**
     * The default maximum linear correction; in meters
     */
    public static final double DEFAULT_MAXIMUM_LINEAR_CORRECTION = 0.2;

    /**
     * The default baumgarte
     */
    public static final double DEFAULT_BAUMGARTE = 0.2;

}
