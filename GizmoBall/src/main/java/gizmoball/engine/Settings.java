package gizmoball.engine;


public class Settings {

    /**
     * 挡板在最高点维持的时间
     */
    public static final Integer FLIPPER_TICKS = 5;

    /**
     * 管道穿模误差
     */
    public static final double PIPE_PIERCE_BIAS = 5;

    /**
     * 默认挡板角速度
     */
    public static final double DEFAULT_FLIPPER_ANGULAR = 15;

    /**
     * 默认每tick挡板旋转角度
     */
    public static final double DEFAULT_FLIPPER_ROTATION = 10;

    /**
     * 默认角速度衰减
     */
    public static final double DEFAULT_ANGULAR_DAMPING = 0.01;

    public static final int TICKS_PER_SECOND = 60;

    /**
     * 默认每tick时长，单位s
     */
    public static final double DEFAULT_TICK_FREQUENCY = 1.0 / TICKS_PER_SECOND;

    /**
     * 默认每tick最大平移距离
     */
    public static final double DEFAULT_MAXIMUM_TRANSLATION = 90.0;

    /**
     * 默认每tick最大旋转角度
     */
    public static final double DEFAULT_MAXIMUM_ROTATION = 0.5 * Math.PI;

    /**
     * 默认求解器迭代次数
     */
    public static final int DEFAULT_SOLVER_ITERATIONS = 20;

    /**
     * 默认线性容差，用于防止过冲
     */
    public static final double DEFAULT_LINEAR_TOLERANCE = 0.05;

    /**
     * 最大线性位置校正，用于防止过冲
     */
    public static final double DEFAULT_MAXIMUM_LINEAR_CORRECTION = 1;

    /**
     * 默认比例因子，用于防止过冲
     */
    public static final double DEFAULT_BAUMGARTE = 1;

}
