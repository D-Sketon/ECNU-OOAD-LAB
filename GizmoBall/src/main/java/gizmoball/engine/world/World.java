package gizmoball.engine.world;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.CollisionDetector;
import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.listener.CollisionListener;
import gizmoball.engine.world.listener.TickListener;
import gizmoball.engine.world.listener.TriggerListener;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对整个弹球游戏物体所处世界的抽象
 */
public class World {

    public static final Vector2 EARTH_GRAVITY = new Vector2(0, -9.8);
    public static final Vector2 MOON_GRAVITY = new Vector2(0, -1.6);
    public static final Vector2 MARS_GRAVITY = new Vector2(0, -3.7);
    public static final Vector2 VENUS_GRAVITY = new Vector2(0, -8.9);
    public static final Vector2 JUPITER_GRAVITY = new Vector2(0, -24.8);
    public static final Vector2 SATURN_GRAVITY = new Vector2(0, -10.4);
    public static final Vector2 URANUS_GRAVITY = new Vector2(0, -8.7);
    public static final Vector2 NEPTUNE_GRAVITY = new Vector2(0, -11.0);
    public static final Vector2 PLUTO_GRAVITY = new Vector2(0, -0.6);
    public static final Vector2 SUN_GRAVITY = new Vector2(0, -274);
    public static final Vector2 ZERO_GRAVITY = new Vector2(0, 0);

    protected Vector2 gravity;

    /**
     * 所有游戏物体信息
     */
    protected final List<PhysicsBody> bodies;

    /**
     * 游戏经过ticks数
     */
    protected int timeTicks;

    protected final List<TickListener> tickListeners;

    protected final List<TriggerListener> triggerListeners;

    protected final CollisionDetector collisionDetector;

    protected final SequentialImpulses solver;

    protected List<CollisionListener> listeners;

    public World(Vector2 gravity, List<CollisionListener> listeners) {
        this.gravity = gravity;
        this.bodies = new ArrayList<>();
        this.timeTicks = 0;
        this.tickListeners = new ArrayList<>();
        this.triggerListeners = new ArrayList<>();
        this.listeners = listeners;
        this.collisionDetector = new BasicCollisionDetector();
        this.solver = new SequentialImpulses();
    }

    public void addBodies(PhysicsBody... bodies) {
        this.bodies.addAll(Arrays.asList(bodies));
    }

    public void removeBodies(PhysicsBody... bodies) {
        this.bodies.removeAll(Arrays.asList(bodies));
    }

    public List<PhysicsBody> getBodies() {
        return this.bodies;
    }

    /**
     * 游戏更新以tick为单位，每个tick更新一次
     */
    public void tick() {
        // -- tickListener.before

        // 碰撞检测，返回碰撞检测
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> pairs = collisionDetector.detect(bodies, listeners);

        List<ContactConstraint> contactConstraints = collisionDetector.preLocalSolve(pairs);
        collisionDetector.LocalSolve(solver, gravity, contactConstraints, bodies);


        // -- triggerListener.before

        // 更新物体状态
        // 1. 根据碰撞信息更新物体速度
        // 2. 根据物体速度更新物体位置

        // -- triggerListener.after

        // -- tickListener.after
    }


}
