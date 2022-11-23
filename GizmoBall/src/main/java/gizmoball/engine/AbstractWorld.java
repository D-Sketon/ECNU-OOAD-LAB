package gizmoball.engine;

import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.CollisionDetector;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 对整个弹球游戏物体所处世界的抽象
 */
public abstract class AbstractWorld<T extends PhysicsBody> {

    /**
     * 地球重力
     */
    public static final Vector2 EARTH_GRAVITY = new Vector2(0, -9.8);

    protected Vector2 gravity;

    protected final List<T> bodies;

    protected final CollisionDetector collisionDetector;

    protected final SequentialImpulses solver;


    public AbstractWorld(Vector2 gravity) {
        this.gravity = gravity;
        this.bodies = new ArrayList<>();

        this.collisionDetector = new BasicCollisionDetector();
        this.solver = new SequentialImpulses();
    }

    public void addBody(T body) {
        this.bodies.add(body);
    }

    public void removeBody(T body) {
        this.bodies.remove(body);
    }

    public void removeAllBodies() {
        bodies.clear();
    }

    public List<T> getBodies() {
        return bodies;
    }

    /**
     * 游戏更新以tick为单位，每个tick更新一次
     */
    public abstract void tick();


}
