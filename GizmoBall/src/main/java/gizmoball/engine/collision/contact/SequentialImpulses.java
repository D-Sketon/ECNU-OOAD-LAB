package gizmoball.engine.collision.contact;

import gizmoball.engine.Settings;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;

import java.util.List;

/**
 * 基于连续冲量的本地碰撞求解器
 */
public class SequentialImpulses {

    /**
     * 对于所给的{@link SolvableContact}计算其有效质量
     *
     * @param contactConstraint 传入{@link ContactConstraint}
     * @param contact           传入{@link SolvableContact}
     * @param n                 法线
     * @return double
     */
    private double getMassCoefficient(ContactConstraint contactConstraint, SolvableContact contact, Vector2 n) {
        return this.getMassCoefficient(contactConstraint, contact.getR1(), contact.getR2(), n);
    }

    /**
     * 对于所给的{@link SolvableContact}计算其有效质量
     *
     * @param contactConstraint 传入{@link ContactConstraint}
     * @param r1                {@link SolvableContact}的r1
     * @param r2                {@link SolvableContact}的r2
     * @param n                 法线
     * @return double
     */
    private double getMassCoefficient(ContactConstraint contactConstraint, Vector2 r1, Vector2 r2, Vector2 n) {
        Mass m1 = contactConstraint.getBody1().getMass();
        Mass m2 = contactConstraint.getBody2().getMass();

        double r1CrossN = r1.cross(n);
        double r2CrossN = r2.cross(n);

        return m1.getInverseMass() + m2.getInverseMass() + m1.getInverseInertia() * r1CrossN * r1CrossN + m2.getInverseInertia() * r2CrossN * r2CrossN;
    }

    /**
     * 对{@link ContactConstraint}中的物体施加所给冲量，改变物体的线速度和角速度
     *
     * @param contactConstraint 传入{@link ContactConstraint}
     * @param contact           传入{@link SolvableContact}
     * @param J                 冲量
     */
    private void updateBodies(ContactConstraint contactConstraint, SolvableContact contact, Vector2 J) {
        PhysicsBody b1 = contactConstraint.getBody1();
        PhysicsBody b2 = contactConstraint.getBody2();
        Mass m1 = b1.getMass();
        Mass m2 = b2.getMass();

        b1.getLinearVelocity().add(new Vector2(J.x * m1.getInverseMass(), J.y * m1.getInverseMass()));
        b1.setAngularVelocity(b1.getAngularVelocity() + m1.getInverseInertia() * contact.getR1().cross(J));

        b2.getLinearVelocity().subtract(new Vector2(J.x * m2.getInverseMass(), J.y * m2.getInverseMass()));
        b2.setAngularVelocity(b2.getAngularVelocity() - m2.getInverseInertia() * contact.getR2().cross(J));
    }

    /**
     * 计算传入{@link ContactConstraint}中物体在穿透法线的相对速度
     *
     * @param contactConstraint 传入{@link ContactConstraint}
     * @param contact           传入{@link SolvableContact}
     * @return double
     */
    private double getRelativeVelocityAlongNormal(ContactConstraint contactConstraint, SolvableContact contact) {
        Vector2 rv = this.getRelativeVelocity(contactConstraint, contact);
        return contactConstraint.getNormal().dot(rv);
    }

    /**
     * 计算传入{@link ContactConstraint}中物体的相对速度
     *
     * @param contactConstraint 传入{@link ContactConstraint}
     * @param contact           传入{@link SolvableContact}
     * @return Vector2
     */
    private Vector2 getRelativeVelocity(ContactConstraint contactConstraint, SolvableContact contact) {
        PhysicsBody b1 = contactConstraint.getBody1();
        PhysicsBody b2 = contactConstraint.getBody2();

        Vector2 lv1 = contact.getR1().cross(b1.getAngularVelocity()).add(b1.getLinearVelocity());
        Vector2 lv2 = contact.getR2().cross(b2.getAngularVelocity()).add(b2.getLinearVelocity());

        return lv1.subtract(lv2);
    }

    /**
     * 初始化，为碰撞约束赋初始值
     *
     * @param contactConstraints 碰撞约束
     */
    public void initialize(List<ContactConstraint> contactConstraints) {
        for (ContactConstraint contactConstraint : contactConstraints) {
            double restitutionVelocity = contactConstraint.getRestitutionVelocity();

            List<SolvableContact> contacts = contactConstraint.getContacts();

            int cSize = contacts.size();
            if (cSize == 0) return;

            PhysicsBody b1 = contactConstraint.getBody1();
            PhysicsBody b2 = contactConstraint.getBody2();

            Transform t1 = b1.getShape().getTransform();
            Transform t2 = b2.getShape().getTransform();

            Mass m1 = b1.getMass();
            Mass m2 = b2.getMass();

            Vector2 c1 = t1.getTransformed(m1.getCenter());
            Vector2 c2 = t2.getTransformed(m2.getCenter());

            Vector2 N = contactConstraint.getNormal();
            Vector2 T = contactConstraint.getTangent();

            for (SolvableContact contact : contacts) {
                contact.setR1(c1.to(contact.getP()));
                contact.setR2(c2.to(contact.getP()));

                contact.setMassN(1.0 / this.getMassCoefficient(contactConstraint, contact, N));
                contact.setMassT(1.0 / this.getMassCoefficient(contactConstraint, contact, T));
                contact.setVb(0.0);

                double rvn = this.getRelativeVelocityAlongNormal(contactConstraint, contact);

                if (rvn < -restitutionVelocity) {
                    contact.vb += -contactConstraint.getRestitution() * rvn;
                }
            }
        }
        // 热启动
        this.warmStart(contactConstraints);
    }

    /**
     * 碰撞热启动以减少迭代次数
     *
     * @param contactConstraints 碰撞约束
     */
    private void warmStart(List<ContactConstraint> contactConstraints) {
        for (ContactConstraint contactConstraint : contactConstraints) {
            Vector2 N = contactConstraint.getNormal();
            Vector2 T = contactConstraint.getTangent();

            List<SolvableContact> contacts = contactConstraint.getContacts();
            int cSize = contactConstraint.getSize();

            for (int j = 0; j < cSize; j++) {
                SolvableContact contact = contacts.get(j);
                Vector2 J = new Vector2(N.x * contact.jn + T.x * contact.jt, N.y * contact.jn + T.y * contact.jt);
                this.updateBodies(contactConstraint, contact, J);
            }
        }
    }

    /**
     * 速度求解器
     *
     * @param contactConstraints 碰撞约束
     */
    public void solveVelocityConstraints(List<ContactConstraint> contactConstraints) {
        for (ContactConstraint contactConstraint : contactConstraints) {
            List<SolvableContact> contacts = contactConstraint.getContacts();
            int cSize = contactConstraint.getSize();
            if (cSize == 0) continue;

            Vector2 N = contactConstraint.getNormal();
            Vector2 T = contactConstraint.getTangent();
            double tangentSpeed = contactConstraint.getTangentSpeed();
            // 施加摩擦冲量
            for (int k = 0; k < cSize; k++) {
                SolvableContact contact = contacts.get(k);
                Vector2 rv = this.getRelativeVelocity(contactConstraint, contact);

                double rvt = T.dot(rv) - tangentSpeed;
                double jt = contact.getMassT() * (-rvt);

                double maxJt = contactConstraint.getFriction() * contact.jn;

                double Jt0 = contact.jt;
                contact.jt = Math.max(-maxJt, Math.min(Jt0 + jt, maxJt));
                jt = contact.jt - Jt0;

                Vector2 J = new Vector2(T.x * jt, T.y * jt);
                this.updateBodies(contactConstraint, contact, J);
            }

            // 施加碰撞冲量
            if (cSize == 1) {
                SolvableContact contact = contacts.get(0);
                double rvn = this.getRelativeVelocityAlongNormal(contactConstraint, contact);

                // 添加与穿透深度有关的偏差项，来对抗物体的下跌
                double j = -contact.getMassN() * (rvn - contact.vb);

                double j0 = contact.jn;
                contact.jn = Math.max(j0 + j, 0.0);
                j = contact.jn - j0;

                Vector2 J = new Vector2(N.x * j, N.y * j);
                this.updateBodies(contactConstraint, contact, J);
            }
            // 不存在多边形和多边形的碰撞
        }
    }

    /**
     * 位置求解器
     *
     * @param contactConstraints 碰撞约束
     */
    public void solvePositionConstraints(List<ContactConstraint> contactConstraints) {
        if (contactConstraints.size() == 0) return;

        // 最大线性位置校正，用于防止过冲
        double maxLinearCorrection = Settings.DEFAULT_MAXIMUM_LINEAR_CORRECTION;
        // 线性容差，用于防止过冲
        double allowedPenetration = Settings.DEFAULT_LINEAR_TOLERANCE;
        // 比例因子，该比例因子确定解决碰撞重叠的速度
        double baumgarte = Settings.DEFAULT_BAUMGARTE;

        for (ContactConstraint contactConstraint : contactConstraints) {
            List<SolvableContact> contacts = contactConstraint.getContacts();
            int cSize = contactConstraint.getSize();
            if (cSize == 0) continue;

            PhysicsBody b1 = contactConstraint.getBody1();
            PhysicsBody b2 = contactConstraint.getBody2();

            Transform t1 = b1.getShape().getTransform();
            Transform t2 = b2.getShape().getTransform();

            Mass m1 = b1.getMass();
            Mass m2 = b2.getMass();

            Vector2 N = contactConstraint.getNormal();

            for (int k = 0; k < cSize; k++) {
                SolvableContact contact = contacts.get(k);

                Vector2 c1 = t1.getTransformed(m1.getCenter());
                Vector2 c2 = t2.getTransformed(m2.getCenter());

                Vector2 r1 = contact.getP1().difference(m1.getCenter());
                t1.transformR(r1);
                Vector2 r2 = contact.getP2().difference(m2.getCenter());
                t2.transformR(r2);

                Vector2 p1 = c1.sum(r1);
                Vector2 p2 = c2.sum(r2);
                Vector2 dp = p1.subtract(p2);

                double penetration = dp.dot(N) - contact.getDepth();

                // 防止过冲
                double cp = baumgarte * Interval.sandwich(penetration + allowedPenetration, -maxLinearCorrection, 0.0);

                // 计算位置冲量
                double K = this.getMassCoefficient(contactConstraint, r1, r2, N);
                double jp = (K > Epsilon.E) ? (-cp / K) : 0.0;

                // 对位置冲量进行约束
                double jp0 = contact.jp;
                contact.jp = Math.max(jp0 + jp, 0.0);
                jp = contact.jp - jp0;

                Vector2 J = N.product(jp);

                // 使用计算出的位置冲量对物体进行旋转和平移
                t1.translate(J.product(m1.getInverseMass()));
                t1.rotate(m1.getInverseInertia() * r1.cross(J), c1.x, c1.y);

                t2.translate(J.product(-m2.getInverseMass()));
                t2.rotate(-m2.getInverseInertia() * r2.cross(J), c2.x, c2.y);
            }
        }
    }
}
