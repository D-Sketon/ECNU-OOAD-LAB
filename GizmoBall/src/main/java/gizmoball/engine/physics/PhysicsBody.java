package gizmoball.engine.physics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gizmoball.engine.Settings;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 代表一个物体，他也可以是一个组件包含多个基础物体
 */
@Data
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class", visible = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhysicsBody {

    /**
     * 物体质量
     */
    protected Mass mass;

    /**
     * 当前速度
     */
    protected final Vector2 linearVelocity;

    /**
     * 角速度
     */
    protected double angularVelocity;

    /**
     * 线性阻尼
     */
    protected double linearDamping;

    /**
     * 当前受力总和
     */
    protected final Vector2 force;

    /**
     * 当前受力列表
     */
    protected final List<Vector2> forces;

    /**
     * 物体组成的部件（简单形状）
     */
    protected final AbstractShape shape;

    /**
     * 重力系数
     */
    protected double gravityScale;

    /**
     * 阻力系数
     */
    protected double friction;

    /**
     * 回弹系数
     */
    protected double restitution;

    /**
     * 回弹速度
     */
    protected double restitutionVelocity;

    // 反序列化调用
    public PhysicsBody() {
        this(null);
    }

    public PhysicsBody(AbstractShape shape) {
        this.mass = new Mass();
        this.linearVelocity = new Vector2();
        this.linearDamping = 0.0;
        this.force = new Vector2();
        this.forces = new ArrayList<>();
        this.shape = shape;
        this.gravityScale = 10.0;
    }

    /**
     * 使用引力列表更新总引力
     */
    private void accumulate() {
        this.force.zero();
        int size = this.forces.size();
        if (size > 0) {
            Iterator<Vector2> it = this.forces.iterator();
            while (it.hasNext()) {
                Vector2 force = it.next();
                this.force.add(force);
                it.remove();
            }
        }
    }

    /**
     * 使用重力和引力列表更新速度
     *
     * @param gravity 重力
     */
    public void integrateVelocity(Vector2 gravity) {
        if (this.mass.getType() == MassType.INFINITE || this.mass.getType() == null) {
            return;
        }
        double elapsedTime = Settings.DEFAULT_STEP_FREQUENCY;
        this.accumulate();

        double mass = this.mass.getMass();
        double inverseMass = this.mass.getInverseMass();

        // 施加重力和引力
        if (inverseMass > Epsilon.E) {
            this.linearVelocity.x += elapsedTime * inverseMass * (gravity.x * this.gravityScale * mass + this.force.x);
            this.linearVelocity.y += elapsedTime * inverseMass * (gravity.y * this.gravityScale * mass + this.force.y);
        }

        // 施加线性阻尼
        if (this.linearDamping != 0.0) {
            double linear = 1.0 - elapsedTime * this.linearDamping;
            linear = Interval.sandwich(linear, 0.0, 1.0);

            this.linearVelocity.x *= linear;
            this.linearVelocity.y *= linear;
        }

        // 施加转动阻尼
        double angular = 1.0 - elapsedTime * Settings.DEFAULT_ANGULAR_DAMPING;
        angular = Interval.sandwich(angular, 0.0, 1.0);

        this.angularVelocity *= angular;
    }

    /**
     * 判断物体是否固定
     *
     * @return boolean
     */
    private boolean isStatic() {
        return this.mass.getType() == MassType.INFINITE &&
                Math.abs(this.linearVelocity.x) <= Epsilon.E &&
                Math.abs(this.linearVelocity.y) <= Epsilon.E &&
                Math.abs(this.angularVelocity) <= Epsilon.E;
    }

    /**
     * 使用速度更新位置
     */
    public void integratePosition() {
        double elapsedTime = Settings.DEFAULT_STEP_FREQUENCY;
        double maxTranslation = Settings.DEFAULT_MAXIMUM_TRANSLATION;
        double maxTranslationSquared = maxTranslation * maxTranslation;
        double maxRotation = Settings.DEFAULT_MAXIMUM_ROTATION;

        if (this.isStatic()) {
            return;
        }

        // 计算1tick内的平移量
        double translationX = this.linearVelocity.x * elapsedTime;
        double translationY = this.linearVelocity.y * elapsedTime;
        double translationMagnitudeSquared = translationX * translationX + translationY * translationY;

        // 确保平移量不超过限制
        if (translationMagnitudeSquared > maxTranslationSquared) {
            double translationMagnitude = Math.sqrt(translationMagnitudeSquared);
            double ratio = maxTranslation / translationMagnitude;

            this.linearVelocity.multiply(ratio);
            translationX *= ratio;
            translationY *= ratio;
        }

        // 计算1tick内的旋转量
        double rotation = this.angularVelocity * elapsedTime;

        // 确保旋转量不超过限制
        if (rotation > maxRotation) {
            double ratio = maxRotation / Math.abs(rotation);
            this.angularVelocity *= ratio;
            rotation *= ratio;
        }

        // 应用平移量和旋转量
        this.getShape().translate(translationX, translationY);
        Vector2 center = this.getShape().getTransform().getTransformed(this.getMass().getCenter());
        this.getShape().rotate(rotation, center.x, center.y);
    }

}


