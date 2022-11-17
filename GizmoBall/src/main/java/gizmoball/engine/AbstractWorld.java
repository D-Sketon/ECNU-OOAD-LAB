package gizmoball.engine;

import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.CollisionDetector;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对整个弹球游戏物体所处世界的抽象
 */
public abstract class AbstractWorld<T> {

    /**
     * 地球重力
     */
    public static final Vector2 EARTH_GRAVITY = new Vector2(0, -9.8);

    protected Vector2 gravity;

    protected final Map<T, List<PhysicsBody>> bodies;

    protected final CollisionDetector collisionDetector;

    protected final SequentialImpulses solver;


    public AbstractWorld(Vector2 gravity) {
        this.gravity = gravity;
        this.bodies = new HashMap<>();

        this.collisionDetector = new BasicCollisionDetector();
        this.solver = new SequentialImpulses();
    }

    public void addBodies(PhysicsBody body, T key) {
        this.bodies.get(key).add(body);
    }

    public void removeBodies(PhysicsBody body, T key) {
        this.bodies.get(key).remove(body);
    }

    public void removeAllBodies() {
        bodies.forEach((k, v) -> v.clear());
    }

    public List<PhysicsBody> getBodies() {
        List<PhysicsBody> bodyList = new ArrayList<>();
        bodies.forEach((k, v) -> bodyList.addAll(v));
        return bodyList;
    }

    /**
     * 游戏更新以tick为单位，每个tick更新一次
     */
    public abstract void tick();


}
