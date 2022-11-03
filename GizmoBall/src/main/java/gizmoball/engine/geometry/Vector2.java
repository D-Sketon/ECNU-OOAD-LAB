package gizmoball.engine.geometry;

import lombok.ToString;

@ToString
public class Vector2 {

    public double x;

    public double y;

    public Vector2() {
        this(0, 0);
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        Vector2 v = (Vector2) obj;
        return Math.abs(this.x - v.x) <= Epsilon.E && Math.abs(this.y - v.y) <= Epsilon.E;
    }

    /**
     * 基于本{@link Vector2}复制一个新向量
     *
     * @return Vector2
     */
    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    /**
     * 返回本{@link Vector2}到给定{@link Vector2}的距离的平方
     *
     * @param point 给定{@link Vector2}
     * @return double
     */
    public double distanceSquared(Vector2 point) {
        double dx = this.x - point.x;
        double dy = this.y - point.y;
        return dx * dx + dy * dy;
    }

    /**
     * 返回本{@link Vector2}的模
     *
     * @return double
     */
    public double getMagnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * 返回本{@link Vector2}模的平方
     *
     * @return double
     */
    public double getMagnitudeSquared() {
        return this.x * this.x + this.y * this.y;
    }

    /**
     * Adds the given {@link Vector2} to this {@link Vector2}.
     *
     * @param vector the {@link Vector2}
     * @return Vector2
     */
    public Vector2 add(Vector2 vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    /**
     * Adds the given {@link Vector2} to this {@link Vector2}.
     *
     * @param x the x component of the {@link Vector2}
     * @param y the y component of the {@link Vector2}
     * @return Vector2
     */
    public Vector2 add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Adds this {@link Vector2} and the given {@link Vector2} returning
     * a new {@link Vector2} containing the result.
     *
     * @param vector the {@link Vector2}
     * @return Vector2
     */
    public Vector2 sum(Vector2 vector) {
        return new Vector2(this.x + vector.x, this.y + vector.y);
    }

    /**
     * Subtracts the given {@link Vector2} from this {@link Vector2}.
     *
     * @param vector the {@link Vector2}
     * @return {@link Vector2} this vector
     */
    public Vector2 subtract(Vector2 vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        return this;
    }

    /**
     * Subtracts the given {@link Vector2} from this {@link Vector2}.
     *
     * @param x the x component of the {@link Vector2}
     * @param y the y component of the {@link Vector2}
     * @return {@link Vector2} this vector
     */
    public Vector2 subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Subtracts the given {@link Vector2} from this {@link Vector2} returning
     * a new {@link Vector2} containing the result.
     *
     * @param vector the {@link Vector2}
     * @return {@link Vector2}
     */
    public Vector2 difference(Vector2 vector) {
        return new Vector2(this.x - vector.x, this.y - vector.y);
    }

    /**
     * Creates a {@link Vector2} from this {@link Vector2} to the given {@link Vector2}.
     *
     * @param vector the {@link Vector2}
     * @return {@link Vector2}
     */
    public Vector2 to(Vector2 vector) {
        return new Vector2(vector.x - this.x, vector.y - this.y);
    }

    /**
     * Multiplies this {@link Vector2} by the given scalar.
     *
     * @param scalar the scalar
     * @return {@link Vector2} this vector
     */
    public Vector2 multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    /**
     * Divides this {@link Vector2} by the given scalar.
     *
     * @param scalar the scalar
     * @return {@link Vector2} this vector
     * @since 3.4.0
     */
    public Vector2 divide(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }

    /**
     * Multiplies this {@link Vector2} by the given scalar returning
     * a new {@link Vector2} containing the result.
     *
     * @param scalar the scalar
     * @return {@link Vector2}
     */
    public Vector2 product(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    /**
     * Returns the dot product of the given {@link Vector2}
     * and this {@link Vector2}.
     *
     * @param vector the {@link Vector2}
     * @return double
     */
    public double dot(Vector2 vector) {
        return this.x * vector.x + this.y * vector.y;
    }

    /**
     * Returns the cross product of the this {@link Vector2} and the given {@link Vector2}.
     *
     * @param vector the {@link Vector2}
     * @return double
     */
    public double cross(Vector2 vector) {
        return this.x * vector.y - this.y * vector.x;
    }


    /**
     * Returns the cross product of this {@link Vector2} and the z value of the right {@link Vector2}.
     *
     * @param z the z component of the {@link Vector2}
     * @return {@link Vector2}
     */
    public Vector2 cross(double z) {
        return new Vector2(-this.y * z, this.x * z);
    }

    /**
     * Returns true if this {@link Vector2} is the zero {@link Vector2}.
     *
     * @return boolean
     */
    public boolean isZero() {
        return Math.abs(this.x) <= Epsilon.E && Math.abs(this.y) <= Epsilon.E;
    }

    /**
     * Negates this {@link Vector2}.
     */
    public Vector2 negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    /**
     * Returns a {@link Vector2} which is the negative of this {@link Vector2}.
     *
     * @return {@link Vector2}
     */
    public Vector2 getNegative() {
        return new Vector2(-this.x, -this.y);
    }

    /**
     * Returns the right-handed normal of this vector.
     *
     * @return {@link Vector2} the right hand orthogonal {@link Vector2}
     */
    public Vector2 getRightHandOrthogonalVector() {
        return new Vector2(-this.y, this.x);
    }


    /**
     * Sets this vector to the left-handed normal of this vector.
     *
     * @return {@link Vector2} this vector
     */
    public Vector2 left() {
        double temp = this.x;
        this.x = this.y;
        this.y = -temp;
        return this;
    }

    /**
     * Sets the {@link Vector2} to the zero {@link Vector2}
     *
     * @return {@link Vector2} this vector
     */
    public Vector2 zero() {
        this.x = 0.0;
        this.y = 0.0;
        return this;
    }

    /**
     * 将本{@link Vector2}规范化并返回
     *
     * @return Vector2
     */
    public Vector2 getNormalized() {
        double magnitude = this.getMagnitude();
        if (magnitude <= Epsilon.E) return new Vector2();
        magnitude = 1.0 / magnitude;
        return new Vector2(this.x * magnitude, this.y * magnitude);
    }

    /**
     * 将本{@link Vector2}规范化并返回其原来的模
     *
     * @return double
     */
    public double normalize() {
        double magnitude = Math.sqrt(this.x * this.x + this.y * this.y);
        if (magnitude <= Epsilon.E) return 0;
        double m = 1.0 / magnitude;
        this.x *= m;
        this.y *= m;
        return magnitude;
    }

}
