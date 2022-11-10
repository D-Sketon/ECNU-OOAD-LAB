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
     * @return 新Vector2
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
     * 将本{@link Vector2}和传入{@link Vector2}相加
     *
     * @param vector 传入{@link Vector2}
     * @return 本Vector2
     */
    public Vector2 add(Vector2 vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    /**
     * 将本{@link Vector2}和传入{@link Vector2}相加，返回一个新{@link Vector2}
     *
     * @param vector 传入{@link Vector2}
     * @return 新Vector2
     */
    public Vector2 sum(Vector2 vector) {
        return new Vector2(this.x + vector.x, this.y + vector.y);
    }

    /**
     * 将本{@link Vector2}和传入{@link Vector2}相减
     *
     * @param vector 传入{@link Vector2}
     * @return 本Vector2
     */
    public Vector2 subtract(Vector2 vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        return this;
    }

    /**
     * 将本{@link Vector2}和传入{@link Vector2}相减，返回一个新Vector2
     *
     * @param vector 传入{@link Vector2}
     * @return 新Vector2
     */
    public Vector2 difference(Vector2 vector) {
        return new Vector2(this.x - vector.x, this.y - vector.y);
    }

    /**
     * 创建一个本{@link Vector2}到传入{@link Vector2}的向量
     *
     * @param vector 传入{@link Vector2}
     * @return 新Vector2
     */
    public Vector2 to(Vector2 vector) {
        return new Vector2(vector.x - this.x, vector.y - this.y);
    }

    /**
     * 将本{@link Vector2}乘上一个系数
     *
     * @param scalar 系数
     * @return 本Vector2
     */
    public Vector2 multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    /**
     * 将本{@link Vector2}除上一个系数
     *
     * @param scalar the scalar
     * @return 本Vector2
     */
    public Vector2 divide(double scalar) {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }

    /**
     * 将本{@link Vector2}乘上一个系数，返回一个新{@link Vector2}
     *
     * @param scalar 系数
     * @return 新Vector2
     */
    public Vector2 product(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    /**
     * 计算本{@link Vector2}和传入{@link Vector2}的点乘
     *
     * @param vector 传入{@link Vector2}
     * @return double
     */
    public double dot(Vector2 vector) {
        return this.x * vector.x + this.y * vector.y;
    }

    /**
     * 计算本{@link Vector2}和传入{@link Vector2}的叉乘
     *
     * @param vector 传入{@link Vector2}
     * @return double
     */
    public double cross(Vector2 vector) {
        return this.x * vector.y - this.y * vector.x;
    }


    /**
     * 计算本{@link Vector2}和z轴为传入参数的{@link Vector2}的叉乘
     *
     * @param z 传入z轴
     * @return 新Vector2
     */
    public Vector2 cross(double z) {
        return new Vector2(-this.y * z, this.x * z);
    }

    /**
     * 判断本{@link Vector2}是否为零向量
     *
     * @return boolean
     */
    public boolean isZero() {
        return Math.abs(this.x) <= Epsilon.E && Math.abs(this.y) <= Epsilon.E;
    }

    /**
     * 将本{@link Vector2}取反
     *
     * @return 本Vector2
     */
    public Vector2 negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    /**
     * 将本{@link Vector2}取反
     *
     * @return 新Vector2
     */
    public Vector2 getNegative() {
        return new Vector2(-this.x, -this.y);
    }

    /**
     * 将本{@link Vector2}置为左手系法线
     *
     * @return 本Vector2
     */
    public Vector2 left() {
        double temp = this.x;
        this.x = this.y;
        this.y = -temp;
        return this;
    }

    /**
     * 将本{@link Vector2}置为右手系法线
     *
     * @return 本Vector2
     */
    public Vector2 right() {
        left();
        return negate();
    }


    /**
     * 将本{@link Vector2}置0
     *
     * @return 本Vector2
     */
    public Vector2 zero() {
        this.x = 0.0;
        this.y = 0.0;
        return this;
    }

    /**
     * 将本{@link Vector2}规范化并返回
     *
     * @return 新Vector2
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
