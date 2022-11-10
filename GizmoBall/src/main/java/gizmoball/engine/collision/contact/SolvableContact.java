package gizmoball.engine.collision.contact;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 接触信息
 */
@Data
@AllArgsConstructor
final class SolvableContact {
    /**
     * 世界坐标系下的接触点坐标
     */
    private Vector2 p;

    /**
     * 穿透深度
     */
    private double depth;

    /**
     * {@link PhysicsBody}1坐标系下的接触点坐标
     */
    private Vector2 p1;

    /**
     * {@link PhysicsBody}2坐标系下的接触点坐标
     */
    private Vector2 p2;

    /**
     * {@link PhysicsBody}1中心点到接触点的向量
     */
    private Vector2 r1;

    /**
     * {@link PhysicsBody}2中心点到接触点的向量
     */
    private Vector2 r2;

    /**
     * 法线冲量，用于施加碰撞冲量
     */
    double jn;

    /**
     * 切线冲量，用于施加摩擦冲量
     */
    double jt;

    /**
     * 位置冲量，用于位置求解器
     */
    double jp;

    /**
     * 法线质量，用于施加碰撞冲量
     */
    private double massN;

    /**
     * 切线质量，用于施加摩擦冲量
     */
    private double massT;

    /**
     * 速度bias，缓解物体镶嵌问题
     */
    double vb;

    public SolvableContact(Vector2 point, double depth, Vector2 p1, Vector2 p2) {
        this.p = point;
        this.depth = depth;
        this.p1 = p1;
        this.p2 = p2;
    }
}
