package gizmoball.engine.physics;

import gizmoball.engine.Settings;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
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
     * 扭转力
     */
    protected double torque;

    /**
     * 当前受力列表
     */
    protected final List<Vector2> forces;

    protected final List<Double> torques;

    /**
     * 物体组成的部件（简单形状）
     */
    protected final AbstractShape shape;

    /**
     * 重力系数
     */
    protected double gravityScale;

    /**
     * 是否是触发器（无碰撞，物体可穿过）
     */
    protected boolean isTrigger;

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

    public PhysicsBody(AbstractShape shape) {
        this.mass = new Mass();
        this.linearVelocity = new Vector2();
        this.linearDamping = 0.0;
        this.force = new Vector2();
        this.forces = new ArrayList<>();
        this.torques = new ArrayList<>();
        this.isTrigger = false;
        this.shape = shape;
        this.gravityScale = 10.0;
    }

    protected void accumulate(double elapsedTime) {
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
        // set the current torque to zero
        this.torque = 0.0;
        // get the number of torques
        size = this.torques.size();
        // check the size
        if (size > 0) {
            // apply all the torques
            Iterator<Double> it = this.torques.iterator();
            while (it.hasNext()) {
                Double torque = it.next();
                this.torque += torque;
                it.remove();

            }
        }
    }


    public void integrateVelocity(Vector2 gravity) {
        if (this.mass.getType() == MassType.INFINITE || this.mass.getType() == null) {
            return;
        }
        double elapsedTime = Settings.DEFAULT_STEP_FREQUENCY;

        // accumulate the forces and torques
        this.accumulate(elapsedTime);

        // get the mass properties
        double mass = this.mass.getMass();
        double inverseMass = this.mass.getInverseMass();
        double inverseInertia = this.mass.getInverseInertia();

        if (inverseMass > Epsilon.E) {
            this.linearVelocity.x += elapsedTime * inverseMass * (gravity.x * this.gravityScale * mass + this.force.x);
            this.linearVelocity.y += elapsedTime * inverseMass * (gravity.y * this.gravityScale * mass + this.force.y);
        }

        // av1 = av0 + (t / I) * dt
        if (inverseInertia > Epsilon.E) {
            // only perform this step if the body does not have
            // a fixed angular velocity
            this.angularVelocity += inverseInertia * this.torque * elapsedTime;
        }

        // apply linear damping
        if (this.linearDamping != 0.0) {
            // Because DEFAULT_LINEAR_DAMPING is 0.0 apply linear damping only if needed
            double linear = 1.0 - elapsedTime * this.linearDamping;
            linear = Interval.sandwich(linear, 0.0, 1.0);

            // inline body.velocity.multiply(linear);
            this.linearVelocity.x *= linear;
            this.linearVelocity.y *= linear;
        }

        // apply angular damping
        double angular = 1.0 - elapsedTime * Settings.DEFAULT_ANGULAR_DAMPING;
        angular = Interval.sandwich(angular, 0.0, 1.0);

        this.angularVelocity *= angular;
    }

    public boolean isStatic() {
        return this.mass.getType() == MassType.INFINITE &&
                Math.abs(this.linearVelocity.x) <= Epsilon.E &&
                Math.abs(this.linearVelocity.y) <= Epsilon.E &&
                Math.abs(this.angularVelocity) <= Epsilon.E;
    }

    public void integratePosition() {
        double elapsedTime = Settings.DEFAULT_STEP_FREQUENCY;
        double maxTranslation = Settings.DEFAULT_MAXIMUM_TRANSLATION;
        double maxTranslationSquared = maxTranslation * maxTranslation;
        double maxRotation = Settings.DEFAULT_MAXIMUM_ROTATION;

        // if the body isn't moving then don't bother
        if (this.isStatic()) {
            return;
        }

        // compute the translation and rotation for this time step
        double translationX = this.linearVelocity.x * elapsedTime;
        double translationY = this.linearVelocity.y * elapsedTime;
        double translationMagnitudeSquared = translationX * translationX + translationY * translationY;

        // make sure the translation is not over the maximum
        if (translationMagnitudeSquared > maxTranslationSquared) {
            double translationMagnitude = Math.sqrt(translationMagnitudeSquared);
            double ratio = maxTranslation / translationMagnitude;

            this.linearVelocity.multiply(ratio);

            translationX *= ratio;
            translationY *= ratio;
        }

        double rotation = this.angularVelocity * elapsedTime;

        // make sure the rotation is not over the maximum
        if (rotation > maxRotation) {
            double ratio = maxRotation / Math.abs(rotation);

            this.angularVelocity *= ratio;
            rotation *= ratio;
        }

        // apply the translation/rotation
        this.getShape().translate(translationX, translationY);
        this.rotateAboutCenter(rotation);
    }

    private void rotateAboutCenter(double theta) {
        Vector2 center = this.getShape().getTransform().getTransformed(this.getMass().getCenter());
        this.getShape().rotate(theta, center.x, center.y);
    }
}


