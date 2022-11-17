package gizmoball.engine.collision.detector;

import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.filter.CollisionFilter;
import javafx.util.Pair;

import java.util.List;

public interface CollisionDetector {

    /**
     * 根据物体列表执行narrowPhase并获得碰撞流形
     *
     * @param bodies1 物体列表1
     * @param bodies2 物体列表2
     * @param filters 碰撞过滤器列表
     * @return List
     */
    List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> detect(List<PhysicsBody> bodies1, List<PhysicsBody> bodies2, List<CollisionFilter> filters);

    /**
     * 本地求解前使用碰撞流形计算出接触约束
     *
     * @param manifolds 碰撞流形列表
     * @return List
     */
    List<ContactConstraint> preLocalSolve(List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> manifolds);

    /**
     * 本地求解接触约束
     *
     * @param solver      约束求解器
     * @param gravity     重力大小
     * @param constraints 接触约束列表
     * @param bodies      物体列表
     */
    void LocalSolve(SequentialImpulses solver, Vector2 gravity, List<ContactConstraint> constraints, List<PhysicsBody> bodies);

}
